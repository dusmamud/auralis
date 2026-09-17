import random
from typing import Dict, Any

ANDROID_DEVICE_PROFILES = [
    {
        "brand": "Google",
        "model": "Pixel 8 Pro",
        "user_agent": "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro Build/UD1A.231105.004; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.6613.127 Mobile Safari/537.36",
    },
    {
        "brand": "Samsung",
        "model": "Galaxy S24 Ultra",
        "user_agent": "Mozilla/5.0 (Linux; Android 14; SM-S928B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/127.0.6533.103 Mobile Safari/537.36",
    },
    {
        "brand": "OnePlus",
        "model": "OnePlus 12",
        "user_agent": "Mozilla/5.0 (Linux; Android 14; CPH2583 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/126.0.6478.122 Mobile Safari/537.36",
    },
    {
        "brand": "Xiaomi",
        "model": "Xiaomi 14 Pro",
        "user_agent": "Mozilla/5.0 (Linux; Android 14; 23116PN5BC Build/UKQ1.230804.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.6613.88 Mobile Safari/537.36",
    },
    {
        "brand": "Nothing",
        "model": "Nothing Phone (2)",
        "user_agent": "Mozilla/5.0 (Linux; Android 14; A065 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/125.0.6422.165 Mobile Safari/537.36",
    },
]

def get_random_android_device() -> Dict[str, Any]:
    return random.choice(ANDROID_DEVICE_PROFILES)

def get_device_headers(device_profile: Dict[str, Any] = None) -> Dict[str, str]:
    profile = device_profile or get_random_android_device()
    return {
        "User-Agent": profile["user_agent"],
        "Accept-Language": "en-US,en;q=0.9",
        "Sec-CH-UA-Mobile": "?1",
        "Sec-CH-UA-Platform": '"Android"',
    }
