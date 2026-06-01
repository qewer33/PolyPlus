//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics.registry

import net.minecraft.resources.Identifier
import org.slf4j.Logger

internal data class CosmeticPack(val name: String) {
    val geometryId: Identifier =
        Identifier.fromNamespaceAndPath(
            CosmeticPaths.cosmeticId(name).namespace,
            "${CosmeticPaths.ROOT}/$name/$name.geo.json",
        )

    val textureId: Identifier = CosmeticPaths.textureId(name)

    val animationId: Identifier = CosmeticPaths.animationId(name)

    companion object {
        fun parse(geometryFile: Identifier, logger: Logger): CosmeticPack? {
            val relative = geometryFile.path.removePrefix("${CosmeticPaths.ROOT}/")
            if (!relative.contains('/')) {
                return null
            }

            val directory = relative.substringBefore('/')
            val fileStem = relative.substringAfterLast('/')
                .removeSuffix(".geo.json")

            if (fileStem != directory) {
                logger.warn(
                    "Cosmetic pack file name must match folder (expected {}/{}.geo.json, got {})",
                    directory,
                    directory,
                    geometryFile.path,
                )
                return null
            }

            return CosmeticPack(directory)
        }
    }
}
//?}
