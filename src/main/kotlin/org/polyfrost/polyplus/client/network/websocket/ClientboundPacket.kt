package org.polyfrost.polyplus.client.network.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientboundPacket {
    @Serializable
    @SerialName("Error")
    data class Error(@SerialName("error_code") val code: String, val message: String) : ClientboundPacket

    @Serializable
    @SerialName("CosmeticsInfo")
    data class CosmeticsInfo(@SerialName("cosmetics") val all: HashMap<String, List<Int>>) : ClientboundPacket

    @Serializable
    @SerialName("EmotePlay")
    data class EmotePlay(
        val player: String,
        @SerialName("emote_id") val emoteId: Int,
        @SerialName("start_time") val startTime: Long,
    ) : ClientboundPacket

    @Serializable
    @SerialName("EmoteStop")
    data class EmoteStop(val player: String) : ClientboundPacket
}
