package io.jadu.ringlr.call.voip

import android.content.Context
import android.os.Bundle
import android.telecom.TelecomManager
import io.jadu.ringlr.call.Call
import io.jadu.ringlr.call.CallState
import io.jadu.ringlr.call.PhoneAccountRegistrar
import io.jadu.ringlr.call.PlatformConfiguration

/**
 * Android implementation. Acts as a bridge between your FCM service and Ringlr.
 *
 * Usage in your FirebaseMessagingService:
 * ```
 * override fun onNewToken(token: String) {
 *     voipPushRegistrar.handleTokenRefresh(token)
 * }
 *
 * override fun onMessageReceived(message: RemoteMessage) {
 *     if (message.data["type"] == "voip") {
 *         voipPushRegistrar.handleVoipPush(
 *             VoipPushPayload(
 *                 callId       = message.data["call_id"]      ?: "",
 *                 callerNumber = message.data["caller_number"] ?: "",
 *                 callerName   = message.data["caller_name"]  ?: "Unknown"
 *             )
 *         )
 *     }
 * }
 * ```
 */
actual class VoipPushRegistrar actual constructor(
    private val configuration: PlatformConfiguration
) {
    private val context: Context get() = configuration.context
    private val telecomManager get() = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val registrar = PhoneAccountRegistrar(context)

    private var listener: VoipPushListener? = null

    actual fun register(listener: VoipPushListener) {
        this.listener = listener
    }

    actual fun unregister() {
        listener = null
    }

    actual fun handleVoipPush(payload: VoipPushPayload) {
        val call = incomingCallFrom(payload)
        notifyIncomingCallViaSystem(payload, call)
        listener?.onIncomingCall(payload)
    }

    actual fun handleTokenRefresh(token: String) {
        listener?.onTokenRefreshed(token)
    }

    private fun incomingCallFrom(payload: VoipPushPayload) = Call(
        id = payload.callId,
        number = payload.callerNumber,
        displayName = payload.callerName,
        state = CallState.RINGING,
        createdAt = System.currentTimeMillis(),
        scheme = payload.scheme
    )

    private fun notifyIncomingCallViaSystem(payload: VoipPushPayload, call: Call) {
        val extras = Bundle().apply {
            putString("call_id", call.id)
            putString("caller_number", call.number)
            putString("caller_name", call.displayName)
            putString("scheme", call.scheme)
        }
        try {
            registrar.addIncomingVoipCall(telecomManager, extras)
        } catch (_: SecurityException) {
            // MANAGE_OWN_CALLS permission missing — listener already notified above,
            // app can still show its own in-app UI without system call screen.
        }
    }
}
