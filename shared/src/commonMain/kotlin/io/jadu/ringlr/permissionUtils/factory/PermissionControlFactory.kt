package io.jadu.ringlr.permissionUtils.factory

import PermissionsController
import androidx.compose.runtime.Composable


fun interface PermissionsControllerFactory {
    fun createPermissionsController(): PermissionsController
}

@Composable
expect fun rememberPermissionsControllerFactory(): PermissionsControllerFactory