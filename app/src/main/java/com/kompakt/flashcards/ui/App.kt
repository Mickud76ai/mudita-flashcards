package com.kompakt.flashcards.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.kompakt.flashcards.data.Deck
import com.kompakt.flashcards.data.DeckListing
import com.kompakt.flashcards.data.FlashCard
import com.kompakt.flashcards.data.Settings
import com.kompakt.flashcards.data.clearAllDeckProgress
import com.kompakt.flashcards.data.deleteDeck
import com.kompakt.flashcards.data.ensureFlashcardsDirectoryReady
import com.kompakt.flashcards.data.getFlashcardsDir
import com.kompakt.flashcards.data.hasFullStorageAccess
import com.kompakt.flashcards.data.observeSettings
import com.kompakt.flashcards.data.openManageStorageSettings
import com.kompakt.flashcards.data.updateSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class Screen {
    data class Browser(val path: File) : Screen()
    data class DeckPreview(val deckFile: File) : Screen()
    data class CardBrowse(val card: FlashCard, val deckName: String, val deckFile: File) : Screen()
    data class Session(val deck: Deck) : Screen()
    data object Settings : Screen()
    data object HowToCreateDeck : Screen()
    data object PersistDisableConfirm : Screen()
    data object DeleteDecks : Screen()
    data class DeleteConfirm(val listing: DeckListing) : Screen()
}

@Composable
fun FlashcardsApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    val settings by context.observeSettings().collectAsState(initial = Settings())
    var current by remember { mutableStateOf<Screen>(Screen.Browser(rootDir)) }
    val onSettingsChange: (Settings) -> Unit = { new ->
        // Intercept the persist OFF transition — needs explicit confirmation and a full
        // progress wipe. Other settings changes go straight to DataStore.
        if (settings.persistProgress && !new.persistProgress) {
            current = Screen.PersistDisableConfirm
        } else {
            scope.launch { context.updateSettings { new } }
        }
    }

    var deleteRefreshKey by remember { mutableIntStateOf(0) }

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
            onOpenSettings = { current = Screen.Settings },
        )
        is Screen.DeckPreview -> DeckPreviewScreen(
            deckFile = screen.deckFile,
            settings = settings,
            onBack = {
                val parent = screen.deckFile.parentFile ?: rootDir
                current = Screen.Browser(parent)
            },
            onStartSession = { deck -> current = Screen.Session(deck) },
            onCardTap = { deck, card ->
                current = Screen.CardBrowse(card, deck.name, deck.sourceFile)
            },
        )
        is Screen.CardBrowse -> CardBrowseScreen(
            card = screen.card,
            deckName = screen.deckName,
            settings = settings,
            onReturn = { current = Screen.DeckPreview(screen.deckFile) },
        )
        is Screen.Session -> CardSessionScreen(
            initialDeck = screen.deck,
            settings = settings,
            onExit = { current = Screen.DeckPreview(screen.deck.sourceFile) },
        )
        is Screen.Settings -> SettingsScreen(
            settings = settings,
            onSettingsChange = onSettingsChange,
            onBack = { current = Screen.Browser(rootDir) },
            onDeleteDecks = { current = Screen.DeleteDecks },
            onOpenHowTo = { current = Screen.HowToCreateDeck },
        )
        is Screen.HowToCreateDeck -> HowToCreateDeckScreen(
            onReturn = { current = Screen.Settings },
        )
        is Screen.PersistDisableConfirm -> PersistDisableConfirmScreen(
            onCancel = { current = Screen.Settings },
            onConfirm = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        clearAllDeckProgress(context)
                    }
                    context.updateSettings { it.copy(persistProgress = false) }
                    current = Screen.Settings
                }
            },
        )
        is Screen.DeleteDecks -> DeleteDecksScreen(
            refreshKey = deleteRefreshKey,
            onBack = { current = Screen.Settings },
            onDeckSelected = { listing -> current = Screen.DeleteConfirm(listing) },
        )
        is Screen.DeleteConfirm -> DeleteConfirmScreen(
            deckName = screen.listing.deckName,
            onCancel = { current = Screen.DeleteDecks },
            onConfirm = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        deleteDeck(context, screen.listing.file)
                    }
                    deleteRefreshKey++
                    current = Screen.DeleteDecks
                }
            },
        )
    }
}
