package io.jadu.ringlr.configs

/**
 * Represents the current state of a call in the system.
 */
enum class CallState {
    DIALING,    // Outgoing call is being established
    RINGING,    // Incoming call is ringing
    ACTIVE,     // Call is connected and active
    HOLDING,    // Call is on hold
    ENDED       // Call has ended
}