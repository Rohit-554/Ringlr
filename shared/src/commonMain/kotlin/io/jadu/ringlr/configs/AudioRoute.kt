package io.jadu.ringlr.configs

/**
 * Represents possible audio routing options for calls.
 */
enum class AudioRoute {
    SPEAKER,        // Phone's loudspeaker
    EARPIECE,       // Phone's earpiece
    BLUETOOTH,      // Connected Bluetooth device
    WIRED_HEADSET  // Wired headphones or headset
}

interface AudioRouteCallback {
    fun onAudioRouteChanged(route: AudioRoute)
}