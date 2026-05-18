package com.example.mudita_flashcards.data

import java.io.File

data class FlashCard(
    val front: String,
    val back: String,
)

data class Deck(
    val name: String,
    val cards: List<FlashCard>,
    val sourceFile: File,
)

data class CardSessionMeta(
    val timesShown: Int = 0,
    val isHard: Boolean = false,
)

data class SessionState(
    val deckName: String,
    val cards: List<FlashCard>,
    val meta: Map<Int, CardSessionMeta> = emptyMap(),
    val currentIndex: Int,
    val lastIndex: Int? = null,
) {
    val currentCard: FlashCard get() = cards[currentIndex]
    fun timesShown(index: Int): Int = meta[index]?.timesShown ?: 0
    fun isHard(index: Int): Boolean = meta[index]?.isHard ?: false
}

sealed class BrowserItem {
    data class FolderItem(val name: String, val path: File) : BrowserItem()
    data class DeckItem(val name: String, val file: File, val cardCount: Int) : BrowserItem()
}

sealed class DeckLoadResult {
    data class Success(val deck: Deck) : DeckLoadResult()
    data class Empty(val deckName: String) : DeckLoadResult()
    data class ParseError(val fileName: String) : DeckLoadResult()
    data class IOError(val fileName: String) : DeckLoadResult()
}

enum class OrderMode(val label: String) {
    Sequential("Sequential"),
    Shuffle("Random"),
    Smart("Smart"),
}

data class Settings(
    val persistProgress: Boolean = false,
    val orderMode: OrderMode = OrderMode.Smart,
    val deepRefresh: Boolean = false,
    val showCardWeights: Boolean = false,
)

data class CardProgressEntry(
    val cardHash: String,
    val timesShown: Int,
    val isHard: Boolean,
)

data class DeckProgress(
    val deckRelativePath: String,
    val deckName: String,
    val entries: Map<String, CardProgressEntry> = emptyMap(),
    val lastSavedAt: Long = System.currentTimeMillis(),
)

data class DeckListing(
    val file: File,
    val deckName: String,
    val displayPath: String,
)
