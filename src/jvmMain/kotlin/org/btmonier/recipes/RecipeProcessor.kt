package org.btmonier.recipes

import org.btmonier.recipes.builder.RecipeHtmlBuilder
import org.btmonier.recipes.jvmmodel.Recipe
import org.btmonier.recipes.parser.RecipeParser

/**
 * Main API for processing recipe markdown files and generating HTML.
 */
class RecipeProcessor {
    private val parser = RecipeParser()
    private val htmlBuilder = RecipeHtmlBuilder()
    
    /**
     * Parses a markdown recipe string into a Recipe object.
     *
     * @param markdown The markdown content with YAML frontmatter
     * @return A Recipe object containing parsed metadata and content
     */
    fun parseRecipe(markdown: String): Recipe {
        return parser.parseRecipe(markdown)
    }
    
    /**
     * Builds HTML from a Recipe object.
     *
     * @param recipe The Recipe object to convert to HTML
     * @return HTML string with Material Design 3 styling
     */
    fun buildHtml(recipe: Recipe): String {
        return htmlBuilder.buildHtml(recipe)
    }
    
    /**
     * Processes a markdown recipe string and returns HTML.
     * This is a convenience method that combines parsing and building.
     *
     * @param markdown The markdown content with YAML frontmatter
     * @return HTML string with Material Design 3 styling
     */
    fun processRecipe(markdown: String): String {
        val recipe = parseRecipe(markdown)
        return buildHtml(recipe)
    }
}

