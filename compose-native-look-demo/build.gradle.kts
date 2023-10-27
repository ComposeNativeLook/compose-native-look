import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":compose-native-look"))
}

compose.desktop {
    application {
        mainClass = "com.github.composenativelook.demo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)

            windows {
                this.upgradeUuid = "1bd41766-6d17-11ee-b962-0242ac120002"
            }

            packageName = "compose-native-look-demo"
            packageVersion = "1.0.0"
        }
    }
}
