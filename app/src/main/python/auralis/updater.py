import sys
import subprocess
import yt_dlp

def get_current_ytdlp_version() -> str:
    try:
        return yt_dlp.version.__version__
    except Exception:
        return "Unknown"

def update_ytdlp() -> dict:
    try:
        result = subprocess.run(
            [sys.executable, "-m", "pip", "install", "--upgrade", "yt-dlp"],
            capture_output=True,
            text=True,
            timeout=120
        )
        if result.returncode == 0:
            import importlib
            importlib.reload(yt_dlp)
            new_ver = get_current_ytdlp_version()
            return {"success": True, "version": new_ver, "error": None}
        else:
            return {"success": False, "version": None, "error": result.stderr}
    except Exception as e:
        return {"success": False, "version": None, "error": str(e)}
