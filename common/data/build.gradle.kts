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

            // Decompose (common)
            implementation(libs.decompose)
            implementation(libs.decomposeExt.compose)
            implementation(libs.decomposeExt.compose.experimental)

            // Ktor Start
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

// Load local.properties (root of the project)
val localProps = Properties().apply {
    val f = rootProject.file("keys.properties")
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