package com.example.mudita_flashcards.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.mudita_flashcards.data.Deck
import com.example.mudita_flashcards.data.ensureFlashcardsDirectoryReady
import com.example.mudita_flashcards.data.getFlashcardsDir
import com.example.mudita_flashcards.data.hasFullStorageAccess
import com.example.mudita_flashcards.data.openManageStorageSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class Screen {
    data class Browser(val path: File) : Screen()
    data class DeckPreview(val deckFile: File) : Screen()
    data class Session(val deck: Deck) : Screen()
}

@Composable
fun FlashcardsApp() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasFullStorageAccess()) }

    if (!hasPermission) {
        PermissionScreen(
            onGrantClick = { openManageStorageSettings(context) },
            onResumeCheck = { hasPermission = hasFullStorageAccess() },
        )
        return
    }

    val rootDir = remember(hasPermission) { getFlashcardsDir() }

    if (rootDir == null) {
        EmptyStateScreen(
            title = "Flashcards",
            variant = EmptyVariant.StorageUnavailable,
            showBack = false,
        )
        return
    }

    LaunchedEffect(hasPermission) {
        withContext(Dispatchers.IO) {
            ensureFlashcardsDirectoryReady(context)
        }
    }

    var current by remember { mutableStateOf<Screen>(Screen.Browser(rootDir)) }

    when (val screen = current) {
        is Screen.Browser -> BrowserScreen(
            currentPath = screen.path,
            rootPath = rootDir,
            onFolderClick = { current = Screen.Browser(it) },
            onDeckClick = { file -> current = Screen.DeckPreview(file) },
            onNavigateUp = {
                if (screen.path.canonicalPath != rootDir.canonicalPath) {
                    val parent = screen.path.parentFile
                    if (parent != null) {
                        current = Screen.Browser(parent)
                    }
                }
            },
        )
        is Screen.DeckPreview -> DeckPreviewScreen(
            deckFile = screen.deckFile,
            onBack = {
                val parent = screen.deckFile.parentFile ?: rootDir
                current = Screen.Browser(parent)
            },
            onStartSession = { deck -> current = Screen.Session(deck) },
        )
        is Screen.Session -> CardSessionScreen(
            initialDeck = screen.deck,
            onExit = { current = Screen.DeckPreview(screen.deck.sourceFile) },
        )
    }
}
