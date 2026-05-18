package com.example.mudita_flashcards.ui

import androidx.activity.compose.BackHandler
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
import com.example.mudita_flashcards.data.Deck
import com.example.mudita_flashcards.data.DeckLoadResult
import com.example.mudita_flashcards.data.loadDeck
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
    onBack: () -> Unit,
    onStartSession: (Deck) -> Unit,
) {
    BackHandler { onBack() }

    var loadResult by remember(deckFile) { mutableStateOf<DeckLoadResult?>(null) }

    LaunchedEffect(deckFile) {
        val result = withContext(Dispatchers.IO) { loadDeck(deckFile) }
        loadResult = result
    }

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
                cardFronts = emptyList(),
                emptyMessage = "This deck has no cards yet.",
                onBack = onBack,
                onStartSession = null,
            )
        }
        is DeckLoadResult.Success -> {
            DeckBody(
                deckName = result.deck.name,
                cardFronts = result.deck.cards.map { it.front },
                emptyMessage = null,
                onBack = onBack,
                onStartSession = { onStartSession(result.deck) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckBody(
    deckName: String,
    cardFronts: List<String>,
    emptyMessage: String?,
    onBack: () -> Unit,
    onStartSession: (() -> Unit)?,
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
                text = "${cardFronts.size} cards",
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
                    items(cardFronts.size) { idx ->
                        TextMMD(
                            text = cardFronts[idx],
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
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
