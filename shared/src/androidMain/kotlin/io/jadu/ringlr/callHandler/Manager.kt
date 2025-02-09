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
import io.jadu.ringlr.cutsomCall.CustomPhoneService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume

class Manager(val context:Context) {

    companion object {
        private const val ACCOUNT_ID = "CustomCallSDK"
        private const val ACCOUNT_LABEL = "Custom Calling App"
        private const val CALLSDK = "CallSDK"
    }


    private val defaultPhoneAccountHandle: PhoneAccountHandle by lazy {
        PhoneAccountHandle(
            ComponentName(context, CallConnectionService::class.java),
            "CallSDK"
        )
    }

    private val customPhoneAccountHandle: PhoneAccountHandle by lazy {
        PhoneAccountHandle(
            ComponentName(context, CustomPhoneService::class.java),
            ACCOUNT_ID
        )
    }

    internal fun registerPhoneAccount(context: Context, telecomManager: TelecomManager) {
        // Implementation for registering phone account
        val componentName = ComponentName(context, CallConnectionService::class.java)
      //  val phoneAccountHandle = PhoneAccountHandle(componentName, "CallSDK")

        val phoneAccount = PhoneAccount.builder(defaultPhoneAccountHandle, CALLSDK)
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    internal fun registerCustomUiPhoneAccount(telecomManager: TelecomManager, setHighlightColor:Int, setDescription:String, setSupportedUriSchemes:List<String>) {
        /*val componentName = ComponentName(context, CustomPhoneService::class.java)
        val phoneAccountHandle = PhoneAccountHandle(componentName, "CustomCallSDK")*/

        val phoneAccount = PhoneAccount.builder(customPhoneAccountHandle, ACCOUNT_LABEL)
            .setCapabilities(
                PhoneAccount.CAPABILITY_CALL_PROVIDER or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            PhoneAccount.CAPABILITY_SELF_MANAGED or
                                    PhoneAccount.CAPABILITY_VIDEO_CALLING or
                                    PhoneAccount.CAPABILITY_SUPPORTS_VIDEO_CALLING
                        } else 0
            )
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setHighlightColor(setHighlightColor)
                    setShortDescription(setDescription)
                    setSupportedUriSchemes(setSupportedUriSchemes)
                }
            }
            .build()

        try {
            telecomManager.registerPhoneAccount(phoneAccount)
        } catch (e: SecurityException) {
            // Handle permission issues
        }
    }

    internal fun unregisterPhoneAccount(telecomManager: TelecomManager) {
        try {
            telecomManager.unregisterPhoneAccount(defaultPhoneAccountHandle)
            telecomManager.unregisterPhoneAccount(customPhoneAccountHandle)
        }catch (e: SecurityException) {
            throw SecurityException("Permission denied")
        }
    }

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    // Helper function to wait for call establishment
    suspend fun waitForCallEstablishment(number: String, context: Context): Call {
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
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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
                        telephonyManager.listen(
                            callback as PhoneStateListener,
                            PhoneStateListener.LISTEN_NONE
                        )
                    }
                }
            }
        }
            ?: throw TimeoutException("Call establishment timeout after ${CALL_ESTABLISHMENT_TIMEOUT / 1000} seconds")
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