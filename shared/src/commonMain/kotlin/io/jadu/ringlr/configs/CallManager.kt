package io.jadu.ringlr.configs


/**
 * Main interface for managing calls across platforms.
 * This interface provides a unified API for handling calls on both Android and iOS.
 */
interface CallManager {
    /**
     * Initiates an outgoing call to the specified number.
     * @param number The phone number to call
     * @param displayName The name to display for the call
     * @return CallResult containing the Call object if successful
     */
    suspend fun startOutgoingCall(number: String, displayName: String): CallResult<Call>

    /**
     * Ends an active call.
     * @param callId The ID of the call to end
     */
    suspend fun endCall(callId: String): CallResult<Unit>

    /**
     * Mutes or unmutes an active call.
     * @param callId The ID of the call to mute/unmute
     * @param muted True to mute, false to unmute
     */
    suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>

    /**
     * Places a call on hold or resumes it.
     * @param callId The ID of the call to hold/resume
     * @param onHold True to hold, false to resume
     */
    suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>

    /** Gets the current state of a specific call */
    suspend fun getCallState(callId: String): CallResult<CallState>

    /** Gets a list of all active calls in the system */
    suspend fun getActiveCalls(): CallResult<List<Call>>

    /** Changes the audio route for all calls */
    suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>

    /** Gets the current audio route */
    suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>

    /** Checks if all required permissions are granted */
    suspend fun checkPermissions(): CallResult<Unit>

    /** Registers a callback for call state changes */
    fun registerCallStateCallback(callback: CallStateCallback)

    /** Unregisters a previously registered callback */
    fun unregisterCallStateCallback(callback: CallStateCallback)
}
