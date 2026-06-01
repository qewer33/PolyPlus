//? if >= 1.21.1 {
package org.polyfrost.polyplus.mixin.polycosmetics.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

    @SuppressWarnings("UnusedReturnValue")
    @Invoker("addLayer")
    boolean polycosmetics$invokeAddLayer(RenderLayer<?, ?> layer);

}
//?}
