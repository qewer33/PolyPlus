//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes.registry

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.PackagedTextures
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient
import org.slf4j.Logger

internal data class EmotePack(val name: String) {
    val geometryId: Identifier =
        emoteAsset("emotes/$name/$name.geo.json")

    val textureId: Identifier =
        PackagedTextures.textureId("emotes", name)

    fun emoteIdFor(animationName: String): Identifier {
        val path = when {
            animationName == name || animationName.startsWith("$name.") -> name
            else -> animationName
        }

        return emoteAsset("emotes/$path")
    }

    companion object {
        fun parse(animationFile: Identifier, logger: Logger): EmotePack? {
            val relative = animationFile.path.removePrefix("${EmotePaths.ROOT}/")
            if (!relative.contains('/'))
                return null

            val directory = relative.substringBefore('/')
            val fileStem = relative.substringAfterLast('/').animationStem()

            if (fileStem != directory) {
                logger.warn(
                    "Emote pack file name must match folder (expected {}/{}.animation.json, got {})",
                    directory,
                    directory,
                    animationFile.path,
                )
                return null
            }

            return EmotePack(directory)
        }
    }
}

internal fun String.animationStem(): String =
    removeSuffix(".json").removeSuffix(".animation")

private fun emoteAsset(path: String): Identifier =
    Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, path)
//?}
