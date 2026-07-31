# UniSync

A DLSU student-productivity prototype built for MOBDEVE. UniSync bundles a class schedule, task tracker, campus crowd monitor, QR check-in, and notification center behind a single bottom-navigation app.

Tasks, schedule, and check-ins persist locally in Room, sign-in runs on Firebase Authentication, and QR check-in uses a real camera scanner. Crowd levels, notifications, and the campus map are still prototype fixtures.

## Tech stack

- Kotlin with XML layouts (Views, not Compose) and ViewBinding
- Material Design 3, ConstraintLayout, RecyclerView, Bottom Navigation
- Jetpack Navigation Component (nav graph + NavigationUI bottom-bar sync)
- Room database behind a repository interface (`data/`), AndroidX ViewModel + LiveData
- CameraX + ML Kit barcode scanning (bundled model, works offline) for QR check-in
- Full light/dark theming via `values-night` resources; AndroidX SplashScreen API launch
- Firebase Authentication (email/password) and Analytics; WorkManager for daily task reminders
- Gradle 9.0 / Android Gradle Plugin 8.13.0 / Kotlin 2.1.0, with Kotlin DSL build scripts and a version catalog (`gradle/libs.versions.toml`)

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

- Unit tests: `./gradlew testDebugUnitTest` (covers `TasksViewModel` against a fake repository, and `NextClassFinder`)
- Code style: `./gradlew ktlintCheck` (auto-fix with `./gradlew ktlintFormat`; style configured in `.editorconfig`)
- Android lint: `./gradlew lintDebug`
- GitHub Actions runs each stage as a separate step plus `assembleDebug` on every push to `main` and every pull request (`.github/workflows/android.yml`); failures are re-emitted as annotations

## App flow

1. System splash (AndroidX SplashScreen API — no splash activity)
2. Login/Register backed by Firebase Authentication (a signed-in user skips straight to step 3)
3. Main app with bottom navigation (Home, Schedule, Tasks, Map, Profile)
4. Dashboard shortcuts open Crowd Monitoring, QR Check-In, Notifications, and Schedule; back (or reselecting the Home tab) returns to the dashboard

## Screen map

| Screen | Code | Layout |
|---|---|---|
| Splash | SplashScreen API (`Theme.UniSync.Starting`) | — |
| Login/Register (launcher) | `AuthActivity` | `activity_auth.xml` |
| Shell + bottom nav | `MainActivity` | `activity_main.xml` |
| Dashboard/Home | `fragments/DashboardFragment` | `fragment_dashboard.xml` |
| Schedule | `fragments/ScheduleFragment` | `fragment_schedule.xml` |
| Tasks | `fragments/TasksFragment` | `fragment_tasks.xml` |
| Campus Map (placeholder) | `fragments/CampusMapFragment` | `fragment_campus_map.xml` |
| Crowd Monitoring | `fragments/CrowdFragment` | `fragment_crowd.xml` |
| QR Check-In (simulated) | `fragments/QrFragment` | `fragment_qr.xml` |
| Notifications | `fragments/NotificationsFragment` | `fragment_notifications.xml` |
| Profile & Settings | `fragments/ProfileFragment` | `fragment_profile.xml` |

Source lives under `app/src/main/java/com/dlsu/unisync/` in `fragments/`, `adapters/`, `models/`, `viewmodels/`, `data/` (Room DAOs and repositories), `work/` (reminder job), and `util/` packages, plus the activities and an edge-to-edge insets helper (`Insets.kt`) at the root. Screen-to-screen navigation is defined in `res/navigation/nav_graph.xml`.

## Design documents

- `public/index.html` — presentation-ready UI board (open in a browser, or deploy to Firebase Hosting; see below)
- `FIGMA_SPEC.md` — Figma build guide (Auto Layout, components, variants, tokens)

## Known limitations (intentional prototype scope)

- Tasks, the class schedule, and check-in history persist locally in Room (schema v2 with a 1→2 migration); crowd/notification content is still dummy fixture data
- Sign-in is restricted to `@dlsu.edu.ph`; change `REQUIRED_EMAIL_DOMAIN` in `AuthActivity` to demo with another address
- The campus map is a static placeholder
- Check-ins are recorded on-device only; QR codes must match the `unisync://checkin/<course>/<room>` payload format (anything else is rejected)

## Firebase setup

The app is connected to Firebase project `mobdeve---unisync` (`app/google-services.json`,
safe to commit — it is a client config, not a secret).

**Required before sign-in works:** in the Firebase console, open **Build → Authentication →
Get started**, then enable **Email/Password** under Sign-in method. Until that is done,
sign-in fails with `CONFIGURATION_NOT_FOUND`.

Not yet configured (optional):
- **SHA-1 fingerprint** — needed for Google Sign-In, phone auth, and Dynamic Links.
  Get it with `./gradlew signingReport`, add it under Project settings → Your apps,
  then re-download `google-services.json`.
- **Firestore** — needed for cross-device sync; the repository interfaces in `data/`
  are the seam where it would plug in.
- **Google Maps** — create a Maps SDK key in Google Cloud console and store it via the
  Secrets Gradle plugin (`local.properties`, not source control), then replace the
  placeholder card in `fragment_campus_map.xml` with a `SupportMapFragment`.

Note: the Firebase BOM is pinned to the 33.x line — see the comment in
`gradle/libs.versions.toml` before upgrading.

## Design board hosting

The UI board in `public/` deploys to Firebase Hosting (project `mobdeve---unisync`).
Config lives in `firebase.json` and `.firebaserc`; only `public/` is published, so
app source and `google-services.json` are never uploaded.

```
firebase login      # once per machine, opens a browser
firebase deploy --only hosting
```

## Toolchain notes

- Gradle was upgraded to 9.0 so the daemon runs on modern JDKs (older Gradle crashed the daemon on newer Java versions).
- AGP is 8.13.0, the first line with official Gradle 9 support, so the toolchain is inside Google's tested compatibility matrix.
- Release builds are minified with R8 (`isMinifyEnabled`/`isShrinkResources`); app-specific keep rules go in `app/proguard-rules.pro`.
