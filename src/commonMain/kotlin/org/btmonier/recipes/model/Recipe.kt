package org.btmonier.recipes.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val metadata: RecipeMetadata,
    val content: RecipeContent
)

