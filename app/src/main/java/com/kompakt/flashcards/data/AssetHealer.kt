package com.kompakt.flashcards.data

import android.content.Context
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

private val MANAGED_ASSETS = listOf("how_to_create_decks.md")
private val LEGACY_ASSETS = listOf("instructions.md", "template.csv")
private const val LEGACY_CLEANUP_SENTINEL = ".legacy_assets_cleaned_v1"

private fun computeSha256(input: InputStream): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192)
    while (true) {
        val read = input.read(buffer)
        if (read <= 0) break
        digest.update(buffer, 0, read)
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

private fun ensureAssetFileIntact(context: Context, assetName: String, destFile: File) {
    val assetHash = context.assets.open(assetName).use { computeSha256(it) }
    val currentHash = if (destFile.exists()) {
        destFile.inputStream().use { computeSha256(it) }
    } else null

    if (currentHash != assetHash) {
        context.assets.open(assetName).use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

private fun cleanupLegacyAssetsOnce(context: Context, flashcardsDir: File) {
    val sentinel = File(context.filesDir, LEGACY_CLEANUP_SENTINEL)
    if (sentinel.exists()) return
    for (legacy in LEGACY_ASSETS) {
        val f = File(flashcardsDir, legacy)
        if (f.exists() && f.isFile) {
            f.delete()
        }
    }
    runCatching { sentinel.createNewFile() }
}

fun ensureFlashcardsDirectoryReady(context: Context): Boolean {
    val flashcardsDir = getFlashcardsDir(context) ?: return false

    if (!flashcardsDir.exists() && !flashcardsDir.mkdirs()) {
        return false
    }

    cleanupLegacyAssetsOnce(context, flashcardsDir)

    for (assetName in MANAGED_ASSETS) {
        try {
            ensureAssetFileIntact(context, assetName, File(flashcardsDir, assetName))
        } catch (e: IOException) {
            // Silent failure: next launch will try again. User's own decks remain unaffected.
        }
    }

    installDefaultDecks(context, flashcardsDir)
    return true
}
