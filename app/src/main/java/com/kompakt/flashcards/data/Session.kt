package com.kompakt.flashcards.data

import android.content.Context

fun pickNextIndex(
    cards: List<FlashCard>,
    meta: Map<Int, CardSessionMeta>,
    lastIndex: Int?,
    orderMode: OrderMode,
): Int {
    if (cards.isEmpty()) return 0
    if (cards.size == 1) return 0

    return when (orderMode) {
        OrderMode.Sequential -> ((lastIndex ?: -1) + 1).mod(cards.size)

        OrderMode.Smart -> {
            val candidates = cards.indices.filter { it != lastIndex }
            val weights = candidates.map { idx ->
                val m = meta[idx]
                val timesShown = m?.timesShown ?: 0
                val hardMultiplier = if (m?.isHard == true) 2.0 else 1.0
                1.0 / (timesShown + 1) * hardMultiplier
            }
            val totalWeight = weights.sum()
            var random = Math.random() * totalWeight
            var picked = candidates.last()
            for (i in candidates.indices) {
                random -= weights[i]
                if (random <= 0.0) {
                    picked = candidates[i]
                    break
                }
            }
            picked
        }
    }
}

fun startSession(deck: Deck, settings: Settings, context: Context): SessionState {
    val loadedMeta: Map<Int, CardSessionMeta> = if (settings.persistProgress) {
        val relPath = deckRelativePath(deck)
        val progress = loadDeckProgress(context, relPath)
        deck.cards.mapIndexedNotNull { idx, card ->
            val entry = progress.entries[cardHash(card)] ?: return@mapIndexedNotNull null
            idx to CardSessionMeta(timesShown = entry.timesShown, isHard = entry.isHard)
        }.toMap()
    } else {
        emptyMap()
    }

    val firstIndex = when (settings.orderMode) {
        OrderMode.Sequential -> 0
        OrderMode.Smart -> pickNextIndex(deck.cards, loadedMeta, lastIndex = null, OrderMode.Smart)
    }

    return SessionState(
        deckName = deck.name,
        cards = deck.cards,
        meta = loadedMeta,
        currentIndex = firstIndex,
        lastIndex = null,
    )
}

fun SessionState.advance(
    wasHard: Boolean,
    settings: Settings,
    deck: Deck,
    context: Context,
): SessionState {
    val current = currentIndex
    val updatedMeta = meta + (current to CardSessionMeta(
        timesShown = timesShown(current) + 1,
        isHard = wasHard,
    ))
    val next = pickNextIndex(cards, updatedMeta, lastIndex = current, settings.orderMode)
    val newState = copy(
        meta = updatedMeta,
        currentIndex = next,
        lastIndex = current,
    )

    if (settings.persistProgress) {
        val relPath = deckRelativePath(deck)
        val existing = loadDeckProgress(context, relPath)
        val newHash = cardHash(deck.cards[current])
        val mergedEntries = existing.entries + (newHash to CardProgressEntry(
            cardHash = newHash,
            timesShown = (existing.entries[newHash]?.timesShown ?: 0) + 1,
            isHard = wasHard,
        ))
        saveDeckProgress(
            context,
            DeckProgress(
                deckRelativePath = relPath,
                deckName = deck.name,
                entries = mergedEntries,
            ),
        )
    }

    return newState
}
