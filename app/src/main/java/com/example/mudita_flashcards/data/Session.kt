package com.example.mudita_flashcards.data

fun pickNextIndex(
    cards: List<FlashCard>,
    meta: Map<Int, CardSessionMeta>,
    lastIndex: Int?,
): Int {
    if (cards.size == 1) return 0

    val candidates = cards.indices.filter { it != lastIndex }

    val weights = candidates.map { idx ->
        val m = meta[idx]
        val timesShown = m?.timesShown ?: 0
        val hardMultiplier = if (m?.isHard == true) 2.0 else 1.0
        1.0 / (timesShown + 1) * hardMultiplier
    }

    val totalWeight = weights.sum()
    var random = Math.random() * totalWeight

    for (i in candidates.indices) {
        random -= weights[i]
        if (random <= 0.0) return candidates[i]
    }

    return candidates.last()
}

fun startSession(deck: Deck): SessionState {
    val firstIndex = deck.cards.indices.random()
    return SessionState(
        deckName = deck.name,
        cards = deck.cards,
        meta = emptyMap(),
        currentIndex = firstIndex,
        lastIndex = null,
    )
}

fun SessionState.advance(wasHard: Boolean): SessionState {
    val current = currentIndex
    val updatedMeta = meta + (current to CardSessionMeta(
        timesShown = timesShown(current) + 1,
        isHard = wasHard,
    ))
    val next = pickNextIndex(cards, updatedMeta, lastIndex = current)
    return copy(
        meta = updatedMeta,
        currentIndex = next,
        lastIndex = current,
    )
}
