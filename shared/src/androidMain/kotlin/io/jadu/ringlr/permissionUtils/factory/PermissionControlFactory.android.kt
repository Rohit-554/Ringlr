package io.jadu.ringlr.permissionUtils.factory

import PermissionsController
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPermissionsControllerFactory(): PermissionsControllerFactory {
    val context: Context = LocalContext.current
    return remember(context) {
        PermissionsControllerFactory {
            PermissionsController(applicationContext = context.applicationContext)
        }
    }
}