package io.jadu.ringlr.callHandler

import io.jadu.ringlr.configs.AudioRoute
import io.jadu.ringlr.configs.Call
import io.jadu.ringlr.configs.CallManagerInterface
import io.jadu.ringlr.configs.CallResult
import io.jadu.ringlr.configs.CallState
import io.jadu.ringlr.configs.CallStateCallback

/**
 * Expected platform-specific configuration class that holds essential platform settings.
 * Android: Will contain Context and necessary Android-specific configurations
 * iOS: Will contain CallKit and AVAudioSession configurations
 */
expect class PlatformConfiguration {

    // Platform-specific initialization
    fun initializeCallConfiguration()

    // Custom Call Configuration
    fun initializeCustomCallConfiguration(setHighlightColor:Int, setDescription:String, setSupportedUriSchemes:List<String>)

    // Platform-specific cleanup
    fun cleanupCallConfiguration()

    companion object {
        fun create(): PlatformConfiguration
    }
}


/**
 * Expected CallManager implementation that bridges to platform-specific calling APIs.
 * Android: Implements using Telecom framework
 * iOS: Implements using CallKit
 */
expect class CallManager(configuration: PlatformConfiguration) :
    CallManagerInterface {

    // Call State Management
    override suspend fun startOutgoingCall(number: String, displayName: String,scheme:String): CallResult<Call>
    override suspend fun endCall(callId: String): CallResult<Unit>
    override suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>
    override suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>

    // Call Information
    override suspend fun getCallState(callId: String): CallResult<CallState>
    override suspend fun getActiveCalls(): CallResult<List<Call>>

    // Audio Route Management
    override suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>
    override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>

    // io.jadu.ringlr.permissionUtils.Permission Management
    override suspend fun checkPermissions(): CallResult<Unit>

    // Callback Registration
    override fun registerCallStateCallback(callback: CallStateCallback)
    override fun unregisterCallStateCallback(callback: CallStateCallback)
}
