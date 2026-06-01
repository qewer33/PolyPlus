//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics.registry

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.PackagedTextures
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.BedrockPlayerGeometry

internal object CosmeticPaths {
    const val ROOT = "cosmetics"

    val reloadListener: Identifier =
        Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, ROOT)

    val playerGeometry: Identifier = BedrockPlayerGeometry.resourceId

    fun isGeometryFile(id: Identifier): Boolean =
        id.namespace == PolyCosmeticsClient.MOD_ID
            && id.path.startsWith("$ROOT/")
            && id.path.endsWith(".geo.json")

    fun cosmeticId(packName: String): Identifier =
        Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, "$ROOT/$packName")

    fun textureId(packName: String): Identifier =
        PackagedTextures.textureId(ROOT, packName)

    fun animationId(packName: String): Identifier =
        Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, "$ROOT/$packName/$packName.animation.json")
}
//?}
