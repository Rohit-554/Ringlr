package io.jadu.ringlr.cutsomCall

import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
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
        // Show incoming call UI
       // showIncomingCallNotification()
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        // Handle audio state changes
        state?.let {
            // Update audio routing
        }
    }

  /*  private fun showIncomingCallNotification() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Incoming Calls",
            NotificationManager.IMPORTANCE_MAX
        ).apply {
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(channel)
    }*/

    companion object {
        private const val CHANNEL_ID = "incoming_calls"
    }
}