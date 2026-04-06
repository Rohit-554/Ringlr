package io.jadu.ringlr.permission.factory

import io.jadu.ringlr.permission.PermissionsController
import androidx.compose.runtime.Composable


fun interface PermissionsControllerFactory {
    fun createPermissionsController(): PermissionsController
}

@Composable
expect fun rememberPermissionsControllerFactory(): PermissionsControllerFactory