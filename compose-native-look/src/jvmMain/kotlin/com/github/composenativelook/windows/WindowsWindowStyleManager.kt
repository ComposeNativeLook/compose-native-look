package com.github.composenativelook.windows

import androidx.compose.ui.awt.ComposeWindow
import com.github.composenativelook.WindowBackdrop
import com.github.composenativelook.WindowFrameStyle
import com.github.composenativelook.WindowStyleManager
import com.github.composenativelook.hackContentPane
import com.github.composenativelook.setComposeLayerTransparency
import com.github.composenativelook.windows.jna.Dwm
import com.github.composenativelook.windows.jna.enums.DwmWindowAttribute
import com.sun.jna.platform.win32.WinDef.HWND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.SwingUtilities
import kotlin.properties.Delegates


/**
 * Windows implementation of [WindowStyleManager]. It is not recommended to use this class directly.
 *
 * If used on an OS other than Windows, it'll crash.
 */
class WindowsWindowStyleManager internal constructor(
    private val window: ComposeWindow,
    preferredBackdrop: WindowBackdrop,
    frameStyle: WindowFrameStyle,
) : WindowStyleManager {
    private val hwnd: HWND = window.hwnd
    private val backdropApis = WindowsBackdropApis.install(hwnd)
    private var isApplied = false

    override var preferredBackdrop: WindowBackdrop by Delegates.observable(preferredBackdrop) { _, oldValue, _ ->
        if (!isApplied) return@observable

        backdrop.applyDiff(oldValue, hwnd, backdropApis)
    }

    override var frameStyle: WindowFrameStyle by Delegates.observable(frameStyle) { _, oldValue, newValue ->
        if (!isApplied) return@observable

        if (oldValue != newValue) {
            updateFrameStyle()
        }
    }

    private val backdrop: WindowBackdrop get() = preferredBackdrop.fallbackIfNotSupported()

    override suspend fun apply(): WindowBackdrop {
        withContext(Dispatchers.IO) {
            // invokeLater is called to make sure that ComposeLayer was initialized first
            SwingUtilities.invokeAndWait {
                // If the window is not already transparent, hack it to be transparent
                if (backdrop !is WindowBackdrop.Solid && !window.isTransparent) {
                    // For some reason, reversing the order of these two calls doesn't work.
                    window.setComposeLayerTransparency(true)
                    window.hackContentPane()
                }

                updateFrameStyle()
                backdrop.applyDiff(null, hwnd, backdropApis)
            }
        }
        isApplied = true
        return backdrop
    }

    private fun updateFrameStyle() {
        if (windowsBuild < WIN11_BUILD_22000_21H2) {
            // Unsupported
            return
        }

        Dwm.setWindowCornerPreference(hwnd, frameStyle.cornerPreference.toDwmWindowCornerPreference())
        Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_BORDER_COLOR, frameStyle.borderColor.toBgr())
        Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_CAPTION_COLOR, frameStyle.titleBarColor.toBgr())
        Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_TEXT_COLOR, frameStyle.captionColor.toBgr())
    }
}