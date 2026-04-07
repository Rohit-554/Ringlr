package io.jadu.ringlr.call

/**
 * Receives real-time call lifecycle events from [CallManager].
 *
 * Register via [CallManagerInterface.registerCallStateCallback] and always
 * unregister when done to avoid memory leaks.
 */
interface CallStateCallback {
    fun onCallStateChanged(call: Call)
    fun onCallAdded(call: Call)
    fun onCallRemoved(call: Call)
}
