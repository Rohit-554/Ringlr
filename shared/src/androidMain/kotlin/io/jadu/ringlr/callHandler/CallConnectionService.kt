package io.jadu.ringlr.callHandler

import android.content.Context
import android.media.AudioManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import io.jadu.ringlr.configs.AudioRoute
import io.jadu.ringlr.configs.AudioRouteCallback
import io.jadu.ringlr.configs.CallState
import io.jadu.ringlr.configs.CallStateCallback
import io.jadu.ringlr.configs.ExtendedCallStateCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * CallConnectionService serves as the bridge between our app and Android's Telecom framework.
 * This service handles the lifecycle of phone calls, managing their states and responding to
 * user actions like muting, holding, or disconnecting calls.
 */
class CallConnectionService : ConnectionService() {

    // Store active connections for managing multiple calls
    private val activeConnections = mutableMapOf<String, CallConnection>()
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }


    /**
     * Called by the system when a new outgoing call needs to be created.
     * This is our entry point for setting up a new outgoing call.
     */
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val connection = CallConnection(
            context = applicationContext,
            number = request.address.schemeSpecificPart,
            displayName = request.extras?.getString("display_name") ?: ""
        )
        activeConnections[connection.connectionId] = connection
        return connection
    }

    /**
     * Inner class that represents a single call connection.
     * Handles the state and capabilities of an individual call.
     */
    private inner class CallConnection(
        private val context: Context,
        private val number: String,
        private val displayName: String
    ) : Connection() {
        private var callStartTime: Long = 0
        private var currentState: Int = STATE_INITIALIZING
        val connectionId: String = UUID.randomUUID().toString()

        init {

        }

        private fun setCallCapabilities() {
            connectionCapabilities = (
                    CAPABILITY_MUTE or
                            CAPABILITY_SUPPORT_HOLD or
                            CAPABILITY_HOLD or
                            CAPABILITY_RESPOND_VIA_TEXT
                    )
        }

        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            currentState = state
            notifyCallStateChanged()
        }

        override fun onAnswer() {
            super.onAnswer()
            setActive()
            callStartTime = System.currentTimeMillis()
            notifyCallStateChanged()
        }

        override fun onReject() {
            super.onReject()
            setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
            destroy()
            removeConnection()
        }

        override fun onAbort() {
            super.onAbort()
            setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
            destroy()
            removeConnection()
        }

        override fun onDisconnect() {
            super.onDisconnect()
            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
            destroy()
            removeConnection()
        }

        override fun onHold() {
            super.onHold()
            setOnHold()
            notifyCallStateChanged()
        }

        override fun onUnhold() {
            super.onUnhold()
            setActive()
            notifyCallStateChanged()
        }

        override fun onCallAudioStateChanged(state: CallAudioState) {
            super.onCallAudioStateChanged(state)
            handleAudioStateChanged(state)
        }


        private fun handleAudioStateChanged(state: CallAudioState) {
            val route = when (state.route) {
                CallAudioState.ROUTE_SPEAKER -> AudioRoute.SPEAKER
                CallAudioState.ROUTE_BLUETOOTH -> AudioRoute.BLUETOOTH
                CallAudioState.ROUTE_WIRED_HEADSET -> AudioRoute.WIRED_HEADSET
                else -> AudioRoute.EARPIECE
            }
            notifyAudioRouteChanged(route)
        }

        private fun removeConnection() {
            activeConnections.remove(connectionId)
            notifyCallStateChanged()
        }


        private fun notifyCallStateChanged() {
            CoroutineScope(Dispatchers.Main).launch {
                val state = when (currentState){
                    STATE_INITIALIZING -> CallState.DIALING
                    STATE_DIALING -> CallState.DIALING
                    STATE_RINGING -> CallState.RINGING
                    STATE_ACTIVE -> CallState.ACTIVE
                    STATE_HOLDING -> CallState.HOLDING
                    STATE_DISCONNECTED -> CallState.ENDED
                    else -> CallState.ENDED
                }

                val call = io.jadu.ringlr.configs.Call(
                    id = connectionId,
                    number = number,
                    displayName = displayName,
                    state = state,
                    createdAt = callStartTime
                )



                CallStateManager.notifyCallStateChanged(call)
            }
        }

        private fun notifyCallFailed(number: String, errorMessage: String) {
            CoroutineScope(Dispatchers.Main).launch {
                val call = io.jadu.ringlr.configs.Call(
                    id = UUID.randomUUID().toString(),
                    number = number,
                    displayName = "",
                    state = CallState.ENDED,
                    createdAt = System.currentTimeMillis()
                )
                CallStateManager.notifyCallFailed(call, errorMessage)
            }
        }

        private fun notifyAudioRouteChanged(route: AudioRoute) {
            CoroutineScope(Dispatchers.Main).launch {
                CallStateManager.notifyAudioRouteChanged(route)
            }
        }



    }

    /**
     * Called by the system when a new incoming call is received.
     * Handles setup of incoming calls.
     */
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val connection = CallConnection(
            context = applicationContext,
            number = request.address.schemeSpecificPart,
            displayName = request.extras?.getString("display_name") ?: ""
        )

        activeConnections[connection.connectionId] = connection
        return connection
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

            internal fun notifyCallStateChanged(call: io.jadu.ringlr.configs.Call) {
                callbacks.forEach { it.onCallStateChanged(call) }
            }

            internal fun notifyCallFailed(call: io.jadu.ringlr.configs.Call, error: String) {
                callbacks.forEach {
                    if (it is ExtendedCallStateCallback) {
                        it.onCallFailed(call, error)
                    }
                }
            }

            internal fun notifyAudioRouteChanged(route: AudioRoute) {
                audioCallbacks.forEach { it.onAudioRouteChanged(route) }
            }
        }
    }
}