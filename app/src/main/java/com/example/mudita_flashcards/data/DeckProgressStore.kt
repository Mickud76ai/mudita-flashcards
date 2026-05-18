package com.example.mudita_flashcards.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

@Serializable
private data class CardProgressEntryDto(
    val cardHash: String,
    val timesShown: Int,
    val isHard: Boolean,
)

@Serializable
private data class DeckProgressDto(
    val deckRelativePath: String,
    val deckName: String,
    val lastSavedAt: Long,
    val entries: Map<String, CardProgressEntryDto>,
)

private val json = Json { ignoreUnknownKeys = true }

private fun sha1(input: String): String {
    val digest = MessageDigest.getInstance("SHA-1")
    return digest.digest(input.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

fun cardHash(card: FlashCard): String = sha1(card.front + " " + card.back)

private fun progressDir(context: Context): File =
    File(context.filesDir, "progress").also { it.mkdirs() }

private fun progressFileFor(context: Context, deckRelativePath: String): File =
    File(progressDir(context), "${sha1(deckRelativePath)}.json")

fun loadDeckProgress(context: Context, deckRelativePath: String): DeckProgress {
    val file = progressFileFor(context, deckRelativePath)
    if (!file.exists()) return DeckProgress(deckRelativePath, "")
    return try {
        val dto = json.decodeFromString<DeckProgressDto>(file.readText(Charsets.UTF_8))
        DeckProgress(
            deckRelativePath = dto.deckRelativePath,
            deckName = dto.deckName,
            entries = dto.entries.mapValues {
                CardProgressEntry(it.value.cardHash, it.value.timesShown, it.value.isHard)
            },
            lastSavedAt = dto.lastSavedAt,
        )
    } catch (e: Exception) {
        DeckProgress(deckRelativePath, "")
    }
}

fun saveDeckProgress(context: Context, progress: DeckProgress) {
    val file = progressFileFor(context, progress.deckRelativePath)
    val dto = DeckProgressDto(
        deckRelativePath = progress.deckRelativePath,
        deckName = progress.deckName,
        lastSavedAt = System.currentTimeMillis(),
        entries = progress.entries.mapValues {
            CardProgressEntryDto(it.value.cardHash, it.value.timesShown, it.value.isHard)
        },
    )
    file.writeText(json.encodeToString(dto), Charsets.UTF_8)
}

fun deleteDeckProgress(context: Context, deckRelativePath: String) {
    progressFileFor(context, deckRelativePath).delete()
}

fun clearAllDeckProgress(context: Context) {
    progressDir(context).listFiles()?.forEach { it.delete() }
}

fun deckRelativePath(deck: Deck): String {
    val rootDir = getFlashcardsDir() ?: return deck.sourceFile.name
    return runCatching {
        deck.sourceFile.relativeTo(rootDir).path.replace('\\', '/')
    }.getOrDefault(deck.sourceFile.name)
}
