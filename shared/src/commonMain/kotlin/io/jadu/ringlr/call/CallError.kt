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
}
