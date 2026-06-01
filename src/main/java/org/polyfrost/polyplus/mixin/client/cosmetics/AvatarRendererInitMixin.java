//? if >= 1.21.1 {
package org.polyfrost.polyplus.mixin.client.cosmetics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.polyfrost.polyplus.client.cosmetics.render.CosmeticRenderLayer;
import org.polyfrost.polyplus.client.emotes.effects.EmoteEffectRenderLayer;

//? if >= 1.21.11 {
import net.minecraft.client.model.player.PlayerModel;
//?} else {
/*import net.minecraft.client.model.PlayerModel;
*///?}
//? if < 1.21.4
//import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?} elif >= 1.21.4 {
/*import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?} else {
/*import net.minecraft.client.renderer.entity.player.PlayerRenderer;
*///?}

//? if >= 1.21.10 {
@Mixin(AvatarRenderer.class)
//?} else {
/*@Mixin(PlayerRenderer.class)
*///?}
public class AvatarRendererInitMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void polyplus$addEffectLayer(EntityRendererProvider.Context context, boolean slimSteve, CallbackInfo ci) {
        LivingEntityRendererInvoker invoker = (LivingEntityRendererInvoker) this;

        //? if >= 1.21.10 {
        @SuppressWarnings("unchecked")
        RenderLayerParent<AvatarRenderState, PlayerModel> parent =
            (RenderLayerParent<AvatarRenderState, PlayerModel>) this;
        //?} elif >= 1.21.4 {
        /*@SuppressWarnings("unchecked")
        RenderLayerParent<PlayerRenderState, PlayerModel> parent =
            (RenderLayerParent<PlayerRenderState, PlayerModel>) this;
        *///?} else {
        /*@SuppressWarnings("unchecked")
        RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent =
            (RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) this;
        *///?}

        invoker.polyplus$invokeAddLayer(new EmoteEffectRenderLayer(parent));
        invoker.polyplus$invokeAddLayer(new CosmeticRenderLayer(parent));
    }
}
//?}
