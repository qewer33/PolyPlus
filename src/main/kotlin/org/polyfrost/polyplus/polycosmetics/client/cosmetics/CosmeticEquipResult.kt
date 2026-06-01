//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client.cosmetics

import net.minecraft.resources.Identifier
import org.polyfrost.polyplus.polycosmetics.client.bedrock.geometry.PlayerModelBone

sealed interface CosmeticEquipResult {
    data object Success : CosmeticEquipResult

    data class SlotOccupied(
        val slot: PlayerModelBone,
        val occupiedBy: Identifier,
    ) : CosmeticEquipResult

    data class UnknownCosmetic(val id: Identifier) : CosmeticEquipResult
}
//?}
