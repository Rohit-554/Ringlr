package io.jadu.ringlr.callHandler

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import io.jadu.ringlr.configs.Call
import io.jadu.ringlr.configs.CallState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume

class Manager {

    fun registerPhoneAccount(context: Context, telecomManager: TelecomManager) {
        // Implementation for registering phone account
        val componentName = ComponentName(context, CallConnectionService::class.java)
        val phoneAccountHandle = PhoneAccountHandle(componentName, "CallSDK")

        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "CallSDK")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    fun unregisterPhoneAccount() {
        // Cleanup implementation
    }

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    // Helper function to wait for call establishment
     suspend fun waitForCallEstablishment(number: String,context: Context): Call {
        return withTimeoutOrNull(CALL_ESTABLISHMENT_TIMEOUT) {
            suspendCancellableCoroutine { continuation ->
                // Store the last known state and number
                var lastKnownNumber: String? = null

                // Use appropriate callback based on API level
                val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                        override fun onCallStateChanged(state: Int) {
                            handleCallStateChange(state, number, continuation)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    object : PhoneStateListener() {
                        @Deprecated("Deprecated in Java")
                        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                            handleCallStateChange(state, number, continuation)
                        }
                    }
                }

                // Register callback based on API level
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    telephonyManager.registerTelephonyCallback(
                        context.mainExecutor,
                        callback as TelephonyCallback
                    )
                } else {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(
                        callback as PhoneStateListener,
                        PhoneStateListener.LISTEN_CALL_STATE
                    )
                }

                // Clean up when cancelled
                continuation.invokeOnCancellation {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        telephonyManager.unregisterTelephonyCallback(callback as TelephonyCallback)
                    } else {
                        @Suppress("DEPRECATION")
                        telephonyManager.listen(callback as PhoneStateListener, PhoneStateListener.LISTEN_NONE)
                    }
                }
            }
        } ?: throw TimeoutException("Call establishment timeout after ${CALL_ESTABLISHMENT_TIMEOUT/1000} seconds")
    }

    private fun handleCallStateChange(
        state: Int,
        number: String,
        continuation: CancellableContinuation<Call>
    ) {
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                val call = Call(
                    id = UUID.randomUUID().toString(),
                    number = number,
                    displayName = "",
                    state = CallState.ACTIVE,
                    createdAt = System.currentTimeMillis()
                )
                continuation.resume(call)
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended or failed
            }
        }
    }
}