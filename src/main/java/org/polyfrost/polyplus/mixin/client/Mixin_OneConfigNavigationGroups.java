package org.polyfrost.polyplus.mixin.client;

import java.util.List;
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationGroup;
import org.polyfrost.polyplus.client.gui.PolyPlusOneConfigIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "org.polyfrost.oneconfig.internal.ui.navigation.RoutesKt", remap = false)
public class Mixin_OneConfigNavigationGroups {
    @Inject(method = "getNavigationGroups", at = @At("RETURN"), cancellable = true, remap = false)
    private static void polyplus$addCosmeticsNavigation(CallbackInfoReturnable<List<NavigationGroup>> cir) {
        cir.setReturnValue(PolyPlusOneConfigIntegration.navigationGroups(cir.getReturnValue()));
    }
}
