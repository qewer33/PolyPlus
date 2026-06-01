//? if >= 1.21.4 {
package org.polyfrost.polyplus.polycosmetics.client.api;

import java.util.Collections;
import java.util.Map;

import org.polyfrost.polyplus.polycosmetics.client.bedrock.playback.BoneTransform;
import org.polyfrost.polyplus.polycosmetics.client.emotes.playback.EmoteController;

public interface AvatarEmoteRenderAccess {

    EmoteController polycosmetics$boundEmoteController();

    void polycosmetics$bindEmoteController(EmoteController controller);

    Map<String, BoneTransform> polycosmetics$lastEmoteSample();

    void polycosmetics$setLastEmoteSample(Map<String, BoneTransform> sample);
}
//?}
