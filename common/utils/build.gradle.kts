
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)

    kotlin("plugin.serialization") version "2.2.0"
//    id("com.codingfeline.buildkonfig") version "0.17.1"
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
            // ktor
            implementation("io.ktor:ktor-http:2.3.12")
            implementation("io.ktor:ktor-client-core:2.3.12")
            // Logging plugin
            implementation("io.ktor:ktor-client-logging:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            // ktor END

            // time
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            // key-value storage
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("com.russhwolf:multiplatform-settings-serialization:1.3.0")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

        }
        androidMain.dependencies {
            // Koin for Android
            implementation("io.insert-koin:koin-android:4.1.0")
        }

        // iOS UI-specific deps go here if/when you add any
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.12")
        }
    }
}

//// load local.properties (untracked) for local dev
//val localProps = Properties().apply {
//    val f = rootProject.file("local.properties")
//    if (f.exists()) f.inputStream().use(::load)
//}
//fun secret(name: String): String? =
//    providers.gradleProperty(name).orNull                             // gradle.properties if you want
//        ?: providers.environmentVariable(name).orNull                 // CI/CD secret
//        ?: localProps.getProperty(name)                               // local dev

//buildkonfig {
//    packageName = "com.tagaev.secrets"
//    objectName = "Secrets"
//
//    defaultConfigs {
//        buildConfigField(STRING, "WEATHER_API_KEY", secret("WEATHER_API_KEY") ?: "")
//    }
//}