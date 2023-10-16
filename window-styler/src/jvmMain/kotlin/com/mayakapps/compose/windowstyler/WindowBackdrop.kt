package com.mayakapps.compose.windowstyler

import androidx.compose.ui.graphics.Color
import com.mayakapps.compose.windowstyler.WindowBackdrop.Acrylic
import com.mayakapps.compose.windowstyler.WindowBackdrop.Mica
import com.mayakapps.compose.windowstyler.WindowBackdrop.Tabbed

internal interface ColorableWindowBackdrop {
    val lightColor: Color
    val darkColor: Color
}

internal data class ColorableWindowBackdropImpl(
    override val lightColor: Color,
    override val darkColor: Color,
) : ColorableWindowBackdrop

/**
 * The type of the window backdrop/background.
 *
 * **Fallback Strategy**
 *
 * In case of unsupported effect the library tries to fall back to the nearest supported effect as follows:
 *
 * [Tabbed] -> [Mica] -> [Acrylic]
 *
 * If [Tabbed] or [Mica] falls back to [Acrylic], high alpha is used with white or black
 * color according to `isDarkTheme` to emulate these effects.
 */
sealed interface WindowBackdrop {
    val WindowBackdrop.supportedSinceBuild: Int

    /**
     * This applies [lightColor] or [darkColor] as a solid background which means that any alpha component is ignored
     * and the color is rendered as opaque.
     */
    data class Solid(override val lightColor: Color, override val darkColor: Color) : WindowBackdrop,
        ColorableWindowBackdrop by ColorableWindowBackdropImpl(lightColor, darkColor) {
        override val WindowBackdrop.supportedSinceBuild: Int get() = 0
    }

    /**
     * This applies [Acrylic](https://docs.microsoft.com/en-us/windows/apps/design/style/acrylic) backdrop blended with
     * the supplied [lightColor] or [darkColor]. If the backdrop is rendered opaque, double check that the colours
     * have reasonable alpha value.
     *
     * **Supported on Windows 10 version 1803 or greater.**
     */
    data class Acrylic(override val lightColor: Color, override val darkColor: Color) : WindowBackdrop,
        ColorableWindowBackdrop by ColorableWindowBackdropImpl(lightColor, darkColor) {

        constructor(color: Color) : this(color, color)

        override val WindowBackdrop.supportedSinceBuild: Int get() = 17063
    }

    /**
     * This applies [Mica](https://docs.microsoft.com/en-us/windows/apps/design/style/mica) backdrop themed according
     * to `isDarkTheme` value.
     *
     * **Supported on Windows 11 21H2 or greater.**
     */
    data object Mica : WindowBackdrop {
        override val WindowBackdrop.supportedSinceBuild: Int get() = 22000
    }

    /**
     * This applies Tabbed backdrop themed according to `isDarkTheme` value. This is a backdrop that is similar to
     * [Mica] but targeted at tabbed windows.
     *
     * **Supported on Windows 11 22H2 or greater.**
     */
    data object Tabbed : WindowBackdrop {
        override val WindowBackdrop.supportedSinceBuild: Int get() = 22523
    }
}
