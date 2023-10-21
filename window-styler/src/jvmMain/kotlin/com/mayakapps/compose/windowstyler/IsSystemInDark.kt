package com.mayakapps.compose.windowstyler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.alexfacciorusso.windowsregistryktx.Registry
import com.alexfacciorusso.windowsregistryktx.values.booleanValue
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

private val lightThemeRegistryValue = Registry.currentUser.subKey(
    "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
).booleanValue("AppsUseLightTheme")

@Composable
fun isSystemInDarkTheme(): Boolean = when (hostOs) {
    OS.Windows -> {
        lightThemeRegistryValue.flowChanges().collectAsState(null).value?.let { !it }
            ?: androidx.compose.foundation.isSystemInDarkTheme()
    }

    else -> androidx.compose.foundation.isSystemInDarkTheme()
}
