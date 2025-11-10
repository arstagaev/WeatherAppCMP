import org.jetbrains.kotlin.gradle.dsl.JvmTarget
//import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
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
                implementation("com.arkivanov.decompose:decompose:3.4.0")
                implementation("com.arkivanov.decompose:extensions-compose:3.4.0")
                implementation("com.arkivanov.decompose:extensions-compose-experimental:3.4.0")

                // Ktor (common)
                implementation("io.ktor:ktor-http:2.3.12")
                implementation("io.ktor:ktor-client-core:2.3.12")


                // koin
                implementation("io.insert-koin:koin-core:4.1.0")
                implementation("io.insert-koin:koin-compose:4.1.0")

                // time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

                // Icons Pack
                implementation("br.com.devsrsouza.compose.icons:feather:1.1.1")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                implementation("com.russhwolf:multiplatform-settings:1.3.0")
                implementation("com.russhwolf:multiplatform-settings-serialization:1.3.0")
                // SQL
                implementation("app.cash.sqldelight:runtime:2.1.0")
                implementation("app.cash.sqldelight:coroutines-extensions:2.1.0") // .asFlow(), mapToList, etc.
                implementation("app.cash.sqldelight:primitive-adapters:2.1.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation("app.cash.sqldelight:android-driver:2.1.0")
            }
        }

        // iOS UI-specific deps go here if/when you add any
        val iosMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.1.0")
            }
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
//
//buildkonfig {
//    packageName = "com.tagaev.secrets"
//    objectName = "Secrets"
//
//    defaultConfigs {
//        buildConfigField(STRING, "WEATHER_API_KEY", secret("WEATHER_API_KEY") ?: "")
//    }
//}


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
