# Ringlr

Ringlr is a cross-platform application designed to handle and integrate phone calls and audio configurations. It leverages platform-specific APIs such as Android's Telecom framework and iOS's CallKit to provide a seamless calling experience.

![ringlr_](https://github.com/user-attachments/assets/65f924e5-8f70-4bd1-87a5-a99eac86eae7)


## Features

- Answer and make phone calls
- Manage call states (mute, hold, end)
- Configure audio settings
- Bluetooth support

## Permissions

The application requires the following permissions:

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

## Getting Started

### Prerequisites

- Android Studio Ladybug Feature Drop | 2024.2.2
- Kotlin 2.0.0
- Gradle 8.8.0

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
2. Open the project in Android Studio.
3. Sync the project with Gradle files.

### Building the Project

To build the project, use the following Gradle command:
```sh
./gradlew build
