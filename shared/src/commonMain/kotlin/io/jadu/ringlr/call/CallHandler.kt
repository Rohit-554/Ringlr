package io.jadu.ringlr.call

expect class PlatformConfiguration {
    fun initializeCallConfiguration()
    fun initializeCustomCallConfiguration(setHighlightColor: Int, setDescription: String, setSupportedUriSchemes: List<String>)
    fun cleanupCallConfiguration()

    companion object {
        fun create(): PlatformConfiguration
    }
}

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
    override suspend fun answerCall(callId: String): CallResult<Unit>
    override suspend fun declineCall(callId: String): CallResult<Unit>
    override fun registerCallStateCallback(callback: CallStateCallback)
    override fun unregisterCallStateCallback(callback: CallStateCallback)
}
