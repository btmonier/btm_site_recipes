package org.btmonier.recipes.model

import kotlinx.serialization.Serializable

/**
 * Represents a list item that can contain nested children for recursive lists.
 */
@Serializable
data class ListItem(
    val content: String,
    val children: List<ListItem> = emptyList()
)

/**
 * Represents a subsection within a recipe section (e.g., "Main Ingredients" under "Ingredients").
 * If title is null, this represents items without a subsection header.
 */
@Serializable
data class RecipeSubsection(
    val title: String? = null,
    val items: List<String> = emptyList(),
    val nestedItems: List<ListItem> = emptyList()
)

@Serializable
data class RecipeContent(
    val ingredients: List<RecipeSubsection>,
    val instructions: List<RecipeSubsection>,
    val notes: List<RecipeSubsection> = emptyList()
)

