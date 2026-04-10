package io.jadu.ringlr

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.jadu.ringlr.call.CallManager
import io.jadu.ringlr.call.PlatformConfiguration
import io.jadu.ringlr.permission.factory.BindEffect
import io.jadu.ringlr.permission.factory.rememberPermissionsControllerFactory

@Composable
fun App(configuration: PlatformConfiguration) {
    val callManager = remember(configuration) {
        configuration.initializeCallConfiguration()
        CallManager(configuration)
    }

    val factory = rememberPermissionsControllerFactory()
    val permissionsController = remember(factory) { factory.createPermissionsController() }
    BindEffect(permissionsController)

    MaterialTheme(colorScheme = darkColorScheme()) {
        CallScreen(
            callManager = callManager,
            permissionsController = permissionsController
        )
    }
}
