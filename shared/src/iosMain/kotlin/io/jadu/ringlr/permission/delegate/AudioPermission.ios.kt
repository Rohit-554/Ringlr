package io.jadu.ringlr.permission.delegate

import io.jadu.ringlr.permission.PermissionDelegate

/**
 * MODIFY_AUDIO_SETTINGS has no iOS equivalent — AVAudioSession configuration
 * is unrestricted. RECORD_AUDIO and MICROPHONE both map to the microphone
 * access prompt via AVAudioSession.
 */
internal actual val modifyAudioSettingsDelegate: PermissionDelegate get() = AlwaysGrantedDelegate
internal actual val recordAudioDelegate: PermissionDelegate get() = MicrophonePermissionDelegate()
internal actual val microphoneDelegate: PermissionDelegate get() = MicrophonePermissionDelegate()
