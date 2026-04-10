package io.jadu.ringlr.demo

import android.app.Application
import io.jadu.ringlr.call.PlatformConfiguration

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformConfiguration.init(this)
    }
}
