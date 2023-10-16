package com.mayakapps.compose.windowstyler

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState


@Composable
fun NativeLookWindow(
    onCloseRequest: () -> Unit,
    preferredBackdropType: WindowBackdrop,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    frameStyle: WindowFrameStyle = WindowFrameStyle(),
    backdropFallbacks: List<WindowBackdrop> = emptyList(),
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable FrameWindowScope.() -> Unit,
) {
    key(preferredBackdropType, backdropFallbacks) {
        Window(
            onCloseRequest,
            state,
            visible,
            title,
            icon,
            undecorated = false,
            transparent = false,
            resizable,
            enabled,
            focusable,
            alwaysOnTop,
            onPreviewKeyEvent,
            onKeyEvent,
        ) {
            val manager = remember {
                WindowStyleManager(
                    window,
                    isDarkTheme,
                    preferredBackdropType,
                    frameStyle,
                    backdropFallbacks
                )
            }

            LaunchedEffect(Unit) {
                manager.apply()
            }

            LaunchedEffect(isDarkTheme) {
                manager.isDarkTheme = isDarkTheme
            }

            content()
        }
    }
}
