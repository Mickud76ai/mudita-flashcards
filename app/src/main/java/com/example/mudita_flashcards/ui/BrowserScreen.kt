package com.example.mudita_flashcards.ui

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mudita_flashcards.data.ScanResult
import com.example.mudita_flashcards.data.ensureFlashcardsDirectoryReady
import com.example.mudita_flashcards.data.scanDirectory
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    currentPath: File,
    rootPath: File,
    onFolderClick: (File) -> Unit,
    onDeckClick: (File) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current
    val isRoot = currentPath.canonicalPath == rootPath.canonicalPath

    if (!isRoot) {
        BackHandler { onNavigateUp() }
    }

    var scan by remember { mutableStateOf<ScanResult?>(null) }
    var rescanTrigger by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                rescanTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(currentPath, rescanTrigger) {
        withContext(Dispatchers.IO) {
            if (isRoot) {
                ensureFlashcardsDirectoryReady(context)
            }
            scan = scanDirectory(currentPath, rootPath)
        }
    }

    val result = scan
    if (result != null && result.folders.isEmpty() && result.decks.isEmpty()) {
        EmptyStateScreen(
            title = if (isRoot) "Flashcards" else currentPath.name,
            variant = if (isRoot) EmptyVariant.NoDecksFound else EmptyVariant.FolderEmpty,
            showBack = !isRoot,
            onBack = onNavigateUp,
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = {
                    TextMMD(
                        text = if (isRoot) "Flashcards" else currentPath.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (!isRoot) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
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
            if (result == null) return@Column

            LazyColumnMMD(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(result.folders.size) { idx ->
                    val folder = result.folders[idx]
                    BrowserRow(
                        text = "▸  ${folder.name}",
                        onClick = { onFolderClick(folder.path) },
                    )
                    HorizontalDividerMMD()
                }
                items(result.decks.size) { idx ->
                    val deck = result.decks[idx]
                    BrowserRow(
                        text = "≡  ${deck.name}",
                        onClick = { onDeckClick(deck.file) },
                    )
                    HorizontalDividerMMD()
                }
            }
        }
    }
}

@Composable
private fun BrowserRow(text: String, onClick: () -> Unit) {
    TextMMD(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )
}

