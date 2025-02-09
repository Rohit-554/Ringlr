package io.jadu.ringlr.permissionUtils.factory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.jadu.ringlr.permissionUtils.PermissionsController

@Composable
actual fun rememberPermissionsControllerFactory(): PermissionsControllerFactory {
    return remember {
        PermissionsControllerFactory {
           PermissionsController()
        }
    }
}