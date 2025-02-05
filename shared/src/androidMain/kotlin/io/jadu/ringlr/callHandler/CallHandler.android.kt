package io.jadu.ringlr.callHandler

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.TelecomManager
import android.telephony.DisconnectCause
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import io.jadu.ringlr.configs.AudioRoute
import io.jadu.ringlr.configs.Call
import io.jadu.ringlr.configs.CallError
import io.jadu.ringlr.configs.CallManager
import io.jadu.ringlr.configs.CallResult
import io.jadu.ringlr.configs.CallState
import io.jadu.ringlr.configs.CallStateCallback
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume

/**
 * Expected platform-specific configuration class that holds essential platform settings.
 * Android: Will contain Context and necessary Android-specific configurations
 * iOS: Will contain CallKit and AVAudioSession configurations
 */
actual class PlatformConfiguration(
    val context:Context
) {

    private val telecomManager: TelecomManager by lazy {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    actual fun initialize() {
        Manager().registerPhoneAccount(context,telecomManager)
    }

    actual fun cleanup() {
        Manager().unregisterPhoneAccount()
    }

}

/**
 * Expected CallManager implementation that bridges to platform-specific calling APIs.
 * Android: Implements using Telecom framework
 * iOS: Implements using CallKit
 */
actual class CallManagerImpl actual constructor(
    configuration: PlatformConfiguration
): CallManager {
    private val manager = Manager();
    private val context = (configuration as PlatformConfiguration).context
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val connections = mutableMapOf<String, Connection>()
    private val callStateCallbacks = mutableSetOf<CallStateCallback>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    actual override suspend fun startOutgoingCall(
        number: String,
        displayName: String
    ): CallResult<Call> = withContext(Dispatchers.Main){
        try {
            when(val permissionResult = checkPermissions()){
                is CallResult.Error -> return@withContext permissionResult
                else -> {
                    // Implementation for starting outgoing call
                    CallResult.Success(Call(1.toString(),number,displayName,CallState.DIALING,manager.getCurrentTime()))
                }
            }

            val outgoingCallExtras = Bundle().apply {
                putString("display_name", displayName)
            }

            telecomManager.placeCall(
                Uri.fromParts("tel", number, null),
                outgoingCallExtras
            )

            val call = manager.waitForCallEstablishment(number,context)
            CallResult.Success(call)
        } catch (e: SecurityException) {
            CallResult.Error(CallError.PermissionDenied("Permission denied: ${e.message}"))
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to start call: ${e.message}", -1))
        }
    }


    actual override suspend fun endCall(callId: String): CallResult<Unit> {
        return try {
            val activeCall = connections[callId] ?: throw IllegalStateException("No active call found with ID: $callId")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (!hasPermission(android.Manifest.permission.ANSWER_PHONE_CALLS)) {
                    return CallResult.Error(CallError.PermissionDenied("ANSWER_PHONE_CALLS permission not granted"))
                }
            }
            activeCall.setDisconnected(android.telecom.DisconnectCause(DisconnectCause.LOCAL))
            activeCall.destroy()
            connections.remove(callId)
            CallResult.Success(Unit)
        } catch (e: SecurityException) {
            CallResult.Error(CallError.PermissionDenied(e.message ?: "Permission denied"))
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to end call: ${e.message}", -1))
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit> {
        return try {
            val activeCall = connections[callId] ?: throw IllegalStateException("No active call found with ID: $callId")

            if (!hasPermission(android.Manifest.permission.MODIFY_PHONE_STATE)) {
                return CallResult.Error(CallError.PermissionDenied("MODIFY_PHONE_STATE permission not granted"))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // For API level 34 and above
                activeCall.onMuteStateChanged(muted)
            } else {
                // For API levels below 34
                val callAudioState = activeCall.callAudioState
                if (callAudioState != null) {
                    val newCallAudioState = CallAudioState(
                        muted,
                        callAudioState.route,
                        callAudioState.supportedRouteMask
                    )
                    activeCall.onCallAudioStateChanged(newCallAudioState)
                } else {
                    throw IllegalStateException("CallAudioState is null")
                }
            }
            CallResult.Success(Unit)
        } catch (e: SecurityException) {
            CallResult.Error(CallError.PermissionDenied(e.message ?: "Permission denied"))
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to mute call: ${e.message}", -1))
        }
    }



    actual override suspend fun holdCall(
        callId: String,
        onHold: Boolean
    ): CallResult<Unit> {
        return try {
            val activeCall = connections[callId] ?: throw IllegalStateException("No active call found with ID: $callId")

            if (!hasPermission(android.Manifest.permission.MODIFY_PHONE_STATE)) {
                return CallResult.Error(CallError.PermissionDenied("MODIFY_PHONE_STATE permission not granted"))
            }

            if (onHold) {
                activeCall.onHold()
            } else {
                activeCall.onUnhold()
            }
            CallResult.Success(Unit)
        } catch (e: SecurityException) {
            CallResult.Error(CallError.PermissionDenied(e.message ?: "Permission denied"))
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to hold call: ${e.message}", -1))
        }
    }

    actual override suspend fun getCallState(callId: String): CallResult<CallState> {
        return try {
            val activeCall = connections[callId] ?: throw IllegalStateException("No active call found with ID: $callId")

            val state = when (activeCall.state) {
                Connection.STATE_INITIALIZING -> CallState.DIALING
                Connection.STATE_NEW -> CallState.DIALING
                Connection.STATE_RINGING -> CallState.RINGING
                Connection.STATE_ACTIVE -> CallState.ACTIVE
                Connection.STATE_HOLDING -> CallState.HOLDING
                Connection.STATE_DISCONNECTED -> CallState.ENDED
                else -> CallState.ENDED
            }
            CallResult.Success(state)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to get call state: ${e.message}", -1))
        }
    }

    actual override suspend fun getActiveCalls(): CallResult<List<Call>> {
        return try {
            val activeCalls = connections.map { (id, connection) ->
                Call(
                    id = id,
                    number = connection.address.schemeSpecificPart,
                    displayName = connection.extras.getString("display_name") ?: "",
                    state = when (connection.state) {
                        Connection.STATE_INITIALIZING -> CallState.DIALING
                        Connection.STATE_NEW -> CallState.DIALING
                        Connection.STATE_RINGING -> CallState.RINGING
                        Connection.STATE_ACTIVE -> CallState.ACTIVE
                        Connection.STATE_HOLDING -> CallState.HOLDING
                        Connection.STATE_DISCONNECTED -> CallState.ENDED
                        else -> CallState.ENDED
                    },
                    createdAt = 0L
                )
            }
            CallResult.Success(activeCalls)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to get active calls: ${e.message}", -1))
        }
    }

    actual override suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit> {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For API level 31 and above
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                val preferredDevice = when (route) {
                    AudioRoute.SPEAKER -> devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                    AudioRoute.EARPIECE -> devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                    AudioRoute.BLUETOOTH -> devices.find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
                    AudioRoute.WIRED_HEADSET -> devices.find { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
                }

                if (preferredDevice != null) {
                    val success = audioManager.setCommunicationDevice(preferredDevice)
                    if (success) {
                        CallResult.Success(Unit)
                    } else {
                        CallResult.Error(CallError.ServiceError("Failed to set communication device", -1))
                    }
                } else {
                    CallResult.Error(CallError.ServiceError("Preferred device not found for the selected route", -1))
                }
            } else {
                when (route) {
                    AudioRoute.SPEAKER -> audioManager.isSpeakerphoneOn = true
                    AudioRoute.EARPIECE -> audioManager.isSpeakerphoneOn = false
                    AudioRoute.BLUETOOTH -> audioManager.startBluetoothSco()
                    AudioRoute.WIRED_HEADSET -> {
                        // No direct method to switch to wired headset; usually handled automatically
                        CallResult.Error(CallError.ServiceError("Manual routing to wired headset is not supported on this API level", -1))
                    }
                }
                CallResult.Success(Unit)
            }
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to set audio route: ${e.message}", -1))
        }
    }



    actual override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute> {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val route = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For API level 31 and above
                val communicationDevice = audioManager.communicationDevice
                when (communicationDevice?.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> AudioRoute.SPEAKER
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> AudioRoute.EARPIECE
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> AudioRoute.BLUETOOTH
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> AudioRoute.WIRED_HEADSET
                    else -> AudioRoute.SPEAKER
                }
            } else {
                // For API levels below 31
                if (audioManager.isSpeakerphoneOn) {
                    AudioRoute.SPEAKER
                } else {
                    AudioRoute.EARPIECE
                }
            }
            CallResult.Success(route)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to get current audio route: ${e.message}", -1))
        }
    }

    actual override suspend fun checkPermissions(): CallResult<Unit> {
        val permission = android.Manifest.permission.CALL_PHONE
        return try {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                CallResult.Success(Unit)
            } else {
                CallResult.Error(CallError.PermissionDenied("Permission denied: $permission"))
            }
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to check permissions: ${e.message}", -1))
        }
    }

    actual override fun registerCallStateCallback(callback: CallStateCallback) {
        callStateCallbacks.add(callback)
    }

    actual override fun unregisterCallStateCallback(callback: CallStateCallback) {
        callStateCallbacks.remove(callback)
    }

}