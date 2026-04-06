package io.jadu.ringlr.call.custom

import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause

class CustomConnection : Connection() {

    override fun onAnswer() {
        setActive()
    }

    override fun onReject() {
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }

    override fun onDisconnect() {
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }

    override fun onHold() {
        setOnHold()
    }

    override fun onUnhold() {
        setActive()
    }

    override fun onShowIncomingCallUi() {
    }

    @Deprecated("Deprecated in framework")
    override fun onCallAudioStateChanged(state: CallAudioState?) {
    }
}
