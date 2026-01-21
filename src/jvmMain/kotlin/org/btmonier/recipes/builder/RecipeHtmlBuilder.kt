package org.btmonier.recipes.builder

import org.btmonier.recipes.jvmmodel.Recipe

class RecipeHtmlBuilder {
    fun buildHtml(recipe: Recipe): String {
        val html = StringBuilder()
        
        html.append("<div class=\"recipe-container\">\n")
        
        // Recipe Header
        html.append(buildHeader(recipe))
        
        // Tags
        if (recipe.metadata.tags.isNotEmpty()) {
            html.append(buildTags(recipe.metadata.tags))
        }
        
        // Ingredients Section
        if (recipe.content.ingredients.isNotEmpty()) {
            html.append(buildIngredients(recipe.content.ingredients))
        }
        
        // Instructions Section
        if (recipe.content.instructions.isNotEmpty()) {
            html.append(buildInstructions(recipe.content.instructions))
        }
        
        // Notes Section
        if (recipe.content.notes.isNotEmpty()) {
            html.append(buildNotes(recipe.content.notes))
        }
        
        html.append("</div>\n")
        
        return html.toString()
    }
    
    private fun buildHeader(recipe: Recipe): String {
        val html = StringBuilder()
        html.append("  <div class=\"recipe-header\">\n")
        html.append("    <h1 class=\"recipe-title\">${escapeHtml(recipe.content.title)}</h1>\n")
        
        // Metadata
        val metadataItems = mutableListOf<String>()
        
        recipe.metadata.servings?.let {
            metadataItems.add("""
            <div class="recipe-metadata-item">
              <span class="material-icons">restaurant</span>
              <span class="recipe-metadata-label">Servings:</span>
              <span>${escapeHtml(it.toString())}</span>
            </div>
            """.trimIndent())
        }
        
        recipe.metadata.prepTime?.let {
            metadataItems.add("""
            <div class="recipe-metadata-item">
              <span class="material-icons">schedule</span>
              <span class="recipe-metadata-label">Prep time:</span>
              <span>${escapeHtml(it)}</span>
            </div>
            """.trimIndent())
        }
        
        recipe.metadata.cookTime?.let {
            metadataItems.add("""
            <div class="recipe-metadata-item">
              <span class="material-icons">timer</span>
              <span class="recipe-metadata-label">Cook time:</span>
              <span>${escapeHtml(it)}</span>
            </div>
            """.trimIndent())
        }
        
        if (metadataItems.isNotEmpty()) {
            html.append("    <div class=\"recipe-metadata\">\n")
            metadataItems.forEach { item ->
                html.append("      $item\n")
            }
            html.append("    </div>\n")
        }
        
        html.append("  </div>\n")
        return html.toString()
    }
    
    private fun buildTags(tags: List<String>): String {
        val html = StringBuilder()
        html.append("  <div class=\"recipe-tags\">\n")
        tags.forEach { tag ->
            html.append("    <span class=\"recipe-tag\">${escapeHtml(tag)}</span>\n")
        }
        html.append("  </div>\n")
        return html.toString()
    }
    
    private fun buildIngredients(ingredients: List<String>): String {
        val html = StringBuilder()
        html.append("  <div class=\"recipe-section\">\n")
        html.append("    <h2 class=\"recipe-section-title\">\n")
        html.append("      <span class=\"material-icons\">shopping_cart</span>\n")
        html.append("      Ingredients\n")
        html.append("    </h2>\n")
        html.append("    <div class=\"recipe-ingredients\">\n")
        html.append("      <ul class=\"recipe-ingredients-list\">\n")
        
        ingredients.forEach { ingredient ->
            html.append("        <li class=\"recipe-ingredient-item\">${processInlineMarkdown(ingredient)}</li>\n")
        }
        
        html.append("      </ul>\n")
        html.append("    </div>\n")
        html.append("  </div>\n")
        return html.toString()
    }
    
    private fun buildInstructions(instructions: List<String>): String {
        val html = StringBuilder()
        html.append("  <div class=\"recipe-section\">\n")
        html.append("    <h2 class=\"recipe-section-title\">\n")
        html.append("      <span class=\"material-icons\">list</span>\n")
        html.append("      Instructions\n")
        html.append("    </h2>\n")
        html.append("    <div class=\"recipe-instructions\">\n")
        html.append("      <ol class=\"recipe-instructions-list\">\n")
        
        instructions.forEach { instruction ->
            html.append("        <li class=\"recipe-instruction-item\">${processInlineMarkdown(instruction)}</li>\n")
        }
        
        html.append("      </ol>\n")
        html.append("    </div>\n")
        html.append("  </div>\n")
        return html.toString()
    }
    
    private fun buildNotes(notes: List<String>): String {
        val html = StringBuilder()
        html.append("  <div class=\"recipe-section\">\n")
        html.append("    <h2 class=\"recipe-section-title\">\n")
        html.append("      <span class=\"material-icons\">note</span>\n")
        html.append("      Notes\n")
        html.append("    </h2>\n")
        html.append("    <div class=\"recipe-notes\">\n")
        html.append("      <ul class=\"recipe-notes-list\">\n")
        
        notes.forEach { note ->
            html.append("        <li class=\"recipe-note-item\">${processInlineMarkdown(note)}</li>\n")
        }
        
        html.append("      </ul>\n")
        html.append("    </div>\n")
        html.append("  </div>\n")
        return html.toString()
    }
    
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
    
    /**
     * Processes inline markdown formatting (bold, italic, and links) and converts to HTML.
     * First escapes HTML to prevent XSS, then converts markdown syntax.
     * 
     * Supported syntax:
     * - **text** or __text__ -> <strong>text</strong>
     * - *text* or _text_ -> <em>text</em>
     * - ***text*** -> <strong><em>text</em></strong>
     * - [text](url) -> <a href="url" target="_blank" rel="noopener noreferrer">text</a>
     */
    private fun processInlineMarkdown(text: String): String {
        // First escape HTML to prevent XSS
        var result = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
        
        // Convert links: [text](url) -> <a href="url">text</a>
        // Process links before bold/italic to avoid conflicts with brackets
        result = result.replace(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")) { match ->
            val linkText = match.groupValues[1]
            val url = match.groupValues[2]
            "<a href=\"$url\" target=\"_blank\" rel=\"noopener noreferrer\">$linkText</a>"
        }
        
        // Convert bold first: **text** or __text__ -> <strong>text</strong>
        result = result.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        result = result.replace(Regex("__(.+?)__"), "<strong>$1</strong>")
        
        // Then convert italic: *text* or _text_ -> <em>text</em>
        result = result.replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
        result = result.replace(Regex("_(.+?)_"), "<em>$1</em>")
        
        return result
    }
}

