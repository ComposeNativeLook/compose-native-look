package com.github.composenativelook

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import com.github.composenativelook.windows.WIN10_BUILD_17763_OCT18
import com.github.composenativelook.windows.WIN10_BUILD_18985
import com.github.composenativelook.windows.WIN11_BUILD_22000_21H2
import com.github.composenativelook.windows.WIN11_BUILD_22523
import com.github.composenativelook.windows.WindowsBackdropApis
import com.github.composenativelook.windows.jna.Dwm
import com.github.composenativelook.windows.jna.enums.AccentFlag
import com.github.composenativelook.windows.jna.enums.AccentState
import com.github.composenativelook.windows.jna.enums.DwmWindowAttribute
import com.github.composenativelook.windows.toAbgr
import com.github.composenativelook.windows.toDwmSystemBackdrop
import com.github.composenativelook.windows.windowsBuild
import com.sun.jna.platform.win32.WinDef.HWND


@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is unstable and may be removed in the future. " +
            "Use at your own risk."
)
annotation class UnstableWindowBackdropApi

internal fun applyAcrylicAccentPolicy(
    color: Color,
    backdropApis: WindowsBackdropApis,
) {
    val colorOrBlack = color.takeIf { it.isSpecified }?.toAbgr() ?: 0 // TODO: check if this is correct

    backdropApis.setAccentPolicy(
        accentState = AccentState.ACCENT_ENABLE_ACRYLICBLURBEHIND,
        accentFlags = setOf(AccentFlag.DRAW_ALL_BORDERS),
        color = colorOrBlack,
    )
}

/**
 * The type of the window backdrop/background.
 */
sealed class WindowBackdrop(open val isDarkTheme: Boolean) {
    abstract val supportedSinceBuild: Int

    protected abstract val fallsBackTo: WindowBackdrop

    fun withTheme(isDarkTheme: Boolean): WindowBackdrop =
        when (this) {
            is MicaTabbed -> copy(isDarkTheme = isDarkTheme)
            is Mica -> copy(isDarkTheme = isDarkTheme)
            is Acrylic -> copy(isDarkTheme = isDarkTheme)
            is AcrylicWithTint -> copy(isDarkTheme = isDarkTheme)
            is Solid -> copy(isDarkTheme = isDarkTheme)
            None -> this
        }

    fun fallbackIfNotSupported(): WindowBackdrop =
        if (windowsBuild >= supportedSinceBuild) this else fallsBackTo.fallbackIfNotSupported()

    internal fun updateTheme(hwnd: HWND) {
        if (windowsBuild < WIN11_BUILD_22000_21H2) {
            return
        }

        val attribute = when {
            windowsBuild >= WIN10_BUILD_18985 -> DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE
            windowsBuild >= WIN10_BUILD_17763_OCT18 -> DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1
            else -> return
        }

        if (Dwm.setWindowAttribute(hwnd, attribute, isDarkTheme)) {
//            if (windowsBuild < WIN11_BUILD_22000_21H2 && this is Acrylic) {
//                apply(windowBackdropApis)
//            }
        }
    }

    internal open fun applyDiff(oldBackdrop: WindowBackdrop?, hwnd: HWND, windowsBackdropApis: WindowsBackdropApis) {
        when {
            oldBackdrop == this -> {
                return
            }

            oldBackdrop?.javaClass != this.javaClass -> {
                updateTheme(hwnd)
                apply(windowsBackdropApis)
            }

            else -> updateTheme(hwnd)
        }
    }

    internal abstract fun apply(windowsBackdropApis: WindowsBackdropApis)

    /**
     * This applies Tabbed backdrop themed according to `isDarkTheme` value. This is a backdrop that is similar to
     * [Mica] but targeted at tabbed windows.
     *
     * **Supported on Windows 11 22H2 or greater.**
     */
    data class MicaTabbed(
        override val isDarkTheme: Boolean,
    ) : WindowBackdrop(isDarkTheme) {
        override val supportedSinceBuild: Int get() = WIN11_BUILD_22523

        override val fallsBackTo: WindowBackdrop
            get() = Mica(isDarkTheme)

        override fun apply(windowsBackdropApis: WindowsBackdropApis) {
            windowsBackdropApis.setSystemBackdrop(this.toDwmSystemBackdrop())
        }
    }

