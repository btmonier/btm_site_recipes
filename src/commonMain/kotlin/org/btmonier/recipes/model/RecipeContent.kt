package org.btmonier.recipes.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeContent(
    val ingredients: List<String>,
    val instructions: List<String>,
    val notes: List<String> = emptyList()
)

