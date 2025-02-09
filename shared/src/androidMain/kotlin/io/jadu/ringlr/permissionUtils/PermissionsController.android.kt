import android.content.Context
import androidx.activity.ComponentActivity
import io.jadu.ringlr.permissionUtils.Permission
import io.jadu.ringlr.permissionUtils.PermissionState
import io.jadu.ringlr.permissionUtils.PermissionsControllerImpl

actual interface PermissionsController {
    actual suspend fun providePermission(permission: Permission)
    actual suspend fun isPermissionGranted(permission: Permission): Boolean
    actual suspend fun getPermissionState(permission: Permission): PermissionState
    actual fun openAppSettings()

    fun bind(activity: ComponentActivity)

    companion object {
        operator fun invoke(
            applicationContext: Context
        ): PermissionsController {
            return PermissionsControllerImpl(
                applicationContext = applicationContext
            )
        }
    }
}
