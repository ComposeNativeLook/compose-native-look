package com.mayakapps.compose.windowstyler.windows.jna

import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentFlag
import com.mayakapps.compose.windowstyler.windows.jna.enums.AccentState
import com.mayakapps.compose.windowstyler.windows.jna.enums.WindowCompositionAttribute
import com.mayakapps.compose.windowstyler.windows.jna.structs.AccentPolicy
import com.mayakapps.compose.windowstyler.windows.jna.structs.WindowCompositionAttributeData
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

internal object User32 {
    private const val COLOR_WINDOW = 5

    fun setAccentPolicy(
        hwnd: WinDef.HWND,
        accentState: AccentState = AccentState.ACCENT_DISABLED,
        accentFlags: Set<AccentFlag> = emptySet(),
        color: Int = 0,
        animationId: Int = 0,
    ): Boolean {
        val data = WindowCompositionAttributeData(
            WindowCompositionAttribute.WCA_ACCENT_POLICY,
            AccentPolicy(accentState, accentFlags, color, animationId),
        )

        val isSuccess = setWindowCompositionAttribute(hwnd, data)

        data.dispose()
        return isSuccess
    }

    private fun setWindowCompositionAttribute(
        hwnd: WinDef.HWND,
        attributeData: WindowCompositionAttributeData,
    ): Boolean {
        Native.setLastError(0)

        val isSuccess = User32Impl.SetWindowCompositionAttribute(hwnd, attributeData)

        if (!isSuccess) println("SetWindowCompositionAttribute(${attributeData.attribute}) failed with last error ${Native.getLastError()}")
        return isSuccess
    }
}

private object User32Impl : User32Api by Native.load("user32", User32Api::class.java, W32APIOptions.DEFAULT_OPTIONS)

@Suppress("FunctionName")
private interface User32Api : StdCallLibrary {
    fun SetWindowCompositionAttribute(hwnd: WinDef.HWND, attributeData: WindowCompositionAttributeData): Boolean

    fun GetSysColor(index: Int): Int
}