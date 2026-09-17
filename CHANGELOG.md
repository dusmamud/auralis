# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-09-17

### Added
- **100% Client-Side Engine**: Integrated Chaquopy (Python 3.10) with embedded `yt-dlp` and `mutagen` for zero-server, zero-rate-limit media extraction.
- **Lossless Audio Extraction**: Direct raw audio extraction (Opus, AAC) and on-device transcoding to MP3 (up to 320 kbps), M4A, FLAC, and Opus.
- **Automated Metadata & ID3 Tagging**: Automatic extraction and embedding of high-resolution album art, title, artist, album name, and track metadata into downloaded audio.
- **4K UHD & HD Video Downloader**: High-definition video downloading up to 4K / 1080p 60FPS remuxed to standard MP4 with Scoped Storage indexing in `Movies/Auralis`.
- **Integrated Offline Media Player**:
  - **Audio Player**: Full-screen modal with album art, interactive seekbar, repeat/shuffle modes, and AndroidX Media3 `MediaSessionService` for background and lockscreen playback.
  - **Gestural Video Player**: Dual vertical swipe gestures for volume and brightness, double-tap 10s seek, aspect ratio toggle (Fit/Zoom/Stretch), and speed controls (0.5x to 2.0x).
- **Download Queue Manager**: Real-time concurrent downloading with speed indicator (`MB/s`), progress percentage, estimated time remaining (ETA), and individual task cancellation.
- **MediaStore Library**: Dual-tab local media browser indexing device audio tracks and videos with instant playback integration.
- **In-App Dynamic Engine Updater**: Settings panel option to dynamically update embedded `yt-dlp` wheels on-the-fly without rebuilding or reinstalling APKs.
- **Modern Jetpack Compose UI**: Pure Jetpack Compose implementation built to the `DESIGN.md` specification with dark theme aesthetics, zero-shadow chrome, and responsive orientation handling.

---

[1.0.0]: https://github.com/dusmamud/auralis/releases/tag/v1.0.0
