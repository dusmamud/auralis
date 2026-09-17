# Contributing to Auralis

Thank you for your interest in contributing to **Auralis**! Open-source software thrives because of contributors like you. Whether you're reporting a bug, improving documentation, refactoring code, or adding new features, we appreciate your help.

---

## Code of Conduct

All contributors and participants are expected to uphold our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before participating in our issues, discussions, or pull requests.

---

## How Can I Contribute?

### 1. Reporting Bugs
- Check the [Issue Tracker](https://github.com/dusmamud/auralis/issues) first to ensure the bug hasn't already been reported.
- If it's a new issue, open a **Bug Report** using our issue template.
- Include complete details:
  - Device model & Android OS version (e.g., Pixel 7, Android 14).
  - App version & `yt-dlp` dynamic engine version.
  - Clear step-by-step reproduction instructions.
  - Relevant Logcat output or stack traces (ensure no personal URLs or tokens are included).

### 2. Suggesting Enhancements
- Have an idea for a feature or UI improvement? Open a **Feature Request**.
- Explain the problem, proposed solution, and alternative ideas considered.

### 3. Submitting Code (Pull Requests)
- For substantial feature additions or architectural modifications, please open an issue to discuss your proposal first.
- For small fixes, typos, and documentation updates, feel free to submit a pull request directly.

---

## Local Development Setup

### System Prerequisites
1. **Operating System:** Windows, macOS, or Linux.
2. **Java Development Kit:** OpenJDK 17 or higher (`JAVA_HOME` must be set).
3. **Android Studio:** Ladybug (2024.2.1+) or newer.
4. **Android SDK:**
   - Compile SDK: `35`
   - Minimum SDK: `26` (Android 8.0+)
   - Android NDK (Installed via Android Studio *SDK Manager > SDK Tools > NDK (Side by side)*).

### Getting the Code
```bash
git clone https://github.com/dusmamud/auralis.git
cd auralis
```

### Building the Project
Open the project in Android Studio and let Gradle sync. To build from the terminal:

```bash
# Windows
.\gradlew assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

> **Note on Chaquopy:** On first build, Gradle downloads Python native wheels and runtime libraries. This may take a few minutes depending on your internet connection.

---

## Code Style & Architecture Guidelines

To maintain code quality and consistency, please adhere to these guidelines:

### 1. Kotlin & Jetpack Compose
- **State Hoisting:** Composable functions should be stateless where possible. Hoist state up to the caller or ViewModel.
- **Unidirectional Data Flow (UDF):** UI emits events/intents; ViewModels process logic and expose an immutable `StateFlow<UiState>`.
- **Performance:** Avoid allocations inside `@Composable` bodies. Wrap lambdas and derived calculations in `remember` or use key-based lazy lists.
- **Design System:** Use tokens from `com.auralis.dld.ui.theme` (Colors, Typography, Shapes). Avoid hardcoding arbitrary hex colors or font sizes.

### 2. Architecture Boundaries
- **UI Layer (`ui/`):** Composables, ViewModels, and UI state models. No direct references to raw Python or lower-level JNI objects.
- **Data Layer (`data/`):** Repositories (`DownloadRepository`, `MediaStoreRepository`, `PythonEngineRepository`) manage business operations and bridge Python/Android APIs.
- **Player Layer (`player/`):** Singleton `AuralisPlayerController` managing ExoPlayer instances and Media3 session binding.

### 3. Error Handling
- Never use empty `catch` blocks or silently ignore exceptions.
- Provide descriptive error states in UI (e.g. snackbars or error cards) so the user understands why an operation failed.

---

## Git Workflow & Commit Guidelines

We follow the **Conventional Commits** specification:

### Branch Naming
- Features: `feature/short-description` (e.g., `feature/opus-equalizer`)
- Bug Fixes: `fix/short-description` (e.g., `fix/video-aspect-ratio`)
- Documentation: `docs/short-description` (e.g., `docs/update-readme`)
- Refactoring: `refactor/short-description` (e.g., `refactor/mediastore-query`)

### Commit Messages
Format: `<type>(<optional scope>): <subject>`

**Allowed types:**
- `feat`: A new feature for the user
- `fix`: A bug fix
- `docs`: Documentation updates only
- `style`: Formatting, semicolons, missing whitespace (no functional change)
- `refactor`: Code restructuring without changing behavior
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build scripts, dependencies, or CI updates

**Example:**
```
feat(player): add double-tap seek gesture to offline video player
fix(extractor): handle age-restricted stream metadata parsing
docs(readme): add screenshot showcase and build instructions
```

---

## Pull Request Checklist

Before submitting your pull request, please verify the following:

- [ ] Code compiles cleanly without errors or new warnings: `.\gradlew compileDebugSources`.
- [ ] Unit tests pass: `.\gradlew testDebugUnitTest`.
- [ ] Code follows project formatting and style rules.
- [ ] PR title follows Conventional Commits.
- [ ] Relevant documentation or comments have been updated.
- [ ] The app has been tested manually on an Android device or emulator.

Thank you for helping make Auralis better! 🚀
