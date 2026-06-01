//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.bedrock.animation.BedrockAnimation
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.PlayerModelBone
import org.polyfrost.polyplus.polycosmetics.client.bedrock.model.BedrockEffectModel

data class Cosmetic(
    val id: Identifier,
    val attachSlot: PlayerModelBone,
    val geometry: BedrockGeometry,
    val texture: Identifier,
    val model: BedrockEffectModel,
    val animation: BedrockAnimation? = null,
)
//?}
