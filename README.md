# Auralis 🎵🎬

> **Lossless YouTube Music & Video Downloader for Android**  
> High-performance client-side extractor, offline player, and lossless media manager built with modern Android standards.

---

## ✨ Features

- **100% Client-Side Engine**: Powered by embedded **Chaquopy (Python 3.10)** running `yt-dlp` and `mutagen` directly on-device. Zero backend server costs, zero centralized rate limits, zero telemetry.
- **Lossless & Hi-Res Audio**: Extract raw streams without re-encoding, or transcode to high-bitrate MP3 (320/256 kbps), M4A, FLAC, or Opus with embedded high-resolution album art and metadata tags.
- **HD & 4K Video Downloader**: Download MP4 video up to 4K / 1080p directly to your device's `Movies/Auralis` directory.
- **Built-in Offline Media Player**:
  - **Audio**: Background playback via AndroidX Media3 `MediaSessionService`, lockscreen controls, system notification ticker, and full player seek slider.
  - **Video**: Gesture-driven offline player with brightness/volume swipes, double-tap seek, playback speed controls, and aspect ratio scaling (Fit, Zoom, Stretch).
- **In-App Dynamic Engine Updater**: Update the internal `yt-dlp` wheel in-app whenever YouTube updates cipher algorithms—no waiting for new APK releases.
- **Auralis Design System**: Crafted strictly to the `DESIGN.md` specification—curated typography hierarchy, zero-shadow chrome, responsive centering for landscape/tablets, and disciplined color restraint.

---

## 🏗️ Architecture & Tech Stack

```
com.auralis.dld/
├── data/
│   └── repository/
│       ├── MediaStoreRepository.kt     # Scoped Storage query/write for Audio & Video
│       ├── PythonEngineRepository.kt   # Chaquopy Python bridge (extract, download, update)
│       └── DownloadRepository.kt       # Active download queue StateFlow manager
├── player/
│   ├── AuralisPlayerController.kt      # Unified singleton ExoPlayer controller
│   └── PlayerState.kt                  # Immutable UI state for playback & progress
├── service/
│   ├── DownloadService.kt              # Foreground download worker service with notification
│   └── PlaybackService.kt              # Media3 MediaSessionService for audio & video
├── ui/
│   ├── components/                     # Auralis design system atoms (Chips, Buttons, Inputs, Art)
│   ├── home/                           # URL input, format selector, instant metadata preview
│   ├── queue/                          # Live progress bars, speed, ETA, and cancellation
│   ├── library/                        # Dual-tab MediaStore library (Songs & Videos)
│   ├── player/                         # Full audio player & offline video player modals
│   ├── settings/                       # Theme mode & dynamic engine updater
│   └── theme/                          # Color palettes, typography ladder, and Compose theme
└── python/
    └── auralis/                        # Python extraction scripts (yt-dlp, mutagen, device_profiler)
```

### Key Libraries
- **Language**: Kotlin 2.0.21 & Java 17
- **UI Framework**: Jetpack Compose (BOM 2024.10.01) + Material 3
- **Embedded Python**: Chaquopy 15.0.1 (Python 3.10)
- **Media Engine**: AndroidX Media3 (ExoPlayer + MediaSession 1.4.1)
- **Image & Thumbnail Loading**: Coil 2.7.0 (AsyncImage with constrained memory downsampling)
- **Concurrency**: Kotlin Coroutines & StateFlow

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Hedgehog or newer
- **JDK**: OpenJDK 17 or higher
- **Android SDK**: Compile SDK 35, Min SDK 26 (Android 8.0+)
- **Device / Emulator**: Android device running Android 8.0+ (ARM64 or x86_64)

### Building from Command Line

```bash
# Clone the repository
git clone https://github.com/dusmamud/auralis.git
cd auralis

# Compile debug sources
./gradlew compileDebugSources --no-parallel

# Run unit tests
./gradlew testDebugUnitTest --no-parallel

# Build Debug APK
./gradlew assembleDebug --no-parallel
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installing on Device via ADB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Security & Privacy

- **Zero Data Collection**: No user queries or URLs are transmitted to any third-party server.
- **Client-Side Processing**: All extraction, tagging, and storage occur entirely within the app sandbox on your physical device.

---

## 📄 License

This project is licensed under the Apache License 2.0.
