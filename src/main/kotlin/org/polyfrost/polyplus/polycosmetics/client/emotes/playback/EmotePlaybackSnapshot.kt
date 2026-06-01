//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes.playback

import org.polyfrost.polyplus.polycosmetics.client.bedrock.playback.BoneTransform
import org.polyfrost.polyplus.polycosmetics.client.emotes.Emote

data class EmotePlaybackSnapshot(
    val emote: Emote,
    val sample: Map<String, BoneTransform>,
    val playerWeight: Float,
    val effectPoseWeight: Float = playerWeight,
)
//?}
