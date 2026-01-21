package org.btmonier.recipes.model

import kotlinx.serialization.Serializable

@Serializable
data class SiteConfig(
    val name: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null
)

