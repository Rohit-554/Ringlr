package io.jadu.ringlr.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.jadu.ringlr.App
import io.jadu.ringlr.call.PlatformConfiguration

/**
 * Single-activity host for the Ringlr demo.
 * Creates PlatformConfiguration (Context already stored via DemoApplication),
 * then delegates all UI to the shared App composable.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val configuration = PlatformConfiguration.create()

        setContent {
            App(configuration = configuration)
        }
    }
}
