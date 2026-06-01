//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics.registry

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.Cosmetic
import org.polyfrost.polyplus.polycosmetics.client.bedrock.animation.BedrockAnimation
import org.polyfrost.polyplus.polycosmetics.client.bedrock.animation.BedrockAnimationParser
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometryParser
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockPlayerGeometry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.resolvePlayerAttachBone
import org.polyfrost.polyplus.polycosmetics.client.bedrock.model.BedrockEffectModel
import org.polyfrost.polyplus.polycosmetics.client.PackagedAssetResources
import org.polyfrost.polyplus.polycosmetics.client.PackagedTextures
import org.slf4j.Logger

internal class CosmeticResourceLoader(
    private val manager: ResourceManager,
    private val playerGeometry: BedrockGeometry,
    private val logger: Logger,
) {
    fun loadInto(target: MutableMap<Identifier, Cosmetic>) {
        PackagedAssetResources.list(manager, CosmeticPaths.ROOT) { id ->
            CosmeticPaths.isGeometryFile(id)
        }.forEach { asset ->
            loadGeometryFile(asset, target)
        }
    }

    private fun loadGeometryFile(
        asset: PackagedAssetResources.Asset,
        target: MutableMap<Identifier, Cosmetic>,
    ) {
        val id = asset.id
        val pack = CosmeticPack.parse(id, logger) ?: return

        try {
            val geometry = asset.open().use(BedrockGeometryParser::parse)
            val attachSlot = geometry.resolvePlayerAttachBone() ?: run {
                logger.warn("Cosmetic {} has no attachment to a player model bone", id)
                return
            }

            if (geometry.bones.values.none { it.cubes.isNotEmpty() }) {
                logger.warn("Cosmetic {} has no cubes", id)
                return
            }

            if (!PackagedTextures.hasAsset(CosmeticPaths.ROOT, pack.name)) {
                logger.warn("Cosmetic {} is missing texture for {}", pack.name, pack.name)
                return
            }

            val texture = PackagedTextures.register(pack.textureId)

            val animation = loadAnimation(pack)
            val cosmeticId = CosmeticPaths.cosmeticId(pack.name)
            val model = BedrockEffectModel.build(geometry, playerGeometry)

            target[cosmeticId] = Cosmetic(
                id = cosmeticId,
                attachSlot = attachSlot,
                geometry = geometry,
                texture = texture,
                model = model,
                animation = animation,
            )

            logger.debug(
                "Loaded cosmetic {} on slot {} (animated={})",
                cosmeticId,
                attachSlot.serializedName,
                animation != null,
            )
        } catch (ex: Exception) {
            logger.error("Failed to load cosmetic geometry {}", id, ex)
        }
    }

    private fun loadAnimation(pack: CosmeticPack): BedrockAnimation? {
        val stream = PackagedAssetResources.open(manager, pack.animationId) ?: return null

        return try {
            stream.use { stream ->
                val file = BedrockAnimationParser.parseStream(stream)
                selectAnimation(file.animations, pack.name)
            }
        } catch (ex: Exception) {
            logger.error("Failed to load cosmetic animation {}", pack.animationId, ex)
            null
        }
    }

    private fun selectAnimation(
        animations: Map<String, BedrockAnimation>,
        packName: String,
    ): BedrockAnimation? {
        if (animations.isEmpty()) {
            return null
        }

        return animations[packName]
            ?: animations["$packName.idle"]
            ?: animations.values.first()
    }

    companion object {
        fun loadPlayerGeometry(manager: ResourceManager) =
            BedrockPlayerGeometry.load(manager)
    }
}
//?}
