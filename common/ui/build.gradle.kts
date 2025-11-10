import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) // com.android.kotlin.multiplatform.library
    alias(libs.plugins.composeMultiplatform)              // org.jetbrains.compose (if you use CM)
    alias(libs.plugins.composeCompiler)                   // org.jetbrains.kotlin.plugin.compose

}

kotlin {
    // Targets: Android + iOS only (add iosX64() if you need Intel simulators)
    androidLibrary {
        namespace = "com.tagaev.common.ui"
        compileSdk = 35
        minSdk = 24
        // withJava() // uncomment if you have Java sources in androidMain
        // Compose note: with the Kotlin Compose Compiler plugin applied, you do not need buildFeatures.compose
    }
    iosArm64()
    iosSimulatorArm64()
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform (common)
                implementation(project(":common:data"))
                implementation(project(":common:core"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Decompose (common)
                implementation("com.arkivanov.decompose:decompose:3.4.0")
                implementation("com.arkivanov.decompose:extensions-compose:3.4.0")
                implementation("com.arkivanov.decompose:extensions-compose-experimental:3.4.0")

                // Ktor (common)
                implementation("io.ktor:ktor-http:2.3.12")
                implementation("io.ktor:ktor-client-core:2.3.12")

                // koin
                implementation("io.insert-koin:koin-core:4.1.0")
                implementation("io.insert-koin:koin-compose:4.1.0")

                // Icons Pack
                implementation("br.com.devsrsouza.compose.icons:feather:1.1.1")

                // charts
                implementation("io.github.koalaplot:koalaplot-core:0.10.0")

                // time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }

        // iOS UI-specific deps go here if/when you add any
        val iosMain by getting
    }
}


