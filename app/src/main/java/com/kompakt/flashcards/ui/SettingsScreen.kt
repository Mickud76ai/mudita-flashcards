package com.kompakt.flashcards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kompakt.flashcards.data.OrderMode
import com.kompakt.flashcards.data.Settings
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.radio_button.RadioButtonMMD
import com.mudita.mmd.components.switcher.SwitchMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onSettingsChange: (Settings) -> Unit,
    onBack: () -> Unit,
    onDeleteDecks: () -> Unit,
    onOpenHowTo: () -> Unit,
) {
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = {
                    TextMMD(
                        text = "Settings",
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButtonMMD(
                onClick = onOpenHowTo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                TextMMD("How to create a deck")
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDividerMMD()

            SettingsToggleRow(
                label = "Persist progress between sessions",
                checked = settings.persistProgress,
                onCheckedChange = { onSettingsChange(settings.copy(persistProgress = it)) },
            )
            HorizontalDividerMMD()

            OrderModeSection(
                current = settings.orderMode,
                onChange = { onSettingsChange(settings.copy(orderMode = it)) },
            )
            HorizontalDividerMMD()

            SettingsToggleRow(
                label = "Deep refresh on deck open",
                checked = settings.deepRefresh,
                onCheckedChange = { onSettingsChange(settings.copy(deepRefresh = it)) },
            )
            HorizontalDividerMMD()

            SettingsToggleRow(
                label = "Show card weights",
                checked = settings.showCardWeights,
                onCheckedChange = { onSettingsChange(settings.copy(showCardWeights = it)) },
            )
            HorizontalDividerMMD()

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButtonMMD(
                onClick = onDeleteDecks,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                TextMMD("Delete decks")
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        SwitchMMD(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun OrderModeSection(
    current: OrderMode,
    onChange: (OrderMode) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        TextMMD(
            text = "Card order",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        OrderMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = current == mode,
                        onClick = { onChange(mode) },
                        role = Role.RadioButton,
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButtonMMD(
                    selected = current == mode,
                    onClick = null,
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextMMD(
                    text = mode.label,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
