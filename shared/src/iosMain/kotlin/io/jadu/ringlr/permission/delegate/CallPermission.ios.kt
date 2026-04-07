package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.PermissionDelegate

/**
 * iOS has no runtime equivalents for Android's telephony permissions.
 * CallKit manages call access at the system level — no grant needed from the app.
 */
internal actual val callDelegate: PermissionDelegate get() = AlwaysGrantedDelegate
internal actual val manageCallDelegate: PermissionDelegate get() = AlwaysGrantedDelegate
internal actual val readPhoneStateDelegate: PermissionDelegate get() = AlwaysGrantedDelegate
internal actual val answerPhoneCallDelegate: PermissionDelegate get() = AlwaysGrantedDelegate
internal actual val readPhoneNumbersDelegate: PermissionDelegate get() = AlwaysGrantedDelegate
