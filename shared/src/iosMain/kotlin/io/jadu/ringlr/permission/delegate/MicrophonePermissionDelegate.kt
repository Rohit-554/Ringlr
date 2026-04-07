package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.DeniedAlwaysException
import io.jadu.ringlr.permission.DeniedException
import io.jadu.ringlr.permission.PermissionDelegate
import io.jadu.ringlr.permission.PermissionState
import io.jadu.ringlr.permission.delegate.MicrophonePermission
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Requests and checks microphone access via AVAudioSession.
 *
 * Used for RECORD_AUDIO and MICROPHONE permissions. Both map to the same
 * underlying iOS permission — the microphone access prompt.
 */
internal class MicrophonePermissionDelegate : PermissionDelegate {

    private val session: AVAudioSession get() = AVAudioSession.sharedInstance()

    override suspend fun getPermissionState(): PermissionState =
        when (session.recordPermission) {
            AVAudioSessionRecordPermissionGranted -> PermissionState.Granted
            AVAudioSessionRecordPermissionDenied -> PermissionState.DeniedAlways
            else -> PermissionState.NotDetermined
        }

    override suspend fun providePermission() {
        val current = getPermissionState()
        if (current == PermissionState.Granted) return
        if (current == PermissionState.DeniedAlways) throw DeniedAlwaysException(MicrophonePermission)

        val granted = requestMicrophoneAccess()
        if (!granted) throw DeniedException(MicrophonePermission)
    }

    private suspend fun requestMicrophoneAccess(): Boolean =
        suspendCancellableCoroutine { continuation ->
            session.requestRecordPermission { granted ->
                if (continuation.isActive) continuation.resume(granted)
            }
        }
}
