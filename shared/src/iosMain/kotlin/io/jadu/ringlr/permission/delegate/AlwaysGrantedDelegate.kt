package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.PermissionDelegate
import io.jadu.ringlr.permission.PermissionState

/**
 * Delegate for permissions that have no iOS equivalent.
 *
 * On Android, permissions like CALL_PHONE guard access to the Telecom API.
 * On iOS, CallKit manages call access at the system level — no runtime grant
 * is required from the app. Reporting Granted avoids blocking the iOS call flow.
 */
internal object AlwaysGrantedDelegate : PermissionDelegate {

    override suspend fun providePermission() = Unit

    override suspend fun getPermissionState(): PermissionState = PermissionState.Granted
}
