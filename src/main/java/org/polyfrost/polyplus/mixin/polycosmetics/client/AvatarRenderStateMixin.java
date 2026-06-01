//? if >= 1.21.4 {
package org.polyfrost.polyplus.mixin.polycosmetics.client;

import java.util.Collections;
import java.util.Map;

import org.polyfrost.polyplus.polycosmetics.client.api.AvatarEmoteRenderAccess;
import org.polyfrost.polyplus.polycosmetics.client.bedrock.playback.BoneTransform;
import org.polyfrost.polyplus.polycosmetics.client.emotes.playback.EmoteController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//? if >= 1.21.10 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?} else {
/*import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?}

//? if >= 1.21.10 {
@Mixin(AvatarRenderState.class)
//?} else {
/*@Mixin(PlayerRenderState.class)
*///?}
public class AvatarRenderStateMixin implements AvatarEmoteRenderAccess {
    @Unique
    private EmoteController polycosmetics$boundEmoteController = new EmoteController();

    @Unique
    private Map<String, BoneTransform> polycosmetics$lastEmoteSample = Collections.emptyMap();

    @Override
    public EmoteController polycosmetics$boundEmoteController() {
        return polycosmetics$boundEmoteController;
    }

    @Override
    public void polycosmetics$bindEmoteController(EmoteController controller) {
        polycosmetics$boundEmoteController = controller;
    }

    @Override
    public Map<String, BoneTransform> polycosmetics$lastEmoteSample() {
        return polycosmetics$lastEmoteSample;
    }

    @Override
    public void polycosmetics$setLastEmoteSample(Map<String, BoneTransform> sample) {
        polycosmetics$lastEmoteSample = sample;
    }
}
//?}
