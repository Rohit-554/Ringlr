package io.jadu.ringlr.configs


/**
 * Interface for receiving updates about call state changes.
 */
interface CallStateCallback {
    fun onCallStateChanged(call: Call)
    fun onCallAdded(call: Call)
    fun onCallRemoved(call: Call)
}