package io.jadu.ringlr.permissionUtils


interface PermissionsControllerProtocol {
    suspend fun providePermission(permission: Permission)
    suspend fun isPermissionGranted(permission: Permission): Boolean
    suspend fun getPermissionState(permission: Permission): PermissionState
    fun openAppSettings()
}
