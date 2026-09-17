import os
import shutil
import traceback
from typing import Dict, Any

import yt_dlp
from .device_profiler import get_random_android_device, get_device_headers
from .metadata import get_square_cover_bytes, apply_perfect_metadata

def download_media(
    url: str,
    output_dir: str,
    file_name: str,
    media_type: str = "audio",
    audio_format: str = "mp3",
    audio_bitrate: str = "320",
    video_quality: str = "1080p",
    thumbnail_url: str = "",
    artist: str = "",
    title: str = "",
    progress_callback = None
) -> dict:
    os.makedirs(output_dir, exist_ok=True)
    out_tmpl = os.path.join(output_dir, f"{file_name}.%(ext)s")

    device = get_random_android_device()
    headers = get_device_headers(device)

    def progress_hook(d: Dict[str, Any]):
        if not progress_callback:
            return
        status = d.get("status")
        if status == "downloading":
            total = d.get("total_bytes") or d.get("total_bytes_estimate") or 1
            downloaded = d.get("downloaded_bytes", 0)
            percent = (downloaded / total) * 100.0
            speed = d.get("speed") or 0.0
            speed_mb = speed / (1024.0 * 1024.0)
            eta = d.get("eta") or 0
            try:
                progress_callback.onProgress(float(percent), float(speed_mb), int(eta))
            except Exception:
                pass
        elif status == "finished":
            try:
                progress_callback.onStatusChange("Processing & Tagging...")
            except Exception:
                pass

    ydl_opts: Dict[str, Any] = {
        "outtmpl": out_tmpl,
        "progress_hooks": [progress_hook],
        "quiet": True,
        "no_warnings": True,
        "nopart": True,
        "continuedl": False,
        "retries": 3,
        "fragment_retries": 3,
        "socket_timeout": 30,
        "http_headers": headers,
        "extractor_args": {
            "youtube": {
                "player_client": ["android", "ios"],
            }
        },
    }

    has_ffmpeg = bool(shutil.which("ffmpeg"))

    if media_type == "audio":
        fmt = audio_format.lower().strip()
        if has_ffmpeg:
            ydl_opts["format"] = "ba[vcodec=none]/bestaudio/best"
            ydl_opts["postprocessors"] = [{
                "key": "FFmpegExtractAudio",
                "preferredcodec": fmt,
                "preferredquality": audio_bitrate,
            }]
        else:
            # Standalone Android: Extract best matching direct audio stream
            if fmt in ["m4a", "aac"]:
                ydl_opts["format"] = "ba[ext=m4a]/ba[acodec^=mp4a]/ba[vcodec=none]/bestaudio/best"
            elif fmt in ["opus", "ogg"]:
                ydl_opts["format"] = "ba[ext=webm]/ba[acodec^=opus]/ba[vcodec=none]/bestaudio/best"
            else:
                # Default / MP3 preference
                ydl_opts["format"] = "ba[ext=mp3]/ba[ext=m4a]/ba[vcodec=none]/bestaudio/best"
    else:
        if has_ffmpeg:
            format_selector = {
                "4k": "bestvideo[height<=2160]+bestaudio/best[height<=2160]",
                "2k": "bestvideo[height<=1440]+bestaudio/best[height<=1440]",
                "1080p": "bestvideo[height<=1080]+bestaudio/best[height<=1080]",
                "720p": "bestvideo[height<=720]+bestaudio/best[height<=720]",
                "best": "bestvideo+bestaudio/best"
            }.get(video_quality, "bestvideo+bestaudio/best")
            ydl_opts["format"] = format_selector
            ydl_opts["merge_output_format"] = "mp4"
        else:
            ydl_opts["format"] = "best[ext=mp4]/best"

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            if not info:
                raise Exception("Failed to retrieve stream info from YouTube.")

        matching_files = [
            os.path.join(output_dir, f) for f in os.listdir(output_dir)
            if f.startswith(file_name) and not f.endswith(".part") and not f.endswith(".ytdl")
        ]
        if not matching_files:
            raise Exception("Downloaded media file not found on disk.")

        matching_files.sort(key=lambda p: os.path.getmtime(p), reverse=True)
        final_path = matching_files[0]

        # For audio, if it's AAC in .mp4 container and user requested m4a, normalize to .m4a
        if media_type == "audio":
            cur_root, cur_ext = os.path.splitext(final_path)
            cur_ext_clean = cur_ext.lower().lstrip(".")
            target_ext = audio_format.lower().strip()
            
            # If AAC audio ended up with .mp4 extension, rename cleanly to .m4a
            if target_ext == "m4a" and cur_ext_clean in ["mp4", "m4a"]:
                desired_path = f"{cur_root}.m4a"
                if desired_path != final_path:
                    if os.path.exists(desired_path):
                        os.remove(desired_path)
                    os.rename(final_path, desired_path)
                    final_path = desired_path

            if os.path.exists(final_path):
                cover_bytes = get_square_cover_bytes(info)
                apply_perfect_metadata(final_path, info, cover_bytes)

        if progress_callback:
            progress_callback.onFinished(final_path)

        return {
            "success": True,
            "file_path": str(final_path),
            "error": None
        }

    except Exception as e:
        traceback.print_exc()
        err_msg = str(e)
        if progress_callback:
            progress_callback.onError(err_msg)
        return {
            "success": False,
            "file_path": None,
            "error": err_msg
        }
