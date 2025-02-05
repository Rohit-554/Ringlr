package io.jadu.ringlr

import android.app.Application
import io.jadu.ringlr.module.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RinglrApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RinglrApp)
            modules(appModule)
        }
    }
}