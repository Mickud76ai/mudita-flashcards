package com.kompakt.flashcards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.kompakt.flashcards.data.Deck
import com.kompakt.flashcards.data.DeckLoadResult
import com.kompakt.flashcards.data.FlashCard
import com.kompakt.flashcards.data.OrderMode
import com.kompakt.flashcards.data.Settings
import com.kompakt.flashcards.data.cardHash
import com.kompakt.flashcards.data.deckRelativePath
import com.kompakt.flashcards.data.loadDeck
import com.kompakt.flashcards.data.loadDeckProgress
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckPreviewScreen(
    deckFile: File,
    settings: Settings,
    onBack: () -> Unit,
    onStartSession: (Deck) -> Unit,
    onCardTap: (Deck, FlashCard) -> Unit,
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var loadResult by remember(deckFile) { mutableStateOf<DeckLoadResult?>(null) }
    var cardRows by remember(deckFile) { mutableStateOf<List<CardRow>?>(null) }

    val useMasterySort = settings.persistProgress && settings.orderMode == OrderMode.Smart
    val showWeights = settings.showCardWeights

    LaunchedEffect(deckFile, useMasterySort, showWeights) {
        val result = withContext(Dispatchers.IO) { loadDeck(deckFile) }
        loadResult = result
        if (result is DeckLoadResult.Success) {
            cardRows = withContext(Dispatchers.IO) {
                prepareCardRows(result.deck, context, useMasterySort, showWeights)
            }
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        DeckPreviewContent(deckFile, loadResult, cardRows, onBack, onStartSession, onCardTap)
        if (settings.deepRefresh) {
            DeepRefreshFlash(triggerKey = deckFile.absolutePath)
        }
    }
}

internal data class CardRow(val card: FlashCard, val debugInfo: String?)

/**
 * Mastery-aware sort plus optional debug strip. Cards the user knows least appear at the top,
 * cards already mastered sink to the bottom. Score follows the Smart-mode weight formula so
 * the on-screen order matches the algorithm's bias. When `showDebug` is true, each row carries
 * a one-liner with shown count, hard flag and computed weight — useful while developing the
 * persistence behaviour.
 */
private fun prepareCardRows(
    deck: Deck,
    context: android.content.Context,
    useMasterySort: Boolean,
    showWeights: Boolean,
): List<CardRow> {
    val progress = if (useMasterySort || showWeights) {
        loadDeckProgress(context, deckRelativePath(deck))
    } else null
    val entries = progress?.entries ?: emptyMap()

    val ordered = if (useMasterySort) {
        deck.cards.sortedByDescending { masteryWeight(it, entries) }
    } else {
        deck.cards
    }

    return ordered.map { card ->
        val info = if (showWeights) {
            val entry = entries[cardHash(card)]
            val shown = entry?.timesShown ?: 0
            val hard = entry?.isHard == true
            val weight = 1.0 / (shown + 1) * (if (hard) 2.0 else 1.0)
            val hardLabel = if (hard) "hard" else "ok"
            "shown ${shown}× · $hardLabel · w=${"%.2f".format(weight)}"
        } else null
        CardRow(card, info)
    }
}

private fun masteryWeight(
    card: FlashCard,
    entries: Map<String, com.kompakt.flashcards.data.CardProgressEntry>,
): Double {
    val entry = entries[cardHash(card)]
    val timesShown = entry?.timesShown ?: 0
    val hardMultiplier = if (entry?.isHard == true) 2.0 else 1.0
    return 1.0 / (timesShown + 1) * hardMultiplier
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckPreviewContent(
    deckFile: File,
    loadResult: DeckLoadResult?,
    cardRows: List<CardRow>?,
    onBack: () -> Unit,
    onStartSession: (Deck) -> Unit,
    onCardTap: (Deck, FlashCard) -> Unit,
) {
    when (val result = loadResult) {
        null -> {
            // Loading state: render only the top bar so layout does not shift on result.
            Scaffold(
                topBar = {
                    TopAppBarMMD(
                        title = {
                            TextMMD(
                                text = deckFile.nameWithoutExtension,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        },
                    )
                },
            ) { padding -> Column(Modifier.fillMaxSize().padding(padding)) {} }
        }
        is DeckLoadResult.IOError, is DeckLoadResult.ParseError -> {
            EmptyStateScreen(
                title = deckFile.parentFile?.name ?: "Flashcards",
                variant = EmptyVariant.CannotOpenDeck,
                showBack = true,
                fileName = deckFile.name,
                onBack = onBack,
            )
        }
        is DeckLoadResult.Empty -> {
            DeckBody(
                deckName = result.deckName,
                rows = emptyList(),
                emptyMessage = "This deck has no cards yet.",
                onBack = onBack,
                onStartSession = null,
                onCardTap = {},
            )
        }
        is DeckLoadResult.Success -> {
            DeckBody(
                deckName = result.deck.name,
                rows = cardRows ?: result.deck.cards.map { CardRow(it, null) },
                emptyMessage = null,
                onBack = onBack,
                onStartSession = { onStartSession(result.deck) },
                onCardTap = { card -> onCardTap(result.deck, card) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckBody(
    deckName: String,
    rows: List<CardRow>,
    emptyMessage: String?,
    onBack: () -> Unit,
    onStartSession: (() -> Unit)?,
    onCardTap: (FlashCard) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = {
                    TextMMD(
                        text = deckName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TextMMD(
                text = "${rows.size} cards",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
            HorizontalDividerMMD()

            if (emptyMessage != null) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    TextMMD(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumnMMD(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    items(rows.size) { idx ->
                        val row = rows[idx]
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCardTap(row.card) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            TextMMD(
                                text = row.card.front,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (row.debugInfo != null) {
                                TextMMD(
                                    text = row.debugInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        HorizontalDividerMMD()
                    }
                }
            }

            HorizontalDividerMMD()
            ButtonMMD(
                onClick = { onStartSession?.invoke() },
                enabled = onStartSession != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                TextMMD("Start Session")
            }
        }
    }
}
