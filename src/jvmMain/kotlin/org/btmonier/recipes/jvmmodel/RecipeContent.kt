package org.btmonier.recipes.jvmmodel

/**
 * Represents a subsection within a recipe section (e.g., "Main Ingredients" under "Ingredients").
 * If title is null, this represents items without a subsection header.
 */
data class RecipeSubsection(
    val title: String? = null,
    val items: List<String>
)

data class RecipeContent(
    val title: String,
    val metadata: String,
    val ingredients: List<RecipeSubsection>,
    val instructions: List<RecipeSubsection>,
    val notes: List<RecipeSubsection>
)

