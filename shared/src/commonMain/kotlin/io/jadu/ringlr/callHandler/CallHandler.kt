package io.jadu.ringlr.callHandler

import io.jadu.ringlr.configs.AudioRoute
import io.jadu.ringlr.configs.Call
import io.jadu.ringlr.configs.CallManager
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
    fun initialize()

    // Platform-specific cleanup
    fun cleanup()
}


/**
 * Expected CallManager implementation that bridges to platform-specific calling APIs.
 * Android: Implements using Telecom framework
 * iOS: Implements using CallKit
 */
expect class CallManagerImpl(configuration: PlatformConfiguration) : CallManager {
    // Constructor that takes platform configuration

    // Call State Management
    override suspend fun startOutgoingCall(number: String, displayName: String): CallResult<Call>
    override suspend fun endCall(callId: String): CallResult<Unit>
    override suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>
    override suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>

    // Call Information
    override suspend fun getCallState(callId: String): CallResult<CallState>
    override suspend fun getActiveCalls(): CallResult<List<Call>>

    // Audio Route Management
    override suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>
    override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>

    // Permission Management
    override suspend fun checkPermissions(): CallResult<Unit>

    // Callback Registration
    override fun registerCallStateCallback(callback: CallStateCallback)
    override fun unregisterCallStateCallback(callback: CallStateCallback)
}
