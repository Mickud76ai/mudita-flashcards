package com.kompakt.flashcards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kompakt.flashcards.data.DeletionTarget
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmScreen(
    target: DeletionTarget,
    persistProgressEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    BackHandler { onCancel() }

    val topBarTitle: String
    val questionText: String
    val primaryName: String
    val subText: String?

    when (target) {
        is DeletionTarget.Deck -> {
            topBarTitle = "Delete deck"
            questionText = "Delete deck?"
            primaryName = target.listing.deckName
            val pathHint = target.listing.displayPath
            subText = pathHint.takeIf { it.isNotBlank() && it != target.listing.deckName }
        }
        is DeletionTarget.Folder -> {
            topBarTitle = "Delete folder"
            questionText = "Delete folder?"
            primaryName = target.folder.name
            subText = when {
                target.deckCount == 0 -> "Empty folder."
                persistProgressEnabled && target.deckCount == 1 ->
                    "Contains 1 deck. Saved progress will also be cleared."
                persistProgressEnabled ->
                    "Contains ${target.deckCount} decks. Saved progress will also be cleared."
                target.deckCount == 1 -> "Contains 1 deck."
                else -> "Contains ${target.deckCount} decks."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = {
                    TextMMD(
                        text = topBarTitle,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextMMD(
                        text = questionText,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextMMD(
                        text = primaryName,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (subText != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextMMD(
                            text = subText,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            HorizontalDividerMMD()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButtonMMD(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                ) {
                    TextMMD("Cancel")
                }
                ButtonMMD(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                ) {
                    TextMMD("Delete")
                }
            }
        }
    }
}
