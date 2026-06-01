package org.polyfrost.polyplus.client.cosmetics

import org.polyfrost.polyplus.client.network.http.responses.CosmeticType
import org.polyfrost.polyplus.client.network.http.responses.PartialActiveCosmetics
import org.polyfrost.polyplus.client.utils.ClientPlatform

object CosmeticService {
    suspend fun equipCape(cosmeticId: Int): Result<Unit> =
        setActiveAndSync(PartialActiveCosmetics(cape = cosmeticId))

    suspend fun equipEmote(cosmeticId: Int): Result<Unit> =
        setActiveAndSync(PartialActiveCosmetics(emote = cosmeticId))

    suspend fun clearCape(): Result<Unit> =
        setActiveAndSync(PartialActiveCosmetics(cape = null))

    suspend fun clearEmote(): Result<Unit> =
        setActiveAndSync(PartialActiveCosmetics(emote = null))

    suspend fun equip(cosmeticId: Int): Result<Unit> {
        val definition = CosmeticCatalog.getDefinition(cosmeticId)
            ?: return Result.failure(IllegalArgumentException("Unknown cosmetic id $cosmeticId"))
        return when (definition.type) {
            CosmeticType.Cape -> equipCape(cosmeticId)
            CosmeticType.Emote -> equipEmote(cosmeticId)
        }
    }

    suspend fun syncLocalActive(): Result<Unit> = runCatching {
        CosmeticCatalog.refreshPlayer()
        val active = CosmeticCatalog.localActive()
        val ids = listOfNotNull(active.cape, active.emote)
        for (id in ids) {
            //? if >= 1.21.1 {
            CosmeticAssetCache.ensureLoaded(id)
            //?}
        }
        ClientPlatform.runOnMain {
            CosmeticCatalog.applyRemoteActive(ClientPlatform.localPlayerUuid(), ids)
            //? if >= 1.21.1 {
            CosmeticSync.applyLocalActiveFromCatalog()
            //?}
        }
    }

    private suspend fun setActiveAndSync(partial: PartialActiveCosmetics): Result<Unit> {
        val setResult = CosmeticCatalog.setActive(partial)
        if (setResult.isFailure) {
            return setResult
        }
        return syncLocalActive()
    }
}
