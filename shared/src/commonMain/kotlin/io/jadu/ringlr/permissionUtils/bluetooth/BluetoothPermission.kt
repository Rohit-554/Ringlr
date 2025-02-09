package io.jadu.ringlr.permissionUtils.bluetooth

import io.jadu.ringlr.permissionUtils.Permission
import io.jadu.ringlr.permissionUtils.PermissionDelegate

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