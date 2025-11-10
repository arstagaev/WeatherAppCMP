import java.util.Properties
import kotlin.apply
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
//import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.io.FileInputStream
import java.io.FileWriter

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)

    kotlin("plugin.serialization") version "2.2.0"
//    id("com.codingfeline.buildkonfig") version "0.17.1"
    id("com.codingfeline.buildkonfig")
}

kotlin {
    androidLibrary {
        namespace = "com.tagaev.common.data"
        compileSdk = 35
        minSdk = 24
        // withJava()
        // Compose note: with the Kotlin Compose Compiler plugin applied, you do not need buildFeatures.compose
    }
    iosArm64()
    iosSimulatorArm64()
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:utils"))

            implementation("com.arkivanov.decompose:decompose:3.4.0")
            implementation("com.arkivanov.decompose:extensions-compose:3.4.0")
            implementation("com.arkivanov.decompose:extensions-compose-experimental:3.4.0")

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

// load local.properties (untracked) for local dev
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
////    targetConfigs {
////        android {
////            buildConfigField 'STRING', 'name2', 'value2'
////        }
////
////        ios {
////            buildConfigField 'STRING', 'name', 'valueIos'
////        }
////    }
//}

//buildkonfig {
//    packageName = "com.tagaev.secrets"
//    objectName = "Secrets"
//
//    // REQUIRED: non-flavored defaults
//    defaultConfigs {
//        // read from gradle/local/env – empty fallback OK for dev
//        val apiKey = providers.gradleProperty("WEATHER_API_KEY").orNull
//            ?: providers.environmentVariable("WEATHER_API_KEY").orNull
//            ?: ""
//        buildConfigField(STRING, "WEATHER_API_KEY", apiKey)
//    }
//
//    // OPTIONAL flavors (ONLY if you’ve set buildkonfig.flavor in gradle.properties)
//    // defaultConfigs("dev") { buildConfigField(STRING, "WEATHER_API_KEY", apiKey) }
//    // targetConfigs { create("android") { /* platform-specific overrides */ } }
//}

// Load local.properties (root of the project)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use(::load)
}

// Resolve the key from (1) Gradle prop, (2) ENV, (3) local.properties
val weatherApiKey: String = providers.gradleProperty("WEATHER_API_KEY").orNull
    ?: providers.environmentVariable("WEATHER_API_KEY").orNull
    ?: localProps.getProperty("WEATHER_API_KEY")
    ?: ""

buildkonfig {
    packageName = "com.tagaev.secrets"   // choose any package you like
    objectName = "Secrets"

    // REQUIRED non-flavored defaults
    defaultConfigs {
        buildConfigField(STRING, "WEATHER_API_KEY", weatherApiKey)
    }
    // If you later use flavors (via buildkonfig.flavor), you STILL keep defaultConfigs above.
}