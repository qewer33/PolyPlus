package org.polyfrost.polyplus.client.network.http.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CosmeticType {
    @SerialName("cape")
    Cape,

    @SerialName("emote")
    Emote,
}
