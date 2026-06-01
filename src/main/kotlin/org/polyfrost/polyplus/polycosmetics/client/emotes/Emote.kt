//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.bedrock.animation.BedrockAnimation
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometry
import org.polyfrost.polyplus.polycosmetics.client.emotes.effects.EmoteEffect
import org.polyfrost.polyplus.polycosmetics.client.emotes.conditions.EmoteRules

data class Emote(
    val id: Identifier,
    val animation: BedrockAnimation,
    val geometry: BedrockGeometry,
    val effects: List<EmoteEffect> = emptyList(),
    val rules: EmoteRules = EmoteRules.DEFAULT,
)
//?}
