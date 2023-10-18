package com.mayakapps.compose.windowstyler.windows

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.isSpecified
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowBackdrop.Mica.supportedSinceBuild
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyleManager
import com.mayakapps.compose.windowstyler.color
import com.mayakapps.compose.windowstyler.hackContentPane
import com.mayakapps.compose.windowstyler.hasColor
import com.mayakapps.compose.windowstyler.isTransparent
import com.mayakapps.compose.windowstyler.setComposeLayerTransparency
import com.mayakapps.compose.windowstyler.windows.jna.Dwm
import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentFlag
import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentState
import com.mayakapps.compose.windowstyler.windows.jna.enums.DwmWindowAttribute
import com.sun.jna.platform.win32.WinDef.HWND
import java.awt.Window
import javax.swing.SwingUtilities
import kotlin.properties.Delegates


private const val WIN10_BUILD_17763_OCT18 = 17763
private const val WIN10_BUILD_18985 = 18985
private const val WIN10_BUILD_19033_20H1 = 19033
private const val WIN11_BUILD_22000_21H2 = 22000
private const val WIN11_BUILD_22523 = 22523

private fun applyAcrylicAccentPolicy(
    backdrop: WindowBackdrop.Acrylic,
    isDarkTheme: Boolean,
    backdropApis: WindowsBackdropApis,
) {
    val color = backdrop.color(isDarkTheme).takeIf { it.isSpecified }?.toAbgr() ?: 0

    backdropApis.setAccentPolicy(
        accentState = AccentState.ACCENT_ENABLE_ACRYLICBLURBEHIND,
        accentFlags = setOf(AccentFlag.NONE),
        color = color,
    )
}

/**
 * Windows implementation of [WindowStyleManager]. It is not recommended to use this class directly.
 *
 * If used on an OS other than Windows, it'll crash.
 */
class WindowsWindowStyleManager internal constructor(
    private val window: Window,
    isDarkTheme: Boolean,
    override val preferredBackdrop: WindowBackdrop,
    frameStyle: WindowFrameStyle,
    override val backdropFallbacks: List<WindowBackdrop>,
) : WindowStyleManager {
    private var isApplied = false
    private val hwnd: HWND = window.hwnd

    private val backdropApis = WindowsBackdropApis.install(hwnd)

    override var isDarkTheme: Boolean by Delegates.observable(isDarkTheme) { _, oldValue, newValue ->
        if (!isApplied) return@observable

        if (newValue != oldValue) {
            updateTheme()
        }
    }

    override var frameStyle: WindowFrameStyle by Delegates.observable(frameStyle) { _, oldValue, newValue ->
        if (!isApplied) return@observable

        if (oldValue != newValue) {
            updateFrameStyle()
        }
    }
    override var hasBackdropApplied: Boolean by mutableStateOf(false)

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

            updateFrameStyle()
            updateTheme()
            updateBackdrop()

            isApplied = true
        }
    }

    private fun updateTheme() {
        val attribute = when {
            windowsBuild >= WIN10_BUILD_18985 -> DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE
            windowsBuild >= WIN10_BUILD_17763_OCT18 -> DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1
            else -> return
        }

        if (Dwm.setWindowAttribute(hwnd, attribute, isDarkTheme)) {
            if (windowsBuild < WIN11_BUILD_22000_21H2) { // `if`s not joined for clarity
                if (_backdrop is WindowBackdrop.Acrylic) updateBackdrop()
            }
        }
    }

    private fun updateBackdrop() {
        val backdrop = _backdrop ?: return

        hasBackdropApplied = when {
            windowsBuild >= WIN11_BUILD_22523 -> {
                backdropApis.setSystemBackdrop(backdrop.toDwmSystemBackdrop())

                if (backdrop is WindowBackdrop.Acrylic && backdrop.hasColor) {
                    applyAcrylicAccentPolicy(backdrop, isDarkTheme, backdropApis)
                }

                true
            }

            windowsBuild >= WIN11_BUILD_22000_21H2 -> {
                if (backdrop is WindowBackdrop.Mica) {
                    backdropApis.setMicaEffectEnabled(true)
                    true
                } else false
            }

            else -> {
                if (backdrop is WindowBackdrop.Acrylic) {
                    applyAcrylicAccentPolicy(backdrop, isDarkTheme, backdropApis)
                    true
                } else false
            }
        }
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