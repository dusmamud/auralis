# Security Policy

The Auralis team takes the security and privacy of our users and their devices seriously. This document outlines our vulnerability disclosure process, supported versions, and security practices.

---

## Supported Versions

Only the latest release of Auralis receives official security updates and patches. We recommend all users keep the application updated to the newest version.

| Version | Supported          |
| :------ | :----------------- |
| `1.0.x` | :white_check_mark: |
| `< 1.0` | :x:                |

---

## Reporting a Vulnerability

If you discover a security vulnerability or potential privacy leak within Auralis, please **do not open a public issue** on GitHub. Public disclosure can put existing users at risk before a fix can be prepared.

### Private Reporting Channels

1. **GitHub Private Vulnerability Reporting (Recommended):**
   - Navigate to the **Security** tab of the [`dusmamud/auralis`](https://github.com/dusmamud/auralis) repository.
   - Click on **"Report a vulnerability"** to initiate a private advisory discussion.

2. **Direct Maintainer Contact:**
   - If GitHub Private Reporting is unavailable, reach out directly to the maintainer via GitHub profile details ([@dusmamud](https://github.com/dusmamud)).

### What to Include in Your Report

To help us investigate and reproduce the issue rapidly, please provide:
- A clear, concise description of the vulnerability.
- Steps to reproduce the vulnerability (proof-of-concept scripts or URL triggers if applicable).
- Device and environment details (Android OS version, device architecture e.g. ARM64, Auralis version).
- The potential impact and severity (e.g. unauthorized file access, remote code execution, denial of service).

---

## Response Timeline & Disclosure Process

1. **Acknowledgment:** We will acknowledge receipt of your vulnerability report within **48 hours**.
2. **Investigation & Triage:** Within **5 business days**, we will validate the vulnerability and assess its risk level.
3. **Patch Preparation:** A security fix will be prepared, reviewed, and tested in a private branch.
4. **Coordinated Release:** We will publish a patched release along with an official security advisory giving proper credit to the reporter (unless anonymity is requested).

---

## Security Architecture & Design Principles

Auralis is designed from the ground up with defensive security best practices:

- **Zero Telemetry & External Logging:** The application never phones home to proprietary tracking servers.
- **On-Device Sandbox:** Chaquopy executes Python code within the Android process sandbox (`uid`), meaning it cannot access other applications' data.
- **Modern Scoped Storage:** All file writes use Android's official `MediaStore` API (`Audio` and `Video` collections) without demanding unrestricted legacy storage access.
- **Strict HTTPS:** All network calls to external CDNs and endpoints require transport-layer encryption (`HTTPS`), preventing cleartext downgrade and Man-in-the-Middle (MitM) snooping.
- **No Hardcoded Secrets:** No API keys, server tokens, or private certificates are embedded in source code.