    /**
     * This applies [Mica](https://docs.microsoft.com/en-us/windows/apps/design/style/mica) backdrop themed according
     * to `isDarkTheme` value.
     *
     * **Supported on Windows 11 21H2 or greater.**
     */
    data class Mica(override val isDarkTheme: Boolean) : WindowBackdrop(isDarkTheme) {
        override val supportedSinceBuild: Int get() = WIN11_BUILD_22000_21H2
        override val fallsBackTo: WindowBackdrop
            get() = Solid(isDarkTheme)

        override fun apply(windowsBackdropApis: WindowsBackdropApis) {
            when {
                windowsBuild >= WIN11_BUILD_22523 -> {
                    windowsBackdropApis.setSystemBackdrop(this.toDwmSystemBackdrop())
                }

                windowsBuild >= WIN11_BUILD_22000_21H2 -> {
                    windowsBackdropApis.setMicaEffectEnabled(true)
                }
            }
        }
    }

    data class Acrylic(
        override val isDarkTheme: Boolean,
    ) : WindowBackdrop(isDarkTheme) {

        override val supportedSinceBuild: Int get() = WIN11_BUILD_22000_21H2

        override fun apply(windowsBackdropApis: WindowsBackdropApis) {
            when {
                windowsBuild >= WIN11_BUILD_22523 -> {
                    windowsBackdropApis.setSystemBackdrop(this.toDwmSystemBackdrop())
                }

                windowsBuild >= WIN11_BUILD_22000_21H2 -> {
                    applyAcrylicAccentPolicy(Color.Unspecified, windowsBackdropApis)
                }
            }
        }

        override val fallsBackTo: WindowBackdrop
            get() = Solid(isDarkTheme)
    }

    @UnstableWindowBackdropApi
    data class AcrylicWithTint @UnstableWindowBackdropApi constructor(
        val lightColor: Color,
        val darkColor: Color,
        override val isDarkTheme: Boolean,
    ) : WindowBackdrop(isDarkTheme) {

        override val supportedSinceBuild: Int get() = WIN11_BUILD_22000_21H2

        constructor(color: Color, isDarkTheme: Boolean) : this(color, color, isDarkTheme)

        private val color: Color get() = if (isDarkTheme) darkColor else lightColor

        override fun applyDiff(oldBackdrop: WindowBackdrop?, hwnd: HWND, windowsBackdropApis: WindowsBackdropApis) {
            if (oldBackdrop is AcrylicWithTint && oldBackdrop.color.isSpecified != color.isSpecified) {
                apply(windowsBackdropApis)
                updateTheme(hwnd)
                return
            }

            super.applyDiff(oldBackdrop, hwnd, windowsBackdropApis)
        }

        override fun apply(windowsBackdropApis: WindowsBackdropApis) {
            if (windowsBuild >= WIN11_BUILD_22523) {
                windowsBackdropApis.setSystemBackdrop(this.toDwmSystemBackdrop())
            }

            if (windowsBuild >= WIN11_BUILD_22000_21H2) {
                applyAcrylicAccentPolicy(color, windowsBackdropApis)
            }
        }

        override val fallsBackTo: WindowBackdrop
            get() = Solid(isDarkTheme)
    }

    data class Solid(override val isDarkTheme: Boolean) : WindowBackdrop(isDarkTheme) {
        override val supportedSinceBuild: Int get() = 0

        val color: Color
            get() = if (isDarkTheme) Color(32, 32, 32) else Color.White

        override val fallsBackTo: WindowBackdrop
            get() = this // no fallbacks as always supported

        override fun apply(windowsBackdropApis: WindowsBackdropApis) {
            // No need to apply
        }
    }

    data object None : WindowBackdrop(false) {
        override val supportedSinceBuild: Int get() = 0
        override val fallsBackTo: WindowBackdrop
            get() = this // no fallbacks as always supported

        override fun apply(windowsBackdropApis: WindowsBackdropApis) {
            // No need to apply
        }
    }
}
