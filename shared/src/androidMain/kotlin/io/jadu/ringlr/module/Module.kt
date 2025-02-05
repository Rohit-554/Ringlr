package io.jadu.ringlr.module

import io.jadu.ringlr.callHandler.PlatformConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Provide PlatformConfiguration with the application context
    single { PlatformConfiguration(androidContext()) }
}