package io.jadu.ringlr.permission.factory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.jadu.ringlr.permission.PermissionsControllerImpl

@Composable
actual fun rememberPermissionsControllerFactory(): PermissionsControllerFactory {
    return remember {
        PermissionsControllerFactory {
            PermissionsControllerImpl()
        }
    }
}