//? if >= 1.21.4 {
package org.polyfrost.polyplus.mixin.client.cosmetics;

import java.util.Collections;
import java.util.Map;

import org.polyfrost.polyplus.client.cosmetics.access.AvatarEmoteRenderAccess;
import org.polyfrost.polyplus.client.bedrock.playback.BoneTransform;
import org.polyfrost.polyplus.client.emotes.playback.EmoteController;
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
    private EmoteController polyplus$boundEmoteController = new EmoteController();

    @Unique
    private Map<String, BoneTransform> polyplus$lastEmoteSample = Collections.emptyMap();

    @Override
    public EmoteController polyplus$boundEmoteController() {
        return polyplus$boundEmoteController;
    }

    @Override
    public void polyplus$bindEmoteController(EmoteController controller) {
        polyplus$boundEmoteController = controller;
    }

    @Override
    public Map<String, BoneTransform> polyplus$lastEmoteSample() {
        return polyplus$lastEmoteSample;
    }

    @Override
    public void polyplus$setLastEmoteSample(Map<String, BoneTransform> sample) {
        polyplus$lastEmoteSample = sample;
    }
}
//?}
