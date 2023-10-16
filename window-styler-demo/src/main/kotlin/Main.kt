import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.mayakapps.compose.windowstyler.WindowBackdrop

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
        RadioGroup("Backdrop Type", backdropOptions, backdropType, onBackdropChange)
    }
}

fun main() = application {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme) }
    var preferredBackdropType by remember { mutableStateOf<WindowBackdrop>(WindowBackdrop.Mica) }

    NativeLookWindow(
        onCloseRequest = ::exitApplication,
        title = "Compose Window Styler Demo",
        preferredBackdropType = preferredBackdropType,
        isDarkTheme = isDarkTheme,
    ) {

        MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onBackground) {
                App(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it },
                    backdropType = preferredBackdropType,
                    onBackdropChange = { preferredBackdropType = it },
                )
            }
        }
    }
}

val themeOptions = listOf(false to "Light", true to "Dark")

val lightRed = Color(255, 128, 128)
val darkRed = Color(169, 45, 45)

val lightBlue = Color(128, 168, 255)
val darkBlue = Color(45, 54, 169)

val backdropOptions = listOf(
    WindowBackdrop.Solid(lightRed, darkRed) to "Solid Light Red (light), Dark Red (dark)",
    WindowBackdrop.Solid(lightBlue, darkBlue) to "Solid Light Blue (light), Dark Blue (dark)",
    WindowBackdrop.Acrylic(Color.Magenta.copy(alpha = .89f)) to "Acrylic Magenta",
    WindowBackdrop.Acrylic(Color.Cyan.copy(alpha = .1f)) to "Acrylic Cyan",
    WindowBackdrop.Mica to "Mica",
    WindowBackdrop.Tabbed to "Tabbed",
)