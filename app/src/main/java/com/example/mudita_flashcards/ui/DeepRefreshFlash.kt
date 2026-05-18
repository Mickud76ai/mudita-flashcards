package com.example.mudita_flashcards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun DeepRefreshFlash(triggerKey: Any = Unit) {
    var phase by remember(triggerKey) { mutableIntStateOf(0) }
    LaunchedEffect(triggerKey) {
        phase = 0
        delay(180)
        phase = 1
        delay(180)
        phase = 2
    }
    if (phase < 2) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (phase == 0) Color.Black else Color.White)
        )
    }
}
