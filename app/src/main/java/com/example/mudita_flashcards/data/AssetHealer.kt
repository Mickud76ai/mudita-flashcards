package com.example.mudita_flashcards.data

import android.content.Context
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

private val MANAGED_ASSETS = listOf("template.csv", "instructions.md")

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

fun ensureFlashcardsDirectoryReady(context: Context): Boolean {
    val flashcardsDir = getFlashcardsDir(context) ?: return false

    if (!flashcardsDir.exists() && !flashcardsDir.mkdirs()) {
        return false
    }

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
