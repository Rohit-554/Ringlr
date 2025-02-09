package io.jadu.ringlr.permissionUtils.callPermissions

import io.jadu.ringlr.permissionUtils.Permission
import io.jadu.ringlr.permissionUtils.PermissionDelegate

internal expect val callDelegate: PermissionDelegate
internal expect val manageCallDelegate: PermissionDelegate
internal expect val readPhoneStateDelegate: PermissionDelegate
internal expect val answerPhoneCallDelegate: PermissionDelegate
internal expect val readPhoneNumbersDelegate: PermissionDelegate

object CallPhonePermission : Permission {
    override val delegate get() = callDelegate
}

object ManageCallPermission : Permission {
    override val delegate get() = manageCallDelegate
}

object ReadPhoneStatePermission : Permission {
    override val delegate get() = readPhoneStateDelegate
}

object AnswerPhoneCallPermission : Permission {
    override val delegate get() = answerPhoneCallDelegate
}

object ReadPhoneNumbersPermission : Permission {
    override val delegate get() = readPhoneNumbersDelegate
}

val Permission.Companion.CALL_PHONE get() = CallPhonePermission

val Permission.Companion.MANAGE_CALL get() = ManageCallPermission

val Permission.Companion.READ_PHONE_STATE get() = ReadPhoneStatePermission

val Permission.Companion.ANSWER_PHONE_CALLS get() = AnswerPhoneCallPermission

val Permission.Companion.READ_PHONE_NUMBERS get() = ReadPhoneNumbersPermission