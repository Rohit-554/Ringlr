package io.jadu.ringlr.permissionUtils.factory

import PermissionsController
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner

@Suppress("FunctionNaming")
@Composable
actual fun BindEffect(permissionsController: PermissionsController) {
    val context = LocalContext.current

    DisposableEffect(permissionsController) {
        val activity = context as? ComponentActivity
        if (activity != null) {
            permissionsController.bind(activity)
        } else {
            throw IllegalStateException("$context is not an instance of ComponentActivity")
        }

        onDispose { }
    }
}