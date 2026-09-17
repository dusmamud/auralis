import traceback
import yt_dlp
from .device_profiler import get_random_android_device, get_device_headers

def fetch_metadata(url: str) -> dict:
    device = get_random_android_device()
    headers = get_device_headers(device)

    ydl_opts = {
        "quiet": True,
        "no_warnings": True,
        "skip_download": True,
        "extract_flat": False,
        "http_headers": headers,
        "extractor_args": {
            "youtube": {
                "player_client": ["android", "ios"],
            }
        },
        "socket_timeout": 20,
    }

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            if not info:
                return {"success": False, "error": "No media information returned from URL."}

            if "entries" in info and info["entries"]:
                info = info["entries"][0]

            title = info.get("track") or info.get("title") or "Unknown Title"
            artist = (
                info.get("artist")
                or (", ".join(info["artists"]) if info.get("artists") else None)
                or info.get("creator")
                or info.get("channel")
                or info.get("uploader")
                or "Unknown Artist"
            )
            duration = int(info.get("duration", 0) or 0)
            thumbnail = info.get("thumbnail", "")

            thumbnails = info.get("thumbnails", [])
            if thumbnails:
                for t in reversed(thumbnails):
                    t_url = t.get("url", "")
                    if "googleusercontent.com" in t_url:
                        thumbnail = t_url
                        break
                if not thumbnail and thumbnails:
                    best_thumb = max(thumbnails, key=lambda t: t.get("width", 0) or 0)
                    thumbnail = best_thumb.get("url", thumbnail)

            return {
                "success": True,
                "id": str(info.get("id", "")),
                "title": str(title),
                "artist": str(artist),
                "duration": duration,
                "thumbnail": str(thumbnail or ""),
                "webpage_url": str(info.get("webpage_url", url)),
                "is_live": bool(info.get("is_live", False)),
                "error": None
            }
    except Exception as e:
        traceback.print_exc()
        return {
            "success": False,
            "error": str(e)
        }
