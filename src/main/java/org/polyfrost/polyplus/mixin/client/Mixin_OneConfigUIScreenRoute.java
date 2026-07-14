package org.polyfrost.polyplus.mixin.client;

import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.internal.ui.compose.impls.OneConfigUIScreen;
import org.polyfrost.polyplus.client.gui.PolyPlusOneConfigIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OneConfigUIScreen.class, remap = false)
public class Mixin_OneConfigUIScreenRoute {
    @Unique
    private Object polyplus$openingRoute;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;Lorg/polyfrost/oneconfig/api/config/v1/Tree;)V", at = @At("RETURN"), remap = false)
    private void polyplus$captureOpeningRoute(String initialTreeId, String initialCategory, Tree initialTree, CallbackInfo ci) {
        polyplus$openingRoute = PolyPlusOneConfigIntegration.consumePendingOpeningRoute();
    }

    @Inject(method = "resolveOpeningBehaviorRoute", at = @At("HEAD"), cancellable = true, remap = false)
    private void polyplus$forceOpeningRoute(CallbackInfoReturnable<Object> cir) {
        if (polyplus$openingRoute != null) {
            cir.setReturnValue(polyplus$openingRoute);
        }
    }
}
