package io.jadu.ringlr.permissionUtils

enum class PermissionState {

    /**
     * Starting state for each permission.
     */
    NotDetermined,

    /**
     * Android-only. This could mean [NotDetermined] or [DeniedAlways], but the OS doesn't
     * expose which of the two it is in all scenarios.
     */
    NotGranted,

    Granted,

    /**
     * Android-only.
     */
    Denied,

    /**
     * On Android only applicable to Push Notifications.
     */
    DeniedAlways
}
