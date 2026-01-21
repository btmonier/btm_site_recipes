package org.btmonier.recipes.parser

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.btmonier.recipes.jvmmodel.Recipe
import org.btmonier.recipes.jvmmodel.RecipeContent
import org.btmonier.recipes.jvmmodel.RecipeMetadata
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecipeParser {
    private val yaml = Yaml.default
    
    fun parseRecipe(markdown: String): Recipe {
        val (frontmatter, content) = splitFrontmatter(markdown)
        val metadata = parseMetadata(frontmatter)
        val recipeContent = parseContent(content, metadata)
        
        return Recipe(metadata, recipeContent)
    }
    
    private fun splitFrontmatter(markdown: String): Pair<String, String> {
        val frontmatterRegex = Regex("^---\\s*\n(.*?)\n---\\s*\n", RegexOption.DOT_MATCHES_ALL)
        val match = frontmatterRegex.find(markdown)
        
        if (match != null) {
            val frontmatter = match.groupValues[1]
            val content = markdown.substring(match.range.last + 1).trim()
            return Pair(frontmatter, content)
        }
        
        throw IllegalArgumentException("Recipe must contain YAML frontmatter delimited by ---")
    }
    
    private fun parseMetadata(frontmatter: String): RecipeMetadata {
        return try {
            yaml.decodeFromString<RecipeMetadata>(frontmatter)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse recipe metadata: ${e.message}", e)
        }
    }
    
    private fun parseContent(content: String, metadata: RecipeMetadata): RecipeContent {
        // Replace template variables
        val processedContent = substituteTemplateVariables(content, metadata)
        
        // Extract title from H1 if present
        val titleRegex = Regex("^#\\s+(.+)$", RegexOption.MULTILINE)
        val titleMatch = titleRegex.find(processedContent)
        val title = titleMatch?.groupValues?.get(1)?.trim() ?: metadata.title
        
        // Remove title and metadata lines from content before splitting sections
        val contentWithoutTitle = processedContent
            .replace(Regex("^#\\s+.*$", RegexOption.MULTILINE), "")
            .replace(Regex("^\\*\\*.*?\\*\\*.*$", RegexOption.MULTILINE), "") // Remove metadata lines like **Servings:** ...
            .replace(Regex("^---\\s*$", RegexOption.MULTILINE), "")
            .trim()
        
        // Split into sections
        val sections = splitIntoSections(contentWithoutTitle)
        
        val metadataText = extractMetadataText(sections)
        val ingredients = extractIngredients(sections)
        val instructions = extractInstructions(sections)
        val notes = extractNotes(sections)
        
        return RecipeContent(
            title = title,
            metadata = metadataText,
            ingredients = ingredients,
            instructions = instructions,
            notes = notes
        )
    }
    
    private fun substituteTemplateVariables(content: String, metadata: RecipeMetadata): String {
        var result = content
        result = result.replace("{{ title }}", metadata.title)
        result = result.replace("{{ slug }}", metadata.slug)
        metadata.servings?.let { result = result.replace("{{ servings }}", it.toString()) }
        metadata.prepTime?.let { result = result.replace("{{ prep_time }}", it) }
        metadata.cookTime?.let { result = result.replace("{{ cook_time }}", it) }
        return result
    }
    
    private fun splitIntoSections(content: String): Map<String, String> {
        val sections = mutableMapOf<String, String>()
        val sectionRegex = Regex("^##\\s+(.+)$", RegexOption.MULTILINE)
        val matches = sectionRegex.findAll(content)
        
        var lastIndex = 0
        var lastSectionName: String? = null
        
        matches.forEach { match ->
            if (lastSectionName != null) {
                val sectionContent = content.substring(lastIndex, match.range.first).trim()
                sections[lastSectionName!!] = sectionContent
            }
            lastSectionName = match.groupValues[1].trim()
            lastIndex = match.range.last + 1
        }
        
        // Add the last section
        if (lastSectionName != null) {
            val sectionContent = content.substring(lastIndex).trim()
            sections[lastSectionName!!] = sectionContent
        }
        
        return sections
    }
    
    private fun extractTitle(sections: Map<String, String>): String? {
        // Title is extracted from the processed content (after template substitution)
        // It's handled in parseContent by checking the first line
        return null
    }
    
    private fun extractMetadataText(sections: Map<String, String>): String {
        // Extract the metadata section (before Ingredients)
        // This would be the content before the first ## section
        // For simplicity, we'll return empty and handle it in the builder
        return ""
    }
    
    private fun extractIngredients(sections: Map<String, String>): List<String> {
        val ingredientsSection = sections["Ingredients"] ?: return emptyList()
        return extractMultiLineListItems(ingredientsSection, supportNumbered = false)
    }
    
    private fun extractInstructions(sections: Map<String, String>): List<String> {
        val instructionsSection = sections["Instructions"] ?: return emptyList()
        return extractMultiLineListItems(instructionsSection, supportNumbered = true)
    }
    
    /**
     * Extracts list items that may span multiple lines.
     * A new list item starts with `-`, `*`, or (if supportNumbered) a number like `1.`
     * Continuation lines are indented or are non-empty lines that don't start a new item.
     */
    private fun extractMultiLineListItems(section: String, supportNumbered: Boolean): List<String> {
        val items = mutableListOf<String>()
        val currentItem = StringBuilder()
        
        val numberedPattern = Regex("^\\d+\\.\\s+.*")
        
        for (line in section.lines()) {
            val trimmedLine = line.trim()
            
            // Check if this line starts a new list item
            val isNewNumberedItem = supportNumbered && trimmedLine.matches(numberedPattern)
            val isNewBulletItem = trimmedLine.startsWith("-") || trimmedLine.startsWith("*")
            val isNewItem = isNewNumberedItem || isNewBulletItem
            
            if (isNewItem) {
                // Save previous item if exists
                if (currentItem.isNotEmpty()) {
                    items.add(currentItem.toString().trim())
                    currentItem.clear()
                }
                // Start new item, stripping the list marker
                val content = when {
                    isNewNumberedItem -> trimmedLine.replace(Regex("^\\d+\\.\\s+"), "")
                    else -> trimmedLine.removePrefix("-").removePrefix("*").trim()
                }
                currentItem.append(content)
            } else if (trimmedLine.isNotEmpty() && currentItem.isNotEmpty()) {
                // Continuation line - append to current item
                currentItem.append(" ").append(trimmedLine)
            }
        }
        
        // Don't forget the last item
        if (currentItem.isNotEmpty()) {
            items.add(currentItem.toString().trim())
        }
        
        return items.filter { it.isNotEmpty() }
    }
    
    private fun extractNotes(sections: Map<String, String>): List<String> {
        val notesSection = sections["Notes"] ?: return emptyList()
        return extractMultiLineListItems(notesSection, supportNumbered = false)
    }
}

