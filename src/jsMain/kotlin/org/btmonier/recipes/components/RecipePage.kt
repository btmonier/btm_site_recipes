package org.btmonier.recipes.components

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.browser.document
import kotlinx.browser.window
import org.btmonier.recipes.model.Recipe
import org.btmonier.recipes.util.processInlineMarkdown
import org.w3c.dom.HTMLElement

fun createRecipePage(recipe: Recipe): HTMLElement {
    val container = document.create.div("recipe-container") {
        // Go Back to Search Button
        div("recipe-back-button-container") {
            a(href = "/", classes = "md-button md-button-text recipe-back-button") {
                span("material-icons") { +"arrow_back" }
                +"Go Back to Search"
            }
        }
        
        // Recipe Header
        div("recipe-header") {
            h1("recipe-title") { +recipe.metadata.title }
            
            // Metadata
            val metadataItems = mutableListOf<Triple<String, String, String>>()
            
            recipe.metadata.servings?.let {
                metadataItems.add(Triple("restaurant", "Servings:", it.toString()))
            }
            
            recipe.metadata.prepTime?.let {
                metadataItems.add(Triple("schedule", "Prep time:", it))
            }
            
            recipe.metadata.cookTime?.let {
                metadataItems.add(Triple("timer", "Cook time:", it))
            }
            
            if (metadataItems.isNotEmpty()) {
                div("recipe-metadata") {
                    metadataItems.forEach { item ->
                        div("recipe-metadata-item") {
                            span("material-icons") { +item.first }
                            span("recipe-metadata-label") { +item.second }
                            span { +item.third }
                        }
                    }
                }
            }
        }
        
        // Tags
        if (recipe.metadata.tags.isNotEmpty()) {
            div("recipe-tags") {
                recipe.metadata.tags.forEach { tag ->
                    span("recipe-tag") { +tag }
                }
            }
        }
        
        // Ingredients Section
        if (recipe.content.ingredients.isNotEmpty()) {
            div("recipe-section") {
                h2("recipe-section-title") {
                    span("material-icons") { +"shopping_cart" }
                    +"Ingredients"
                }
                div("recipe-ingredients") {
                    var ingredientIndex = 0
                    recipe.content.ingredients.forEach { subsection ->
                        // Render subsection title if present
                        subsection.title?.let { title ->
                            h3("recipe-subsection-title") { +title }
                        }
                        ul("recipe-ingredients-list") {
                            subsection.items.forEach { ingredient ->
                                val currentIndex = ingredientIndex++
                                li("recipe-ingredient-item") {
                                    input(type = InputType.checkBox, classes = "recipe-ingredient-checkbox") {
                                        id = "ingredient-$currentIndex"
                                    }
                                    label {
                                        htmlFor = "ingredient-$currentIndex"
                                        unsafe { +processInlineMarkdown(ingredient) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Instructions Section
        if (recipe.content.instructions.isNotEmpty()) {
            div("recipe-section") {
                h2("recipe-section-title") {
                    span("material-icons") { +"list" }
                    +"Instructions"
                }
                div("recipe-instructions") {
                    recipe.content.instructions.forEach { subsection ->
                        // Render subsection title if present
                        subsection.title?.let { title ->
                            h3("recipe-subsection-title") { +title }
                        }
                        ol("recipe-instructions-list") {
                            subsection.items.forEach { instruction ->
                                li("recipe-instruction-item") {
                                    span { unsafe { +processInlineMarkdown(instruction) } }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Notes Section
        if (recipe.content.notes.isNotEmpty()) {
            div("recipe-section") {
                h2("recipe-section-title") {
                    span("material-icons") { +"note" }
                    +"Notes"
                }
                div("recipe-notes") {
                    recipe.content.notes.forEach { subsection ->
                        // Render subsection title if present
                        subsection.title?.let { title ->
                            h3("recipe-subsection-title") { +title }
                        }
                        ul("recipe-notes-list") {
                            subsection.items.forEach { note ->
                                li("recipe-note-item") {
                                    span { unsafe { +processInlineMarkdown(note) } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Setup click handler for back button
    val backButton = container.querySelector(".recipe-back-button") as? HTMLElement
    backButton?.addEventListener("click", { e ->
        e.preventDefault()
        window.location.href = "/"
    })
    
    return container
}

