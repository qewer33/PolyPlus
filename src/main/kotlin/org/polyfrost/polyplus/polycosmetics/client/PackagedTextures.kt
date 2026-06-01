//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client

import com.mojang.blaze3d.platform.NativeImage
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

internal object PackagedTextures {
    private val logger = LoggerFactory.getLogger("${PolyCosmeticsClient.MOD_ID}/textures")
    private val registered = LinkedHashMap<Identifier, DynamicTexture>()

    fun textureId(category: String, name: String): Identifier =
        Identifier.fromNamespaceAndPath(PolyCosmeticsClient.MOD_ID, "$category/$name")

    fun hasAsset(category: String, name: String): Boolean =
        findModAsset("textures/$category/$name.png") != null

    fun register(textureId: Identifier): Identifier {
        val assetPath = assetPathFor(textureId)
        val file = findModAsset(assetPath)
            ?: throw IllegalStateException("Missing packaged texture at $assetPath")

        val client = Minecraft.getInstance()
        release(textureId)

        val nativeImage = Files.newInputStream(file).use(NativeImage::read)
        val dynamicTexture = DynamicTexture(
            //? if >= 1.21.5 {
            { textureId.toString() },
            //?}
            nativeImage,
        )
        client.textureManager.register(textureId, dynamicTexture)
        registered[textureId] = dynamicTexture
        logger.debug("Registered packaged texture {}", textureId)
        return textureId
    }

    fun releaseAll() {
        val client = Minecraft.getInstance()
        for (id in registered.keys.toList()) {
            client.textureManager.release(id)
        }
        registered.clear()
    }

    private fun release(textureId: Identifier) {
        if (!registered.containsKey(textureId)) {
            return
        }

        Minecraft.getInstance().textureManager.release(textureId)
        registered.remove(textureId)
    }

    private fun assetPathFor(textureId: Identifier): String =
        "textures/${textureId.path}.png"

    private fun findModAsset(path: String): Path? {
        val namespace = PolyCosmeticsClient.MOD_ID
        val mod = FabricLoader.getInstance().getModContainer(namespace).orElse(null)
            ?: return null

        return mod.findPath("assets/$namespace/$path").orElse(null)
    }
}
//?}
