import java.util.Properties
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    id("app.cash.sqldelight") version "2.1.0" apply false
    id("com.codingfeline.buildkonfig") version "0.17.1" apply false
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