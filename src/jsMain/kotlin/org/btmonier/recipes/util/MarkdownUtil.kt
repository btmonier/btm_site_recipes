package org.btmonier.recipes.util

/**
 * Processes inline markdown formatting (bold, italic, and links) and converts to HTML.
 * First escapes HTML to prevent XSS, then converts markdown syntax.
 * 
 * Supported syntax:
 * - **text** or __text__ -> <strong>text</strong>
 * - *text* or _text_ -> <em>text</em>
 * - ***text*** -> <strong><em>text</em></strong>
 * - [text](url) -> <a href="url" target="_blank" rel="noopener noreferrer">text</a>
 */
fun processInlineMarkdown(text: String): String {
    // First escape HTML to prevent XSS
    var result = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
    
    // Convert links: [text](url) -> <a href="url">text</a>
    // Process links before bold/italic to avoid conflicts with brackets
    result = result.replace(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")) { match ->
        val linkText = match.groupValues[1]
        val url = match.groupValues[2]
        "<a href=\"$url\" target=\"_blank\" rel=\"noopener noreferrer\">$linkText</a>"
    }
    
    // Convert bold first: **text** or __text__ -> <strong>text</strong>
    result = result.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
    result = result.replace(Regex("__(.+?)__"), "<strong>$1</strong>")
    
    // Then convert italic: *text* or _text_ -> <em>text</em>
    result = result.replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
    result = result.replace(Regex("_(.+?)_"), "<em>$1</em>")
    
    return result
}
