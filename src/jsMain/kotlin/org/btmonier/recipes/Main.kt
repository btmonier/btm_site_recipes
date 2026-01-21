package org.btmonier.recipes

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import org.btmonier.recipes.components.*
import org.btmonier.recipes.model.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

private val scope = MainScope()

private var currentPage = "recipes"
private var siteConfig: SiteConfig? = null
private var recipesData: List<Recipe>? = null
private var currentRecipe: Recipe? = null

fun main() {
    console.log("Recipe app: main() called")
    // Use DOMContentLoaded for faster initialization
    // This fires when the DOM is ready, before images/stylesheets finish loading
    val readyState = document.readyState.asDynamic().toString() as String
    console.log("Recipe app: document.readyState = $readyState")
    if (readyState == "complete" || readyState == "interactive") {
        // DOM is already ready, initialize immediately
        console.log("Recipe app: DOM already ready, initializing immediately")
        initializeApp()
    } else {
        // Wait for DOM to be ready
        console.log("Recipe app: Waiting for DOMContentLoaded")
        document.addEventListener("DOMContentLoaded", {
            console.log("Recipe app: DOMContentLoaded fired, initializing")
            initializeApp()
        })
    }
}

private fun initializeApp() {
    console.log("Recipe app: initializeApp() called")
    showLoading()
    
    scope.launch {
        try {
            console.log("Recipe app: Loading site config...")
            // Load all content
            siteConfig = ContentLoader.loadSiteConfig()
            console.log("Recipe app: Site config loaded: ${siteConfig?.name}")
            
            console.log("Recipe app: Loading recipes...")
            recipesData = ContentLoader.loadRecipes()
            console.log("Recipe app: Loaded ${recipesData?.size ?: 0} recipes")
            
            // Get initial page from hash
            val hash = window.location.hash.removePrefix("#")
            when {
                hash.startsWith("recipe-") -> {
                    val slug = hash.removePrefix("recipe-")
                    val recipe = recipesData!!.find { it.metadata.slug == slug }
                    if (recipe != null) {
                        currentRecipe = recipe
                        currentPage = "recipe"
                    } else {
                        currentPage = "recipes"
                    }
                }
                hash.isNotEmpty() -> currentPage = hash
                else -> currentPage = "recipes"
            }
            
            // Render the application
            renderApp()
            
            // Setup navigation listeners
            window.onhashchange = { 
                val newHash = window.location.hash.removePrefix("#")
                when {
                    newHash.startsWith("recipe-") -> {
                        val slug = newHash.removePrefix("recipe-")
                        val recipe = recipesData?.find { it.metadata.slug == slug }
                        if (recipe != null) {
                            currentRecipe = recipe
                            currentPage = "recipe"
                            renderPage()
                        } else {
                            navigateToPage("recipes")
                        }
                    }
                    newHash.isNotEmpty() && newHash != currentPage -> {
                        currentPage = newHash
                        renderPage()
                        updateActiveNavItems()
                    }
                }
            }
            
        } catch (e: Exception) {
            showError("Failed to load content: ${e.message}")
            console.error("Error loading content:", e)
        }
    }
}

private fun showLoading() {
    document.body?.innerHTML = ""
    document.body?.append {
        div("loading") {
            div("loading-spinner") {}
            p("loading-text") { +"Loading..." }
        }
    }
}

private fun showError(message: String) {
    document.body?.innerHTML = ""
    document.body?.append {
        div {
            id = "error-container"
            style = """
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                min-height: 100vh;
                padding: 24px;
                text-align: center;
            """.trimIndent()
            span("material-icons") {
                style = "font-size: 64px; color: var(--md-sys-color-error); margin-bottom: 16px;"
                +"error_outline"
            }
            h2 { 
                style = "margin-bottom: 8px;"
                +"Something went wrong" 
            }
            p { 
                style = "color: var(--md-sys-color-on-surface-variant);"
                +message 
            }
            button(classes = "md-button md-button-filled") {
                id = "retry-button"
                style = "margin-top: 24px;"
                +"Retry"
            }
        }
    }
    
    document.getElementById("retry-button")?.addEventListener("click", {
        window.location.reload()
    })
}

private fun renderApp() {
    document.body?.innerHTML = ""
    document.title = "${getPageTitle(currentPage)} - ${siteConfig?.name ?: "Recipes"}"
    
    val appLayout = document.create.div("app-layout") {
        id = "app-layout"
    }
    document.body?.appendChild(appLayout)
    
    // Create Navigation Bar
    appLayout.appendChild(createNavBar())
    
    // Create Main Content Area
    val mainContent = document.create.div("main-content") {
        id = "main-content"
    }
    appLayout.appendChild(mainContent)
    
    // Create Page Container
    val pageContainer = document.create.div("page-container") {
        id = "page-container"
    }
    mainContent.appendChild(pageContainer)
    
    // Render current page
    renderPage()
    
    // Setup nav click handlers
    setupNavigation()
}

