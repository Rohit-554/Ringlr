package io.jadu.ringlr.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.jadu.ringlr.App
import io.jadu.ringlr.call.CallManager
import io.jadu.ringlr.call.PlatformConfiguration

class MainActivity : ComponentActivity() {

    private lateinit var callManager: CallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val configuration = PlatformConfiguration.create()
        configuration.initializeCallConfiguration()
        callManager = CallManager(configuration)

        setContent {
            App(
                configuration = configuration,
                callManager   = callManager
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callManager.release()
    }
}
