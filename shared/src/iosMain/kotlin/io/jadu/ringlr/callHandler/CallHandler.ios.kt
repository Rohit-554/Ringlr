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
actual class PlatformConfiguration {

    actual fun cleanupCallConfiguration() {
    }

    actual companion object {
        actual fun create(): PlatformConfiguration {
            TODO("Not yet implemented")
        }
    }

    actual fun initializeCustomCallConfiguration(
        setHighlightColor: Int,
        setDescription: String,
        setSupportedUriSchemes: List<String>
    ) {
    }

    actual fun initializeCallConfiguration() {
    }

}

/**
 * Expected CallManager implementation that bridges to platform-specific calling APIs.
 * Android: Implements using Telecom framework
 * iOS: Implements using CallKit
 */
actual class CallManager : CallManagerInterface {
    actual constructor(configuration: PlatformConfiguration) {
        TODO("Not yet implemented")
    }

    actual override suspend fun startOutgoingCall(
        number: String,
        displayName: String,
        scheme: String
    ): CallResult<Call> {
        TODO("Not yet implemented")
    }

    override suspend fun startCustomOutgoingCall(
        number: String,
        displayName: String
    ): CallResult<Call> {
        TODO("Not yet implemented")
    }


    actual override suspend fun endCall(callId: String): CallResult<Unit> {
        TODO("Not yet implemented")
    }

    actual override suspend fun muteCall(
        callId: String,
        muted: Boolean
    ): CallResult<Unit> {
        TODO("Not yet implemented")
    }

    actual override suspend fun holdCall(
        callId: String,
        onHold: Boolean
    ): CallResult<Unit> {
        TODO("Not yet implemented")
    }

    actual override suspend fun getCallState(callId: String): CallResult<CallState> {
        TODO("Not yet implemented")
    }

    actual override suspend fun getActiveCalls(): CallResult<List<Call>> {
        TODO("Not yet implemented")
    }

    actual override suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit> {
        TODO("Not yet implemented")
    }

    actual override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute> {
        TODO("Not yet implemented")
    }

    actual override suspend fun checkPermissions(): CallResult<Unit> {
        TODO("Not yet implemented")
    }

    actual override fun registerCallStateCallback(callback: CallStateCallback) {
    }

    actual override fun unregisterCallStateCallback(callback: CallStateCallback) {
    }

}