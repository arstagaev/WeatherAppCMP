package com.tagaev.core

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single<Settings> {
        val ctx: Context = androidContext()
        SharedPreferencesSettings(
            ctx.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        )
    }
}