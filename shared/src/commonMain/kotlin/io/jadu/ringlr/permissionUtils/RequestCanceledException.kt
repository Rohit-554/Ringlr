package io.jadu.ringlr.permissionUtils

class RequestCanceledException(
    val permission: Permission,
    message: String? = null
) : Exception(message)
