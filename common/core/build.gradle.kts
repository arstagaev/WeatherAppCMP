
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) // com.android.kotlin.multiplatform.library
    alias(libs.plugins.composeMultiplatform)              // org.jetbrains.compose (if you use CM)
    alias(libs.plugins.composeCompiler)                   // org.jetbrains.kotlin.plugin.compose

    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    androidLibrary {
        namespace = "com.tagaev.common.core"
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

                // time
                implementation(libs.kotlinx.datetime)

                // Icons Pack
                implementation(libs.feather)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.serialization)
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
            }
        }
    }
}

//sqldelight {
//    databases {
//        create("WeatherDatabase") {
//            packageName.set("com.tagaev.data.db")
//            // optional:
//            // deriveSchemaFromMigrations.set(true)
//            // verifyMigrations.set(true)
//        }
//    }
//}
