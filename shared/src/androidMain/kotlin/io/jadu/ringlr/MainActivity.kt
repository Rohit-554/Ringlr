package io.jadu.ringlr

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.jadu.ringlr.callHandler.PlatformConfiguration
import org.koin.android.ext.android.inject

class MainActivity:AppCompatActivity() {
    private val platformConfiguration: PlatformConfiguration by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformConfiguration.init(this)
        platformConfiguration.initializeCallConfiguration()
    }

    override fun onDestroy() {
        super.onDestroy()
        platformConfiguration.cleanupCallConfiguration()
    }

}