package io.jadu.ringlr.call

/**
 * Represents every failure case that can arise during a call operation.
 *
 * Extends [Exception] so it can be thrown inside coroutines and caught
 * naturally, while still being returnable inside [CallResult.Error] for
 * callers that prefer result-style error handling.
 */
sealed class CallError : Exception() {
    data class PermissionDenied(override val message: String) : CallError()
    data class MissingPermission(val permissionName: String) : CallError()
    data class InvalidCallState(val currentState: CallState, val requestedOperation: String) : CallError()
    data class CallNotFound(val callId: String) : CallError()
    data class AudioDeviceError(override val message: String) : CallError()
    data class NetworkError(override val message: String) : CallError()
    data class ServiceNotInitialized(override val message: String) : CallError()
    data class ServiceError(override val message: String, val code: Int) : CallError()

    /**
     * Returned when SIP/VoIP calling is requested on a platform or OS version that
     * does not provide a built-in SIP stack.
     *
     * On Android 12+ (API 31+), Google removed native SIP support with no official
     * replacement. Ringlr covers the Telecom/CallKit UI layer; the app must supply
     * its own SIP or VoIP stack for signaling and media.
     */
    data class SipUnsupported(override val message: String) : CallError()
}
