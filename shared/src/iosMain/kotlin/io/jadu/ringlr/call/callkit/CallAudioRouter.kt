package io.jadu.ringlr.call.callkit

import io.jadu.ringlr.call.AudioRoute
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionPortBluetoothA2DP
import platform.AVFAudio.AVAudioSessionPortBluetoothHFP
import platform.AVFAudio.AVAudioSessionPortBuiltInSpeaker
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.AVAudioSessionPortHeadphones
import platform.AVFAudio.AVAudioSessionPortOverrideNone
import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker

/**
 * Routes audio output for an active call using AVAudioSession.
 *
 * The session must be active (CallKit activates it automatically during a call
 * via provider(_:didActivate:)) before routing calls are meaningful.
 */
internal class CallAudioRouter {

    private val session: AVAudioSession get() = AVAudioSession.sharedInstance()

    fun activate() {
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setActive(true, error = null)
    }

    fun deactivate() {
        session.setActive(false, error = null)
    }

    fun routeTo(destination: AudioRoute): Boolean = when (destination) {
        AudioRoute.SPEAKER -> overrideToSpeaker()
        AudioRoute.EARPIECE -> overrideToEarpiece()
        AudioRoute.BLUETOOTH -> routeToBluetoothHeadset()
        AudioRoute.WIRED_HEADSET -> routeToWiredHeadset()
    }

    fun currentRoute(): AudioRoute {
        val outputs = session.currentRoute.outputs.filterIsInstance<AVAudioSessionPortDescription>()
        return when {
            outputs.any { it.portType == AVAudioSessionPortBuiltInSpeaker } -> AudioRoute.SPEAKER
            outputs.any { it.isBluetooth() } -> AudioRoute.BLUETOOTH
            outputs.any { it.portType == AVAudioSessionPortHeadphones } -> AudioRoute.WIRED_HEADSET
            else -> AudioRoute.EARPIECE
        }
    }

    private fun overrideToSpeaker(): Boolean =
        session.overrideOutputAudioPort(AVAudioSessionPortOverrideSpeaker, error = null)

    private fun overrideToEarpiece(): Boolean =
        session.overrideOutputAudioPort(AVAudioSessionPortOverrideNone, error = null)

    private fun routeToBluetoothHeadset(): Boolean {
        val port = findInput(AVAudioSessionPortBluetoothHFP) ?: return false
        return session.setPreferredInput(port, error = null)
    }

    private fun routeToWiredHeadset(): Boolean {
        val port = findInput(AVAudioSessionPortHeadphones) ?: return false
        return session.setPreferredInput(port, error = null)
    }

    private fun findInput(portType: String): AVAudioSessionPortDescription? =
        session.availableInputs
            ?.filterIsInstance<AVAudioSessionPortDescription>()
            ?.firstOrNull { it.portType == portType }

    private fun AVAudioSessionPortDescription.isBluetooth(): Boolean =
        portType == AVAudioSessionPortBluetoothHFP || portType == AVAudioSessionPortBluetoothA2DP
}
