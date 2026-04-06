package io.jadu.ringlr.call


/**
 * Extended callback interface for additional call events
 */
interface ExtendedCallStateCallback : CallStateCallback {
    fun onCallFailed(call: Call, error: String)
}