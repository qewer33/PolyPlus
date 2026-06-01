//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import org.polyfrost.polyplus.polycosmetics.client.PackagedAssetResources
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient

object BedrockPlayerGeometry {
    val resourceId: Identifier =
        Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, "models/player.geo.json")

    fun load(manager: ResourceManager): BedrockGeometry {
        val stream = PackagedAssetResources.open(manager, resourceId) ?: throw
            IllegalStateException("Missing required geometry: $resourceId")

        return stream.use(BedrockGeometryParser::parse)
    }
}
//?}
