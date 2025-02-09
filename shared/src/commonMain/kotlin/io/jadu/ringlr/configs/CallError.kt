package io.jadu.ringlr.configs

/**
 * Represents possible errors that can occur during call operations.
 * This sealed class hierarchy provides type-safe error handling across platforms.
 */
sealed class CallError : Exception() {
    // io.jadu.ringlr.permissionUtils.Permission-related errors
    data class PermissionDenied(override val message: String) : CallError()
    data class MissingPermission(val permission: String) : CallError()

    // Call state errors
    data class InvalidCallState(val currentState: CallState, val requestedOperation: String) : CallError()
    data class CallNotFound(val callId: String) : CallError()

    // Device/hardware errors
    data class AudioDeviceError(override val message: String) : CallError()
    data class NetworkError(override val message: String) : CallError()

    // Service errors
    data class ServiceNotInitialized(override val message: String) : CallError()
    data class ServiceError(override val message: String, val code: Int) : CallError()
}