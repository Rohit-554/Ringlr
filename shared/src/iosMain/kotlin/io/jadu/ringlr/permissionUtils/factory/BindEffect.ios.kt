package io.jadu.ringlr.permissionUtils.factory

import PermissionsController
import androidx.compose.runtime.Composable

// on iOS side we should not do anything to prepare PermissionsController to work
@Suppress("FunctionNaming")
@Composable
actual fun BindEffect(permissionsController: PermissionsController) = Unit