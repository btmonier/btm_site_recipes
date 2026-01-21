package org.btmonier.recipes.jvmmodel

data class RecipeContent(
    val title: String,
    val metadata: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val notes: List<String>
)

