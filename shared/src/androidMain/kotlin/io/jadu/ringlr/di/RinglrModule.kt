package io.jadu.ringlr.di

import io.jadu.ringlr.call.PlatformConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val ringlrModule = module {
    // Provide PlatformConfiguration with the application context
    single { PlatformConfiguration(androidContext()) }
}