package io.jadu.ringlr.call

import android.content.Context
import android.media.AudioManager
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class TelecomConnectionService : ConnectionService() {

    private val activeConnections = mutableMapOf<String, CallConnection>()
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val connection = CallConnection(
            number = request.address.schemeSpecificPart,
            displayName = request.extras?.getString("display_name") ?: ""
        )
        activeConnections[connection.connectionId] = connection
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val connection = CallConnection(
            number = request.address.schemeSpecificPart,
            displayName = request.extras?.getString("display_name") ?: ""
        )
        activeConnections[connection.connectionId] = connection
        return connection
    }

    private inner class CallConnection(
        private val number: String,
        private val displayName: String
    ) : Connection() {

        private var callStartTime: Long = 0
        private var currentState: Int = STATE_INITIALIZING
        val connectionId: String = UUID.randomUUID().toString()

        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            currentState = state
            broadcastCallState()
        }

        override fun onAnswer() {
            super.onAnswer()
            setActive()
            callStartTime = System.currentTimeMillis()
            broadcastCallState()
        }

        override fun onReject() {
            super.onReject()
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()
            detach()
        }

        override fun onAbort() {
            super.onAbort()
            setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
            destroy()
            detach()
        }

        override fun onDisconnect() {
            super.onDisconnect()
            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
            destroy()
            detach()
        }

        override fun onHold() {
            super.onHold()
            setOnHold()
            broadcastCallState()
        }

        override fun onUnhold() {
            super.onUnhold()
            setActive()
            broadcastCallState()
        }

        @Deprecated("Deprecated in framework")
        override fun onCallAudioStateChanged(state: CallAudioState) {
            super.onCallAudioStateChanged(state)
            val route = when (state.route) {
                CallAudioState.ROUTE_SPEAKER -> AudioRoute.SPEAKER
                CallAudioState.ROUTE_BLUETOOTH -> AudioRoute.BLUETOOTH
                CallAudioState.ROUTE_WIRED_HEADSET -> AudioRoute.WIRED_HEADSET
                else -> AudioRoute.EARPIECE
            }
            CoroutineScope(Dispatchers.Main).launch {
                CallStateManager.notifyAudioRouteChanged(route)
            }
        }

        private fun detach() {
            activeConnections.remove(connectionId)
            broadcastCallState()
        }

        private fun broadcastCallState() {
            CoroutineScope(Dispatchers.Main).launch {
                val call = Call(
                    id = connectionId,
                    number = number,
                    displayName = displayName,
                    state = toCallState(),
                    createdAt = callStartTime
                )
                CallStateManager.notifyCallStateChanged(call)
            }
        }

        private fun toCallState(): CallState = when (currentState) {
            STATE_INITIALIZING, STATE_DIALING -> CallState.DIALING
            STATE_RINGING -> CallState.RINGING
            STATE_ACTIVE -> CallState.ACTIVE
            STATE_HOLDING -> CallState.HOLDING
            else -> CallState.ENDED
        }
    }

    companion object {
        object CallStateManager {
            private val callbacks = mutableSetOf<CallStateCallback>()
            private val audioCallbacks = mutableSetOf<AudioRouteCallback>()

            fun registerCallback(callback: CallStateCallback) {
                callbacks.add(callback)
            }

            fun unregisterCallback(callback: CallStateCallback) {
                callbacks.remove(callback)
            }

            fun registerAudioCallback(callback: AudioRouteCallback) {
                audioCallbacks.add(callback)
            }

            fun unregisterAudioCallback(callback: AudioRouteCallback) {
                audioCallbacks.remove(callback)
            }

            internal fun notifyCallStateChanged(call: Call) {
                callbacks.forEach { it.onCallStateChanged(call) }
            }

            internal fun notifyCallFailed(call: Call, error: String) {
                callbacks.filterIsInstance<ExtendedCallStateCallback>()
                    .forEach { it.onCallFailed(call, error) }
            }

            internal fun notifyAudioRouteChanged(route: AudioRoute) {
                audioCallbacks.forEach { it.onAudioRouteChanged(route) }
            }
        }
    }
}
