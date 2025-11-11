package com.tagaev.core

import com.tagaev.core.di.commonModule
import org.koin.core.context.startKoin

fun initKoin() = startKoin {
    modules(commonModule, iosModule)
}