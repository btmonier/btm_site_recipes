package org.btmonier.recipes.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeList(
    val recipes: List<Recipe>
)

