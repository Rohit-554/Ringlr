package io.jadu.ringlr.call

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.net.sip.SipAudioCall
import android.net.sip.SipException
import android.net.sip.SipManager
import android.os.Build
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.TelecomManager
import android.telephony.DisconnectCause
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume

actual class PlatformConfiguration(
    val context: Context
) {

    private val registrar = PhoneAccountRegistrar(context)
    private val telecomManager: TelecomManager by lazy {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    actual fun initializeCallConfiguration() {
        registrar.registerPhoneAccount(context, telecomManager)
    }

    actual fun cleanupCallConfiguration() {
        registrar.unregisterPhoneAccount(telecomManager)
    }

    actual companion object {
        actual fun create(): PlatformConfiguration {
            return PlatformConfiguration(context = appContext)
        }

        private lateinit var appContext: Context

        fun init(context: Context) {
            appContext = context
        }
    }

    actual fun initializeCustomCallConfiguration(
        setHighlightColor: Int,
        setDescription: String,
        setSupportedUriSchemes: List<String>
    ) {
        registrar.registerCustomUiPhoneAccount(
            telecomManager, setHighlightColor, setDescription, setSupportedUriSchemes
        )
    }
}

actual class CallManager actual constructor(
    configuration: PlatformConfiguration
) : CallManagerInterface {

    private val context = configuration.context
    private val registrar = PhoneAccountRegistrar(context)
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val connections = mutableMapOf<String, Connection>()
    private val sipCalls = mutableMapOf<String, SipAudioCall>()
    private val callStateCallbacks = mutableSetOf<CallStateCallback>()

    @Suppress("DEPRECATION")
    private var activeSipManager: SipManager? = null

    @Suppress("DEPRECATION")
    private var activeSipProfile: android.net.sip.SipProfile? = null

    actual override suspend fun configureSipAccount(profile: SipProfile): CallResult<Unit> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return CallResult.Error(CallError.SipUnsupported(
                "Android dropped built-in SIP support in API 31 (Android 12) with no official replacement. " +
                "Ringlr handles the Telecom UI layer — bring your own SIP/VoIP stack (e.g. Linphone, PJSIP, WebRTC) " +
                "for signaling and media, then call startOutgoingCall() once your stack has established the session."
            ))
        }
        return buildSipAccount(profile)
    }

    @Suppress("DEPRECATION")
    private fun buildSipAccount(profile: SipProfile): CallResult<Unit> {
        if (!SipManager.isApiSupported(context)) {
            return CallResult.Error(CallError.ServiceError("SIP API not supported on this device", -1))
        }
        return try {
            val manager = SipManager.newInstance(context)
                ?: return CallResult.Error(CallError.ServiceError("Failed to create SipManager", -1))
            val localProfile = android.net.sip.SipProfile.Builder(profile.username, profile.server)
                .setPassword(profile.password)
                .setPort(profile.port)
                .setDisplayName(profile.displayName)
                .build()
            activeSipManager = manager
            activeSipProfile = localProfile
            CallResult.Success(Unit)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to configure SIP account: ${e.message}", -1))
        }
    }

    actual override suspend fun startOutgoingCall(
        number: String,
        displayName: String,
        scheme: String
    ): CallResult<Call> {
        return if (scheme == "sip") placeSipCall(number, displayName)
        else placePstnCall(number, displayName, scheme)
    }

    private suspend fun placePstnCall(
        number: String,
        displayName: String,
        scheme: String
    ): CallResult<Call> = withContext(Dispatchers.Main) {
        try {
            val extras = Bundle().apply { putString("display_name", displayName) }
            val callScheme = scheme.ifEmpty { "tel" }
            telecomManager.placeCall(Uri.fromParts(callScheme, number, null), extras)
            val call = registrar.waitForCallEstablishment(number, context)
            CallResult.Success(call)
        } catch (e: SecurityException) {
            CallResult.Error(CallError.PermissionDenied("Permission denied: ${e.message}"))
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to start call: ${e.message}", -1))
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun placeSipCall(number: String, displayName: String): CallResult<Call> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return CallResult.Error(CallError.SipUnsupported(
                "SIP calling is not available on Android 12+. See configureSipAccount() for details."
            ))
        }
        val sipManager = activeSipManager
            ?: return CallResult.Error(CallError.ServiceNotInitialized("Call configureSipAccount before placing a SIP call"))
        val localProfile = activeSipProfile
            ?: return CallResult.Error(CallError.ServiceNotInitialized("SIP account not configured"))

        val peerUri = resolvePeerUri(number, localProfile.sipDomain)
        return suspendCancellableCoroutine { continuation ->
            val callId = UUID.randomUUID().toString()
            val listener = object : SipAudioCall.Listener() {
                override fun onCallEstablished(sipCall: SipAudioCall) {
                    sipCalls[callId] = sipCall
                    val call = Call(
                        id = callId,
                        number = number,
                        displayName = displayName,
                        state = CallState.ACTIVE,
                        createdAt = System.currentTimeMillis(),
                        scheme = "sip"
                    )
                    callStateCallbacks.forEach { it.onCallAdded(call) }
                    continuation.resume(CallResult.Success(call))
                }

                override fun onError(sipCall: SipAudioCall?, errorCode: Int, errorMessage: String?) {
                    continuation.resume(CallResult.Error(
                        CallError.ServiceError(errorMessage ?: "SIP call failed", errorCode)
                    ))
                }
            }
            try {
                sipManager.makeAudioCall(localProfile.uriString, peerUri, listener, SIP_CALL_TIMEOUT_SECONDS)
            } catch (e: SipException) {
                continuation.resume(CallResult.Error(
                    CallError.ServiceError("SIP call failed: ${e.message}", -1)
                ))
            }
        }
    }

    private fun resolvePeerUri(number: String, sipDomain: String): String {
        val isSipAddress = number.contains("@")
        return if (isSipAddress) "sip:$number" else "sip:$number@$sipDomain"
    }

    actual override suspend fun endCall(callId: String): CallResult<Unit> {
        sipCalls[callId]?.let { return endSipCall(callId, it) }
        return try {
            val activeCall = connections[callId]
                ?: return CallResult.Error(CallError.CallNotFound(callId))

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

    @Suppress("DEPRECATION")
    private fun endSipCall(callId: String, sipCall: SipAudioCall): CallResult<Unit> {
        return try {
            sipCall.endCall()
            sipCalls.remove(callId)
            CallResult.Success(Unit)
        } catch (e: SipException) {
            CallResult.Error(CallError.ServiceError("Failed to end SIP call: ${e.message}", -1))
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit> {
        return try {
            val activeCall = connections[callId]
                ?: return CallResult.Error(CallError.CallNotFound(callId))

            if (!hasPermission(android.Manifest.permission.MODIFY_PHONE_STATE)) {
                return CallResult.Error(CallError.PermissionDenied("MODIFY_PHONE_STATE permission not granted"))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activeCall.onMuteStateChanged(muted)
            } else {
                val callAudioState = activeCall.callAudioState
                    ?: return CallResult.Error(CallError.AudioDeviceError("CallAudioState is null"))

                val newCallAudioState = CallAudioState(
                    muted,
                    callAudioState.route,
                    callAudioState.supportedRouteMask
                )
                activeCall.onCallAudioStateChanged(newCallAudioState)
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
            val activeCall = connections[callId]
                ?: return CallResult.Error(CallError.CallNotFound(callId))

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
            val activeCall = connections[callId]
                ?: return CallResult.Error(CallError.CallNotFound(callId))

            CallResult.Success(activeCall.toCallState())
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
                    state = connection.toCallState(),
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
                setAudioRouteModern(audioManager, route)
            } else {
                setAudioRouteLegacy(audioManager, route)
            }
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to set audio route: ${e.message}", -1))
        }
    }

    @Suppress("NewApi")
    private fun setAudioRouteModern(audioManager: AudioManager, route: AudioRoute): CallResult<Unit> {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val preferredDevice = when (route) {
            AudioRoute.SPEAKER -> devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            AudioRoute.EARPIECE -> devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
            AudioRoute.BLUETOOTH -> devices.find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
            AudioRoute.WIRED_HEADSET -> devices.find { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
        } ?: return CallResult.Error(CallError.AudioDeviceError("Device not found for route: $route"))

        return if (audioManager.setCommunicationDevice(preferredDevice)) {
            CallResult.Success(Unit)
        } else {
            CallResult.Error(CallError.AudioDeviceError("Failed to set communication device"))
        }
    }

    @Suppress("DEPRECATION")
    private fun setAudioRouteLegacy(audioManager: AudioManager, route: AudioRoute): CallResult<Unit> {
        when (route) {
            AudioRoute.SPEAKER -> audioManager.isSpeakerphoneOn = true
            AudioRoute.EARPIECE -> audioManager.isSpeakerphoneOn = false
            AudioRoute.BLUETOOTH -> audioManager.startBluetoothSco()
            AudioRoute.WIRED_HEADSET -> return CallResult.Error(
                CallError.AudioDeviceError("Manual routing to wired headset is not supported on this API level")
            )
        }
        return CallResult.Success(Unit)
    }

    actual override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute> {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val route = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resolveAudioRouteModern(audioManager)
            } else {
                resolveAudioRouteLegacy(audioManager)
            }
            CallResult.Success(route)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to get current audio route: ${e.message}", -1))
        }
    }

    @Suppress("NewApi")
    private fun resolveAudioRouteModern(audioManager: AudioManager): AudioRoute {
        return when (audioManager.communicationDevice?.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> AudioRoute.SPEAKER
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> AudioRoute.EARPIECE
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> AudioRoute.BLUETOOTH
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> AudioRoute.WIRED_HEADSET
            else -> AudioRoute.SPEAKER
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveAudioRouteLegacy(audioManager: AudioManager): AudioRoute {
        return if (audioManager.isSpeakerphoneOn) AudioRoute.SPEAKER else AudioRoute.EARPIECE
    }

    actual override suspend fun answerCall(callId: String): CallResult<Unit> {
        val ringingCall = connections[callId]
            ?: return CallResult.Error(CallError.CallNotFound(callId))
        return try {
            ringingCall.onAnswer()
            CallResult.Success(Unit)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to answer call: ${e.message}", -1))
        }
    }

    actual override suspend fun declineCall(callId: String): CallResult<Unit> {
        val ringingCall = connections[callId]
            ?: return CallResult.Error(CallError.CallNotFound(callId))
        return try {
            ringingCall.setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.REJECTED))
            ringingCall.destroy()
            connections.remove(callId)
            CallResult.Success(Unit)
        } catch (e: Exception) {
            CallResult.Error(CallError.ServiceError("Failed to decline call: ${e.message}", -1))
        }
    }

    actual override suspend fun checkPermissions(): CallResult<Unit> {
        return CallResult.Success(Unit)
    }

    actual override fun registerCallStateCallback(callback: CallStateCallback) {
        callStateCallbacks.add(callback)
    }

    actual override fun unregisterCallStateCallback(callback: CallStateCallback) {
        callStateCallbacks.remove(callback)
    }

    companion object {
        private const val SIP_CALL_TIMEOUT_SECONDS = 30
    }
}

private fun Connection.toCallState(): CallState = when (state) {
    Connection.STATE_INITIALIZING, Connection.STATE_NEW -> CallState.DIALING
    Connection.STATE_RINGING -> CallState.RINGING
    Connection.STATE_ACTIVE -> CallState.ACTIVE
    Connection.STATE_HOLDING -> CallState.HOLDING
    else -> CallState.ENDED
}
