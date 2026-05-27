package com.kompakt.flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.kompakt.flashcards.data.ensureFlashcardsDirectoryReady
import com.kompakt.flashcards.ui.FlashcardsApp
import com.mudita.mmd.ThemeMMD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) {
            ensureFlashcardsDirectoryReady(applicationContext)
        }
        setContent {
            ThemeMMD {
                FlashcardsApp()
            }
        }
    }
}
