package org.btmonier.recipes

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.btmonier.recipes.model.*
import org.w3c.fetch.Response

object ContentLoader {
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    private suspend fun fetchText(path: String): String {
        val response: Response = window.fetch(path).await()
        if (!response.ok) {
            throw RuntimeException("Failed to fetch $path: ${response.status}")
        }
        return response.text().await()
    }

    suspend fun loadSiteConfig(): SiteConfig {
        val text = fetchText("content/site.json")
        return json.decodeFromString<SiteConfig>(text)
    }

    suspend fun loadRecipes(): List<Recipe> {
        val text = fetchText("content/recipes.json")
        return json.decodeFromString<RecipeList>(text).recipes
    }
}

