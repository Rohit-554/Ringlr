package io.jadu.ringlr.permission

interface Permission {
    val delegate: PermissionDelegate

    // Extended by individual permission delegates
    companion object
}
