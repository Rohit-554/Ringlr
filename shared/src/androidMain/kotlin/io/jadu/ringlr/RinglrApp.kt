package io.jadu.ringlr

import android.app.Application
import io.jadu.ringlr.call.PlatformConfiguration
import io.jadu.ringlr.di.ringlrModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RinglrApp: Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformConfiguration.init(this)
        startKoin {
            androidContext(this@RinglrApp)
            modules(ringlrModule)
        }
    }
}