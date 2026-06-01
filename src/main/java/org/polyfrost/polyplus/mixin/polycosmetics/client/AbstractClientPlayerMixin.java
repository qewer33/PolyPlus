//? if >= 1.21.1 {
package org.polyfrost.polyplus.mixin.polycosmetics.client;

import org.polyfrost.polyplus.polycosmetics.client.api.PlayerCosmeticsAccess;
import org.polyfrost.polyplus.polycosmetics.client.api.PlayerEmotesAccess;
import org.polyfrost.polyplus.polycosmetics.client.cosmetics.CosmeticEquipment;
import org.polyfrost.polyplus.polycosmetics.client.emotes.playback.EmoteController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.AbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements PlayerEmotesAccess, PlayerCosmeticsAccess {
    @Unique
    private final EmoteController polycosmetics$emoteController = new EmoteController();

    @Unique
    private final CosmeticEquipment polycosmetics$cosmeticEquipment = new CosmeticEquipment();

    @Override
    public EmoteController polycosmetics$emoteController() {
        return polycosmetics$emoteController;
    }

    @Override
    public CosmeticEquipment polycosmetics$cosmeticEquipment() {
        return polycosmetics$cosmeticEquipment;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void polycosmetics$tickEmote(CallbackInfo ci) {
        polycosmetics$emoteController.tick((AbstractClientPlayer) (Object) this);
    }
}
//?}
