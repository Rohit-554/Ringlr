package io.jadu.ringlr.permissionUtils

import io.jadu.ringlr.permissionUtils.Permission
import io.jadu.ringlr.permissionUtils.PermissionState
import io.jadu.ringlr.permissionUtils.PermissionsControllerProtocol
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class PermissionsController : PermissionsControllerProtocol {

    override suspend fun providePermission(permission: Permission) {
        return permission.delegate.providePermission()
    }

    override suspend fun isPermissionGranted(permission: Permission): Boolean {
        return permission.delegate.getPermissionState() == PermissionState.Granted
    }

    override suspend fun getPermissionState(permission: Permission): PermissionState {
        return permission.delegate.getPermissionState()
    }

    override fun openAppSettings() {
        val settingsUrl: NSURL = NSURL.URLWithString(UIApplicationOpenSettingsURLString)!!
        UIApplication.sharedApplication.openURL(settingsUrl, mapOf<Any?, Any>(), null)
    }

}