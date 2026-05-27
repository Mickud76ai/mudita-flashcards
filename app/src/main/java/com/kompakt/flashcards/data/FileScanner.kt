package com.kompakt.flashcards.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import java.io.File

const val FLASHCARDS_DIR_NAME = "Flashcards"

@Suppress("DEPRECATION")
fun getFlashcardsDir(): File? {
    val externalRoot = Environment.getExternalStorageDirectory() ?: return null
    return File(externalRoot, FLASHCARDS_DIR_NAME)
}

fun getFlashcardsDir(@Suppress("UNUSED_PARAMETER") context: Context): File? = getFlashcardsDir()

fun hasFullStorageAccess(): Boolean = Environment.isExternalStorageManager()

fun openManageStorageSettings(context: Context) {
    val packageUri = Uri.parse("package:${context.packageName}")
    val direct = Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        packageUri,
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(direct) }
        .recoverCatching {
            val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
        }
}

fun shouldIgnoreFile(file: File, @Suppress("UNUSED_PARAMETER") rootDir: File): Boolean {
    if (!file.name.endsWith(".csv", ignoreCase = true)) return true
    return false
}

data class ScanResult(
    val folders: List<BrowserItem.FolderItem>,
    val decks: List<BrowserItem.DeckItem>,
)

fun scanDirectory(dir: File, rootDir: File): ScanResult {
    val contents = dir.listFiles() ?: return ScanResult(emptyList(), emptyList())

    val folders = contents
        .filter { it.isDirectory }
        .sortedBy { it.name.lowercase() }
        .map { BrowserItem.FolderItem(name = it.name, path = it) }

    val decks = contents
        .filter { it.isFile && !shouldIgnoreFile(it, rootDir) }
        .sortedBy { it.name.lowercase() }
        .map { file ->
            val (name, count) = quickParseCsvHeader(file)
            BrowserItem.DeckItem(name = name, file = file, cardCount = count)
        }

    return ScanResult(folders = folders, decks = decks)
}

fun listAllDecks(rootDir: File): List<DeckListing> {
    if (!rootDir.exists()) return emptyList()
    return rootDir.walkTopDown()
        .filter { it.isFile && !shouldIgnoreFile(it, rootDir) }
        .map { file ->
            val (name, _) = quickParseCsvHeader(file)
            val relative = file.relativeTo(rootDir).path
                .replace('\\', '/')
                .removeSuffix(".csv")
            DeckListing(file = file, deckName = name, displayPath = relative)
        }
        .sortedBy { it.displayPath.lowercase() }
        .toList()
}

fun deleteDeck(context: Context, deckFile: File) {
    val rootDir = getFlashcardsDir() ?: return
    val relativePath = runCatching {
        deckFile.relativeTo(rootDir).path.replace('\\', '/')
    }.getOrNull() ?: deckFile.name
    deckFile.delete()
    deleteDeckProgress(context, relativePath)
}
