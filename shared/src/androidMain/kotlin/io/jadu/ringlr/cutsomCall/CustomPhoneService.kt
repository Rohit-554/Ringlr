package io.jadu.ringlr.cutsomCall

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

class CustomPhoneService : ConnectionService() {

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        return CustomConnection().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Set connection properties for Android O and above
                connectionProperties = Connection.PROPERTY_SELF_MANAGED
                connectionCapabilities = Connection.CAPABILITY_HOLD or
                        Connection.CAPABILITY_SUPPORT_HOLD
                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                setInitializing()
                setActive()
            } else {
                // Set connection properties for pre-Android O
                connectionCapabilities = Connection.CAPABILITY_HOLD or
                        Connection.CAPABILITY_SUPPORT_HOLD or
                        Connection.CAPABILITY_MUTE or
                        Connection.CAPABILITY_DISCONNECT_FROM_CONFERENCE

                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                audioModeIsVoip = true
                setInitializing()

                // Optional: Set outgoing call extras if available
                request.extras?.let { extras ->
                    setExtras(extras)
                }
                setActive()
            }
            setDialing()
        }
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        return CustomConnection().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Set connection properties for Android O and above
                connectionProperties = Connection.PROPERTY_SELF_MANAGED
                connectionCapabilities = Connection.CAPABILITY_HOLD or Connection.CAPABILITY_SUPPORT_HOLD
                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                setRinging()
            } else {
                connectionCapabilities = Connection.CAPABILITY_HOLD or
                        Connection.CAPABILITY_SUPPORT_HOLD or
                        Connection.CAPABILITY_MUTE or
                        Connection.CAPABILITY_DISCONNECT_FROM_CONFERENCE

                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                audioModeIsVoip = true
                setRinging()
            }
            setInitialized()
        }
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        // Handle incoming call failure
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        // Handle outgoing call failure
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

}

fun checkSDKVersion(sdkVersion: Int, callback: (Boolean) -> Unit) {
    val isSupported = sdkVersion >= android.os.Build.VERSION_CODES.LOLLIPOP
    callback(isSupported)
}

