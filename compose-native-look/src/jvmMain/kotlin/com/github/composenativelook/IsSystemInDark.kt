package com.github.composenativelook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.alexfacciorusso.windowsregistryktx.Registry
import com.alexfacciorusso.windowsregistryktx.values.booleanValue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

private val lightThemeRegistryValue = Registry.currentUser.subKey(
    "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
).booleanValue("AppsUseLightTheme")

@Composable
fun isSystemInDarkTheme(): Boolean = when (hostOs) {
    OS.Windows -> {
        val flow = remember { lightThemeRegistryValue.flowChanges().filterNotNull().map { !it } }

        flow.collectAsState(false).value
    }

    else -> androidx.compose.foundation.isSystemInDarkTheme()
}
