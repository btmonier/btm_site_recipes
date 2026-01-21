package org.btmonier.recipes.components

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.browser.document
import kotlinx.browser.window
import org.btmonier.recipes.model.Recipe
import org.btmonier.recipes.model.SiteConfig
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

fun createRecipeListPage(config: SiteConfig, recipes: List<Recipe>): HTMLElement {
    // Create container
    val container = document.create.div("recipe-list-container") {
        div("recipe-list-header") {
            h1("recipe-list-title") { +"Recipe Collection" }
            if (config.subtitle != null) {
                p("recipe-list-subtitle") { +config.subtitle }
            }
        }
        
        // Search Bar
        div("recipe-search-container") {
            div("recipe-search-wrapper") {
                span("material-icons recipe-search-icon") { +"search" }
                input(InputType.text, classes = "recipe-search-input") {
                    id = "recipe-search-input"
                    placeholder = "Search recipes..."
                    attributes["autocomplete"] = "off"
                }
            }
        }
        
        // Results Container
        div("recipe-results-container") {
            id = "recipe-results-container"
            // Results will be populated by search
        }
    }
    
    // Setup search functionality - query elements from container
    val searchInput = container.querySelector("#recipe-search-input") as? HTMLInputElement
    val resultsContainer = container.querySelector("#recipe-results-container") as? HTMLElement
    
    fun performSearch(query: String) {
        resultsContainer?.innerHTML = ""
        
        val filteredRecipes = if (query.isBlank()) {
            emptyList()
        } else {
            val searchLower = query.lowercase().trim()
            val results = recipes.filter { recipe ->
                // Primary search: title from markdown file entry (case-insensitive)
                recipe.metadata.title.lowercase().contains(searchLower) ||
                // Also search in tags for additional matching
                recipe.metadata.tags.any { tag -> tag.lowercase().contains(searchLower) }
            }
            results
        }
        
        if (filteredRecipes.isEmpty() && query.isNotBlank()) {
            resultsContainer?.appendChild(document.create.div("recipe-no-results") {
                span("material-icons") { +"search_off" }
                p { +"No recipes found matching \"$query\"" }
            })
        } else if (filteredRecipes.isNotEmpty()) {
            filteredRecipes.forEach { recipe ->
                val card = document.create.a(href = "#recipe-${recipe.metadata.slug}", classes = "recipe-card md-card md-card-elevated") {
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
                // Setup click handler for the card
                card.addEventListener("click", { e ->
                    e.preventDefault()
                    val slug = card.getAttribute("data-slug") ?: return@addEventListener
                    // Navigate to recipe - this will be handled by the main app
                    window.location.hash = "#recipe-$slug"
                })
                resultsContainer?.appendChild(card)
            }
        }
    }
    
    // Setup search input event listener
    searchInput?.addEventListener("input", { e ->
        val query = (e.target as? HTMLInputElement)?.value ?: ""
        performSearch(query)
    })
    
    // Also handle keyup for better responsiveness
    searchInput?.addEventListener("keyup", { e ->
        val query = (e.target as? HTMLInputElement)?.value ?: ""
        performSearch(query)
    })
    
    // Initial empty state - show nothing until user types
    performSearch("")
    
    return container
}

