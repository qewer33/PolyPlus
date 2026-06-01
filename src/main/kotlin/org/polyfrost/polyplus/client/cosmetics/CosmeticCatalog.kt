package org.polyfrost.polyplus.client.cosmetics

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.LogManager
import org.polyfrost.polyplus.client.PolyPlusClient
import org.polyfrost.polyplus.client.PolyPlusConfig
import org.polyfrost.polyplus.client.network.http.getBodyAuthorized
import org.polyfrost.polyplus.client.network.http.putAuthorized
import org.polyfrost.polyplus.client.network.http.responses.ActiveCosmetics
import org.polyfrost.polyplus.client.network.http.responses.CosmeticDefinition
import org.polyfrost.polyplus.client.network.http.responses.CosmeticList
import org.polyfrost.polyplus.client.network.http.responses.CosmeticType
import org.polyfrost.polyplus.client.network.http.responses.PartialActiveCosmetics
import org.polyfrost.polyplus.client.network.http.responses.PlayerCosmetics
import org.polyfrost.polyplus.client.network.http.responses.SetActiveCosmeticsRequest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object CosmeticCatalog {
    private val LOGGER = LogManager.getLogger()
    private val lock = Mutex()

    private val definitions = ConcurrentHashMap<Int, CosmeticDefinition>()
    private val remoteActive = ConcurrentHashMap<UUID, Map<CosmeticType, Int>>()
    private var localActive: ActiveCosmetics = ActiveCosmetics()
    private var ownedIds: Set<Int> = emptySet()

    fun getDefinition(id: Int): CosmeticDefinition? = definitions[id]

    fun allDefinitions(): Collection<CosmeticDefinition> = definitions.values

    fun getRemoteActive(uuid: UUID): Map<CosmeticType, Int>? = remoteActive[uuid]

    fun getActiveId(uuid: UUID, type: CosmeticType): Int? =
        remoteActive[uuid]?.get(type)

    fun localActive(): ActiveCosmetics = localActive

    fun ownedIds(): Set<Int> = ownedIds

    suspend fun refreshCatalog() {
        val list = runCatching {
            PolyPlusClient.HTTP.get("${PolyPlusConfig.apiUrl}/cosmetics").body<CosmeticList>()
        }.onFailure { LOGGER.error("Failed to fetch cosmetic catalog", it) }
            .getOrNull() ?: return

        lock.withLock {
            definitions.clear()
            for (definition in list.contents) {
                definitions[definition.id] = definition
            }
        }

        //? if >= 1.21.1 {
        CosmeticAssetCache.preloadDefinitions(list.contents)
        //?}
        LOGGER.info("Loaded {} cosmetic definition(s) from API", list.contents.size)
    }

    suspend fun refreshPlayer() {
        val player = PolyPlusClient.HTTP
            .getBodyAuthorized<PlayerCosmetics>("${PolyPlusConfig.apiUrl}/cosmetics/player")
            .onFailure { LOGGER.error("Failed to fetch player cosmetics", it) }
            .getOrNull() ?: return

        lock.withLock {
            localActive = player.active
            ownedIds = player.owned.map { it.id }.toSet()
            for (definition in player.owned) {
                definitions[definition.id] = definition
            }
        }

        //? if >= 1.21.1 {
        CosmeticAssetCache.preloadDefinitions(player.owned)
        //?}
    }

    suspend fun setActive(partial: PartialActiveCosmetics): Result<Unit> = runCatching {
        PolyPlusClient.HTTP.putAuthorized("${PolyPlusConfig.apiUrl}/cosmetics/player") {
            contentType(ContentType.Application.Json)
            setBody(SetActiveCosmeticsRequest(partial))
        }
        Unit
    }.onFailure {
        LOGGER.error("Failed to set active cosmetics", it)
    }

    fun applyRemoteActive(uuid: UUID, cosmeticIds: List<Int>) {
        val map = mutableMapOf<CosmeticType, Int>()
        for (id in cosmeticIds) {
            val definition = getDefinition(id) ?: continue
            map[definition.type] = id
        }
        if (map.isEmpty()) {
            remoteActive.remove(uuid)
        } else {
            remoteActive[uuid] = map
        }
    }

    fun removeRemote(uuid: UUID) {
        remoteActive.remove(uuid)
    }

    fun reset() {
        definitions.clear()
        remoteActive.clear()
        localActive = ActiveCosmetics()
        ownedIds = emptySet()
    }
}
