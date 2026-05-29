package com.kompakt.flashcards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kompakt.flashcards.data.DeckListing
import com.kompakt.flashcards.data.ScanResult
import com.kompakt.flashcards.data.countDecksInFolder
import com.kompakt.flashcards.data.scanDirectory
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDecksScreen(
    currentPath: File,
    rootPath: File,
    refreshKey: Int,
    onBack: () -> Unit,
    onFolderDrill: (File) -> Unit,
    onDeckDelete: (DeckListing) -> Unit,
    onFolderDelete: (folder: File, deckCount: Int) -> Unit,
) {
    BackHandler { onBack() }
    val isRoot = currentPath.canonicalPath == rootPath.canonicalPath

    var scan by remember(currentPath) { mutableStateOf<ScanResult?>(null) }

    LaunchedEffect(currentPath, refreshKey) {
        scan = withContext(Dispatchers.IO) {
            scanDirectory(currentPath, rootPath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = {
                    TextMMD(
                        text = if (isRoot) "Delete decks" else currentPath.name,
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
        val result = scan
        val isEmpty = result != null && result.folders.isEmpty() && result.decks.isEmpty()

        if (isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextMMD(
                    text = if (isRoot) "No decks to delete." else "Folder is empty.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (result == null) return@Column
            LazyColumnMMD(modifier = Modifier.fillMaxSize()) {
                items(result.folders.size) { idx ->
                    val folder = result.folders[idx]
                    FolderDeleteRow(
                        name = folder.name,
                        onDrill = { onFolderDrill(folder.path) },
                        onDelete = {
                            val count = countDecksInFolder(folder.path, rootPath)
                            onFolderDelete(folder.path, count)
                        },
                    )
                    HorizontalDividerMMD()
                }
                items(result.decks.size) { idx ->
                    val deck = result.decks[idx]
                    val listing = DeckListing(
                        file = deck.file,
                        deckName = deck.name,
                        displayPath = runCatching {
                            deck.file.relativeTo(rootPath).path
                                .replace('\\', '/')
                                .removeSuffix(".csv")
                        }.getOrDefault(deck.name),
                    )
                    DeckDeleteRow(
                        listing = listing,
                        onDelete = { onDeckDelete(listing) },
                    )
                    HorizontalDividerMMD()
                }
            }
        }
    }
}

@Composable
private fun FolderDeleteRow(
    name: String,
    onDrill: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextMMD(
            text = "▸  $name",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onDrill)
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete folder $name",
            )
        }
    }
}

@Composable
private fun DeckDeleteRow(listing: DeckListing, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        ) {
            TextMMD(
                text = "≡  ${listing.deckName}",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete ${listing.deckName}",
            )
        }
    }
}
