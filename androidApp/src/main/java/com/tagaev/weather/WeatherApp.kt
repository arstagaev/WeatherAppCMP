package com.tagaev.weather

import android.app.Application
import com.tagaev.core.androidPlatformModule
import com.tagaev.core.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(commonModule, androidPlatformModule)
        }
    }
}
