import re
import os
import io
import urllib.request
from typing import Dict, Any, Optional

try:
    import requests
except ImportError:
    requests = None

from PIL import Image
import mutagen
from mutagen.mp3 import MP3
from mutagen.id3 import ID3, TIT2, TPE1, TPE2, TALB, TCON, TDRC, APIC, ID3NoHeaderError
from mutagen.mp4 import MP4, MP4Cover
from mutagen.flac import FLAC, Picture
from mutagen.oggopus import OggOpus

def _fetch_bytes(url: str) -> Optional[bytes]:
    if requests:
        try:
            resp = requests.get(url, timeout=15, headers={"User-Agent": "Mozilla/5.0"})
            if resp.status_code == 200:
                return resp.content
        except Exception:
            pass
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=15) as res:
            return res.read()
    except Exception:
        return None

def get_square_cover_bytes(info: Dict[str, Any], temp_thumb_path: Optional[str] = None) -> Optional[bytes]:
    thumbnails = info.get("thumbnails", [])
    square_url = None

    for t in reversed(thumbnails):
        url = t.get("url", "")
        if "googleusercontent.com" in url:
            square_url = re.sub(r"=w\d+-h\d+.*", "=w1200-h1200-l90-rj", url)
            if not square_url.endswith("=w1200-h1200-l90-rj"):
                square_url += "=w1200-h1200-l90-rj"
            break
        elif t.get("width") and t.get("width") == t.get("height") and t.get("width") >= 500:
            square_url = url
            break

    video_id = info.get("id")
    candidates = []
    if square_url:
        candidates.append(square_url)
    if info.get("thumbnail"):
        candidates.append(info["thumbnail"])
    if video_id:
        candidates.append(f"https://i.ytimg.com/vi/{video_id}/maxresdefault.jpg")
        candidates.append(f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg")

    img = None
    for cand_url in candidates:
        raw_bytes = _fetch_bytes(cand_url)
        if not raw_bytes:
            continue
        try:
            test_img = Image.open(io.BytesIO(raw_bytes))
            img = test_img
            break
        except Exception:
            continue

    if not img:
        return None

    try:
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")
        w, h = img.size
        if w != h:
            min_dim = min(w, h)
            left = (w - min_dim) // 2
            top = (h - min_dim) // 2
            img = img.crop((left, top, left + min_dim, top + min_dim))

        out_buf = io.BytesIO()
        img.save(out_buf, format="JPEG", quality=95)
        return out_buf.getvalue()
    except Exception as ex:
        print(f"Cover art error: {ex}")
        return None

def apply_perfect_metadata(file_path: str, info: Dict[str, Any], cover_bytes: Optional[bytes] = None) -> bool:
    ext = os.path.splitext(file_path)[1].lower()
    title = info.get("track") or info.get("title") or "Unknown Title"
    artist = (
        info.get("artist")
        or (", ".join(info["artists"]) if info.get("artists") else None)
        or info.get("creator")
        or info.get("channel")
        or info.get("uploader")
        or "Unknown Artist"
    )
    album = info.get("album") or f"{title} - Single"
    year = info.get("release_year") or (info.get("upload_date")[:4] if info.get("upload_date") else "")
    genre = info.get("genre") or "Music"

    try:
        if ext == ".mp3":
            try:
                audio = MP3(file_path, ID3=ID3)
            except ID3NoHeaderError:
                audio = MP3(file_path)
                audio.add_tags()

            audio.tags.add(TIT2(encoding=3, text=title))
            audio.tags.add(TPE1(encoding=3, text=artist))
            audio.tags.add(TPE2(encoding=3, text=artist))
            audio.tags.add(TALB(encoding=3, text=album))
            audio.tags.add(TCON(encoding=3, text=genre))
            if year:
                audio.tags.add(TDRC(encoding=3, text=str(year)))

            if cover_bytes:
                audio.tags.add(
                    APIC(
                        encoding=3,
                        mime="image/jpeg",
                        type=3,
                        desc="Cover",
                        data=cover_bytes
                    )
                )
            audio.save(v2_version=3)
            return True

        elif ext in [".m4a", ".mp4"]:
            audio = MP4(file_path)
            audio["\xa9nam"] = [title]
            audio["\xa9ART"] = [artist]
            audio["aART"] = [artist]
            audio["\xa9alb"] = [album]
            audio["\xa9gen"] = [genre]
            if year:
                audio["\xa9day"] = [str(year)]

            if cover_bytes:
                audio["covr"] = [MP4Cover(cover_bytes, imageformat=MP4Cover.FORMAT_JPEG)]
            audio.save()
            return True

        elif ext == ".flac":
            audio = FLAC(file_path)
            audio["title"] = title
            audio["artist"] = artist
            audio["album"] = album
            audio["genre"] = genre
            if year:
                audio["date"] = str(year)

            if cover_bytes:
                pic = Picture()
                pic.type = 3
                pic.mime = "image/jpeg"
                pic.desc = "Cover"
                pic.data = cover_bytes
                audio.clear_pictures()
                audio.add_picture(pic)
            audio.save()
            return True

        elif ext in [".opus", ".ogg"]:
            try:
                audio = OggOpus(file_path)
                audio["title"] = title
                audio["artist"] = artist
                audio["album"] = album
                audio.save()
                return True
            except Exception:
                pass

        return True
    except Exception as e:
        print(f"Failed to tag {file_path}: {e}")
        return False
