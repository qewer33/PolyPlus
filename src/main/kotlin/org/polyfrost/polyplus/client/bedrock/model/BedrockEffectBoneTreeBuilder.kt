//? if >= 1.21.1 {
package org.polyfrost.polyplus.client.bedrock.model

import org.polyfrost.polyplus.client.bedrock.geometry.BedrockGeometry
import org.polyfrost.polyplus.client.bedrock.geometry.childrenByParent
import org.polyfrost.polyplus.client.bedrock.geometry.initialEffectPosition
import org.polyfrost.polyplus.client.bedrock.geometry.initialEffectRotation
import org.polyfrost.polyplus.client.bedrock.render.BedrockBoneRenderer
import org.polyfrost.polyplus.client.bedrock.render.BedrockMesh

internal class BedrockEffectBoneTreeBuilder(
    private val geometry: BedrockGeometry,
    private val playerGeometry: BedrockGeometry,
) {
    private val textureWidth = geometry.description.textureWidth
    private val textureHeight = geometry.description.textureHeight
    private val childrenByParent = geometry.childrenByParent()
    private val built = mutableMapOf<String, BedrockBoneRenderer>()

    val bones: Map<String, BedrockBoneRenderer> get() = built

    fun buildBone(name: String): BedrockBoneRenderer {
        built[name]?.let { return it }

        val bone = geometry.bones[name] ?: error("Missing bone $name")
        val children = (childrenByParent[name] ?: emptyList()).map(::buildBone)
        val position = bone.initialEffectPosition(geometry.bones, playerGeometry)
        val rotation = bone.initialEffectRotation()

        return BedrockBoneRenderer(
            name = name,
            mesh = BedrockMesh.fromBone(bone, textureWidth, textureHeight),
            children = children,
            initialPosition = position,
            initialRotation = rotation,
        ).also { built[name] = it }
    }
}
//?}
