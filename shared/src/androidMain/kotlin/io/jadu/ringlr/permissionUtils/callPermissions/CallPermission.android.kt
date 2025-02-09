package io.jadu.ringlr.permissionUtils.callPermissions

import android.Manifest
import android.content.Context
import io.jadu.ringlr.permissionUtils.PermissionDelegate


actual val callDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null

    override fun getPlatformPermission() =
        listOf(
            Manifest.permission.CALL_PHONE
        )
}

/**
 * This delegate is used to manage call permissions
 * Use this for Android Oreo and above
*/
actual val manageCallDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null
    override fun getPlatformPermission() = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        listOf(
            Manifest.permission.ANSWER_PHONE_CALLS
        )
    } else {
        listOf()
    }
}

internal actual val readPhoneStateDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null
    override fun getPlatformPermission() = listOf(
        Manifest.permission.READ_PHONE_STATE
    )
}

internal actual val answerPhoneCallDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null
    override fun getPlatformPermission() = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        listOf(
            Manifest.permission.ANSWER_PHONE_CALLS
        )
    } else {
        listOf()
    }
}

internal actual val readPhoneNumbersDelegate = object : PermissionDelegate {
    override fun getPermissionStateOverride(applicationContext: Context) = null
    override fun getPlatformPermission() = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        listOf(
            Manifest.permission.READ_PHONE_NUMBERS
        )
    } else {
        listOf()
    }
}