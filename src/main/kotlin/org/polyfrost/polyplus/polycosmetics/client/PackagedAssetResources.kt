//? if >= 1.21.1 {
package org.polyfrost.polyplus.polycosmetics.client

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

internal object PackagedAssetResources {
    data class Asset(
        val id: Identifier,
        val open: () -> InputStream,
    )

    fun open(manager: ResourceManager, id: Identifier): InputStream? {
        manager.getResource(id).orElse(null)?.let { return it.open() }

        val path = packagedPath(id) ?: return null
        return Files.newInputStream(path)
    }

    fun exists(manager: ResourceManager, id: Identifier): Boolean =
        manager.getResource(id).isPresent || packagedPath(id) != null

    fun list(
        manager: ResourceManager,
        root: String,
        predicate: (Identifier) -> Boolean,
    ): List<Asset> {
        val assets = LinkedHashMap<Identifier, Asset>()

        manager.listResources(root) { predicate(it) }
            .forEach { (id, resource) ->
                assets[id] = Asset(id) { resource.open() }
            }

        listPackaged(root, predicate).forEach { asset ->
            assets.putIfAbsent(asset.id, asset)
        }

        return assets.values.toList()
    }

    private fun listPackaged(
        root: String,
        predicate: (Identifier) -> Boolean,
    ): List<Asset> {
        val namespace = PolyCosmeticsClient.MOD_ID
        val base = packagedPath(namespace, root) ?: return emptyList()
        val assets = mutableListOf<Asset>()

        Files.walk(base).use { paths ->
            val iterator = paths.iterator()
            while (iterator.hasNext()) {
                val path = iterator.next()
                if (!Files.isRegularFile(path)) {
                    continue
                }

                val relative = base.relativize(path).toResourcePath()
                val id = Identifier.fromNamespaceAndPath(namespace, "$root/$relative")
                if (predicate(id)) {
                    assets += Asset(id) { Files.newInputStream(path) }
                }
            }
        }

        return assets.sortedBy { it.id.toString() }
    }

    private fun packagedPath(id: Identifier): Path? =
        packagedPath(id.namespace, id.path)

    private fun packagedPath(namespace: String, path: String): Path? {
        val mod = FabricLoader.getInstance().getModContainer(namespace).orElse(null)
            ?: return null

        return mod.findPath("assets/$namespace/$path").orElse(null)
    }

    private fun Path.toResourcePath(): String =
        joinToString("/") { it.toString() }
}
//?}
