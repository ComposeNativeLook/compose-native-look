package com.mayakapps.compose.windowstyler

import com.mayakapps.compose.windowstyler.windows.WindowsWindowStyleManager
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Window

internal interface WindowStyleManager {
    /**
     * This property should match the theming system used in your application. It's effect depends on the used backdrop
     * as follows:
     * * If the [preferredBackdrop] is [WindowBackdrop.Mica] or [WindowBackdrop.MicaTabbed], it is
     * used to manage the color of the background whether it is light or dark.
     * * Otherwise, it is used to control the color of the title bar of the window white/black.
     */
    var isDarkTheme: Boolean

    /**
     * The type of the window backdrop/background. See [WindowBackdrop] and its implementations.
     */
    val preferredBackdrop: WindowBackdrop

    /**
     * Fallbacks to be used in case the [preferredBackdrop] is not supported on the current OS build.
     */
    val backdropFallbacks: List<WindowBackdrop>

    /**
     * The style of the window frame which includes the title bar and window border. See [WindowFrameStyle].
     */
    val frameStyle: WindowFrameStyle

    /**
     * Whether the backdrop is applied to the window.
     */
    val hasBackdropApplied: Boolean

    fun apply()
}

internal class StubWindowStyleManager(
    override var isDarkTheme: Boolean,
    override var preferredBackdrop: WindowBackdrop,
    override var frameStyle: WindowFrameStyle,
    override var backdropFallbacks: List<WindowBackdrop>,
) : WindowStyleManager {
    override val hasBackdropApplied: Boolean = true

    override fun apply() {}
}

/**
 * Creates a suitable [WindowStyleManager] for [window] or a stub manager if the OS is not supported.
 *
 * The created manager is initialized by the supplied parameters.
 * See [WindowStyleManager.isDarkTheme], [WindowBackdrop], [WindowFrameStyle].
 */
internal fun WindowStyleManager(
    window: Window,
    isDarkTheme: Boolean,
    preferredBackdrop: WindowBackdrop,
    frameStyle: WindowFrameStyle,
    backdropFallbacks: List<WindowBackdrop>,
) = when (hostOs) {
    OS.Windows -> WindowsWindowStyleManager(window, isDarkTheme, preferredBackdrop, frameStyle, backdropFallbacks)
    else -> StubWindowStyleManager(isDarkTheme, preferredBackdrop, frameStyle, backdropFallbacks)
}
