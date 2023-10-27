# Compose Native Look

Compose Native Look is a library that lets you style your Compose for Desktop window to have more native and modern
UI. This includes styling the window to use native styles like acrylic and mica, with fall backs in place when the OS
does not support the current effect.

This repo is a direct fork/rewriting of https://github.com/MayakaApps/ComposeWindowStyler, so a lots of thanks to them 
for the inspiration and the base to start this project.

## Video

![Demo Screenshot](res/demo_preview.gif)

## Setup (Gradle)

Kotlin DSL:

```kotlin
```

Groovy DSL:

```gradle
```

Don't forget to replace `<version>` with the latest/desired version found on the badges above.

## Usage

You can apply the desired to your window by using `WindowStyle` inside the `WindowScope` of `Window` or similar
composable calls. It can be placed anywhere inside them.

Sample Code:

```kotlin
fun main() = application {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme) }
    var preferredBackdropType by remember { mutableStateOf<WindowBackdrop>(WindowBackdrop.Mica) }

    NativeLookWindow(
        onCloseRequest = ::exitApplication,
        preferredBackdropType = WindowBackdrop.Mica,
    ) {
        // content...
    }
}
```

## Documentation

See documentation [here](https://mayakaapps.github.io/ComposeWindowStyler/index.html)

## Available Styles

### `isDarkTheme`

This property should match the theming system used in your application. By default, it uses the
`isSystemInDarkTheme()` compose function to determine the current state of the system. It's effect depends on the used
backdrop as follows:

* If the `preferredBackdropType` is `WindowBackdrop.Mica` or `WindowBackdrop.Tabbed`, it is used to manage the color of
  the
  background whether it is light or dark.
* Otherwise, it is used to control the color of the title bar of the window white/black.

### Backdrop types (e.g. `preferredBackdropType`)

* `WindowBackdrop.Solid`: This applies a white colour in case of light theme and dark grey (Windows 10 default) colour 
  in case of dark theme.
* `WindowBackdrop.Acrylic`: This
  applies [Acrylic](https://docs.microsoft.com/en-us/windows/apps/design/style/acrylic) backdrop . If the backdrop is 
  rendered opaque, double check that `color` has a reasonable alpha value. Supported on 
  Windows 11.
* `WindowBackdrop.Mica`: This applies [Mica](https://docs.microsoft.com/en-us/windows/apps/design/style/mica) backdrop
  themed according to `isDarkTheme` value. Supported on Windows 11 21H2 or greater.
* `WindowBackdrop.Tabbed`: This applies Tabbed backdrop themed according to `isDarkTheme` value. This is a backdrop that
  is similar to `Mica` but targeted at tabbed windows. Supported on Windows 11 22H2 or greater.

#### Fallback Strategy

In case of unsupported effect the library tries to fall back to the Solid

### `frameStyle`

All the following properties are only supported on Windows 11 or greater and has no effect on other OSes.

* `borderColor`: specifies the color of the window border that is running around the window if the window is decorated.
  This property doesn't support transparency.
* `titleBarColor`: specifies the color of the window title bar (caption bar) if the window is decorated. This property
  doesn't support transparency.
* `captionColor`: specifies the color of the window caption (title) text if the window is decorated. This property
  doesn't support transparency.
* `cornerPreference`: specifies the shape of the corners you want. For example, you can use this property to avoid
  rounded corners in a decorated window or get the corners rounded in an undecorated window.

## License

This library is distributed under the MIT license.

## Contributing

All contributions are welcome. If you are reporting an issue, please use the provided template. If you're planning to
contribute to the code, please open an issue first describing what feature you're planning to add or what issue you're
planning to fix. This allows better discussion and coordination of efforts. You can also check open issues for
bugs/features that needs to be fixed/implemented.

## Acknowledgements

* [flutter_acrylic](https://github.com/alexmercerind/flutter_acrylic): This library is heavily based on flutter_acrylic
* [Swing Acrylic](https://github.com/krlvm/SwingAcrylic): as a reference for the Java implementation of required APIs
