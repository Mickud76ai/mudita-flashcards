package com.kompakt.flashcards.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

enum class EmptyVariant {
    NoDecksFound,
    FolderEmpty,
    CannotOpenDeck,
    StorageUnavailable,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyStateScreen(
    title: String,
    variant: EmptyVariant,
    showBack: Boolean,
    fileName: String? = null,
    onBack: () -> Unit = {},
) {
    val headline = when (variant) {
        EmptyVariant.NoDecksFound -> "No decks found."
        EmptyVariant.FolderEmpty -> "This folder is empty."
        EmptyVariant.CannotOpenDeck -> "Cannot open deck."
        EmptyVariant.StorageUnavailable -> "Storage unavailable."
    }
    val body = when (variant) {
        EmptyVariant.NoDecksFound ->
            "Connect Kompakt to a computer via USB-C and copy .csv files into the Flashcards folder. See how_to_create_decks.md for details."
        EmptyVariant.FolderEmpty -> null
        EmptyVariant.CannotOpenDeck ->
            "Check that ${fileName ?: "the file"} matches the format described in how_to_create_decks.md."
        EmptyVariant.StorageUnavailable ->
            "The app cannot access external storage. Try restarting the device or reconnecting it to a computer via USB-C."
    }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = {
                    TextMMD(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                TextMMD(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (body != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextMMD(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
