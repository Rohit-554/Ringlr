package io.jadu.ringlr.permission.factory

import io.jadu.ringlr.permission.PermissionsController
import androidx.compose.runtime.Composable

@Suppress("FunctionNaming")
@Composable
expect fun BindEffect(permissionsController: PermissionsController)