package io.jadu.ringlr.permission

class RequestCanceledException(
    val permission: Permission,
    message: String? = null
) : Exception(message)
