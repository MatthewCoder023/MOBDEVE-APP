# UniSync

A DLSU student-productivity prototype built for MOBDEVE. UniSync bundles a class schedule, task tracker, campus crowd monitor, QR check-in, and notification center behind a single bottom-navigation app.

All data is local dummy data and authentication is simulated — this is a presentation-ready prototype intended to be extended later with Firebase, Room, Google Maps, real QR scanning, and real DLSU data.

## Tech stack

- Kotlin with XML layouts (Views, not Compose) and ViewBinding
- Material Design 3, ConstraintLayout, RecyclerView, Bottom Navigation
- Jetpack Navigation Component (nav graph + NavigationUI bottom-bar sync)
- AndroidX ViewModel + in-memory repositories (`data/`) for screen state
- Gradle 9.0 / Android Gradle Plugin 8.6.1 / Kotlin 2.0.20

## Requirements

- Android Studio (recent stable version)
- JDK 17 or newer (Gradle 9 requires 17+; the project builds fine on JDK 24)
- Android SDK Platform 35
- Emulator or device on API 23+. Use **API 35 or newer** so enforced edge-to-edge is exercised — team demos use the API 37 emulator.

## Running the app

1. Clone the repo and open the project folder in Android Studio.
2. Let Gradle sync finish (the first sync downloads Gradle 9.0 and dependencies).
3. Pick an emulator (API 35+ recommended) and press Run.

From the command line: `./gradlew :app:assembleDebug`

## App flow

1. Splash screen
2. Login/Register (simulated — any input continues)
3. Main app with bottom navigation (Home, Schedule, Tasks, Map, Profile)
4. Dashboard shortcuts open Crowd Monitoring, QR Check-In, and Notifications; back (or reselecting the Home tab) returns to the dashboard

## Screen map

| Screen | Code | Layout |
|---|---|---|
| Splash | `SplashActivity` | `activity_splash.xml` |
| Login/Register | `AuthActivity` | `activity_auth.xml` |
| Shell + bottom nav | `MainActivity` | `activity_main.xml` |
| Dashboard/Home | `fragments/DashboardFragment` | `fragment_dashboard.xml` |
| Schedule | `fragments/ScheduleFragment` | `fragment_schedule.xml` |
| Tasks | `fragments/TasksFragment` | `fragment_tasks.xml` |
| Campus Map (placeholder) | `fragments/CampusMapFragment` | `fragment_campus_map.xml` |
| Crowd Monitoring | `fragments/CrowdFragment` | `fragment_crowd.xml` |
| QR Check-In (simulated) | `fragments/QrFragment` | `fragment_qr.xml` |
| Notifications | `fragments/NotificationsFragment` | `fragment_notifications.xml` |
| Profile & Settings | `fragments/ProfileFragment` | `fragment_profile.xml` |

Source lives under `app/src/main/java/com/dlsu/unisync/` in `fragments/`, `adapters/`, `models/`, `viewmodels/`, and `data/` (in-memory repositories) packages, plus the activities and an edge-to-edge insets helper (`Insets.kt`) at the root. Screen-to-screen navigation is defined in `res/navigation/nav_graph.xml`.

## Design documents

- `index.html` — presentation-ready UI board (open in a browser)
- `FIGMA_SPEC.md` — Figma build guide (Auto Layout, components, variants, tokens)

## Known limitations (intentional prototype scope)

- Dummy data only; task state lives in an in-memory ViewModel and resets when the app process ends
- Authentication is simulated; no accounts, no network calls
- Campus map and the QR code are static placeholders
- Light theme only (dark mode is deliberately disabled until night resources exist)

## Toolchain notes

- Gradle was upgraded to 9.0 so the daemon runs on modern JDKs (older Gradle crashed the daemon on newer Java versions).
- AGP 8.6.1 on Gradle 9.0 configures and builds, but is outside Google's officially tested matrix (Gradle 9 support formally landed in AGP 8.13). If an unexplained build error appears after an update, upgrading AGP is the first fix to try.
