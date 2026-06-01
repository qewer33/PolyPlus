//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes.effects

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.model.BedrockEffectModel

data class EmoteEffect(
    val id: String,
    val geometry: BedrockGeometry,
    val texture: Identifier,
    val model: BedrockEffectModel,
)
//?}
