# Ringlr

Ringlr is a cross-platform application designed to handle and integrate phone calls and audio configurations. It leverages platform-specific APIs such as Android's Telecom framework and iOS's CallKit to provide a seamless calling experience.

![ringlr_](https://github.com/user-attachments/assets/65f924e5-8f70-4bd1-87a5-a99eac86eae7)

## Contents
- [Features](#features)
- [Upcoming Changes](#upcoming-changes-and-work-left)
- [Contribution Guidelines](https://github.com/Rohit-554/Ringlr/blob/master/Contributing.md)
- [Permissions](#permissions)
- [CallManager API Reference](#callmanager-api-reference)
  - [Class Declaration](#class-declaration)
  - [Constructor Parameters](#constructor-parameters)
  - [Call Management Functions](#call-management-functions)
  - [Call State Control](#call-state-control)
  - [Call Information](#call-information)
  - [Audio Route Management](#audio-route-management)
  - [Callback Management](#callback-management)
  - [Example Usage](#example-usage)
- [Getting Started](#getting-started)
  - [Project Structure](#project-structure)
  - [Installation](#installation)
- [Implementation Guide](#implementation-guide)
  - [Local Library Integration](#local-library-integration)
  - [Initialize the Library](#2-initialize-the-library)
  - [Permission Handling](#3-permission-handling)
  - [Making Calls](#4-making-calls)
  - [Complete App.kt Example](#complete-appkt-example)
- [Important Notes](#important-notes)
- [Troubleshooting](#troubleshooting)
- [Building the Project](#building-the-project)
  

## Features

- Answer and make phone calls
- Grant permissions 
- Manage call states (mute, hold, end)
- Configure audio settings
- Bluetooth support

## Upcoming Changes and Work Left

- Implementation on the ios side

## Permissions

The application can handle these permissions right now:

### Android

- `android.permission.ANSWER_PHONE_CALLS`
- `android.permission.CALL_PHONE`
- `android.permission.READ_PHONE_STATE`
- `android.permission.MANAGE_OWN_CALLS`
- `android.permission.READ_PHONE_NUMBERS`
- `android.permission.MODIFY_AUDIO_SETTINGS`
- `android.permission.RECORD_AUDIO`
- `android.permission.BLUETOOTH`
- `android.permission.BLUETOOTH_CONNECT`

# CallManager API Reference

The `CallManager` class is the core component for handling call-related operations in Ringlr. It implements the `CallManagerInterface` and provides comprehensive call management functionality.

## Class Declaration

```kotlin
expect class CallManager(configuration: PlatformConfiguration) : CallManagerInterface
```

## Constructor Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `configuration` | `PlatformConfiguration` | Platform-specific configuration for call handling |

## Call Management Functions

### Outgoing Call Control

```kotlin
suspend fun startOutgoingCall(
    number: String,
    displayName: String,
    scheme: String
): CallResult<Call>
```
Initiates an outgoing call.
- `number`: Phone number to call
- `displayName`: Name to display for the call
- `scheme`: URI scheme for the call (e.g., "tel", "sip")
- Returns: Result containing the Call object if successful

```kotlin
suspend fun endCall(callId: String): CallResult<Unit>
```
Ends an active call.
- `callId`: Identifier of the call to end

### Call State Control

```kotlin
suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit>
```
Controls call muting.
- `callId`: Identifier of the call
- `muted`: True to mute, false to unmute

```kotlin
suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit>
```
Controls call hold state.
- `callId`: Identifier of the call
- `onHold`: True to hold, false to resume

## Call Information

```kotlin
suspend fun getCallState(callId: String): CallResult<CallState>
```
Retrieves the current state of a call.
- `callId`: Identifier of the call
- Returns: Current CallState

```kotlin
suspend fun getActiveCalls(): CallResult<List<Call>>
```
Gets a list of all active calls.
- Returns: List of active Call objects

## Audio Route Management

```kotlin
suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit>
```
Sets the audio output route.
- `route`: Desired AudioRoute (e.g., SPEAKER, EARPIECE, BLUETOOTH)

```kotlin
suspend fun getCurrentAudioRoute(): CallResult<AudioRoute>
```
Gets the current audio route.
- Returns: Current AudioRoute

## Callback Management

```kotlin
fun registerCallStateCallback(callback: CallStateCallback)
```
Registers a callback for call state changes.
- `callback`: CallStateCallback implementation to receive updates

```kotlin
fun unregisterCallStateCallback(callback: CallStateCallback)
```
Unregisters a previously registered callback.
- `callback`: CallStateCallback to unregister

## Example Usage

```kotlin
val callManager = CallManager(platformConfiguration)

// Start an outgoing call
val callResult = callManager.startOutgoingCall(
    number = "+1234567890",
    displayName = "John Doe",
    scheme = "tel"
)

// Handle call state changes
val callback = object : CallStateCallback {
    override fun onCallStateChanged(call: Call, state: CallState) {
        // Handle state change
    }
}
callManager.registerCallStateCallback(callback)

// End call
callResult.onSuccess { call ->
    callManager.endCall(call.id)
}

// Clean up
callManager.unregisterCallStateCallback(callback)
```
## Getting Started

### Project Structure 
```
ringlr/
├── androidApp/           # Android specific code
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/    # Android implementation
│   │   │   └── res/     # Android resources
│   │   └── test/        # Android tests
├── shared/              # Shared KMM code
│   ├── src/
│   │   ├── commonMain/  # Common code
│   │   ├── androidMain/ # Android-specific implementations
│   │   └── iosMain/     # iOS-specific implementations
└── iosApp/              # iOS specific code (planned)

```
### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/ringlr.git
    ```

# Implementation Guide

### Local Library Integration

Ringlr is currently available as a local module. Follow these steps to integrate it into your project:

1. Clone the Ringlr repository:
```bash
git clone https://github.com/Rohit-554/Ringlr.git
```

2. Import the `shared` module:
   - Copy the `shared` directory from the cloned repository to your project's root directory

3. Add the module to your project's settings.gradle.kts:
```kotlin
// settings.gradle.kts
include(":shared")
```

4. Add the dependency in your app's build.gradle.kts:
```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":shared"))
}
```

5. Sync your project with Gradle files

Note: Make sure the `shared` module's Gradle configuration is compatible with your project's Gradle version and configuration.

### 2. Initialize the Library

Create an Application class in your androidMain source set:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Platform Configuration
        PlatformConfiguration.init(this)
    }
}
```

Don't forget to declare your Application class in the AndroidManifest.xml along with the service class:

```xml
<application
    android:name=".MyApplication"
    ...>
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



### 3. Permission Handling

Ringlr requires specific permissions to handle calls. Here's how to implement the permission flow in your Compose UI:

#### Basic Permission Setup

in AndroidManifest.xml androidMain[main] (your project manifest) configure what permissions you want
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <!-- Essential permissions for call handling -->
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />

    <!-- Permissions for audio handling -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Bluetooth permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <!-- Feature declarations -->
    <uses-feature android:name="android.hardware.telephony" android:required="true" />
    <uses-feature android:name="android.hardware.microphone" android:required="true" />
 ...
```

```kotlin
@Composable
fun App() {
    // Initialize permission controller
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Bind the controller
    BindEffect(controller)
    
    // Your UI content
}
```

#### Complete Permission Flow Example

Here's a complete example of handling call permissions and making calls:

```kotlin
@Composable
fun CallScreen(
    phoneNumber: String,
    platformConfig: PlatformConfig
) {
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = {
            scope.launch {
                try {
                    val initialState = controller.getPermissionState(Permission.CALL_PHONE)

                    when (initialState) {
                        PermissionState.Granted -> {
                            // Permission already granted, make the call
                            CallManager(platformConfig).startOutgoingCall(
                                phoneNumber,
                                "Call from KMM"
                            )
                        }
                        PermissionState.DeniedAlways -> {
                            return@launch
                        }
                        else -> {
                            // Request permission
                            handlePermissionRequest(
                                controller,
                                phoneNumber,
                                platformConfig
                            )
                        }
                    }
                } catch (e: Exception) {
                    handlePermissionError(e)
                }
            }
        }
    ) {
        Text("Make Call")
    }
}

private suspend fun handlePermissionRequest(
    controller: PermissionsController,
    phoneNumber: String,
    platformConfig: PlatformConfig
) {
    try {
        controller.providePermission(Permission.CALL_PHONE)
        
        when (controller.getPermissionState(Permission.CALL_PHONE)) {
            PermissionState.Granted -> {
                CallManager(platformConfig).startOutgoingCall(
                    phoneNumber,
                    "Call from KMM"
                )
            }
            PermissionState.DeniedAlways -> {
                return
            }
            else -> {
                return
            }
        }
    } catch (e: Exception) {
        handlePermissionError(e)
    }
}

private fun handlePermissionError(error: Exception) {
    // Handle errors according to your app's needs
    when (error) {
        is DeniedAlwaysException -> { /* Handle permanent denial */ }
        is DeniedException -> { /* Handle temporary denial */ }
        else -> { /* Handle other errors */ }
    }
}
```

### 4. Making Calls

Once permissions are granted, you can use the `CallManager` to initiate calls:

```kotlin
CallManager(platformConfig).startOutgoingCall(
    phoneNumber = phoneNumber,    // The phone number to call
    displayName = "Call from KMM" // Display name shown on the call screen
)
```

This will launch the default system dialer app to make the call.

### Complete App.kt Example

Here's a complete example of how your App.kt might look:

```kotlin
@Composable
@Preview
fun App() {
    val isToastTapped = remember { mutableStateOf(false) }
    MaterialTheme {
            Scaffold(
                Modifier.fillMaxSize()
            ) {
                val platformConfig = PlatformConfiguration.create()
                platformConfig.initializeCallConfiguration()
                val scope = rememberCoroutineScope()

                val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
                val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
                BindEffect(controller)
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
                                    val initialState = controller.getPermissionState(Permission.CALL_PHONE)

                                    when (initialState) {
                                        PermissionState.Granted -> {
                                            // Permission already granted, proceed with call
                                            CallManager(platformConfig).startOutgoingCall(
                                                phoneNumber,
                                                "Call from KMM"
                                            )
                                        }
                                        PermissionState.DeniedAlways -> {
                                           // print denied always
                                            return@launch
                                        }
                                        else -> {
                                            // Request permission
                                            try {
                                                controller.providePermission(Permission.CALL_PHONE)

                                                // Check result after permission request
                                                when (controller.getPermissionState(Permission.CALL_PHONE)) {
                                                    PermissionState.Granted -> {
                                                        CallManager(platformConfig).startOutgoingCall(
                                                            phoneNumber,
                                                            "Call from KMM"
                                                        )
                                                    }
                                                    PermissionState.DeniedAlways -> {
                                                        //print permission denied
                                                        return@launch
                                                    }
                                                    else -> {
                                                        // print permission required to make calls 
                                                        return@launch
                                                    }
                                                }
                                            } catch (e: DeniedAlwaysException) {
                                                toastManager.showShortToast("Permission permanently denied")
                                                return@launch
                                            } catch (e: DeniedException) {
                                                toastManager.showShortToast("Permission denied")
                                                return@launch
                                            }
                                        }
                                    }
                                } catch (e: DeniedAlwaysException) {
                                   //print exception
                                } catch (e: DeniedException) {
                                    toastManager.showShortToast("Permission denied")
                                } catch (e: Exception) {
                                   //print exception
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

## Important Notes

- The `CallManager` requires valid platform configuration to work properly
- Always handle permission cases appropriately to ensure good user experience
- The library will use the system's default dialer app for making calls
- Make sure to handle all potential exceptions when requesting permissions
- Test the implementation thoroughly on different Android versions

## Troubleshooting

Common issues and their solutions:

1. Permission Denied Always:
   - Guide users to app settings to enable permissions manually
   - Consider implementing your own user feedback mechanism

2. Call Failed to Initialize:
   - Verify platform configuration is properly initialized
   - Check if all required permissions are granted

3. Permission Flow Issues:
   - Ensure `BindEffect` is called with the controller
   - Verify the permission state handling in all cases

### Building the Project

To build the project, use the following Gradle command:
```sh
./gradlew build
