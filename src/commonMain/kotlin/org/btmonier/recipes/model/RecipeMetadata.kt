package org.btmonier.recipes.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeMetadata(
    val title: String,
    val slug: String,
    val date: String? = null,
    val tags: List<String> = emptyList(),
    val servings: Int? = null,
    @SerialName("prep_time")
    val prepTime: String? = null,
    @SerialName("cook_time")
    val cookTime: String? = null
)

