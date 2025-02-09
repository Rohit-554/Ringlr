package io.jadu.ringlr.permissionUtils

interface Permission {
    val delegate: PermissionDelegate

    // Extended by individual permission delegates
    companion object
}
