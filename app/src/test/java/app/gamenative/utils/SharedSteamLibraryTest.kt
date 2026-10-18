package app.gamenative.utils

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SharedSteamLibraryTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun recognizesAStandardSteamLibraryAndRejectsOtherFolders() {
        val root = temporary.newFolder("GameNative")
        val steamapps = File(root, "Steam/steamapps").apply { mkdirs() }
        val common = File(steamapps, "common").apply { mkdirs() }
        val game = File(common, "My Game").apply { mkdirs() }

        assertEquals(steamapps.canonicalFile, SharedSteamLibrary.steamappsForRoot(game, root)?.canonicalFile)
        assertNull(SharedSteamLibrary.steamappsForRoot(File(root, "GOG/My Game"), root))
        assertNull(SharedSteamLibrary.steamappsForRoot(File(steamapps, "My Game"), root))
    }

    @Test fun publishesOnlyTheManifestWithoutOverwritingSteamChanges() {
        val root = temporary.newFolder("GameNative")
        val steamapps = File(root, "Steam/steamapps").apply { mkdirs() }
        val privateManifest = File(temporary.root, "appmanifest_42.acf").apply { writeText("original") }
        val game = File(steamapps, "common/Test").apply { mkdirs() }
        File(game, "game.exe").writeText("game file")

        assertTrue(SharedSteamLibrary.publishManifest(privateManifest, steamapps))
        val published = File(steamapps, privateManifest.name)
        assertEquals("original", published.readText())
        published.writeText("steam updated")
        assertTrue(SharedSteamLibrary.publishManifest(privateManifest, steamapps))
        assertEquals("steam updated", published.readText())
        assertEquals("game file", File(game, "game.exe").readText())
    }

    @Test fun repairsStaleWineSymlinksAndPreservesRealDirectories() {
        val root = temporary.newFolder("wine")
        val common = File(root, "common").apply { mkdirs() }
        val game = temporary.newFolder("movedGame")
        val old = File(temporary.root, "oldGame")
        val link = File(common, "Example")
        Files.createSymbolicLink(link.toPath(), old.toPath())

        assertTrue(SharedSteamLibrary.repointWineLink(link, game))
        assertEquals(game.toPath(), Files.readSymbolicLink(link.toPath()))
        assertTrue(SharedSteamLibrary.repointWineLink(link, game))

        val realDirectory = File(common, "Other").apply { mkdirs() }
        assertFalse(SharedSteamLibrary.repointWineLink(realDirectory, game))
        assertTrue(realDirectory.isDirectory)
    }
}
