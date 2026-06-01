package org.polyfrost.polyplus.client.network.http.responses

import kotlinx.serialization.Serializable

@Serializable
data class CosmeticDefinition(
    val id: Int,
    val type: CosmeticType,
    val url: String? = null,
    val hash: String,
)
