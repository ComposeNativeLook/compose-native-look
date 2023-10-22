@file:OptIn(UnstableWindowBackdropApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.mayakapps.compose.windowstyler.NativeLookWindow
import com.mayakapps.compose.windowstyler.UnstableWindowBackdropApi
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.isSystemInDarkTheme

fun backdropOptions(isDarkTheme: Boolean) = listOf(
    WindowBackdrop.Solid(isDarkTheme) to "Solid (Win 10 fallback)",
    WindowBackdrop.AcrylicWithTint(
        Color.Magenta.copy(alpha = .20f),
        isDarkTheme
    ) to "Tinted Acrylic (API unstable)",
    WindowBackdrop.Acrylic(isDarkTheme) to "Acrylic",
    WindowBackdrop.Mica(isDarkTheme) to "Mica",
    WindowBackdrop.MicaTabbed(isDarkTheme) to "Tabbed",
)

@Composable
@Preview
fun App(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    backdropType: WindowBackdrop,
    onBackdropChange: (WindowBackdrop) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RadioGroup("Theme", themeOptions, isDarkTheme, onThemeChange)
        Spacer(Modifier.height(50.dp))
        RadioGroup("Backdrop Type", backdropOptions(isDarkTheme), backdropType, onBackdropChange)
    }
}

fun main() = application {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var preferredBackdropType by remember { mutableStateOf<WindowBackdrop>(WindowBackdrop.Mica(isSystemInDarkTheme)) }
    val isDarkTheme by derivedStateOf { preferredBackdropType.isDarkTheme }

    LaunchedEffect(isSystemInDarkTheme) {
        preferredBackdropType = preferredBackdropType.withTheme(isDarkTheme = isSystemInDarkTheme)
    }

    NativeLookWindow(
        onCloseRequest = ::exitApplication,
        preferredBackdropType = preferredBackdropType,
        title = "Compose Window Styler Demo",
    ) {
        MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onBackground) {
                App(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = {
                        preferredBackdropType = preferredBackdropType.withTheme(isDarkTheme = it)
                    },
                    backdropType = preferredBackdropType,
                    onBackdropChange = { preferredBackdropType = it },
                )
            }
        }
    }
}

val themeOptions = listOf(false to "Light", true to "Dark")
