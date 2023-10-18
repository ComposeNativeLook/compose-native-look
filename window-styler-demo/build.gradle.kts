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
    implementation(project(":window-styler"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)

            windows {
                this.upgradeUuid = "1bd41766-6d17-11ee-b962-0242ac120002"
            }

            packageName = "window-styler-demo"
            packageVersion = "1.0.0"
        }
    }
}
