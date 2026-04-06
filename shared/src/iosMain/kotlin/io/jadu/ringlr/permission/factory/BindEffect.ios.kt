package io.jadu.ringlr.permission.factory

import io.jadu.ringlr.permission.PermissionsController
import androidx.compose.runtime.Composable

// on iOS side we should not do anything to prepare PermissionsController to work
@Suppress("FunctionNaming")
@Composable
actual fun BindEffect(permissionsController: PermissionsController) = Unit