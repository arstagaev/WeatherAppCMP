
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)

    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    androidLibrary {
        namespace = "com.tagaev.common.data"
        compileSdk = 35
        minSdk = 24
    }
    iosArm64()
    iosSimulatorArm64()
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // Ktor
            implementation(libs.ktor.http)
            implementation(libs.ktor.client.core)
            // Logging plugin
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Ktor END

            // time
            implementation(libs.kotlinx.datetime)

            // key-value storage
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.serialization)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

        }
        androidMain.dependencies {
            // Koin for Android
            implementation(libs.koin.android)
        }

        // iOS UI-specific deps go here if/when you add any
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
