package io.jadu.ringlr.call.voip

import io.jadu.ringlr.call.PlatformConfiguration

/**
 * Registers the device for VoIP push notifications and routes incoming pushes
 * into the Ringlr call lifecycle.
 *
 * **iOS** — wraps PushKit ([PKPushRegistry]). Token delivery and incoming push
 * callbacks are handled automatically once [register] is called.
 * [handleVoipPush] and [handleTokenRefresh] are no-ops on iOS.
 *
 * **Android** — provides the bridge between your FCM service and Ringlr.
 * Call [handleTokenRefresh] from [com.google.firebase.messaging.FirebaseMessagingService.onNewToken]
 * and [handleVoipPush] from [onMessageReceived] whenever a VoIP message arrives.
 *
 * Always call [register] before placing or receiving any VoIP call.
 * Call [unregister] when the calling session ends.
 */
expect class VoipPushRegistrar(configuration: PlatformConfiguration) {
    fun register(listener: VoipPushListener)
    fun unregister()
    fun handleVoipPush(payload: VoipPushPayload)
    fun handleTokenRefresh(token: String)
}
