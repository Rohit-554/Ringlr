package io.jadu.ringlr.call.custom

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

class CustomConnectionService : ConnectionService() {

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        return CustomConnection().apply {
            configureCapabilities(request, isSelfManaged = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setInitializing()
            setActive()
            setDialing()
        }
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        return CustomConnection().apply {
            configureCapabilities(request, isSelfManaged = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setRinging()
            setInitialized()
        }
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }
}

private fun Connection.configureCapabilities(request: ConnectionRequest, isSelfManaged: Boolean) {
    if (isSelfManaged) {
        connectionProperties = Connection.PROPERTY_SELF_MANAGED
        connectionCapabilities = Connection.CAPABILITY_HOLD or Connection.CAPABILITY_SUPPORT_HOLD
    } else {
        connectionCapabilities = Connection.CAPABILITY_HOLD or
                Connection.CAPABILITY_SUPPORT_HOLD or
                Connection.CAPABILITY_MUTE or
                Connection.CAPABILITY_DISCONNECT_FROM_CONFERENCE
        audioModeIsVoip = true
        request.extras?.let { setExtras(it) }
    }
    setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
}
