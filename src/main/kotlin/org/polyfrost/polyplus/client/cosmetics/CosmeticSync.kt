package org.polyfrost.polyplus.client.cosmetics

import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.PacketEvent
import org.polyfrost.oneconfig.api.event.v1.events.WorldEvent
import kotlinx.coroutines.launch
import org.polyfrost.polyplus.client.PolyPlusClient
import org.polyfrost.polyplus.client.cosmetics.access.PlayerEmotesAccess
import org.polyfrost.polyplus.client.network.http.responses.CosmeticType
import org.polyfrost.polyplus.client.network.websocket.ClientboundPacket
import org.polyfrost.polyplus.client.network.websocket.PolyConnection
import org.polyfrost.polyplus.client.network.websocket.ServerboundPacket
import org.polyfrost.polyplus.client.utils.ClientPlatform
import org.polyfrost.polyplus.events.WebSocketMessage
import org.polyfrost.polyplus.utils.Batcher
import org.polyfrost.polyplus.utils.EarlyInitializable
import java.time.Duration
import java.util.UUID

object CosmeticSync : EarlyInitializable {
    private val LOGGER = LogManager.getLogger()
    private val BATCHER = Batcher(Duration.ofMillis(200), HashSet<String>()) { players ->
        PolyConnection.sendPacket(ServerboundPacket.GetActiveCosmetics(players.toList()))
    }

    override fun earlyInitialize() {
        eventHandler<WorldEvent.Load> {
            PolyPlusClient.refreshCosmetics()
        }.register()

        eventHandler<WorldEvent.Unload> {
            CosmeticCatalog.reset()
            //? if >= 1.21.1
            CosmeticAssetCache.reset()
        }.register()

        eventHandler<WebSocketMessage> { event ->
            when (val packet = event.packet) {
                is ClientboundPacket.CosmeticsInfo -> handleCosmeticsInfo(packet)
                //? if >= 1.21.1 {
                is ClientboundPacket.EmotePlay -> handleEmotePlay(packet)
                is ClientboundPacket.EmoteStop -> handleEmoteStop(packet)
                //?}
                else -> Unit
            }
        }.register()

        eventHandler<PacketEvent.Receive> { event ->
            val packet = event.getPacket<Any>() as? ClientboundPlayerInfoUpdatePacket ?: return@eventHandler
            for (action in packet.actions()) {
                processPlayerInfoAction(action, packet.entries())
            }
        }

        eventHandler<PacketEvent.Receive> { event ->
            val packet = event.getPacket<Any>() as? ClientboundPlayerInfoRemovePacket ?: return@eventHandler
            for (uuid in packet.profileIds()) {
                CosmeticCatalog.removeRemote(uuid)
            }
        }
    }

    fun applyLocalActiveFromCatalog() {
        val active = CosmeticCatalog.localActive()
        val client = Minecraft.getInstance().player ?: return
        applyActiveToPlayer(client.uuid, listOfNotNull(active.cape, active.emote))
    }

    private fun handleCosmeticsInfo(packet: ClientboundPacket.CosmeticsInfo) {
        for ((uuidString, ids) in packet.all) {
            val uuid = UUID.fromString(uuidString)
            CosmeticCatalog.applyRemoteActive(uuid, ids)
            applyActiveToPlayer(uuid, ids)
        }
    }

    //? if >= 1.21.1 {
    private fun handleEmotePlay(packet: ClientboundPacket.EmotePlay) {
        val uuid = UUID.fromString(packet.player)
        val player = findPlayer(uuid) ?: return
        PolyPlusClient.SCOPE.launch {
            if (!CosmeticAssetCache.ensureLoaded(packet.emoteId)) return@launch
            val emote = CosmeticAssetCache.getEmote(packet.emoteId) ?: return@launch
            ClientPlatform.runOnMain {
                (player as PlayerEmotesAccess).`polyplus$emoteController`().play(emote)
            }
        }
    }

    private fun handleEmoteStop(packet: ClientboundPacket.EmoteStop) {
        val uuid = UUID.fromString(packet.player)
        val player = findPlayer(uuid) ?: return
        (player as PlayerEmotesAccess).`polyplus$emoteController`().stop()
    }
    //?}

    private fun applyActiveToPlayer(uuid: UUID, cosmeticIds: List<Int>) {
        val player = findPlayer(uuid)
        if (player == null) {
            LOGGER.debug("Deferred cosmetic apply for {} ({} id(s)) — player not loaded", uuid, cosmeticIds.size)
            return
        }

        for (id in cosmeticIds) {
            val definition = CosmeticCatalog.getDefinition(id) ?: continue
            when (definition.type) {
                CosmeticType.Cape -> Unit
                //? if >= 1.21.1 {
                CosmeticType.Emote -> applyEmote(player, id)
                //?}
            }
        }
    }

    //? if >= 1.21.1 {
    private fun applyEmote(player: AbstractClientPlayer, cosmeticId: Int) {
        PolyPlusClient.SCOPE.launch {
            if (!CosmeticAssetCache.ensureLoaded(cosmeticId)) return@launch
            val emote = CosmeticAssetCache.getEmote(cosmeticId) ?: return@launch
            ClientPlatform.runOnMain {
                (player as PlayerEmotesAccess).`polyplus$emoteController`().play(emote)
            }
        }
    }
    //?}

    private fun processPlayerInfoAction(
        action: ClientboundPlayerInfoUpdatePacket.Action,
        entries: List<ClientboundPlayerInfoUpdatePacket.Entry>,
    ) {
        when (action) {
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER -> {
                entries.forEach { entry ->
                    val uuid = entry.profileId()
                    if (uuid.isRealPlayer()) {
                        BATCHER.add(uuid.toString())
                    }
                }
            }
            else -> return
        }
    }

    private fun findPlayer(uuid: UUID): AbstractClientPlayer? {
        val level = Minecraft.getInstance().level ?: return null
        return level.players().firstOrNull { it.uuid == uuid }
    }

    private fun UUID.isRealPlayer(): Boolean = version() != 2
}
