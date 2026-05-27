package com.kompakt.flashcards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kompakt.flashcards.data.FlashCard
import com.kompakt.flashcards.data.Settings
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBrowseScreen(
    card: FlashCard,
    deckName: String,
    settings: Settings,
    onReturn: () -> Unit,
) {
    // Browse mode opens the back of the card first — the user already saw the front on
    // the deck preview list. Tapping the content flips back and forth freely. No grading,
    // no progress mutation — this is a calm read, not a session.
    var isFlipped by remember(card) { mutableStateOf(true) }

    BackHandler { onReturn() }

    val echoSlotHeight = 56.dp
    val returnSlotHeight = 72.dp

    Box(modifier = Modifier.fillMaxSize()) {
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
                        IconButton(onClick = onReturn) {
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
                // Slot 2 — echo of front when back is shown.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(echoSlotHeight),
                ) {
                    if (isFlipped) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextMMD(
                                text = card.front,
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

                // Slot 3 — card content. Tap toggles front / back.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { isFlipped = !isFlipped }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TextMMD(
                        text = if (isFlipped) card.back else card.front,
                        style = if (isFlipped) MaterialTheme.typography.bodyLarge
                        else MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Slot 4 — Return button (replaces "Flip the card" from session mode).
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(returnSlotHeight),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        HorizontalDividerMMD()
                        OutlinedButtonMMD(
                            onClick = onReturn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            TextMMD("Return")
                        }
                    }
                }
            }
        }
        if (settings.deepRefresh) {
            DeepRefreshFlash(triggerKey = "browse-${card.front}")
        }
    }
}
