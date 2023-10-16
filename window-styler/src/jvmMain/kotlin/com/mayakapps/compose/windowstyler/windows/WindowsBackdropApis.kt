package com.mayakapps.compose.windowstyler.windows

import com.mayakapps.compose.windowstyler.windows.jna.Dwm
import com.mayakapps.compose.windowstyler.windows.jna.User32
import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentFlag
import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentState
import com.mayakapps.compose.windowstyler.windows.jna.enums.DwmSystemBackdrop
import com.mayakapps.compose.windowstyler.windows.jna.enums.DwmWindowAttribute
import com.sun.jna.platform.win32.WinDef

internal class WindowsBackdropApis(private val hwnd: WinDef.HWND) {
    fun setSystemBackdrop(systemBackdrop: DwmSystemBackdrop) {
        createSheetOfGlassEffect()
        Dwm.setSystemBackdrop(hwnd, systemBackdrop)
    }

    fun setMicaEffectEnabled(enabled: Boolean) {
        createSheetOfGlassEffect()
        Dwm.setWindowAttribute(hwnd, DwmWindowAttribute.DWMWA_MICA_EFFECT, enabled)
        if (enabled) setAccentPolicy(AccentState.ACCENT_DISABLED)
    }

    fun setAccentPolicy(
        accentState: AccentState = AccentState.ACCENT_DISABLED,
        accentFlags: Set<AccentFlag> = emptySet(),
        color: Int = 0,
        animationId: Int = 0,
    ) {
        if (User32.setAccentPolicy(hwnd, accentState, accentFlags, color, animationId)) {
            if (accentState != AccentState.ACCENT_DISABLED) {
                resetWindowFrame()
            }
        }
    }

    fun createSheetOfGlassEffect() {
        Dwm.extendFrameIntoClientArea(hwnd, -1)
    }

    fun resetWindowFrame() {
        // At least one margin should be non-negative in order to show the DWM
        // window shadow created by handling [WM_NCCALCSIZE].
        //
        // Matching value with bitsdojo_window.
        // https://github.com/bitsdojo/bitsdojo_window/blob/adad0cd40be3d3e12df11d864f18a96a2d0fb4fb/bitsdojo_window_windows/windows/bitsdojo_window.cpp#L149
        Dwm.extendFrameIntoClientArea(hwnd, 0, 0, 1, 0)
    }
}