package io.jadu.ringlr.call

/**
 * Holds the platform-specific runtime dependencies needed to initialise the calling stack.
 *
 * On Android this wraps TelecomManager and handles phone account registration.
 * On iOS this owns the single CXProvider and AVAudioSession required by CallKit.
 *
 * Always create via [PlatformConfiguration.create] and call [initializeCallConfiguration]
 * before constructing a [CallManager].
 */
expect class PlatformConfiguration {
    fun initializeCallConfiguration()
    fun initializeCustomCallConfiguration(setHighlightColor: Int, setDescription: String, setSupportedUriSchemes: List<String>)
    fun cleanupCallConfiguration()

    companion object {
        fun create(): PlatformConfiguration
    }
}

/**
 * Entry point for all call operations in Ringlr.
 *
 * Bridges the shared [CallManagerInterface] to the platform calling API —
 * Android Telecom on Android, CallKit on iOS. Construct with a fully
 * initialised [PlatformConfiguration].
 *
 * All suspend functions are safe to call from any coroutine context.
 * Register a [CallStateCallback] to receive real-time call lifecycle events.
 */
expect class CallManager(configuration: PlatformConfiguration) : CallManagerInterface {
    override suspend fun startOutgoingCall(number: String, displayName: String, scheme: String): CallResult<Call>
    override suspend fun endCall(callId: String): CallResult<Unit>
    override suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>
    override suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>
    override suspend fun getCallState(callId: String): CallResult<CallState>
    override suspend fun getActiveCalls(): CallResult<List<Call>>
    override suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>
    override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>
    override suspend fun configureSipAccount(profile: SipProfile): CallResult<Unit>
    override suspend fun checkPermissions(): CallResult<Unit>
    override fun registerCallStateCallback(callback: CallStateCallback)
    override fun unregisterCallStateCallback(callback: CallStateCallback)
}
