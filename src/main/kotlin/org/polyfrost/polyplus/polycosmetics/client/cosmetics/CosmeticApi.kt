//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics

import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.api.PlayerCosmeticsAccess
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.registry.CosmeticRegistry
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.PlayerModelBone

object CosmeticApi {
    fun find(id: Identifier): Cosmetic? = CosmeticRegistry.find(id)

    fun all(): Collection<Cosmetic> = CosmeticRegistry.all()

    fun equipped(player: AbstractClientPlayer): Collection<CosmeticEquipment.EquippedEntry> =
        (player as PlayerCosmeticsAccess).`polycosmetics$cosmeticEquipment`().equipped()

    fun equip(player: AbstractClientPlayer, cosmeticId: Identifier): CosmeticEquipResult {
        val cosmetic = CosmeticRegistry.find(cosmeticId)
            ?: return CosmeticEquipResult.UnknownCosmetic(cosmeticId)

        return (player as PlayerCosmeticsAccess).`polycosmetics$cosmeticEquipment`().equip(cosmetic)
    }

    fun unequip(player: AbstractClientPlayer, cosmeticId: Identifier): Boolean =
        (player as PlayerCosmeticsAccess).`polycosmetics$cosmeticEquipment`().unequip(cosmeticId)

    fun unequipSlot(player: AbstractClientPlayer, slot: PlayerModelBone): Boolean =
        (player as PlayerCosmeticsAccess).`polycosmetics$cosmeticEquipment`().unequip(slot)

    fun clear(player: AbstractClientPlayer) {
        (player as PlayerCosmeticsAccess).`polycosmetics$cosmeticEquipment`().clear()
    }
}
//?}
