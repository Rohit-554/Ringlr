package io.jadu.ringlr.call

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import io.jadu.ringlr.call.custom.CustomConnectionService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume

class PhoneAccountRegistrar(private val context: Context) {

    private val defaultAccountHandle: PhoneAccountHandle by lazy {
        PhoneAccountHandle(
            ComponentName(context, TelecomConnectionService::class.java),
            CALL_SDK_ID
        )
    }

    private val customAccountHandle: PhoneAccountHandle by lazy {
        PhoneAccountHandle(
            ComponentName(context, CustomConnectionService::class.java),
            CUSTOM_ACCOUNT_ID
        )
    }

    internal fun registerPhoneAccount(context: Context, telecomManager: TelecomManager) {
        val phoneAccount = PhoneAccount.builder(defaultAccountHandle, CALL_SDK_ID)
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    internal fun registerCustomUiPhoneAccount(
        telecomManager: TelecomManager,
        highlightColor: Int,
        description: String,
        supportedUriSchemes: List<String>
    ) {
        val phoneAccount = PhoneAccount.builder(customAccountHandle, CUSTOM_ACCOUNT_LABEL)
            .setCapabilities(buildCustomCapabilities())
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setHighlightColor(highlightColor)
                    setShortDescription(description)
                    setSupportedUriSchemes(supportedUriSchemes)
                }
            }
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    private fun buildCustomCapabilities(): Int {
        var capabilities = PhoneAccount.CAPABILITY_CALL_PROVIDER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            capabilities = capabilities or
                    PhoneAccount.CAPABILITY_SELF_MANAGED or
                    PhoneAccount.CAPABILITY_VIDEO_CALLING or
                    PhoneAccount.CAPABILITY_SUPPORTS_VIDEO_CALLING
        }
        return capabilities
    }

    internal fun unregisterPhoneAccount(telecomManager: TelecomManager) {
        telecomManager.unregisterPhoneAccount(defaultAccountHandle)
        telecomManager.unregisterPhoneAccount(customAccountHandle)
    }

    suspend fun waitForCallEstablishment(number: String, context: Context): Call {
        return withTimeoutOrNull(CALL_ESTABLISHMENT_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                val callback = registerCallStateListener(telephonyManager, number, continuation)

                continuation.invokeOnCancellation {
                    unregisterCallStateListener(telephonyManager, callback)
                }
            }
        } ?: throw TimeoutException(
            "Call establishment timeout after ${CALL_ESTABLISHMENT_TIMEOUT_MS / 1000} seconds"
        )
    }

    private fun registerCallStateListener(
        telephonyManager: TelephonyManager,
        number: String,
        continuation: CancellableContinuation<Call>
    ): Any {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    resolveCallState(state, number, continuation)
                }
            }
            telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
            callback
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    resolveCallState(state, number, continuation)
                }
            }
            @Suppress("DEPRECATION")
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
            listener
        }
    }

    private fun unregisterCallStateListener(telephonyManager: TelephonyManager, callback: Any) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(callback as TelephonyCallback)
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(callback as PhoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private fun resolveCallState(
        state: Int,
        number: String,
        continuation: CancellableContinuation<Call>
    ) {
        if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            val call = Call(
                id = UUID.randomUUID().toString(),
                number = number,
                displayName = "",
                state = CallState.ACTIVE,
                createdAt = System.currentTimeMillis()
            )
            continuation.resume(call)
        }
    }

    companion object {
        private const val CALL_SDK_ID = "CallSDK"
        private const val CUSTOM_ACCOUNT_ID = "CustomCallSDK"
        private const val CUSTOM_ACCOUNT_LABEL = "Custom Calling App"
        private const val CALL_ESTABLISHMENT_TIMEOUT_MS = 30_000L
    }
}
