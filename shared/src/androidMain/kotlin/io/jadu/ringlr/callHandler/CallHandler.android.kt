package io.jadu.ringlr.callHandler

import android.content.Context
import android.telecom.TelecomManager

/**
 * Expected platform-specific configuration class that holds essential platform settings.
 * Android: Will contain Context and necessary Android-specific configurations
 * iOS: Will contain CallKit and AVAudioSession configurations
 */
actual class PlatformConfiguration(
    val context:Context
) {

    private val telecomManager: TelecomManager by lazy {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    actual fun initialize() {

    }

    actual fun cleanup() {
    }

}