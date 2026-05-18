package com.example.mudita_flashcards.data

import java.io.File
import java.io.IOException

const val FRONT_MAX = 80
const val BACK_MAX = 300

fun truncateWithEllipsis(text: String, max: Int): String {
    if (text.length <= max) return text
    return text.take(max - 3).trimEnd() + "..."
}

fun loadDeck(file: File): DeckLoadResult {
    return try {
        file.bufferedReader(Charsets.UTF_8).use { reader ->
            val deckName = reader.readLine()
                ?.trimStart('﻿')
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: return DeckLoadResult.ParseError(file.name)

            val cards = reader.lineSequence()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split(';', limit = 2)
                    if (parts.size < 2) return@mapNotNull null
                    val frontRaw = parts[0].trim()
                    val backRaw = parts[1].trim()
                    if (frontRaw.isEmpty()) return@mapNotNull null
                    FlashCard(
                        front = truncateWithEllipsis(frontRaw, FRONT_MAX),
                        back = truncateWithEllipsis(backRaw, BACK_MAX),
                    )
                }
                .toList()

            if (cards.isEmpty()) {
                DeckLoadResult.Empty(deckName)
            } else {
                DeckLoadResult.Success(Deck(name = deckName, cards = cards, sourceFile = file))
            }
        }
    } catch (e: IOException) {
        DeckLoadResult.IOError(file.name)
    } catch (e: Exception) {
        DeckLoadResult.ParseError(file.name)
    }
}

fun quickParseCsvHeader(file: File): Pair<String, Int> {
    return try {
        file.bufferedReader(Charsets.UTF_8).use { reader ->
            val deckName = reader.readLine()
                ?.trimStart('﻿')
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: file.nameWithoutExtension
            val cardCount = reader.lineSequence()
                .filter { it.isNotBlank() && it.contains(';') }
                .count()
            Pair(deckName, cardCount)
        }
    } catch (e: Exception) {
        Pair(file.nameWithoutExtension, 0)
    }
}
