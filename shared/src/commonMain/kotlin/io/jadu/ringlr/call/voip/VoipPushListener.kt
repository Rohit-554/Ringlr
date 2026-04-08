package io.jadu.ringlr.call.voip

/**
 * Receives VoIP push events from [VoipPushRegistrar].
 *
 * [onTokenRefreshed] — send this token to your push server whenever it changes.
 * [onIncomingCall]   — an incoming VoIP call arrived; show your in-app UI or
 *                      use [io.jadu.ringlr.call.CallManager] to manage the call.
 */
interface VoipPushListener {
    fun onTokenRefreshed(token: String)
    fun onIncomingCall(payload: VoipPushPayload)
}
