package com.mayakapps.compose.windowstyler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

interface NativeLookWindowScope : FrameWindowScope {
    val appliedBackdrop: WindowBackdrop
}

private data class NativeLookWindowScopeImpl(
    override val appliedBackdrop: WindowBackdrop,
    private val frameWindowScope: FrameWindowScope,
) : NativeLookWindowScope, FrameWindowScope by frameWindowScope

@Composable
fun NativeLookWindow(
    onCloseRequest: () -> Unit,
    preferredBackdropType: WindowBackdrop,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    frameStyle: WindowFrameStyle = WindowFrameStyle(),
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable (NativeLookWindowScope.() -> Unit),
) {
    key(preferredBackdropType::class) {
        Window(
            onCloseRequest,
            state,
            visible,
            title,
            icon,
            undecorated,
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
                    preferredBackdropType,
                    frameStyle,
                )
            }

            var appliedBackdrop by remember {
                mutableStateOf<WindowBackdrop>(WindowBackdrop.None)
            }

            LaunchedEffect(preferredBackdropType) {
                manager.preferredBackdrop = preferredBackdropType
            }

            LaunchedEffect(preferredBackdropType) {
                // TODO: to explore if manager can be totally removed
                appliedBackdrop = manager.apply()
            }

            @Suppress("NAME_SHADOWING")
            when (val appliedBackdrop = appliedBackdrop) {
                is WindowBackdrop.Solid -> {
                    Box(modifier = Modifier.background(appliedBackdrop.color)) {
                        content(
                            NativeLookWindowScopeImpl(
                                appliedBackdrop,
                                this@Window
                            )
                        )
                    }
                }

                else -> {
                    content(
                        NativeLookWindowScopeImpl(
                            appliedBackdrop,
                            this@Window
                        )
                    )
                }
            }
        }
    }
}
