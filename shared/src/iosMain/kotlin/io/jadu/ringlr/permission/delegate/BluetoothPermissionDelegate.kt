package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.DeniedAlwaysException
import io.jadu.ringlr.permission.DeniedException
import io.jadu.ringlr.permission.PermissionDelegate
import io.jadu.ringlr.permission.PermissionState
import io.jadu.ringlr.permission.delegate.BluetoothPermission
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManagerAuthorization
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.Foundation.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Requests and checks Bluetooth access via CBCentralManager (iOS 13+).
 *
 * Instantiating a CBCentralManager is what triggers the system authorization
 * prompt when status is NotDetermined. The delegate observes state changes.
 */
internal class BluetoothPermissionDelegate : PermissionDelegate {

    override suspend fun getPermissionState(): PermissionState =
        cbAuthorizationToPermissionState(CBCentralManager.authorization())

    override suspend fun providePermission() {
        val current = getPermissionState()
        if (current == PermissionState.Granted) return
        if (current == PermissionState.DeniedAlways) throw DeniedAlwaysException(BluetoothPermission)

        requestBluetoothAccess()

        val result = getPermissionState()
        if (result != PermissionState.Granted) throw DeniedException(BluetoothPermission)
    }

    private suspend fun requestBluetoothAccess() {
        suspendCancellableCoroutine { continuation ->
            val observer = object : NSObject(), CBCentralManagerDelegateProtocol {
                override fun centralManagerDidUpdateState(central: CBCentralManager) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
            CBCentralManager(delegate = observer, queue = null)
        }
    }

    private fun cbAuthorizationToPermissionState(authorization: CBManagerAuthorization): PermissionState =
        when (authorization) {
            CBManagerAuthorizationAllowedAlways -> PermissionState.Granted
            CBManagerAuthorizationDenied, CBManagerAuthorizationRestricted -> PermissionState.DeniedAlways
            CBManagerAuthorizationNotDetermined -> PermissionState.NotDetermined
            else -> PermissionState.NotDetermined
        }
}
