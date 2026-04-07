package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.PermissionDelegate

/**
 * Both BLUETOOTH and BLUETOOTH_CONNECT map to CBCentralManager authorization on iOS.
 * The OS presents a single Bluetooth access prompt for both use cases.
 */
internal actual val bluetoothDelegate: PermissionDelegate get() = BluetoothPermissionDelegate()
internal actual val bluetoothConnectDelegate: PermissionDelegate get() = BluetoothPermissionDelegate()
