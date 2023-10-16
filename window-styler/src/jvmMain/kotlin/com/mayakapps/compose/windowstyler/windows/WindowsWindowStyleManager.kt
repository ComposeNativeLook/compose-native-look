package com.mayakapps.compose.windowstyler.windows

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.isSpecified
import com.mayakapps.compose.windowstyler.ColorableWindowBackdrop
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowBackdrop.Mica.supportedSinceBuild
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyleManager
import com.mayakapps.compose.windowstyler.hackContentPane
import com.mayakapps.compose.windowstyler.isTransparent
import com.mayakapps.compose.windowstyler.isUndecorated
import com.mayakapps.compose.windowstyler.setComposeLayerTransparency
import com.mayakapps.compose.windowstyler.windows.jna.Dwm
import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentFlag
import com.mayakapps.compose.windowstyler.windows.jna.enums.DwmWindowAttribute
import com.sun.jna.platform.win32.WinDef.HWND
import java.awt.Window
import javax.swing.SwingUtilities
import kotlin.properties.Delegates

/**
 * Windows implementation of [WindowStyleManager]. It is not recommended to use this class directly.
 *
 * If used on an OS other than Windows, it'll crash.
 */
class WindowsWindowStyleManager internal constructor(
    private val window: Window,
    isDarkTheme: Boolean = false,
    override val preferredBackdrop: WindowBackdrop,
    frameStyle: WindowFrameStyle = WindowFrameStyle(),
    backdropFallbacks: List<WindowBackdrop>,
) : WindowStyleManager {
    private val hwnd: HWND = window.hwnd
    private val isUndecorated = window.isUndecorated

    private val backdropApis = WindowsBackdropApis(hwnd)

    override val backdropFallbacks: List<WindowBackdrop> by Delegates.observable(backdropFallbacks) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            updateBackdrop()
        }
    }

    override var isDarkTheme: Boolean by Delegates.observable(isDarkTheme) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            updateTheme()
        }
    }

    private val _backdrop: WindowBackdrop?
        get() = (listOf(preferredBackdrop) + backdropFallbacks).firstOrNull {
            it.supportedSinceBuild <= windowsBuild
        }

    override fun apply() {
        if (_backdrop == null) return

        // invokeLater is called to make sure that ComposeLayer was initialized first
        SwingUtilities.invokeLater {
            // If the window is not already transparent, hack it to be transparent
            if (!window.isTransparent) {
                // For some reason, reversing the order of these two calls doesn't work.
                if (window is ComposeWindow) window.setComposeLayerTransparency(true)
                window.hackContentPane()
            }

            updateTheme()
            updateBackdrop()
            updateFrameStyle()
        }
    }

    override var frameStyle: WindowFrameStyle = frameStyle
        set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                updateFrameStyle(oldValue)
            }
        }

    private fun updateTheme() {
        val attribute =
            when {
                windowsBuild < 17763 -> return
                windowsBuild >= 18985 -> DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE
                else -> DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1
            }

        if (windowsBuild >= 17763 && Dwm.setWindowAttribute(hwnd, attribute, isDarkTheme)) {
            // ThemedAcrylic: Update the acrylic effect if it is themed
            if (_backdrop is WindowBackdrop.Acrylic || _backdrop is WindowBackdrop.Solid) updateBackdrop()
            // This is necessary for window buttons to change color correctly
            else if (_backdrop is WindowBackdrop.Mica && !isUndecorated) {
                backdropApis.resetWindowFrame()
                backdropApis.createSheetOfGlassEffect()
            }
        }
    }

    private fun updateBackdrop() {
        val backdrop = _backdrop ?: return

        // This is done to make sure that the window has become visible
        // If the window isn't shown yet, and we try to apply Default, Solid, Aero,
        // or Acrylic, the effect will be applied to the title bar background
        // leaving the caption with awkward background box.
        // Unfortunately, even with this method, mica has this background box.
        SwingUtilities.invokeLater {
            // Only on later Windows 11 versions and if effect is WindowEffect.mica,
            // WindowEffect.acrylic or WindowEffect.tabbed, otherwise fallback to old
            // approach.
            if (
                windowsBuild >= 22523 &&
                (backdrop is WindowBackdrop.Acrylic || backdrop is WindowBackdrop.Mica || _backdrop is WindowBackdrop.Tabbed)
            ) {
                backdropApis.setSystemBackdrop(backdrop.toDwmSystemBackdrop())
            } else {
                if (backdrop is WindowBackdrop.Mica) {
                    backdropApis.setMicaEffectEnabled(true)
                } else {
                    val color = when (backdrop) {
                        // As the transparency hack is irreversible, the default effect is applied by solid backdrop.
                        // The default color is white or black depending on the theme
                        is ColorableWindowBackdrop -> (if (isDarkTheme) backdrop.darkColor else backdrop.lightColor).toAbgr()
                        else -> 0x7FFFFFFF
                    }

                    val accentState = backdrop.toAccentState()
                    backdropApis.setAccentPolicy(
                        accentState = accentState,
                        accentFlags = setOf(AccentFlag.DRAW_ALL_BORDERS),
                        color = color,
                    )
                }
            }
        }
    }

    /*
     * Frame Style
     */

    private fun updateFrameStyle(oldStyle: WindowFrameStyle? = null) {
        if (windowsBuild >= 22000) {
            if ((oldStyle?.cornerPreference ?: WindowCornerPreference.DEFAULT) != frameStyle.cornerPreference) {
                Dwm.setWindowCornerPreference(hwnd, frameStyle.cornerPreference.toDwmWindowCornerPreference())
            }

            if (frameStyle.borderColor.isSpecified && oldStyle?.borderColor != frameStyle.borderColor) {
                Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_BORDER_COLOR, frameStyle.borderColor.toBgr())
            }

            if (frameStyle.titleBarColor.isSpecified && oldStyle?.titleBarColor != frameStyle.titleBarColor) {
                Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_CAPTION_COLOR, frameStyle.titleBarColor.toBgr())
            }

            if (frameStyle.captionColor.isSpecified && oldStyle?.captionColor != frameStyle.captionColor) {
                Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_TEXT_COLOR, frameStyle.captionColor.toBgr())
            }
        }
    }
}