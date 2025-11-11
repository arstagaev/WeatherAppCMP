import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) // com.android.kotlin.multiplatform.library
    alias(libs.plugins.composeMultiplatform)              // org.jetbrains.compose (if you use CM)
    alias(libs.plugins.composeCompiler)                   // org.jetbrains.kotlin.plugin.compose

}

kotlin {
    applyDefaultHierarchyTemplate()
    androidLibrary {
        namespace = "com.tagaev.common.ui"
        compileSdk = 35
        minSdk = 24
        // withJava() // uncomment if you have Java sources in androidMain
        // Compose note: with the Kotlin Compose Compiler plugin applied, you do not need buildFeatures.compose
    }
    val iosArm64Target = iosArm64()
    val iosSimArm64Target = iosSimulatorArm64()

    // Optional: JVM target for any desktop previews
    jvmToolchain(17)

    // Create an XCFramework aggregator (this is what adds assembleXCFramework*)
    val xcf = XCFramework()

    listOf(
        iosArm64Target,
        iosSimArm64Target
    ).forEach { target ->
        target.binaries.framework {
            baseName = "WeatherUI"   // <- Swift import name
            isStatic = false
            xcf.add(this)            // <- registers assembleXCFramework tasks
        }
    }

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
                implementation(libs.decompose)
                implementation(libs.decomposeExt.compose)
                implementation(libs.decomposeExt.compose.experimental)

                // Ktor (common)
                implementation(libs.ktor.http)
                implementation(libs.ktor.client.core)

                // koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // Icons Pack
                implementation(libs.feather)

                // charts
                implementation(libs.koalaplot.core)

                // time
                implementation(libs.kotlinx.datetime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }

        // iOS UI-specific deps go here if/when you add any
        val iosMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
            }
        }
    }
}
