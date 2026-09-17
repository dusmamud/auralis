# Frequently Asked Questions (FAQ)

---

### 1. Why does extraction fail with a "Cipher" or "Signatures" error?
YouTube frequently updates its internal player algorithms to deter automated scraping. 
- **Solution:** Open **Settings** within Auralis and tap **"Check for Engine Update"**. 
- Auralis will dynamically pull the latest `yt-dlp` release directly from upstream and hot-reload it into memory—no need to wait for a new app update.

---

### 2. Which audio format should I pick?
- **Original / M4A (AAC)**: Best balance of fidelity and broad compatibility across car stereos, older MP3 players, and Apple devices without any re-encoding overhead.
- **MP3 (320 kbps)**: Ideal for universal compatibility across any legacy audio hardware.
- **FLAC**: Lossless archival container. (Note: Audio stream from YouTube will be converted losslessly from the source Opus/AAC stream).
- **Opus**: The highest quality-to-size ratio. YouTube's native audio stream is Opus, so selecting this avoids lossy re-encoding artifacts.

---

### 3. Why does downloading pause when my screen turns off?
Aggressive OEM battery savers (such as Xiaomi MIUI/HyperOS, Samsung OneUI, or Huawei EMUI) may kill background processes even when a foreground service is running.
- **Solution:** Go to your device's **Settings > Apps > Auralis > Battery** and set it to **"Unrestricted"** or **"Don't optimize"**.

---

### 4. Where are downloaded songs and videos stored?
Auralis adheres to Android's modern Scoped Storage guidelines:
- **Audio Files**: Stored in `Music/Auralis/`
- **Video Files**: Stored in `Movies/Auralis/`
- They will immediately appear in your device's stock Gallery, VLC, or preferred music player app.

---

### 5. Why is Auralis 100% client-side instead of using a cloud backend?
Backend-based downloaders suffer from:
- Bandwidth bottlenecks and download throttling.
- IP-based rate limiting and captcha blocks from YouTube.
- Server downtime and monthly hosting fees.
- Privacy risks (the server knows every URL you paste).

By running Python 3.10 and `yt-dlp` directly on your device via Chaquopy, **Auralis guarantees total privacy, uncapped speeds, and unlimited scalability**.
