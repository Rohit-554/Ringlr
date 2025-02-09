package io.jadu.ringlr.permissionUtils.audioPermissions

import io.jadu.ringlr.permissionUtils.Permission
import io.jadu.ringlr.permissionUtils.PermissionDelegate

internal expect val modifyAudioSettingsDelegate: PermissionDelegate
internal expect val recordAudioDelegate: PermissionDelegate
internal expect val microphoneDelegate: PermissionDelegate

object ModifyAudioSettingsPermission : Permission {
    override val delegate get() = modifyAudioSettingsDelegate
}

object RecordAudioPermission : Permission {
    override val delegate get() = recordAudioDelegate
}

object MicrophonePermission : Permission {
    override val delegate get() = microphoneDelegate
}

val Permission.Companion.MODIFY_AUDIO_SETTINGS get() = ModifyAudioSettingsPermission

val Permission.Companion.RECORD_AUDIO get() = RecordAudioPermission
val Permission.Companion.MICROPHONE get() = MicrophonePermission
