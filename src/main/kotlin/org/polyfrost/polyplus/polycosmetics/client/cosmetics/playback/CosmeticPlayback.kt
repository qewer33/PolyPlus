//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics.playback

import org.polyfrost.polyplus.polycosmetics.client.bedrock.playback.BedrockAnimationPlayback
import org.polyfrost.polyplus.polycosmetics.client.bedrock.playback.BoneTransform
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.Cosmetic
import org.polyfrost.polyplus.polycosmetics.client.render.PlayerRenderContext

object CosmeticPlayback {
    fun sample(
        cosmetic: Cosmetic,
        startTimeMs: Long,
        renderContext: PlayerRenderContext,
    ): Map<String, BoneTransform> {
        val animation = cosmetic.animation ?: return emptyMap()

        return BedrockAnimationPlayback.sampleTimed(
            animation = animation,
            startTimeMs = startTimeMs,
            renderContext = renderContext,
            clampOnce = true,
        )
    }
}
//?}
