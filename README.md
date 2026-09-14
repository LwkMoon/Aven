# Aven — Behavioral Awareness & Living World

Aven is a calm-technology Android application built with modern **Jetpack Compose**, **Kotlin Coroutines**, **Room Database**, and **Material Design 3**. 

Instead of punitive blockers or guilt-inducing timers, Aven introduces a gentle, breathing pause between impulse and action, helping you understand your intent while nurturing a living, evolving ecosystem on your device.

---

## Key Features

- **Conscious Pause Interventions**: Introduces a customizable 3–15 second breathing window when opening monitored impulse-prone applications, with intent reflection tags (*"Reply to someone"*, *"Look up specific info"*, *"Pass time"*).
- **Procedural Harmonic Audio**: Asset-free, synthesized harmonic chimes using Android's `AudioTrack`—soothing breath cues, completion chimes, and growth harmonies that provide auditory grounding without heavy audio files.
- **Living World Dynamic Ecosystem**: A custom procedural Compose Canvas that visualizes your behavioral balance as a living sanctuary (*Seed* → *Sprout* → *Grove* → *Habitat* → *Living World*). Features serene wind sway animations, day/night diurnal lighting shifts, and unlocked flora lore.
- **Behavioral Insights & Trend Tracking**: Weekly awareness score, conscious intentionality rate, impulse avoidance count, and app breakdown charts.
- **Real-Time Launch Interception (`AvenAccessibilityService`)**: An optional on-device Accessibility Service that detects when monitored apps open and presents the pause window before habitual scrolling takes over.
- **Installed Apps Discovery (`InstalledAppsProvider`)**: Query and monitor any launchable application installed on your Android device with real app icons and search filtering.
- **Quiet Milestone Notifications (`AvenNotificationService`)**: Gentle, non-guilt status bar notifications celebrating world growth milestones.
- **Home Screen Living World Widget (`AvenGardenWidgetProvider`)**: An interactive Android AppWidget displaying your current sanctuary stage, growth progress bar, and weekly awareness rate.
- **100% Local & Private**: All data is stored locally in an encrypted Room SQLite database on your device. Zero cloud sync, zero external analytics, and zero tracking.

---


---

## Installation & First-Time Setup (v0.1 Prototype)

As a public open-source prototype app, Aven runs entirely on your device with 100% privacy. To use Aven to its full potential and allow automatic app interception, follow these steps:

### 1. Download & Install the APK
1. Download the latest `Aven-v0.1-prototype.apk` file from this repository's Releases section.
2. Open the file on your Android device. If prompted, allow installations from your browser or file manager ("Unknown sources").
3. Tap **Install** to complete setup.

### 2. Critical System Configurations (Required)
When you open Aven for the first time, you will see a **System Configuration Required** banner on the home screen. Tap the shortcuts to enable:
- **Accessibility Service**: This allows Aven to detect when monitored habit apps (like Instagram) are launched so it can introduce a conscious pause before the feed loads. *No screen text or user data is ever read or collected.*
- **Display Over Apps (Overlay)**: This gives Aven permission to draw the beautiful breathing pause interface over your screen when a monitored application starts.

---

## Developer Guide & Building From Source

### Prerequisites
- **Android Studio** (Koala, Ladybug, or later)
- **JDK 17 or 21**
- **Android SDK** (Min SDK 24, Target/Compile SDK 36)

### Steps to Build
1. Open this project directory in Android Studio.
2. Select an Android device or emulator from the toolbar.
3. Click **Run** (`Shift + F10`) or assemble the APK via terminal:
```bash
gradle :app:assembleDebug
```
The compiled APK will be located at `app/build/outputs/apk/debug/Aven-v0.1-prototype.apk`.

---

## Architecture Overview

The codebase is organized into clean, focused packages:

```
com.aven.app/
├── MainActivity.kt                       # Single activity entry point with edge-to-edge Compose
├── androidintegration/
│   ├── accessibility/
│   │   └── AvenAccessibilityService.kt   # System-level launch interception & cooldown engine
│   ├── appdetection/
│   │   └── SimulatedAppLaunchDetector.kt # In-app and external launch event dispatcher
│   ├── apps/
│   │   └── InstalledAppsProvider.kt      # Device package scanner for launchable apps
│   ├── notifications/
│   │   └── AvenNotificationService.kt    # Calm milestone notification channels
│   ├── permissions/
│   │   └── SystemPermissionHelper.kt     # Permission checks & deep links to system settings
│   └── widget/
│       └── AvenGardenWidgetProvider.kt   # Home screen AppWidget provider & live broadcast updater
├── core/
│   ├── audio/
│   │   └── CalmAudioSynthesizer.kt       # Low-latency procedural audio engine via AudioTrack
│   ├── behavior/
│   │   └── BehaviorEngine.kt             # Daily aggregation, weekly intentionality, and awareness metrics
│   ├── garden/
│   │   └── GardenEngine.kt               # Environmental rules, flora unlocks, and soft decay logic
│   └── intent/
│       └── IntentEngine.kt               # Conscious intent taxonomy and reflections
├── data/
│   ├── local/
│   │   ├── AvenDatabase.kt               # Room database configuration
│   │   ├── dao/                          # Type-safe Room DAOs
│   │   └── entities/                     # Monitored apps, intent events, daily summaries, garden state
│   ├── preferences/
│   │   └── AvenPreferences.kt            # DataStore preference storage (durations, theme, reduced motion)
│   └── repository/
│       └── AvenRepository.kt             # Unified repository synchronizing DB, preferences, widgets, and notifications
└── ui/
    ├── AvenApp.kt                        # Scaffold, navigation backstack, and global launch observer
    ├── components/                       # Shared UI: Wordmarks, badges, cards, metrics
    ├── garden/                           # Living world Canvas, diurnal lighting, flora lore sheet
    ├── home/                             # Daily pulse, quick launch pauses, intention status
    ├── insights/                         # 7-day behavioral analytics, hourly impulse charts
    ├── intervention/                     # The conscious pause: breathing timer, intention selector, decision screen
    ├── navigation/                       # Type-safe navigation routes and icons
    ├── onboarding/                       # 4-step calm onboarding journey
    ├── settings/                         # App management, device app picker, sound preview, system status
    └── theme/                            # Material Design 3 palette, typography, and shape system
```

---

## Testing Real-Device Integrations

1. **Test the Intervention Flow**:
   - In the Home screen, tap any monitored app (e.g. *Instagram*, *YouTube*, or any added app) to preview the breathing pause.
   - Select an intention, take a mindful breath, and choose either to **Continue with Intent** or **Pause and Step Away**.
   - Notice the harmonic audio chime and the immediate growth registered in your Living World.

2. **Browse & Monitor Device Apps**:
   - Navigate to **Settings** → **Monitored Apps** → tap **Browse device**.
   - Search for any installed application on your phone and toggle monitoring with one tap.

3. **Enable System Interception**:
   - In **Settings** → **Native Android Awareness**, tap **Enable** next to **Accessibility Service**.
   - Toggle on *Aven* in Android Accessibility Settings.
   - Now, whenever you open a monitored app on your device, Aven will gently intercept the launch to offer a conscious pause.

4. **Add the Home Screen Widget**:
   - Long-press on your device home screen, select **Widgets**, find **Aven**, and drag the widget to your home screen.
   - The widget automatically updates whenever you complete an intervention, reset data, or progress in growth.

---

## License & Privacy

Aven is designed under a **Local-First, Privacy-Guaranteed** philosophy. No personal data, app usage logs, or behavioral decisions ever leave your device.
