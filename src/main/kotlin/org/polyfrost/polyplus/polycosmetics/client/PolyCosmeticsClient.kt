//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
//? if < 1.21.10 {
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
*///?}
//? if >= 26.1 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommands as ClientCommandManager
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
//?}
//? if >= 1.21.10 {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
//?}
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
//? if < 1.21.10 {
/*import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
*///?}
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.CosmeticApi
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.CosmeticEquipResult
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.registry.CosmeticRegistry
import org.polyfrost.polyplus.polycosmetics.client.emotes.EmoteApi
import org.polyfrost.polyplus.polycosmetics.client.emotes.registry.EmoteRegistry
import org.slf4j.LoggerFactory


class PolyCosmeticsClient : ClientModInitializer {
    companion object {
        const val MOD_ID = org.polyfrost.polyplus.PolyPlusConstants.ID
        private val LOGGER = LoggerFactory.getLogger(MOD_ID)
    }

    override fun onInitializeClient() {
        //? if >= 1.21.10 {
        //? if >= 26.1 {
        /*ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(EmoteRegistry.identifier, EmoteRegistry)
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(CosmeticRegistry.identifier, CosmeticRegistry)
        *///?} else {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(EmoteRegistry.identifier, EmoteRegistry)
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(CosmeticRegistry.identifier, CosmeticRegistry)
        //?}
        //?} else {
        /*ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            identifiedReloadListener(EmoteRegistry.identifier, EmoteRegistry::onResourceManagerReload),
        )
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            identifiedReloadListener(CosmeticRegistry.identifier, CosmeticRegistry::onResourceManagerReload),
        )
        *///?}

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("emotes")
                    .then(
                        ClientCommandManager.literal("play")
                            .then(
                                ClientCommandManager.argument("id", StringArgumentType.string())
                                    .executes { ctx ->
                            val rawId = StringArgumentType.getString(ctx, "id")
                            val emote = EmoteApi.findEmote(EmoteApi.parseEmoteId(rawId))
                            if (emote == null) {
                                LOGGER.warn("Unknown emote: {}", rawId)
                                return@executes 0
                            }

                            if (!EmoteApi.play(ctx.source.player, emote)) {
                                0
                            } else {
                                1
                            }
                        },
                            ),
                    ),
            )

            dispatcher.register(
                ClientCommandManager.literal("cosmetics")
                    .then(
                        ClientCommandManager.literal("list")
                            .executes { ctx ->
                                val names = CosmeticApi.all().map { it.id.toString() }
                                if (names.isEmpty()) {
                                    LOGGER.info("No cosmetics loaded")
                                } else {
                                    LOGGER.info("Cosmetics: {}", names.joinToString(", "))
                                }
                                1
                            },
                    )
                    .then(
                        ClientCommandManager.literal("equipped")
                            .executes { ctx ->
                                val player = ctx.source.player
                                val equipped = CosmeticApi.equipped(player)
                                if (equipped.isEmpty()) {
                                    LOGGER.info("No cosmetics equipped")
                                } else {
                                    for (entry in equipped) {
                                        LOGGER.info(
                                            "  {} on {}",
                                            entry.cosmetic.id,
                                            entry.cosmetic.attachSlot.serializedName,
                                        )
                                    }
                                }
                                1
                            },
                    )
                    .then(
                        ClientCommandManager.literal("equip")
                            .then(
                                ClientCommandManager.argument("id", StringArgumentType.string())
                                    .executes { ctx ->
                                val rawId = StringArgumentType.getString(ctx, "id")
                                val cosmeticId = if (rawId.contains(':')) {
                                    Identifier.parse(rawId)
                                } else {
                                    Identifier.fromNamespaceAndPath(MOD_ID, "cosmetics/$rawId")
                                }

                                when (val result = CosmeticApi.equip(ctx.source.player, cosmeticId)) {
                                    is CosmeticEquipResult.Success -> {
                                        LOGGER.info("Equipped {}", cosmeticId)
                                        1
                                    }
                                    is CosmeticEquipResult.SlotOccupied -> {
                                        LOGGER.warn(
                                            "Slot {} already has {} (cannot equip {})",
                                            result.slot.serializedName,
                                            result.occupiedBy,
                                            cosmeticId,
                                        )
                                        0
                                    }
                                    is CosmeticEquipResult.UnknownCosmetic -> {
                                        LOGGER.warn("Unknown cosmetic: {}", cosmeticId)
                                        0
                                    }
                                }
                            },
                            ),
                    )
                    .then(
                        ClientCommandManager.literal("unequip")
                            .then(
                                ClientCommandManager.argument("id", StringArgumentType.string())
                                    .executes { ctx ->
                                val rawId = StringArgumentType.getString(ctx, "id")
                                val cosmeticId = if (rawId.contains(':')) {
                                    Identifier.parse(rawId)
                                } else {
                                    Identifier.fromNamespaceAndPath(MOD_ID, "cosmetics/$rawId")
                                }

                                if (CosmeticApi.unequip(ctx.source.player, cosmeticId)) {
                                    LOGGER.info("Unequipped {}", cosmeticId)
                                    1
                                } else {
                                    LOGGER.warn("Cosmetic not equipped: {}", cosmeticId)
                                    0
                                }
                            },
                            ),
                    )
            )
        })

        LOGGER.info("PolyCosmetics client initialized")
    }

    //? if < 1.21.10 {
    /*private fun identifiedReloadListener(
        id: Identifier,
        reload: (ResourceManager) -> Unit,
    ) = object : IdentifiableResourceReloadListener, ResourceManagerReloadListener {
        override fun getFabricId(): Identifier = id

        override fun onResourceManagerReload(manager: ResourceManager) = reload(manager)
    }
    *///?}
}
//?}
