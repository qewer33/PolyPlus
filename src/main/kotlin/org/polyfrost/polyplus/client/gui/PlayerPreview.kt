package org.polyfrost.polyplus.client.gui

import net.minecraft.world.entity.player.Player
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.component.extensions.named
import org.polyfrost.polyui.unit.Vec2

fun PlayerPreview(
    player: Player,
    size: Vec2,
): Drawable {
    return object : Drawable(
        size = size
    ) {
        override fun render() {
            val posX = this.x.toInt()
            val posY = this.y.toInt()
            val width = this.width
            val height = this.height
            val scale = ((if (width < height) width else height) / 1.5f).toInt()
        }
    }.named("PlayerPreview")
}
