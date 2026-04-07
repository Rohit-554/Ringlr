package io.jadu.ringlr.demo

import android.app.Application
import io.jadu.ringlr.call.PlatformConfiguration

/**
 * Initialises the Ringlr Android calling stack once, at application startup.
 * PlatformConfiguration.init stores the application Context so that
 * TelecomManager can be resolved without passing Context everywhere.
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformConfiguration.init(this)
    }
}
