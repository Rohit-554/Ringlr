package io.jadu.ringlr.configs


/**
 * Extended callback interface for additional call events
 */
interface ExtendedCallStateCallback : CallStateCallback {
    fun onCallFailed(call: Call, error: String)
}