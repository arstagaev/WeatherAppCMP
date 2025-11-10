package com.tagaev.core.di

import com.russhwolf.settings.Settings
import com.tagaev.core.ui.ThemeController
import com.tagaev.data.local.AppSettings
import com.tagaev.data.remote.HttpClientFactory
import com.tagaev.data.remote.WeatherApi
import com.tagaev.data.remote.ApiConfig
import com.tagaev.core.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val commonModule = module {
    // App-wide scope for background jobs from components (e.g., network calls)
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }
    single { HttpClientFactory.create(json = get(), logBodies = true) }
    single { ApiConfig() }
    single { WeatherApi(get()) }
    single { WeatherRepository(get(), get()) }
    single { AppSettings(get<Settings>()) }

    single { ThemeController(get()) }
}
