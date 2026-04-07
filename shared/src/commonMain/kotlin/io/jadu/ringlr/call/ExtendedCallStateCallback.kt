package io.jadu.ringlr.call

/**
 * Extends [CallStateCallback] with failure notifications.
 *
 * Use this instead of [CallStateCallback] when the caller needs to respond
 * to error conditions mid-call, such as network drops or hardware faults.
 */
interface ExtendedCallStateCallback : CallStateCallback {
    fun onCallFailed(call: Call, error: String)
}
