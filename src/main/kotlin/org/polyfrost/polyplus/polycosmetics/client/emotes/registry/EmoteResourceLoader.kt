//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes.registry

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import org.polyfrost.polyplus.polycosmetics.client.PackagedAssetResources
import org.polyfrost.polyplus.polycosmetics.client.PackagedTextures
import org.polyfrost.polyplus.polycosmetics.client.emotes.Emote
import org.polyfrost.polyplus.polycosmetics.client.bedrock.animation.BedrockAnimationParser
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockGeometryParser
import org.polyfrost.polyplus.polycosmetics.client.emotes.conditions.EmoteRules
import org.polyfrost.polyplus.polycosmetics.client.emotes.conditions.EmoteRulesParser
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockPlayerGeometry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.model.BedrockEffectModel
import org.polyfrost.polyplus.polycosmetics.client.emotes.effects.EmoteEffect
import org.slf4j.Logger

internal class EmoteResourceLoader(
    private val manager: ResourceManager,
    private val playerGeometry: BedrockGeometry,
    private val logger: Logger,
) {
    fun loadInto(target: MutableMap<Identifier, Emote>) {
        PackagedAssetResources.list(manager, EmotePaths.ROOT, EmotePaths::isAnimationFile)
            .forEach { asset -> loadAnimationFile(asset, target) }
    }

    private fun loadAnimationFile(
        asset: PackagedAssetResources.Asset,
        target: MutableMap<Identifier, Emote>,
    ) {
        val id = asset.id
        try {
            asset.open().use { stream ->
                val file = BedrockAnimationParser.parseStream(stream)
                val effects = loadPairedEffects(id)
                val rulesByAnimation = loadEmoteRules(id)

                for ((animationName, animation) in file.animations) {
                    val emoteId = resolveEmoteId(id, animationName, logger)
                    val rules = rulesByAnimation[animationName] ?: EmoteRules.DEFAULT
                    target[emoteId] = Emote(emoteId, animation, playerGeometry, effects, rules)

                    logger.debug(
                        "Loaded emote {} ({} effect model(s))",
                        emoteId,
                        effects.size,
                    )
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to load emote animation {}", id, ex)
        }
    }

    private fun loadEmoteRules(animationFile: Identifier): Map<String, EmoteRules> {
        val manifestId = EmotePaths.emoteManifestId(animationFile)
        val stream = PackagedAssetResources.open(manager, manifestId) ?: return emptyMap()

        return try {
            stream.use(EmoteRulesParser::parseStream)
        } catch (ex: Exception) {
            logger.error("Failed to load emote rules {}", manifestId, ex)
            emptyMap()
        }
    }

    private fun loadPairedEffects(animationFile: Identifier): List<EmoteEffect> {
        val pack = EmotePack.parse(animationFile, logger) ?: return emptyList()

        val stream = PackagedAssetResources.open(manager, pack.geometryId) ?: run {
            logger.warn(
                "Packaged emote {} is missing geometry at {}",
                animationFile,
                pack.geometryId,
            )
            return emptyList()
        }

        return try {
            stream.use { stream ->
                val geometry = BedrockGeometryParser.parse(stream)
                if (geometry.bones.values.none { it.cubes.isNotEmpty() }) {
                    logger.warn("Effect geometry {} has no cubes", pack.geometryId)
                    return emptyList()
                }

                if (!PackagedTextures.hasAsset("emotes", pack.name)) {
                    logger.warn("Packaged emote {} is missing texture for {}", animationFile, pack.name)
                    return emptyList()
                }

                listOf(
                    EmoteEffect(
                        id = geometry.identifier,
                        geometry = geometry,
                        texture = PackagedTextures.register(pack.textureId),
                        model = BedrockEffectModel.build(geometry, playerGeometry),
                    ),
                )
            }
        } catch (ex: Exception) {
            logger.error("Failed to load effect geometry {}", pack.geometryId, ex)
            emptyList()
        }
    }

    companion object {
        fun loadPlayerGeometry(manager: ResourceManager) =
            BedrockPlayerGeometry.load(manager)
    }
}
//?}
