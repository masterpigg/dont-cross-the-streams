package com.example.dont_cross_the_streams

import androidx.compose.ui.window.ComposeUIViewController
import com.example.dont_cross_the_streams.ui.main.MainAdaptiveScreen
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    DontcrossthestreamsTheme {
        MainAdaptiveScreen()
    }
}
