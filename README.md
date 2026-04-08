# Ringlr

Ringlr is a Kotlin Multiplatform Mobile (KMM) library for handling phone calls and audio configuration across Android and iOS. It provides a single unified API on top of Android's Telecom framework and iOS's CallKit.

![ringlr_](https://github.com/user-attachments/assets/65f924e5-8f70-4bd1-87a5-a99eac86eae7)

## Contents
- [Features](#features)
- [Upcoming Changes](#upcoming-changes)
- [Contribution Guidelines](https://github.com/Rohit-554/Ringlr/blob/master/Contributing.md)
- [Permissions](#permissions)
- [Implementation Guide](#implementation-guide)
  - [Local Library Integration](#1-local-library-integration)
  - [Initialize the Library](#2-initialize-the-library)
  - [Permission Handling](#3-permission-handling)
  - [Making Calls](#4-making-calls)
  - [Complete App.kt Example](#complete-appkt-example)
- [CallManager API Reference](#callmanager-api-reference)
- [Project Structure](#project-structure)
- [Important Notes](#important-notes)
- [Troubleshooting](#troubleshooting)
- [Building the Project](#building-the-project)

---

## Features

- Make and manage outgoing phone calls
- Answer incoming calls (via system UI)
- Mute, hold, and end active calls
- Route audio to speaker or earpiece; iOS auto-routes to connected Bluetooth/wired headset
- Request and check call and microphone permissions
- Real-time call state callbacks
- Android Telecom framework integration
- iOS CallKit integration

---

## VoIP / SIP Support

Ringlr provides the **UI and call lifecycle layer** for VoIP/SIP calls — it does not bundle a SIP stack or media engine.

### How it works

| Layer | Ringlr's role | Your responsibility |
|---|---|---|
| System call UI (CallKit / Telecom) | Handled by Ringlr | — |
| Call state (dialing, active, hold, end) | Handled by Ringlr | — |
| SIP signaling (REGISTER, INVITE, BYE) | Not included | Your SIP stack |
| Media / audio (RTP, codecs, SRTP) | Not included | Your SIP stack |

### Android

Android removed its built-in `SipManager` API in **Android 12 (API 31)** with no official replacement.

On **API ≤ 30**, Ringlr uses the system `SipManager` automatically after `configureSipAccount()`.

On **API 31+**, `configureSipAccount()` returns `CallError.SipUnsupported`. The recommended flow:

1. Establish your session with a SIP/VoIP library (Linphone, PJSIP, WebRTC, etc.)
2. Once connected, call `startOutgoingCall(number, displayName, scheme = "sip")` so Ringlr registers the call with the Telecom framework and shows system UI

### iOS

CallKit is transport-agnostic — it shows the system call UI regardless of the underlying protocol. `configureSipAccount()` on iOS stores the profile for your use; CallKit handles the UI automatically via `startOutgoingCall(scheme = "sip")`.

### VoIP vs SIP

- **VoIP** — any voice call over the internet (WhatsApp, FaceTime, Zoom are all VoIP)
- **SIP** — one open protocol for VoIP (what `scheme = "sip"` targets)
- **WebRTC** — another common VoIP protocol; not covered by the SIP scheme

---

## VoIP Push Notifications

VoIP push wakes the app for an incoming call even when it is suspended. Without it, iOS will not display the incoming call screen reliably.

### iOS (PushKit — automatic)

```kotlin
val registrar = VoipPushRegistrar(platformConfig)
registrar.register(object : VoipPushListener {
    override fun onTokenRefreshed(token: String) {
        // Send token to your push server
    }
    override fun onIncomingCall(payload: VoipPushPayload) {
        // CallKit system UI is already shown by Ringlr.
        // Use payload for your own in-app handling if needed.
    }
})
```

PushKit delivers the push directly to the delegate. Ringlr calls `reportIncomingCall` on CallKit automatically — no extra steps needed.

Expected push payload keys from your server:

| Key | Value |
|---|---|
| `call_id` | Unique call identifier |
| `caller_number` | Phone number or SIP address |
| `caller_name` | Display name |
| `scheme` | `"sip"` or `"tel"` |

### Android (FCM — app bridges to Ringlr)

```kotlin
// In Application.onCreate
val registrar = VoipPushRegistrar(platformConfig)
registrar.register(object : VoipPushListener {
    override fun onTokenRefreshed(token: String) { sendToServer(token) }
    override fun onIncomingCall(payload: VoipPushPayload) { /* show in-app UI */ }
})

// In your FirebaseMessagingService
override fun onNewToken(token: String) {
    registrar.handleTokenRefresh(token)
}

override fun onMessageReceived(message: RemoteMessage) {
    if (message.data["type"] == "voip") {
        registrar.handleVoipPush(
            VoipPushPayload(
                callId       = message.data["call_id"]       ?: "",
                callerNumber = message.data["caller_number"] ?: "",
                callerName   = message.data["caller_name"]   ?: "Unknown",
                scheme       = message.data["scheme"]        ?: "sip"
            )
        )
    }
}
```

Ringlr notifies `onIncomingCall` and registers the call with the Telecom framework for the system call log. Add `MANAGE_OWN_CALLS` to your manifest for the system call screen to appear.

---

## Upcoming Changes

- ✨ Custom in-app calling UI
- ✨ Publishing on Maven Central

---

## Permissions

### Android

Declare these in your `AndroidManifest.xml`:

| Permission | Purpose |
|---|---|
| `ANSWER_PHONE_CALLS` | Answer incoming calls programmatically |
| `CALL_PHONE` | Place outgoing calls |
| `READ_PHONE_STATE` | Read active call state |
| `MANAGE_OWN_CALLS` | Register a custom phone account |
| `READ_PHONE_NUMBERS` | Read the device's phone number |
| `MODIFY_AUDIO_SETTINGS` | Switch audio routes |
| `RECORD_AUDIO` | Microphone access during calls |
| `BLUETOOTH` / `BLUETOOTH_CONNECT` | Bluetooth headset support |

### iOS

Add these keys to your `Info.plist`:

| Key | Purpose |
|---|---|
| `NSMicrophoneUsageDescription` | Microphone access during calls |
| `NSBluetoothAlwaysUsageDescription` | Bluetooth headset support |

iOS call access (CallKit) is managed by the system — no runtime `CALL_PHONE` equivalent is required.

---

## CallManager API Reference

`CallManager` is the single entry point for all call operations.

```kotlin
expect class CallManager(configuration: PlatformConfiguration) : CallManagerInterface
```

### Outgoing Calls

```kotlin
suspend fun startOutgoingCall(
    number: String,
    displayName: String,
    scheme: String = "tel"   // "tel" for PSTN, "sip" for VoIP
): CallResult<Call>

suspend fun endCall(callId: String): CallResult<Unit>
```

### Call State Control

```kotlin
suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>
suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>
```

### Call Information

```kotlin
suspend fun getCallState(callId: String): CallResult<CallState>
suspend fun getActiveCalls(): CallResult<List<Call>>
```

### Audio Route Management

```kotlin
suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>
suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>
```

`AudioRoute` values: `SPEAKER`, `EARPIECE`, `BLUETOOTH`, `WIRED_HEADSET`

> **iOS note:** `SPEAKER` and `EARPIECE` are controlled via `AVAudioSession` port override. For `BLUETOOTH` and `WIRED_HEADSET`, iOS automatically routes to any connected device when the speaker override is removed — no additional API call is required. Use `AVRoutePickerView` for fine-grained Bluetooth device selection.

### Callbacks

```kotlin
fun registerCallStateCallback(callback: CallStateCallback)
fun unregisterCallStateCallback(callback: CallStateCallback)
```

`CallStateCallback` events: `onCallStateChanged`, `onCallAdded`, `onCallRemoved`

### CallResult

Every operation returns `CallResult<T>` — either `Success(data)` or `Error(callError)`. No exceptions leak through the API boundary.

```kotlin
when (val result = callManager.startOutgoingCall("+1234567890", "John Doe")) {
    is CallResult.Success -> println("Call started: ${result.data.id}")
    is CallResult.Error   -> println("Failed: ${result.error}")
}
```

---

## Project Structure

```
shared/
└── src/
    ├── commonMain/          # Shared interfaces, models, expect declarations
    │   └── call/
    │       ├── CallHandler.kt          # expect PlatformConfiguration + CallManager
    │       ├── CallManagerInterface.kt
    │       ├── Call.kt
    │       ├── CallState.kt
    │       ├── CallResult.kt
    │       ├── CallError.kt
    │       ├── AudioRoute.kt
    │       └── CallStateCallback.kt
    ├── androidMain/         # Android Telecom implementation
    │   └── call/
    │       ├── CallHandler.android.kt
    │       ├── TelecomConnectionService.kt
    │       └── PhoneAccountRegistrar.kt
    └── iosMain/             # iOS CallKit implementation
        └── call/
            ├── CallHandler.ios.kt
            └── callkit/
                ├── ActiveCallRegistry.kt      # Tracks Call ↔ NSUUID
                ├── SystemCallBridge.kt        # CXProviderDelegate
                ├── CallActionDispatcher.kt    # Submits CXTransactions
                └── CallAudioRouter.kt         # AVAudioSession speaker/earpiece override; iOS routes Bluetooth/wired automatically
```

---

## Implementation Guide

### 1. Local Library Integration

Ringlr is available as a local Gradle module.

1. Clone the repository:
```bash
git clone https://github.com/Rohit-554/Ringlr.git
```

2. Copy the `shared` directory into your project root.

3. Add to `settings.gradle.kts`:
```kotlin
include(":shared")
```

4. Add the dependency in your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":shared"))
}
```

5. Sync Gradle.

---

### 2. Initialize the Library

#### Android

Create an `Application` class and call `PlatformConfiguration.init`:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformConfiguration.init(this)
    }
}
```

Register it in `AndroidManifest.xml` along with the `ConnectionService`:

```xml
<application android:name=".MyApplication">
    <service
        android:name=".ringlr.callHandler.CallConnectionService"
        android:permission="android.permission.BIND_TELECOM_CONNECTION_SERVICE"
        android:exported="true">
        <intent-filter>
            <action android:name="android.telecom.ConnectionService" />
        </intent-filter>
    </service>
</application>
```

#### iOS

No `Application` subclass is needed. Create and initialise `PlatformConfiguration` directly in your Compose entry point:

```kotlin
val platformConfig = PlatformConfiguration.create()
platformConfig.initializeCallConfiguration()
```

Make sure `NSMicrophoneUsageDescription` and `NSBluetoothAlwaysUsageDescription` are set in `Info.plist`.

---

### 3. Permission Handling

```kotlin
@Composable
fun App() {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    // ... your UI
}
```

Requesting a permission:

```kotlin
scope.launch {
    when (controller.getPermissionState(Permission.CALL_PHONE)) {
        PermissionState.Granted -> placeCall()
        PermissionState.DeniedAlways -> showOpenSettingsPrompt()
        else -> {
            try {
                controller.providePermission(Permission.CALL_PHONE)
                placeCall()
            } catch (e: DeniedAlwaysException) {
                showOpenSettingsPrompt()
            } catch (e: DeniedException) {
                showPermissionRationale()
            }
        }
    }
}
```

Available permissions: `Permission.CALL_PHONE`, `Permission.MICROPHONE`, `Permission.BLUETOOTH`, `Permission.BLUETOOTH_CONNECT`, `Permission.RECORD_AUDIO`, and more.

---

### 4. Making Calls

```kotlin
val platformConfig = PlatformConfiguration.create()
platformConfig.initializeCallConfiguration()
val callManager = CallManager(platformConfig)

val result = callManager.startOutgoingCall(
    number = "+1234567890",
    displayName = "John Doe"
)

when (result) {
    is CallResult.Success -> {
        val call = result.data
        // register a callback, mute, hold, end, etc.
    }
    is CallResult.Error -> {
        // handle result.error
    }
}
```

---

### Complete App.kt Example

```kotlin
@Composable
@Preview
fun App() {
    val platformConfig = remember {
        PlatformConfiguration.create().also { it.initializeCallConfiguration() }
    }
    val callManager = remember { CallManager(platformConfig) }
    val scope = rememberCoroutineScope()

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    MaterialTheme {
        Scaffold(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                var phoneNumber by remember { mutableStateOf("") }

                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Enter phone number") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                controller.providePermission(Permission.CALL_PHONE)
                                callManager.startOutgoingCall(phoneNumber, "Call from KMM")
                            } catch (e: DeniedAlwaysException) {
                                controller.openAppSettings()
                            } catch (e: DeniedException) {
                                // show rationale
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Call")
                }
            }
        }
    }
}
```

---

## Important Notes

- Always call `initializeCallConfiguration()` before constructing `CallManager`
- Call `cleanupCallConfiguration()` when the calling session ends (e.g., `onDestroy`)
- On Android, register `TelecomConnectionService` in your manifest — calls will silently fail otherwise
- On iOS, the `CXProvider` is a singleton per app; `PlatformConfiguration` owns it and must not be recreated per call
- Always unregister callbacks to avoid memory leaks

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Call never starts on Android | Check `TelecomConnectionService` is declared in the manifest with the correct permission |
| Permission permanently denied | Call `controller.openAppSettings()` to redirect the user |
| Audio routes back to earpiece after Bluetooth connects | Re-call `setAudioRoute(AudioRoute.BLUETOOTH)` after `onCallStateChanged` fires `ACTIVE` |
| iOS call does not show system call UI | Ensure `PlatformConfiguration.initializeCallConfiguration()` was called before placing the call |
| `CallResult.Error(CallNotFound)` | The `callId` was not found — use the `id` from the `Call` returned by `startOutgoingCall` |

---

## Building the Project

```sh
./gradlew build
```
