package com.mayakapps.compose.windowstyler

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import com.mayakapps.compose.windowstyler.WindowBackdrop.Acrylic
import com.mayakapps.compose.windowstyler.WindowBackdrop.Mica
import com.mayakapps.compose.windowstyler.WindowBackdrop.MicaTabbed


@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is unstable and may be removed in the future. " +
            "Use at your own risk."
)
annotation class UnstableWindowBackdropApi

internal interface ColorableWindowBackdrop {
    val lightColor: Color
    val darkColor: Color
}

internal fun ColorableWindowBackdrop.color(isDarkTheme: Boolean) = if (isDarkTheme) darkColor else lightColor
internal val ColorableWindowBackdrop.hasColor get() = lightColor.isSpecified || darkColor.isSpecified

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
 * [MicaTabbed] -> [Mica] -> [Acrylic]
 *
 * If [MicaTabbed] or [Mica] falls back to [Acrylic], high alpha is used with white or black
 * color according to `isDarkTheme` to emulate these effects.
 */
sealed interface WindowBackdrop {
    val WindowBackdrop.supportedSinceBuild: Int

    /**
     * This applies [Acrylic](https://docs.microsoft.com/en-us/windows/apps/design/style/acrylic) backdrop blended with
     * the supplied [lightColor] or [darkColor]. If the backdrop is rendered opaque, double check that the colours
     * have reasonable alpha value.
     *
     * **Supported on Windows 10 version 1803 or greater.**
     */
    data class Acrylic @UnstableWindowBackdropApi constructor(
        override val lightColor: Color,
        override val darkColor: Color,
    ) : WindowBackdrop,
        ColorableWindowBackdrop by ColorableWindowBackdropImpl(lightColor, darkColor) {

        @UnstableWindowBackdropApi
        constructor(color: Color) : this(color, color)

        @UnstableWindowBackdropApi
        constructor(alpha: Float) : this(Color.Black.copy(alpha = alpha))

        @OptIn(UnstableWindowBackdropApi::class)
        constructor() : this(Color.Unspecified)

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
    data object MicaTabbed : WindowBackdrop {
        override val WindowBackdrop.supportedSinceBuild: Int get() = 22523
    }
}
