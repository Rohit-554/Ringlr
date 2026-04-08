package io.jadu.ringlr.call.voip

/**
 * Parsed representation of an incoming VoIP push notification.
 *
 * On iOS, Ringlr extracts these fields from the PushKit payload dictionary.
 * On Android, the app extracts them from the FCM message and passes them
 * to [VoipPushRegistrar.handleVoipPush].
 *
 * Expected push payload keys: "call_id", "caller_number", "caller_name", "scheme".
 */
data class VoipPushPayload(
    val callId: String,
    val callerNumber: String,
    val callerName: String,
    val scheme: String = "sip"
)
