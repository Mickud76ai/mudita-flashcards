package com.kompakt.flashcards.data

import android.content.Context
import android.content.res.AssetManager
import java.io.File
import java.io.IOException

private const val DEFAULTS_ASSET_ROOT = "default_decks"
private const val DEFAULTS_VERSION = "v1"
private const val SENTINEL_FILENAME = ".defaults_installed_$DEFAULTS_VERSION"

private fun defaultsInstalled(context: Context): Boolean =
    File(context.filesDir, SENTINEL_FILENAME).exists()

private fun markDefaultsInstalled(context: Context) {
    try {
        File(context.filesDir, SENTINEL_FILENAME).createNewFile()
    } catch (e: IOException) {
        // Silent — next launch will retry the whole install
    }
}

fun installDefaultDecks(context: Context, flashcardsDir: File) {
    if (defaultsInstalled(context)) return

    val success = try {
        copyAssetTree(context.assets, DEFAULTS_ASSET_ROOT, flashcardsDir)
        true
    } catch (e: IOException) {
        false
    }
    if (success) markDefaultsInstalled(context)
}

private fun copyAssetTree(assets: AssetManager, assetPath: String, destDir: File) {
    val children = assets.list(assetPath) ?: return
    if (children.isEmpty()) {
        // Leaf file — copy directly into destDir
        val name = assetPath.substringAfterLast('/')
        assets.open(assetPath).use { input ->
            File(destDir, name).outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return
    }
    // Directory — ensure destDir exists, recurse into children
    destDir.mkdirs()
    for (child in children) {
        val childAssetPath = "$assetPath/$child"
        val grandchildren = assets.list(childAssetPath) ?: continue
        if (grandchildren.isEmpty()) {
            // File
            assets.open(childAssetPath).use { input ->
                File(destDir, child).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            // Subdirectory
            copyAssetTree(assets, childAssetPath, File(destDir, child))
        }
    }
}
