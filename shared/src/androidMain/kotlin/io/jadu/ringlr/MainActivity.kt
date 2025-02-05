package io.jadu.ringlr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.jadu.ringlr.callHandler.PlatformConfiguration
import org.koin.android.ext.android.inject

class MainActivity:AppCompatActivity() {
    private val platformConfiguration: PlatformConfiguration by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        platformConfiguration.initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        platformConfiguration.cleanup()
    }
}