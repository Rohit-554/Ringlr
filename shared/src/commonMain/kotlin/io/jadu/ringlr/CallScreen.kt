package io.jadu.ringlr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.jadu.ringlr.call.AudioRoute
import io.jadu.ringlr.call.Call
import io.jadu.ringlr.call.CallManager
import io.jadu.ringlr.call.CallResult
import io.jadu.ringlr.call.CallState
import io.jadu.ringlr.call.CallStateCallback
import io.jadu.ringlr.permission.DeniedAlwaysException
import io.jadu.ringlr.permission.DeniedException
import io.jadu.ringlr.permission.Permission
import io.jadu.ringlr.permission.PermissionState
import io.jadu.ringlr.permission.PermissionsController
import io.jadu.ringlr.permission.delegate.CALL_PHONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The single screen of the Ringlr demo.
 *
 * Shows a [Dialer] when no call is active, and switches to [ActiveCallPanel]
 * once a call is in progress. Call state changes arrive via [ObserveCallState].
 */
@Composable
fun CallScreen(callManager: CallManager, permissionsController: PermissionsController) {
    val scope = rememberCoroutineScope()
    var activeCall by remember { mutableStateOf<Call?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    ObserveCallState(
        callManager = callManager,
        onCallUpdated = { call -> activeCall = call },
        onCallEnded = { activeCall = null }
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (activeCall == null) {
                Dialer(
                    errorMessage = errorMessage,
                    onCallRequested = { number ->
                        errorMessage = ""
                        scope.placeCallWithPermissionCheck(
                            number = number,
                            callManager = callManager,
                            permissionsController = permissionsController,
                            onError = { errorMessage = it }
                        )
                    }
                )
            } else {
                ActiveCallPanel(
                    call = activeCall!!,
                    callManager = callManager,
                    onCallEnded = { activeCall = null }
                )
            }
        }
    }
}

@Composable
private fun Dialer(errorMessage: String, onCallRequested: (number: String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }

    Text("Ringlr Demo", style = MaterialTheme.typography.headlineMedium)

    Spacer(Modifier.height(32.dp))

    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { phoneNumber = it },
        label = { Text("Phone number") },
        placeholder = { Text("+1 555 000 0000") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    Button(
        onClick = { onCallRequested(phoneNumber) },
        enabled = phoneNumber.isNotBlank(),
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
    ) {
        Text("Call", style = MaterialTheme.typography.titleMedium)
    }

    if (errorMessage.isNotBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ActiveCallPanel(call: Call, callManager: CallManager, onCallEnded: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isMuted by remember { mutableStateOf(false) }
    var isOnHold by remember { mutableStateOf(false) }
    var audioRoute by remember { mutableStateOf(AudioRoute.EARPIECE) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(call.displayName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = call.number,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            CallStateBadge(call.state)

            Spacer(Modifier.height(24.dp))

            CallControlRow(
                isMuted = isMuted,
                isOnHold = isOnHold,
                onMuteToggled = {
                    scope.launch {
                        val next = !isMuted
                        if (callManager.muteCall(call.id, next) is CallResult.Success) isMuted = next
                    }
                },
                onHoldToggled = {
                    scope.launch {
                        val next = !isOnHold
                        if (callManager.holdCall(call.id, next) is CallResult.Success) isOnHold = next
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            AudioRouteSelector(
                selected = audioRoute,
                onRouteSelected = { route ->
                    scope.launch {
                        if (callManager.setAudioRoute(route) is CallResult.Success) audioRoute = route
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        callManager.endCall(call.id)
                        onCallEnded()
                    }
                },
                modifier = Modifier.size(width = 160.dp, height = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("End Call", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun CallControlRow(
    isMuted: Boolean,
    isOnHold: Boolean,
    onMuteToggled: () -> Unit,
    onHoldToggled: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterChip(
            selected = isMuted,
            onClick = onMuteToggled,
            label = { Text(if (isMuted) "Unmute" else "Mute") }
        )
        FilterChip(
            selected = isOnHold,
            onClick = onHoldToggled,
            label = { Text(if (isOnHold) "Resume" else "Hold") }
        )
    }
}

@Composable
private fun AudioRouteSelector(selected: AudioRoute, onRouteSelected: (AudioRoute) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AudioRoute.entries.forEach { route ->
            FilterChip(
                selected = selected == route,
                onClick = { onRouteSelected(route) },
                label = { Text(route.label()) }
            )
        }
    }
}

@Composable
private fun CallStateBadge(state: CallState) {
    Surface(
        shape = RoundedCornerShape(50),
        color = state.badgeColor().copy(alpha = 0.15f)
    ) {
        Text(
            text = state.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = state.badgeColor()
        )
    }
}

@Composable
private fun ObserveCallState(
    callManager: CallManager,
    onCallUpdated: (Call) -> Unit,
    onCallEnded: (Call) -> Unit
) {
    DisposableEffect(callManager) {
        val callback = object : CallStateCallback {
            override fun onCallStateChanged(call: Call) {
                if (call.state == CallState.ENDED) onCallEnded(call) else onCallUpdated(call)
            }
            override fun onCallAdded(call: Call) = onCallUpdated(call)
            override fun onCallRemoved(call: Call) = onCallEnded(call)
        }
        callManager.registerCallStateCallback(callback)
        onDispose { callManager.unregisterCallStateCallback(callback) }
    }
}

private fun CoroutineScope.placeCallWithPermissionCheck(
    number: String,
    callManager: CallManager,
    permissionsController: PermissionsController,
    onError: (String) -> Unit
) {
    launch {
        try {
            when (permissionsController.getPermissionState(Permission.CALL_PHONE)) {
                PermissionState.Granted -> callManager.startOutgoingCall(number, number)
                PermissionState.DeniedAlways -> permissionsController.openAppSettings()
                else -> {
                    permissionsController.providePermission(Permission.CALL_PHONE)
                    callManager.startOutgoingCall(number, number)
                }
            }
        } catch (e: DeniedAlwaysException) {
            permissionsController.openAppSettings()
        } catch (e: DeniedException) {
            onError("Call permission is required to place a call.")
        } catch (e: Exception) {
            onError(e.message ?: "Failed to place call.")
        }
    }
}

private fun CallState.badgeColor(): Color = when (this) {
    CallState.DIALING -> Color(0xFF2196F3)
    CallState.RINGING -> Color(0xFFFF9800)
    CallState.ACTIVE  -> Color(0xFF4CAF50)
    CallState.HOLDING -> Color(0xFF9E9E9E)
    CallState.ENDED   -> Color(0xFFF44336)
}

private fun AudioRoute.label(): String = when (this) {
    AudioRoute.SPEAKER       -> "Speaker"
    AudioRoute.EARPIECE      -> "Earpiece"
    AudioRoute.BLUETOOTH     -> "BT"
    AudioRoute.WIRED_HEADSET -> "Headset"
}
