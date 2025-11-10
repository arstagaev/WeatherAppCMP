package com.tagaev.core

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults
import org.koin.dsl.module

val iosPlatformModule = module {
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults()) }
}