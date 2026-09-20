package com.example.dont_cross_the_streams

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.example.dont_cross_the_streams.ui.main.MainAdaptiveScreen
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("Don't Cross the Streams", canvasElementId = "ComposeTarget") {
        DontcrossthestreamsTheme {
            MainAdaptiveScreen()
        }
    }
}
