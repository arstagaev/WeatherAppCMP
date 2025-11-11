package com.tagaev.core

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.tagaev.core.di.commonModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import platform.Foundation.NSUserDefaults
import org.koin.dsl.module

val iosModule = module {
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults()) }
}

fun initKoinIos(): KoinApplication = startKoin { modules(commonModule, iosModule) }
