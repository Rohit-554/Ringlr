@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.jadu.ringlr.call.voip

import io.jadu.ringlr.call.Call
import io.jadu.ringlr.call.CallState
import io.jadu.ringlr.call.PlatformConfiguration
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.NSUUID
import platform.PushKit.PKPushCredentials
import platform.PushKit.PKPushPayload
import platform.PushKit.PKPushRegistry
import platform.PushKit.PKPushRegistryDelegateProtocol
import platform.PushKit.PKPushTypeVoIP
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.posix.time

/**
 * iOS implementation. Wraps PushKit so the app receives VoIP push tokens and
 * incoming call payloads without any extra FCM/APNs setup.
 *
 * PushKit wakes the app even when it is suspended, which is required for
 * CallKit to display the incoming call screen in time.
 *
 * Usage:
 * ```
 * val registrar = VoipPushRegistrar(platformConfig)
 * registrar.register(object : VoipPushListener {
 *     override fun onTokenRefreshed(token: String) { sendToServer(token) }
 *     override fun onIncomingCall(payload: VoipPushPayload) { /* optional extra handling */ }
 * })
 * ```
 *
 * [handleVoipPush] and [handleTokenRefresh] are no-ops — PushKit delivers
 * events directly to this delegate.
 */
actual class VoipPushRegistrar actual constructor(
    private val configuration: PlatformConfiguration
) : NSObject(), PKPushRegistryDelegateProtocol {

    private var pushRegistry: PKPushRegistry? = null
    private var listener: VoipPushListener? = null

    actual fun register(listener: VoipPushListener) {
        this.listener = listener
        val registry = PKPushRegistry(queue = dispatch_get_main_queue())
        registry.delegate = this
        registry.desiredPushTypes = setOf(PKPushTypeVoIP)
        pushRegistry = registry
    }

    actual fun unregister() {
        pushRegistry?.delegate = null
        pushRegistry = null
        listener = null
    }

    actual fun handleVoipPush(payload: VoipPushPayload) = Unit

    actual fun handleTokenRefresh(token: String) = Unit

    override fun pushRegistry(
        registry: PKPushRegistry,
        didUpdatePushCredentials: PKPushCredentials,
        forType: String?
    ) {
        val token = hexToken(didUpdatePushCredentials.token)
        listener?.onTokenRefreshed(token)
    }

    override fun pushRegistry(
        registry: PKPushRegistry,
        didReceiveIncomingPushWithPayload: PKPushPayload,
        forType: String?,
        withCompletionHandler: () -> Unit
    ) {
        val dict = didReceiveIncomingPushWithPayload.dictionaryPayload
        val payload = VoipPushPayload(
            callId = dict?.get("call_id") as? String ?: NSUUID().UUIDString,
            callerNumber = dict?.get("caller_number") as? String ?: "",
            callerName = dict?.get("caller_name") as? String ?: "Unknown",
            scheme = dict?.get("scheme") as? String ?: "sip"
        )
        reportToCallKit(payload)
        listener?.onIncomingCall(payload)
        withCompletionHandler()
    }

    private fun reportToCallKit(payload: VoipPushPayload) {
        val uuid = NSUUID()
        val call = Call(
            id = uuid.UUIDString,
            number = payload.callerNumber,
            displayName = payload.callerName,
            state = CallState.RINGING,
            createdAt = time(null),
            scheme = payload.scheme
        )
        configuration.systemBridge.reportIncomingCall(uuid, call)
    }

    private fun hexToken(tokenData: NSData): String {
        val bytes = tokenData.bytes?.reinterpret<ByteVar>()
        val length = tokenData.length.toInt()
        return buildString {
            for (i in 0 until length) {
                append(bytes!![i].toInt().and(0xff).toString(16).padStart(2, '0'))
            }
        }
    }
}
