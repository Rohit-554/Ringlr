@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.jadu.ringlr.call.callkit

import io.jadu.ringlr.call.AudioRoute
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionPortOverrideNone
import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker

/**
 * Routes audio output for an active call using AVAudioSession.
 *
 * CallKit activates and deactivates the audio session automatically via
 * [CXProviderDelegate.provider(_:didActivate:)] and
 * [CXProviderDelegate.provider(_:didDeactivate:)]. This class only
 * configures the session category and manages the speaker override —
 * it does not call setActive directly.
 *
 * For Bluetooth and wired headset routing, iOS routes automatically to
 * any connected device when the speaker override is removed. Apps that
 * need fine-grained Bluetooth control should use [AVRoutePickerView].
 */
internal class CallAudioRouter {

    private val session: AVAudioSession get() = AVAudioSession.sharedInstance()
    private var activeRoute: AudioRoute = AudioRoute.EARPIECE

    fun activate() {
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
    }

    fun deactivate() {
        activeRoute = AudioRoute.EARPIECE
    }

    fun routeTo(destination: AudioRoute): Boolean {
        val success = applyRoute(destination)
        if (success) activeRoute = destination
        return success
    }

    fun currentRoute(): AudioRoute = activeRoute

    private fun applyRoute(destination: AudioRoute): Boolean = when (destination) {
        AudioRoute.SPEAKER -> overrideToSpeaker()
        AudioRoute.EARPIECE, AudioRoute.BLUETOOTH, AudioRoute.WIRED_HEADSET -> removeOverride()
    }

    private fun overrideToSpeaker(): Boolean =
        session.overrideOutputAudioPort(AVAudioSessionPortOverrideSpeaker, error = null)

    private fun removeOverride(): Boolean =
        session.overrideOutputAudioPort(AVAudioSessionPortOverrideNone, error = null)
}
