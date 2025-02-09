package io.jadu.ringlr.permissionUtils.audioPermissions

import android.Manifest
import android.content.Context
import io.jadu.ringlr.permissionUtils.PermissionDelegate

internal actual val modifyAudioSettingsDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null

    override fun getPlatformPermission() =
        listOf(
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
}

internal actual val recordAudioDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null
    override fun getPlatformPermission() = listOf(
        Manifest.permission.RECORD_AUDIO
    )
}

internal actual val microphoneDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null
    override fun getPlatformPermission() = listOf(
        Manifest.permission.RECORD_AUDIO
    )
}