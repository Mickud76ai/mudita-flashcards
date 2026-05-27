package com.kompakt.flashcards.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "flashcards_settings")

private object SettingsKeys {
    val PERSIST_PROGRESS = booleanPreferencesKey("persist_progress")
    val ORDER_MODE = stringPreferencesKey("order_mode")
    val DEEP_REFRESH = booleanPreferencesKey("deep_refresh")
    val SHOW_CARD_WEIGHTS = booleanPreferencesKey("show_card_weights")
}

private fun Preferences.toSettings(): Settings {
    val orderName = this[SettingsKeys.ORDER_MODE]
    val orderMode = if (orderName != null) {
        runCatching { OrderMode.valueOf(orderName) }.getOrDefault(OrderMode.Smart)
    } else {
        OrderMode.Smart
    }
    return Settings(
        persistProgress = this[SettingsKeys.PERSIST_PROGRESS] ?: false,
        orderMode = orderMode,
        deepRefresh = this[SettingsKeys.DEEP_REFRESH] ?: false,
        showCardWeights = this[SettingsKeys.SHOW_CARD_WEIGHTS] ?: false,
    )
}

fun Context.observeSettings(): Flow<Settings> =
    settingsDataStore.data.map { it.toSettings() }

suspend fun Context.updateSettings(transform: (Settings) -> Settings) {
    settingsDataStore.edit { prefs ->
        val next = transform(prefs.toSettings())
        prefs[SettingsKeys.PERSIST_PROGRESS] = next.persistProgress
        prefs[SettingsKeys.ORDER_MODE] = next.orderMode.name
        prefs[SettingsKeys.DEEP_REFRESH] = next.deepRefresh
        prefs[SettingsKeys.SHOW_CARD_WEIGHTS] = next.showCardWeights
    }
}
