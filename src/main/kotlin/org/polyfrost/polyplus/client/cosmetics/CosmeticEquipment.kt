//? if >= 1.21.1 {
package org.polyfrost.polyplus.client.cosmetics

import net.minecraft.resources.Identifier
//? if >= 1.21.11 {
import net.minecraft.util.Util
//?} else {
/*import net.minecraft.Util
*///?}
import org.polyfrost.polyplus.client.bedrock.geometry.PlayerModelBone
import org.polyfrost.polyplus.client.cosmetics.runtime.AttachedCosmetic
import java.util.EnumMap

class CosmeticEquipment {
    private val equipped = EnumMap<PlayerModelBone, EquippedEntry>(PlayerModelBone::class.java)

    fun equipped(): Collection<EquippedEntry> = equipped.values

    fun get(slot: PlayerModelBone): EquippedEntry? = equipped[slot]

    fun findById(id: Identifier): EquippedEntry? =
        equipped.values.firstOrNull { it.cosmetic.id == id }

    fun equip(cosmetic: AttachedCosmetic): CosmeticEquipResult {
        val existing = equipped[cosmetic.attachSlot]
        if (existing != null) {
            return CosmeticEquipResult.SlotOccupied(cosmetic.attachSlot, existing.cosmetic.id)
        }

        equipped[cosmetic.attachSlot] = EquippedEntry(cosmetic, Util.getMillis())
        return CosmeticEquipResult.Success
    }

    fun unequip(slot: PlayerModelBone): Boolean =
        equipped.remove(slot) != null

    fun unequip(id: Identifier): Boolean {
        val slot = equipped.entries.firstOrNull { it.value.cosmetic.id == id }?.key ?: return false
        return equipped.remove(slot) != null
    }

    fun clear() {
        equipped.clear()
    }

    data class EquippedEntry(
        val cosmetic: AttachedCosmetic,
        val startTimeMs: Long,
    )
}
//?}
