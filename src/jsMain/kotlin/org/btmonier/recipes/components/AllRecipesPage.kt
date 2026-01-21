package org.btmonier.recipes.components

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.browser.document
import kotlinx.browser.window
import org.btmonier.recipes.model.Recipe
import org.w3c.dom.HTMLElement

fun createAllRecipesPage(recipes: List<Recipe>): HTMLElement {
    val container = document.create.div("all-recipes-container") {
        div("all-recipes-header") {
            h1("all-recipes-title") { +"All Recipes" }
            p("all-recipes-count") { +"${recipes.size} recipes" }
        }
        
        div("all-recipes-grid") {
            id = "all-recipes-grid"
            recipes.forEach { recipe ->
                a(href = "#recipe-${recipe.metadata.slug}", classes = "recipe-card md-card md-card-elevated") {
                    attributes["data-slug"] = recipe.metadata.slug
                    
                    div("recipe-card-header") {
                        h2("recipe-card-title") { +recipe.metadata.title }
                        if (recipe.metadata.tags.isNotEmpty()) {
                            div("recipe-card-tags") {
                                recipe.metadata.tags.take(3).forEach { tag ->
                                    span("recipe-card-tag") { +tag }
                                }
                            }
                        }
                    }
                    
                    if (recipe.metadata.prepTime != null || recipe.metadata.cookTime != null) {
                        div("recipe-card-metadata") {
                            recipe.metadata.prepTime?.let {
                                span("recipe-card-time") {
                                    span("material-icons") { +"schedule" }
                                    +it
                                }
                            }
                            recipe.metadata.cookTime?.let {
                                span("recipe-card-time") {
                                    span("material-icons") { +"timer" }
                                    +it
                                }
                            }
                        }
                    }
                    
                    span("material-icons recipe-card-arrow") { +"arrow_forward" }
                }
            }
        }
    }
    
    // Setup click handlers for recipe cards
    container.querySelectorAll(".recipe-card").asList().forEach { card ->
        card.addEventListener("click", { e ->
            e.preventDefault()
            val slug = card.getAttribute("data-slug") ?: return@addEventListener
            window.location.hash = "#recipe-$slug"
        })
    }
    
    return container
}

// Extension function to convert NodeList to List
private fun org.w3c.dom.NodeList.asList(): List<org.w3c.dom.Element> {
    val list = mutableListOf<org.w3c.dom.Element>()
    for (i in 0 until this.length) {
        this.item(i)?.let { list.add(it as org.w3c.dom.Element) }
    }
    return list
}
