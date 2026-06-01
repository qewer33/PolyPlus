//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics.registry

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.Cosmetic
import org.slf4j.LoggerFactory

object CosmeticRegistry : ResourceManagerReloadListener {
    private val logger = LoggerFactory.getLogger("${PolyCosmeticsClient.MOD_ID}/cosmetics")
    private val cosmetics = LinkedHashMap<Identifier, Cosmetic>()

    fun find(id: Identifier): Cosmetic? = cosmetics[id]

    fun all(): Collection<Cosmetic> = cosmetics.values

    val identifier: Identifier = CosmeticPaths.reloadListener

    override fun onResourceManagerReload(manager: ResourceManager) {
        logger.info("Reloading cosmetics...")
        cosmetics.clear()

        val playerGeometry = CosmeticResourceLoader.loadPlayerGeometry(manager)
        CosmeticResourceLoader(manager, playerGeometry, logger).loadInto(cosmetics)

        logger.info("Loaded {} cosmetic(s)", cosmetics.size)
    }
}
//?}
