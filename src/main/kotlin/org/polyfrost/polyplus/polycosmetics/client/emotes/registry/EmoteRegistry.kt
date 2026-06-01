//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes.registry

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient
import org.polyfrost.polyplus.polycosmetics.client.emotes.Emote
import org.slf4j.LoggerFactory

object EmoteRegistry : ResourceManagerReloadListener {
    private val logger = LoggerFactory.getLogger("${PolyCosmeticsClient.MOD_ID}/emotes")
    private val emotes = LinkedHashMap<Identifier, Emote>()

    fun find(id: Identifier): Emote? = emotes[id]

    fun all(): Collection<Emote> = emotes.values

    val identifier: Identifier = EmotePaths.reloadListener

    override fun onResourceManagerReload(manager: ResourceManager) {
        logger.info("Reloading emotes...")
        emotes.clear()

        val playerGeometry = EmoteResourceLoader.loadPlayerGeometry(manager)
        EmoteResourceLoader(manager, playerGeometry, logger).loadInto(emotes)

        logger.info("Loaded {} emote(s)", emotes.size)
    }
}
//?}
