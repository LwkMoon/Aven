# Aven — Behavioral Awareness & Living World

Aven is a local-first Android app built with **Kotlin**, **Jetpack Compose**, **Room**, **DataStore**, and **Material 3**.

Instead of blocking apps or using guilt-based timers, Aven adds a short pause before opening apps you choose to monitor. You can reflect on what you intended to do, continue, or step away. Your choices also affect Aven's small living-world visualization.

> **Status:** v0.1.0 prototype. Expect rough edges and device-specific behavior.

---

## What it does

- **Conscious pauses** — configurable 3–15 second pauses for monitored apps.
- **Intent reflection** — choose a reason such as replying to someone, looking something up, or passing time.
- **Procedural audio** — short synthesized cues generated locally with `AudioTrack`.
- **Living world** — a procedural Compose Canvas that changes as your awareness metrics change.
- **Insights** — weekly awareness and intentionality metrics with app breakdowns.
- **App discovery** — browse launchable apps installed on the device and choose which ones to monitor.
- **Accessibility interception** — optionally detects launches of monitored apps without reading their screen contents.
- **Milestone notifications** — optional local notifications for progress.
- **Home-screen widget** — shows the current living-world stage and progress.
- **Local-first storage** — behavioral data stays on the device; Aven does not currently use cloud sync or external analytics.

## Privacy

Aven's v0.1 prototype is designed to work locally. The Accessibility Service is used to detect app-window changes for monitored applications; it is configured not to retrieve window content.

The local Room database is **not currently encrypted at the database layer**. Do not describe v0.1 as using an encrypted database.

Aven does not require a Gemini/API key for the current prototype.

---

## Installation

### APK

Download the `Aven-v0.1-prototype.apk` asset from the [v0.1 release](https://github.com/LwkMoon/Aven/releases/tag/v0.1).

Android may require you to allow your browser or file manager to install apps from that source.

### First-time setup

For automatic interception, Aven needs:

1. **Accessibility Service** — lets Aven observe which app window is opened. Aven does not retrieve the window's text/content.
2. **Display over other apps** — lets Aven show its pause interface when a monitored app is opened.

Both permissions are optional for exploring the rest of the prototype.

---

## Build from source

### Requirements

- Android Studio Koala, Ladybug, or later
- JDK 17 or 21
- Android SDK 36
- Android device or emulator running Android 7.0+ (API 24+)

### Build

Open the repository in Android Studio and let Gradle sync. Then run the `app` configuration, or use:

```bash
./gradlew :app:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

---

## Architecture

The project is split by responsibility rather than by screen:

```text
com.aven.app/
├── androidintegration/   # Android system integrations
├── core/                 # Behavior, intent, garden, and audio logic
├── data/                 # Room, DataStore, and repository layer
└── ui/                   # Compose screens, components, navigation, and theme
```

Important integrations include:

- `AvenAccessibilityService` — monitors relevant accessibility window events.
- `InstalledAppsProvider` — discovers launchable apps on the device.
- `AvenNotificationService` — handles local milestone notifications.
- `AvenGardenWidgetProvider` — provides the home-screen widget.
- `AvenRepository` — coordinates local persistence and app state.

---

## Real-device testing

1. Add an app under **Settings → Monitored Apps**.
2. Preview the intervention from the Home screen.
3. Enable Aven's Accessibility Service if you want automatic interception.
4. Enable the overlay permission when prompted.
5. Open a monitored app and verify that the pause appears.
6. Add the Aven widget from your device's widget picker.

Behavior can vary between Android versions and manufacturers, so real-device testing is preferred over relying only on an emulator.

---

## Current limitations

Aven v0.1.0 is a prototype, not a production release.

- The local database is not encrypted at the database layer.
- Room migrations are not yet production-ready.
- Accessibility behavior can differ across Android OEMs.
- Some behavior and growth systems are still being refined.
- Release hardening, automated builds, and broader device testing are planned for later versions.

---

## Roadmap

The project is still early. Planned work includes improving reliability across Android devices, refining behavioral metrics, adding safer database migrations, expanding tests, and polishing the intervention experience based on real-world use.

Bug reports and concrete reproduction steps are especially useful during the prototype stage.

---

## License

Aven is released under the **MIT License**. See [`LICENSE`](LICENSE) for details.
