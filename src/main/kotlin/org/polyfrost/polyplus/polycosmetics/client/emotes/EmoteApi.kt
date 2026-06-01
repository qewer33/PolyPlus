//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.emotes

import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.api.PlayerEmotesAccess
import org.polyfrost.polyplus.polycosmetics.client.emotes.conditions.EmoteConditions
import org.polyfrost.polyplus.polycosmetics.client.PolyCosmeticsClient
import org.polyfrost.polyplus.polycosmetics.client.emotes.registry.EmoteRegistry

object EmoteApi {
    fun parseEmoteId(raw: String): Identifier {
        if (':' in raw) {
            return Identifier.parse(raw)
        }

        val path = raw.removePrefix("/")
        val normalized = if (path.startsWith("emotes/")) path else "emotes/$path"
        return Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, normalized)
    }

    fun findEmote(id: Identifier): Emote? {
        EmoteRegistry.find(id)?.let { return it }

        if (!id.path.startsWith("emotes/")) {
            EmoteRegistry.find(
                Identifier.fromNamespaceAndPath(id.namespace, "emotes/${id.path}"),
            )?.let { return it }
        }

        val stem = id.path.removePrefix("emotes/")
        val matches = EmoteRegistry.all().filter { emote ->
            val emotePath = emote.id.path.removePrefix("emotes/")
            emotePath == stem || emotePath.startsWith("$stem.")
        }
        if (matches.size == 1) {
            return matches.first()
        }

        return null
    }

    fun play(player: AbstractClientPlayer, emote: Emote): Boolean {
        if (!EmoteConditions.allows(player, emote.rules))
            return false

        (player as PlayerEmotesAccess).`polycosmetics$emoteController`().play(emote)
        return true
    }

    fun stop(player: AbstractClientPlayer) {
        (player as PlayerEmotesAccess).`polycosmetics$emoteController`().stop()
    }
}
//?}
