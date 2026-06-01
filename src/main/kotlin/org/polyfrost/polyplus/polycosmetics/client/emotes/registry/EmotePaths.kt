//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes.registry

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient

internal object EmotePaths {
    const val ROOT = "emotes"

    val reloadListener: Identifier =
        Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, ROOT)

    fun isAnimationFile(id: Identifier): Boolean =
        id.namespace == PolyCosmeticsClient.MOD_ID
            && id.path.endsWith(".json")
            && !id.path.endsWith(".geo.json")
            && !id.path.endsWith(".emote.json")

    fun emoteManifestId(animationFile: Identifier): Identifier {
        val manifestPath = animationFile.path
            .removeSuffix(".animation.json")
            .removeSuffix(".json") + ".emote.json"
        return Identifier.fromNamespaceAndPath(animationFile.namespace, manifestPath)
    }
}

internal fun resolveEmoteId(
    animationFile: Identifier,
    animationName: String,
    logger: org.slf4j.Logger,
): Identifier {
    EmotePack.parse(animationFile, logger)?.let {
        return it.emoteIdFor(animationName)
    }

    val flatStem = animationFile.path
        .removePrefix("${EmotePaths.ROOT}/")
        .animationStem()

    val path = if (animationName == flatStem)
        flatStem
    else
        animationName

    return Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, "${EmotePaths.ROOT}/$path")
}
//?}
