package com.kompakt.flashcards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.kompakt.flashcards.data.Deck
import com.kompakt.flashcards.data.Settings
import com.kompakt.flashcards.data.advance
import com.kompakt.flashcards.data.startSession
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSessionScreen(
    initialDeck: Deck,
    settings: Settings,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(startSession(initialDeck, settings, context)) }
    var isFlipped by remember(state.currentIndex) { mutableStateOf(false) }

    BackHandler { onExit() }

    val echoSlotHeight = 56.dp
    val flipSlotHeight = 72.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBarMMD(
                    title = {
                        TextMMD(
                            text = state.deckName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        IconButton(onClick = onExit) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Exit session",
                            )
                        }
                    },
                )
            },
            bottomBar = {
                Column {
                    HorizontalDividerMMD()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButtonMMD(
                            onClick = { state = state.advance(wasHard = true, settings = settings, deck = initialDeck, context = context) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Still Learning",
                            )
                        }
                        ButtonMMD(
                            onClick = { state = state.advance(wasHard = false, settings = settings, deck = initialDeck, context = context) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Know",
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                // Slot 2 — Echo of front (visible only when flipped)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(echoSlotHeight),
                ) {
                    if (isFlipped) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextMMD(
                                text = state.currentCard.front,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                            HorizontalDividerMMD()
                        }
                    }
                }

                // Slot 3 — Card content. Tap to flip (Quizlet/Anki convention).
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { isFlipped = !isFlipped }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TextMMD(
                        text = if (isFlipped) state.currentCard.back else state.currentCard.front,
                        style = if (isFlipped) MaterialTheme.typography.bodyLarge
                        else MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Slot 4 — Flip slot. Button always visible, label adapts to direction.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(flipSlotHeight),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        HorizontalDividerMMD()
                        OutlinedButtonMMD(
                            onClick = { isFlipped = !isFlipped },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            TextMMD("Flip the card")
                        }
                    }
                }
            }
        }
        if (settings.deepRefresh) {
            DeepRefreshFlash(triggerKey = "session-start")
        }
    }
}
