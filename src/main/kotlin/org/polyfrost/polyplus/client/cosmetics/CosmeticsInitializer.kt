//? if >= 1.21.1 {
package org.polyfrost.polyplus.client.cosmetics

import org.polyfrost.polyplus.utils.EarlyInitializable

/** Registers cosmetics/emotes client hooks (render layers are added via mixin). */
object CosmeticsInitializer : EarlyInitializable {
    override fun earlyInitialize() {
        CosmeticSync.earlyInitialize()
    }
}
//?}
