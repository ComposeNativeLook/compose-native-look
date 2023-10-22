package com.mayakapps.compose.windowstyler

import androidx.compose.ui.awt.ComposeWindow
import com.mayakapps.compose.windowstyler.windows.WindowsWindowStyleManager
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

internal interface WindowStyleManager {
    /**
     * The type of the window backdrop/background. See [WindowBackdrop] and its implementations.
     */
    var preferredBackdrop: WindowBackdrop

    /**
     * The style of the window frame which includes the title bar and window border. See [WindowFrameStyle].
     */
    val frameStyle: WindowFrameStyle

    suspend fun apply(): WindowBackdrop
}

internal class StubWindowStyleManager(
    override var preferredBackdrop: WindowBackdrop,
    override var frameStyle: WindowFrameStyle,
) : WindowStyleManager {
    override suspend fun apply(): WindowBackdrop = WindowBackdrop.None
}

/**
 * Creates a suitable [WindowStyleManager] for [window] or a stub manager if the OS is not supported.
 */
internal fun WindowStyleManager(
    window: ComposeWindow,
    preferredBackdrop: WindowBackdrop,
    frameStyle: WindowFrameStyle,
) = when (hostOs) {
    OS.Windows -> WindowsWindowStyleManager(window, preferredBackdrop, frameStyle)
    else -> StubWindowStyleManager(preferredBackdrop, frameStyle)
}
