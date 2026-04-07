package io.jadu.ringlr.call

/**
 * Notified whenever the active audio output route changes during a call.
 *
 * Route changes can be triggered by the user, by connecting/disconnecting
 * a headset or Bluetooth device, or by the platform reclaiming the session.
 */
interface AudioRouteCallback {
    fun onAudioRouteChanged(route: AudioRoute)
}
