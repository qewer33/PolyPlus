package org.polyfrost.polyplus.client.network.http.responses

import kotlinx.serialization.Serializable

@Serializable
data class SetActiveCosmeticsRequest(
    val active: PartialActiveCosmetics,
)
