package org.btmonier.recipes

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.btmonier.recipes.model.RecipeList
import org.btmonier.recipes.parser.RecipeParser
import java.nio.file.Files
import java.nio.file.Paths

// Import commonMain models for serialization
import org.btmonier.recipes.model.Recipe as CommonRecipe
import org.btmonier.recipes.model.RecipeContent as CommonRecipeContent
import org.btmonier.recipes.model.RecipeMetadata as CommonRecipeMetadata

// Import jvmMain models (from jvmmodel package)
import org.btmonier.recipes.jvmmodel.Recipe as JvmRecipe

/**
 * Utility class to convert markdown recipe files to JSON format.
 * 
 * Usage:
 * 1. Place your markdown recipe files in a directory (e.g., `data/recipes/`)
 * 2. Run this main function or use the convertRecipesToJson method
 * 3. The output JSON will be written to the specified output file
 */
object RecipeToJsonConverter {
    private val parser = RecipeParser()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Ensures unique slugs by adding numeric suffixes to duplicates
     */
    private fun ensureUniqueSlugs(recipes: List<CommonRecipe>): List<CommonRecipe> {
        val slugCounts = mutableMapOf<String, Int>()
        val result = mutableListOf<CommonRecipe>()
        
        recipes.forEach { recipe ->
            var slug = recipe.metadata.slug
            val count = slugCounts.getOrDefault(slug, 0)
            
            if (count > 0) {
                // Slug already exists, add numeric suffix
                slug = "$slug-$count"
            }
            
            slugCounts[recipe.metadata.slug] = count + 1
            
            // Create new recipe with potentially modified slug
            val updatedMetadata = CommonRecipeMetadata(
                title = recipe.metadata.title,
                slug = slug,
                date = recipe.metadata.date,
                tags = recipe.metadata.tags,
                servings = recipe.metadata.servings,
                prepTime = recipe.metadata.prepTime,
                cookTime = recipe.metadata.cookTime
            )
            
            result.add(CommonRecipe(updatedMetadata, recipe.content))
        }
        
        return result
    }
    
    /**
     * Converts a jvmMain Recipe to a commonMain Recipe (for serialization)
     */
    private fun convertToCommonRecipe(recipe: JvmRecipe): CommonRecipe {
        val commonMetadata = CommonRecipeMetadata(
            title = recipe.metadata.title,
            slug = recipe.metadata.slug,
            date = recipe.metadata.date,
            tags = recipe.metadata.tags,
            servings = recipe.metadata.servings,
            prepTime = recipe.metadata.prepTime,
            cookTime = recipe.metadata.cookTime
        )
        
        val commonContent = CommonRecipeContent(
            ingredients = recipe.content.ingredients,
            instructions = recipe.content.instructions,
            notes = recipe.content.notes
        )
        
        return CommonRecipe(commonMetadata, commonContent)
    }
    
    /**
     * Converts all markdown recipe files in a directory to a single JSON file.
     * 
     * @param inputDir Directory containing markdown recipe files (.md)
     * @param outputFile Path to the output JSON file
     */
    fun convertRecipesToJson(inputDir: String, outputFile: String) {
        val inputPath = Paths.get(inputDir)
        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            throw IllegalArgumentException("Input directory does not exist: $inputDir")
        }
        
        val recipes = mutableListOf<CommonRecipe>()
        
        // Read all .md files from the input directory
        Files.walk(inputPath)
            .filter { Files.isRegularFile(it) && it.toString().endsWith(".md") }
            .forEach { filePath ->
                try {
                    val markdown = Files.readString(filePath)
                    val jvmRecipe = parser.parseRecipe(markdown)
                    val commonRecipe = convertToCommonRecipe(jvmRecipe)
                    recipes.add(commonRecipe)
                    println("Parsed: ${filePath.fileName} -> ${commonRecipe.metadata.title}")
                } catch (e: Exception) {
                    System.err.println("Error parsing ${filePath.fileName}: ${e.message}")
                    e.printStackTrace()
                }
            }
        
        // Ensure unique slugs (add numeric suffixes to duplicates)
        val uniqueRecipes = ensureUniqueSlugs(recipes)
        
        // Convert to JSON
        val recipeList = RecipeList(uniqueRecipes)
        val jsonString = json.encodeToString(recipeList)
        
        // Write to output file
        val outputPath = Paths.get(outputFile)
        Files.createDirectories(outputPath.parent)
        Files.writeString(outputPath, jsonString)
        
        println("\nSuccessfully converted ${recipes.size} recipes to JSON")
        println("Output written to: $outputFile")
    }
    
    /**
     * Converts a single markdown recipe file to JSON.
     * 
     * @param inputFile Path to the markdown recipe file
     * @return JSON string representation of the recipe
     */
    fun convertSingleRecipeToJson(inputFile: String): String {
        val markdown = Files.readString(Paths.get(inputFile))
        val jvmRecipe = parser.parseRecipe(markdown)
        val commonRecipe = convertToCommonRecipe(jvmRecipe)
        return json.encodeToString(commonRecipe)
    }
}

/**
 * Main function to run the converter from command line.
 * 
 * Usage:
 *   Use the Gradle task: gradlew convertRecipesToJson
 *   Or run directly with: gradlew runJvmMainKotlin --args="inputDir outputFile"
 */
fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: RecipeToJsonConverter <inputDir> <outputFile>")
        println("Example: RecipeToJsonConverter data/recipes src/jsMain/resources/content/recipes.json")
        System.exit(1)
    }
    
    val inputDir = args[0]
    val outputFile = args[1]
    
    try {
        RecipeToJsonConverter.convertRecipesToJson(inputDir, outputFile)
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}

