# Ringlr

A Kotlin Multiplatform Mobile (KMM) library for handling phone calls across Android and iOS. One unified API on top of Android Telecom and iOS CallKit.

![ringlr_](https://github.com/user-attachments/assets/65f924e5-8f70-4bd1-87a5-a99eac86eae7)

## Documentation

Full setup guide, API reference, and VoIP/SIP docs are at:

**[rohit-554.github.io/Ringlr](https://rohit-554.github.io/Ringlr/)**

## What it does

- Place and receive PSTN and SIP/VoIP calls
- Mute, hold, end, answer, and decline calls
- Switch audio routes — speaker, earpiece, Bluetooth, wired headset
- Real-time call state callbacks
- Built-in Compose Multiplatform call UI (`CallScreen`)
- VoIP push — PushKit on iOS, FCM bridge on Android
- Unified permission handling across platforms

## Quick start

```bash
git clone https://github.com/Rohit-554/Ringlr.git
```

Copy the `shared` module into your project, add `include(":shared")` to `settings.gradle.kts`, then follow the [setup guide](https://rohit-554.github.io/Ringlr/#setup).

## Contributing

See [Contributing.md](Contributing.md).

## License

Apache 2.0
