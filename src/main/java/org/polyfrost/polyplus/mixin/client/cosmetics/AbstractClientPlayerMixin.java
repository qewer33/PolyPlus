//? if >= 1.21.1 {
package org.polyfrost.polyplus.mixin.client.cosmetics;

import org.polyfrost.polyplus.client.cosmetics.access.PlayerCosmeticsAccess;
import org.polyfrost.polyplus.client.cosmetics.access.PlayerEmotesAccess;
import org.polyfrost.polyplus.client.cosmetics.CosmeticEquipment;
import org.polyfrost.polyplus.client.emotes.playback.EmoteController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.AbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements PlayerEmotesAccess, PlayerCosmeticsAccess {
    @Unique
    private final EmoteController polyplus$emoteController = new EmoteController();

    @Unique
    private final CosmeticEquipment polyplus$cosmeticEquipment = new CosmeticEquipment();

    @Override
    public EmoteController polyplus$emoteController() {
        return polyplus$emoteController;
    }

    @Override
    public CosmeticEquipment polyplus$cosmeticEquipment() {
        return polyplus$cosmeticEquipment;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void polyplus$tickEmote(CallbackInfo ci) {
        polyplus$emoteController.tick((AbstractClientPlayer) (Object) this);
    }
}
//?}
