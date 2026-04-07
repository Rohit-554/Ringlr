package io.jadu.ringlr.call

/**
 * Defines the contract for managing phone calls across platforms.
 *
 * All operations return a [CallResult] so callers can handle success and
 * failure without exceptions leaking through the API boundary.
 * Each function targets a specific call by its [Call.id].
 */
interface CallManagerInterface {
    suspend fun startOutgoingCall(
        number: String,
        displayName: String,
        scheme: String = "tel"
    ): CallResult<Call>

    suspend fun endCall(callId: String): CallResult<Unit>
    suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>
    suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>
    suspend fun getCallState(callId: String): CallResult<CallState>
    suspend fun getActiveCalls(): CallResult<List<Call>>
    suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>
    suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>
    suspend fun checkPermissions(): CallResult<Unit>
    fun registerCallStateCallback(callback: CallStateCallback)
    fun unregisterCallStateCallback(callback: CallStateCallback)
}
