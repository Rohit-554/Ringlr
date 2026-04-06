package io.jadu.ringlr.permission

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual typealias PermissionsController = PermissionsControllerProtocol

class PermissionsControllerImpl : PermissionsControllerProtocol {

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