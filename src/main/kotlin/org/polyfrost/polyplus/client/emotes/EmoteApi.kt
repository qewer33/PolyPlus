//? if >= 1.21.1 {
package org.polyfrost.polyplus.client.emotes

import net.minecraft.client.player.AbstractClientPlayer
import kotlinx.coroutines.launch
import org.polyfrost.polyplus.client.PolyPlusClient
import org.polyfrost.polyplus.client.cosmetics.CosmeticAssetCache
import org.polyfrost.polyplus.client.cosmetics.CosmeticCatalog
import org.polyfrost.polyplus.client.cosmetics.access.PlayerEmotesAccess
import org.polyfrost.polyplus.client.emotes.conditions.EmoteConditions
import org.polyfrost.polyplus.client.utils.ClientPlatform

object EmoteApi {
    fun findEmote(cosmeticId: Int): Emote? = CosmeticAssetCache.getEmote(cosmeticId)

    fun play(player: AbstractClientPlayer, cosmeticId: Int): Boolean {
        val emote = findEmote(cosmeticId) ?: return false
        return play(player, emote)
    }

    fun play(player: AbstractClientPlayer, emote: Emote): Boolean {
        if (!EmoteConditions.allows(player, emote.rules)) {
            return false
        }

        (player as PlayerEmotesAccess).`polyplus$emoteController`().play(emote)
        return true
    }

    fun playRemote(cosmeticId: Int) {
        PolyPlusClient.SCOPE.launch {
            if (!CosmeticAssetCache.ensureLoaded(cosmeticId)) return@launch
            val emote = findEmote(cosmeticId) ?: return@launch
            ClientPlatform.runOnMain {
                val player = net.minecraft.client.Minecraft.getInstance().player ?: return@runOnMain
                play(player, emote)
            }
        }
    }

    fun stop(player: AbstractClientPlayer) {
        (player as PlayerEmotesAccess).`polyplus$emoteController`().stop()
    }

    fun playOwnedEmote(player: AbstractClientPlayer, cosmeticId: Int): Boolean {
        if (cosmeticId !in CosmeticCatalog.ownedIds()) {
            return false
        }
        PolyPlusClient.SCOPE.launch {
            CosmeticAssetCache.ensureLoaded(cosmeticId)
            ClientPlatform.runOnMain {
                play(player, cosmeticId)
            }
        }
        return true
    }
}
//?}
