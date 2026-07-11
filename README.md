# UniSync

A DLSU student-productivity prototype built for MOBDEVE. UniSync bundles a class schedule, task tracker, campus crowd monitor, QR check-in, and notification center behind a single bottom-navigation app.

All data is local dummy data and authentication is simulated — this is a presentation-ready prototype intended to be extended later with Firebase, Room, Google Maps, real QR scanning, and real DLSU data.

## Tech stack

- Kotlin with XML layouts (Views, not Compose) and ViewBinding
- Material Design 3, ConstraintLayout, RecyclerView, Bottom Navigation
- Jetpack Navigation Component (nav graph + NavigationUI bottom-bar sync)
- Room database behind a repository interface (`data/`), AndroidX ViewModel + LiveData
- CameraX + ML Kit barcode scanning (bundled model, works offline) for QR check-in
- Full light/dark theming via `values-night` resources
- Gradle 9.0 / Android Gradle Plugin 8.13.0 / Kotlin 2.0.20, with Kotlin DSL build scripts and a version catalog (`gradle/libs.versions.toml`)

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

## Tests, lint, and CI

- Unit tests: `./gradlew testDebugUnitTest` (covers `TasksViewModel` against a fake repository)
- Code style: `./gradlew ktlintCheck` (auto-fix with `./gradlew ktlintFormat`; style configured in `.editorconfig`)
- Android lint: `./gradlew lintDebug`
- GitHub Actions runs all of the above plus `assembleDebug` on every push to `main` and every pull request (`.github/workflows/android.yml`)

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

- Tasks persist locally in Room and profile preferences in SharedPreferences, but schedule/crowd/notification content is still dummy fixture data
- Authentication is simulated (with real input validation); no accounts, no network calls
- The campus map is a static placeholder
- QR scanning is real (camera + ML Kit), but a scanned code only updates the status text — it is not sent anywhere

## Cloud features — setup required before implementation

These Phase 4 items need accounts/keys that must be created by a project owner. Do the console steps first; the code changes are small once the config files exist.

**Firebase Authentication** (replaces the simulated login)
1. Create a project at console.firebase.google.com, add an Android app with package `com.dlsu.unisync`, and register the debug SHA-1 (`./gradlew signingReport`).
2. Download `google-services.json` into `app/` (never needed in `.gitignore` — it is safe to commit for this use).
3. Add to the version catalog and build scripts: the `com.google.gms.google-services` plugin and the `com.google.firebase:firebase-bom` + `firebase-auth-ktx` dependencies. Do not add the plugin before the JSON exists — the build fails without it.
4. In `AuthActivity`, replace the fake success path inside the `validateInput()` branch with `FirebaseAuth.signInWithEmailAndPassword` / `createUserWithEmailAndPassword` (Login vs Register tab), or Google Sign-In restricted to the `dlsu.edu.ph` domain.

**Push notifications (FCM)** — same Firebase project; add `firebase-messaging-ktx`, a `FirebaseMessagingService`, and a runtime `POST_NOTIFICATIONS` permission request on API 33+.

**Google Maps campus map** — create an API key in Google Cloud console (Maps SDK for Android), store it via the Secrets Gradle plugin (`local.properties`, not source control), then swap the placeholder card in `fragment_campus_map.xml` for a `SupportMapFragment` centered on the DLSU campus.

## Toolchain notes

- Gradle was upgraded to 9.0 so the daemon runs on modern JDKs (older Gradle crashed the daemon on newer Java versions).
- AGP is 8.13.0, the first line with official Gradle 9 support, so the toolchain is inside Google's tested compatibility matrix.
- Release builds are minified with R8 (`isMinifyEnabled`/`isShrinkResources`); app-specific keep rules go in `app/proguard-rules.pro`.
