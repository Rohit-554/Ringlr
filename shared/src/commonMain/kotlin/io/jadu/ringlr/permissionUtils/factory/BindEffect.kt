package io.jadu.ringlr.permissionUtils.factory

import PermissionsController
import androidx.compose.runtime.Composable

@Suppress("FunctionNaming")
@Composable
expect fun BindEffect(permissionsController: PermissionsController)