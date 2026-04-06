package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.Permission
import io.jadu.ringlr.permission.PermissionDelegate

internal expect val bluetoothDelegate: PermissionDelegate
internal expect val bluetoothConnectDelegate: PermissionDelegate

object BluetoothPermission: Permission {
    override val delegate get() = bluetoothDelegate
}

object BluetoothConnectPermission: Permission {
    override val delegate get() = bluetoothConnectDelegate
}

val Permission.Companion.BLUETOOTH get() = BluetoothPermission
val Permission.Companion.BLUETOOTH_CONNECT get() = BluetoothConnectPermission