private fun createNavBar(): HTMLElement {
    val config = siteConfig!!
    
    return document.create.nav("nav-bar") {
        id = "nav-bar"
        
        // Brand/Logo
        div("nav-bar-brand") {
            div("nav-bar-logo") {
                id = "nav-logo"
                title = config.name
                img(classes = "nav-logo-img", src = "images/avatar.svg", alt = config.name)
            }
            div("nav-bar-title-group") {
                span("nav-bar-title") { +"Brandon Monier" }
                span("nav-bar-subtitle") { +"(recipes)" }
            }
        }
        
        // Navigation Items
        div("nav-bar-items") {
            a(href = "#all-recipes", classes = "nav-bar-item") {
                attributes["data-page"] = "all-recipes"
                if (currentPage == "all-recipes") classes = classes + " active"
                span("material-icons") { +"menu_book" }
                span("nav-bar-item-label") { +"All Recipes" }
            }
        }
    }
}

private fun renderPage() {
    val container = document.getElementById("page-container") ?: return
    container.innerHTML = ""
    
    // Update document title
    val pageTitle = when (currentPage) {
        "recipe" -> currentRecipe?.metadata?.title ?: "Recipe"
        else -> getPageTitle(currentPage)
    }
    document.title = "$pageTitle - ${siteConfig?.name ?: "Recipes"}"
    
    val pageContent: HTMLElement = when (currentPage) {
        "recipes" -> createRecipeListPage(siteConfig!!, recipesData!!)
        "all-recipes" -> createAllRecipesPage(recipesData!!)
        "recipe" -> {
            val recipe = currentRecipe ?: recipesData!!.first()
            createRecipePage(recipe)
        }
        else -> createRecipeListPage(siteConfig!!, recipesData!!)
    }
    
    container.appendChild(pageContent)
    
    // Setup recipe card click handlers if on list page
    // Note: For search results, handlers are set up dynamically in RecipeListPage
    if (currentPage == "recipes" || currentPage == "all-recipes") {
        setupRecipeCardHandlers()
    }
}

private fun setupNavigation() {
    // Setup click handlers for nav items
    document.querySelectorAll(".nav-bar-item").asList().forEach { element ->
        element.addEventListener("click", { e ->
            e.preventDefault()
            val page = element.getAttribute("data-page") ?: "recipes"
            navigateToPage(page)
        })
    }
    
    // Logo click navigates to front page (no hash)
    document.getElementById("nav-logo")?.addEventListener("click", {
        currentPage = "recipes"
        window.history.pushState(null, "", window.location.pathname)
        renderPage()
        updateActiveNavItems()
        window.scrollTo(0.0, 0.0)
    })
}

private fun setupRecipeCardHandlers() {
    document.querySelectorAll(".recipe-card").asList().forEach { card ->
        card.addEventListener("click", { e ->
            e.preventDefault()
            val slug = card.getAttribute("data-slug") ?: return@addEventListener
            navigateToRecipe(slug)
        })
    }
}

private fun navigateToPage(page: String) {
    if (page == currentPage) return
    
    currentPage = page
    window.history.pushState(null, "", "#$page")
    renderPage()
    updateActiveNavItems()
    
    // Scroll to top
    window.scrollTo(0.0, 0.0)
}

private fun navigateToRecipe(slug: String) {
    val recipe = recipesData?.find { it.metadata.slug == slug }
    if (recipe != null) {
        currentRecipe = recipe
        currentPage = "recipe"
        window.history.pushState(null, "", "#recipe-$slug")
        renderPage()
        window.scrollTo(0.0, 0.0)
    }
}

private fun updateActiveNavItems() {
    document.querySelectorAll(".nav-bar-item").asList().forEach { element ->
        val page = element.getAttribute("data-page")
        if (page == currentPage || (currentPage == "recipe" && page == "all-recipes")) {
            element.classList.add("active")
        } else {
            element.classList.remove("active")
        }
    }
}

private fun getPageTitle(page: String): String {
    return when (page) {
        "recipes" -> "Recipe Search"
        "all-recipes" -> "All Recipes"
        "recipe" -> "Recipe"
        else -> "Recipe Search"
    }
}

// Extension function to convert NodeList to List
private fun org.w3c.dom.NodeList.asList(): List<Element> {
    val list = mutableListOf<Element>()
    for (i in 0 until this.length) {
        this.item(i)?.let { list.add(it as Element) }
    }
    return list
}

