# Compose Native Look

[![](https://jitpack.io/v/ComposeNativeLook/compose-native-look.svg)](https://jitpack.io/#ComposeNativeLook/compose-native-look/main-SNAPSHOT)

Compose Native Look is a library that lets you style your Compose for Desktop window to have more native and modern
UI. This includes styling the window to use native styles like acrylic and mica, with fall backs in place when the OS
does not support the current effect.

This repo is a direct fork/rewriting of https://github.com/MayakaApps/ComposeWindowStyler, so a lots of thanks to them
for the inspiration and the base to start this project.

## Video

![Demo Screenshot](res/demo_preview.gif)

## Setup (Gradle)

Follow the instructions on

https://jitpack.io/#ComposeNativeLook/compose-native-look/main-SNAPSHOT

## Usage

You can apply the desired to your window by using `WindowStyle` inside the `WindowScope` of `Window` or similar
composable calls. It can be placed anywhere inside them.

Sample Code:

```kotlin
fun main() = application {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme) }

    NativeLookWindow(
        onCloseRequest = ::exitApplication,
        preferredBackdropType = WindowBackdrop.Mica,
    ) {
        // content...
    }
}
```

## License

This library is distributed under the MIT license.

## Contributing

All contributions are welcome. If you are reporting an issue, please use the provided template. If you're planning to
contribute to the code, please open an issue first describing what feature you're planning to add or what issue you're
planning to fix. This allows better discussion and coordination of efforts. You can also check open issues for
bugs/features that needs to be fixed/implemented.

## Acknowledgements

* [ComposeWindowStyler](https://github.com/MayakaApps/ComposeWindowStyler): this project is directly a fork of it
