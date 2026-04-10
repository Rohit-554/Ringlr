package io.jadu.ringlr.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.jadu.ringlr.App
import io.jadu.ringlr.call.PlatformConfiguration

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
