package io.jadu.ringlr.callHandler

/**
 * Expected platform-specific configuration class that holds essential platform settings.
 * Android: Will contain Context and necessary Android-specific configurations
 * iOS: Will contain CallKit and AVAudioSession configurations
 */
expect class PlatformConfiguration {

    // Platform-specific initialization
    fun initialize()

    fun cleanup()
}