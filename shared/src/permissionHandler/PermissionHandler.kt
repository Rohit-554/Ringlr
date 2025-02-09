package io.jadu.ringlr.permissionHandler

interface PermissionHandler {
    fun requestPermission(permission: String, requestCode: Int)
    fun checkPermission(permission: String): Boolean
}