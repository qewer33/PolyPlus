//? if >= 1.21.4 && < 1.21.5 {
/*package org.polyfrost.polyplus.mixin.client.cosmetics;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherPlayerAccessor {
    @Accessor("playerRenderers")
    Map<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> polyplus$playerRenderers();
}
*///?}
