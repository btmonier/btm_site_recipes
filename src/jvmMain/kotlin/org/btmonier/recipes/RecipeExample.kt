package org.btmonier.recipes

import java.io.File

/**
 * Example usage of the RecipeProcessor.
 * This demonstrates how to process a recipe markdown file and generate HTML.
 */
fun main(args: Array<String>) {
    val processor = RecipeProcessor()
    
    // Read the example recipe
    val recipeFile = File("data/recipe_example.md")
    if (!recipeFile.exists()) {
        println("Error: Recipe file not found at ${recipeFile.absolutePath}")
        return
    }
    
    val markdown = recipeFile.readText()
    
    // Process the recipe
    val html = processor.processRecipe(markdown)
    
    // Create a complete HTML page with styles
    val fullHtml = createFullHtmlPage(html)
    
    // Write to output file
    val outputFile = File("build/recipe_output.html")
    outputFile.parentFile.mkdirs()
    outputFile.writeText(fullHtml)
    
    println("Recipe HTML generated successfully!")
    println("Output file: ${outputFile.absolutePath}")
    println("Open this file in your browser to view the recipe.")
}

private fun createFullHtmlPage(recipeHtml: String): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recipe</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Calistoga&family=Roboto:wght@400;500&display=swap">
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
    <style>
        /* Import the main.css from btm_site_core if available, or use inline styles */
        /* For now, we'll include the essential M3 variables and base styles */
        :root {
            --md-sys-color-primary: #006A6A;
            --md-sys-color-on-primary: #FFFFFF;
            --md-sys-color-primary-container: #cce8e7;
            --md-sys-color-on-primary-container: #002020;
            --md-sys-color-secondary: #4A6363;
            --md-sys-color-on-secondary: #FFFFFF;
            --md-sys-color-secondary-container: #CCE8E7;
            --md-sys-color-on-secondary-container: #051F1F;
            --md-sys-color-tertiary: #4B607C;
            --md-sys-color-on-tertiary: #FFFFFF;
            --md-sys-color-tertiary-container: #D3E4FF;
            --md-sys-color-on-tertiary-container: #041C35;
            --md-sys-color-error: #BA1A1A;
            --md-sys-color-on-error: #FFFFFF;
            --md-sys-color-error-container: #FFDAD6;
            --md-sys-color-on-error-container: #410002;
            --md-sys-color-surface: #FAFDFC;
            --md-sys-color-on-surface: #191C1C;
            --md-sys-color-surface-variant: #DAE5E4;
            --md-sys-color-on-surface-variant: #3F4948;
            --md-sys-color-surface-container-lowest: #FFFFFF;
            --md-sys-color-surface-container-low: #F4F7F6;
            --md-sys-color-surface-container: #EEF1F0;
            --md-sys-color-surface-container-high: #E8EBEA;
            --md-sys-color-surface-container-highest: #E3E6E5;
            --md-sys-color-outline: #6F7979;
            --md-sys-color-outline-variant: #BEC9C8;
            --md-sys-color-background: #FAFDFC;
            --md-sys-color-on-background: #191C1C;
            --md-sys-typescale-display-large: 400 57px/64px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-display-medium: 400 45px/52px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-display-small: 400 36px/44px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-headline-large: 400 32px/40px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-headline-medium: 400 28px/36px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-headline-small: 400 24px/32px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-title-large: 400 22px/28px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-title-medium: 500 16px/24px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-title-small: 500 14px/20px 'Google Sans', 'Roboto', sans-serif;
            --md-sys-typescale-body-large: 400 16px/24px 'Roboto', sans-serif;
            --md-sys-typescale-body-medium: 400 14px/20px 'Roboto', sans-serif;
            --md-sys-typescale-body-small: 400 12px/16px 'Roboto', sans-serif;
            --md-sys-typescale-label-large: 500 14px/20px 'Roboto', sans-serif;
            --md-sys-typescale-label-medium: 500 12px/16px 'Roboto', sans-serif;
            --md-sys-typescale-label-small: 500 11px/16px 'Roboto', sans-serif;
            --md-sys-shape-corner-none: 0px;
            --md-sys-shape-corner-extra-small: 4px;
            --md-sys-shape-corner-small: 8px;
            --md-sys-shape-corner-medium: 12px;
            --md-sys-shape-corner-large: 16px;
            --md-sys-shape-corner-extra-large: 28px;
            --md-sys-shape-corner-full: 9999px;
            --md-sys-elevation-1: 0px 1px 2px rgba(0, 0, 0, 0.3), 0px 1px 3px 1px rgba(0, 0, 0, 0.15);
            --md-sys-elevation-2: 0px 1px 2px rgba(0, 0, 0, 0.3), 0px 2px 6px 2px rgba(0, 0, 0, 0.15);
        }
        
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }
        
        body {
            font: var(--md-sys-typescale-body-large);
            background-color: var(--md-sys-color-background);
            color: var(--md-sys-color-on-background);
            min-height: 100vh;
            padding: 32px 24px;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }
        
        ${File("src/jvmMain/resources/styles/recipe.css").readText()}
    </style>
</head>
<body>
    $recipeHtml
</body>
</html>
    """.trimIndent()
}

