package app.gamenative.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS

internal object SharedSteamLibrary {
    fun steamappsForRoot(gameDir: File, publicRoot: File): File? {
        val common = File(publicRoot, "Steam/steamapps/common")
        return if (gameDir.parentFile?.canonicalFile == common.canonicalFile) common.parentFile else null
    }

    fun sharedSteamapps(gameDir: File): File? {
        val common = gameDir.parentFile ?: return null
        val steamapps = common.parentFile ?: return null
        val steam = steamapps.parentFile ?: return null
        val publicRoot = steam.parentFile ?: return null
        if (common.name != "common" || steamapps.name != "steamapps" ||
            steam.name != "Steam" || publicRoot.name != "GameNative" ||
            !publicRoot.absolutePath.startsWith("/storage/emulated/")
        ) return null
        return steamappsForRoot(gameDir, publicRoot)
    }

    fun repointWineLink(link: File, target: File): Boolean {
        val path = link.toPath()
        if (Files.isSymbolicLink(path)) {
            if (Files.readSymbolicLink(path) == target.toPath()) return true
            Files.delete(path)
        } else if (Files.exists(path, NOFOLLOW_LINKS)) {
            return false
        }
        if (link.parentFile?.isDirectory != true) return false
        Files.createSymbolicLink(path, target.toPath())
        return true
    }

    fun publishManifest(manifest: File, steamapps: File?): Boolean {
        if (steamapps == null || !manifest.isFile || !steamapps.isDirectory) return false
        val destination = File(steamapps, manifest.name)
        if (destination.isFile) return true
        if (destination.exists()) return false
        manifest.copyTo(destination, overwrite = false)
        return true
    }
}
