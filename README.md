# UniSync

A DLSU student-productivity app built for MOBDEVE. UniSync brings a class schedule, task
tracker, campus crowd monitor, QR attendance check-in, and notification centre together
behind a single bottom-navigation shell.

- **Design board:** https://mobdeve---unisync.web.app
- **Firebase project:** `mobdeve---unisync`

Sign-in runs on Firebase Authentication, tasks sync per-account through Firestore, the
class schedule and check-in history persist locally in Room, and QR check-in uses a real
camera scanner. Crowd levels, notifications, and the campus map are still prototype
fixtures — see [Known limitations](#known-limitations).

## Tech stack

- Kotlin with XML layouts (Views, not Compose) and ViewBinding
- Material Design 3, ConstraintLayout, RecyclerView, Bottom Navigation
- Jetpack Navigation Component (nav graph + NavigationUI bottom-bar sync)
- Firebase Authentication (email/password), Firestore, and Analytics
- Room for the class schedule and check-in history, behind repository interfaces
- AndroidX ViewModel + LiveData; WorkManager for daily deadline reminders
- CameraX + ML Kit barcode scanning (bundled model, works offline)
- Full light/dark theming via `values-night`; AndroidX SplashScreen API launch
- Gradle 9.0 / AGP 8.13.0 / Kotlin 2.1.0, Kotlin DSL build scripts, version catalog
  (`gradle/libs.versions.toml`)

## Requirements

- Android Studio (recent stable version)
- JDK 17 or newer (Gradle 9 requires 17+; builds fine on JDK 24)
- Android SDK Platform 35
- Emulator or device on API 23+. Use **API 35 or newer** so enforced edge-to-edge is
  exercised — team demos use the API 37 emulator.

## Running the app

1. Clone the repo and open the project folder in Android Studio.
2. Let Gradle sync finish (the first sync downloads Gradle 9.0 and dependencies).
3. Pick an emulator (API 35+ recommended) and press Run.

From the command line: `./gradlew :app:assembleDebug`

Register with an `@dlsu.edu.ph` address, or set `REQUIRED_EMAIL_DOMAIN` to `null` in
`AuthActivity` to sign in with any address while demoing.

## Architecture

UI never touches a data source directly. Fragments observe a ViewModel, which talks to a
repository interface, which hides whether the data lives in Firestore or Room. That seam
is what let tasks move from Room to Firestore without changing a single fragment.

```
Fragment ──observes──▶ ViewModel ──▶ Repository (interface)
                                        ├── FirestoreTaskRepository   → users/{uid}/tasks
                                        ├── RoomScheduleRepository    → schedule_entries
                                        └── RoomCheckInRepository     → check_ins
```

| Package | Contents |
|---|---|
| `data/` | Repository interfaces and implementations, Room DAOs, the database, Firestore mapping |
| `models/` | `TaskItem`, `ScheduleEntry`, `CheckIn`, `SimpleItem`, and the shared `TASK_ORDER` comparator |
| `viewmodels/` | `TasksViewModel`, `ScheduleViewModel`, `CheckInsViewModel` (activity-scoped) |
| `fragments/` | One fragment per screen |
| `adapters/` | `TaskAdapter` and `ScheduleAdapter` (ListAdapter + DiffUtil), `SimpleItemAdapter` |
| `views/` | `ScreenHeaderView` — the shared title/subtitle header |
| `work/` | `TaskReminderWorker` and its scheduler |
| `util/` | `NextClassFinder`, `UserProfile`, `Prefs` |

Navigation lives in `res/navigation/nav_graph.xml`; `Insets.kt` at the package root
handles edge-to-edge padding.

**Where data lives, and why**

- **Tasks → Firestore** (`users/{uid}/tasks`). Firestore's own offline cache serves reads
  and replays writes without a connection, so there is no second local copy to reconcile —
  which avoids hand-written conflict resolution, tombstones, and sync loops. Ordering is
  applied client-side via `TASK_ORDER`, so no composite index is needed and "undated last"
  stays expressible.
- **Schedule and check-ins → Room** (database v3, migrations `1→2` and `2→3`). This data is
  deliberately device-local.
- Firestore documents are mapped by hand rather than by reflection: Firestore's bean rules
  would rename `isDone` to `done`, and reflective conversion imposes no-arg-constructor
  requirements on data classes.

## Design system

Tokens are defined once and referenced everywhere; layouts should not hard-code values.

- **Spacing** — 4dp scale in `dimens.xml` (`space_xs` … `space_xxxl`). Every layout margin
  and padding uses these.
- **Shape** — `radius_sm/md/lg`, wired into the theme's `shapeAppearance*Component` attrs.
- **Type** — Manrope via downloadable fonts (fetched by Play Services, so no APK weight),
  with a ramp of `TextAppearance.UniSync.*` styles: `Display`, `ScreenTitle`,
  `SectionTitle`, `CardTitle`, `CardSubtitle`, `Body`, `Label`, `Hero*`.
- **Colour** — brand green plus semantic `status_low/medium/high` and matching container
  tints, each with a `values-night` variant. `brand_accent` lightens in dark mode;
  `dark_green` stays constant because it colours containers; `surface` is the card/nav
  background.
- **Components** — `Widget.UniSync.Button`, `.Button.Outlined`, and `.Card` are theme
  defaults, so screens do not repeat tint/corner attributes.

## Screen map

| Screen | Code | Layout |
|---|---|---|
| Splash | SplashScreen API (`Theme.UniSync.Starting`) | — |
| Login/Register (launcher) | `AuthActivity` | `activity_auth.xml` |
| Shell + bottom nav | `MainActivity` | `activity_main.xml` |
| Dashboard/Home | `fragments/DashboardFragment` | `fragment_dashboard.xml` |
| Schedule | `fragments/ScheduleFragment` | `fragment_schedule.xml` |
| Tasks | `fragments/TasksFragment` | `fragment_tasks.xml` |
| Campus Map (illustrative, interactive) | `fragments/CampusMapFragment` | `fragment_campus_map.xml` |
| Crowd Monitoring | `fragments/CrowdFragment` | `fragment_crowd.xml` |
| QR Check-In | `fragments/QrFragment` | `fragment_qr.xml` |
| Notifications | `fragments/NotificationsFragment` | `fragment_notifications.xml` |
| Profile & Settings | `fragments/ProfileFragment` | `fragment_profile.xml` |

## Feature notes

**Dashboard** — time-of-day greeting with the signed-in user's name; the "next class" card
is computed from the saved schedule by `NextClassFinder`, which parses day tokens and times
out of free-text entries like `Mon/Wed • 1:00 PM`.

**Tasks** — created and edited through a dialog with a Material date picker. Sorted by
urgency: open first, soonest due date next (undated last), completed sink to the bottom.
Overdue tasks get a red due date and an "Overdue" chip; completed ones are struck through.
Swipe to delete with an undo Snackbar. `MaterialDatePicker` returns UTC-midnight
timestamps, so all due-date formatting and comparison is done in UTC.

**QR check-in** — CameraX preview with a corner-bracket reticle and ML Kit scanning.
Only payloads matching `unisync://checkin/<course>/<room>` are accepted; anything else is
rejected without echoing its contents. Accepted check-ins are stored and listed. A simulate
button covers emulators without a camera, and a permanently denied camera permission offers
a deep link to app settings.

**Campus map** — a vector illustration whose building blocks are tap targets. `CampusMapView`
holds each building's bounds in the drawable's own 320x220 viewport and scales them to
whatever width it is laid out at, so taps hit-test in the same space the art was drawn in.
Selection is mirrored between the map and the list below, and the list doubles as the
accessible path to it. Redrawing `img_campus_map.xml` means updating the bounds in
`CampusRepository.keyLocations`.

**Reminders** — a WorkManager job runs daily at 08:00, finds tasks due today or overdue, and
posts one summary notification. Enabling the switch requests `POST_NOTIFICATIONS` on API 33+
and rolls back if denied; disabling it (or logging out) cancels the job.

## Firebase

`app/google-services.json` is committed on purpose — it is client configuration, not a
secret.

| Service | Status |
|---|---|
| Authentication (email/password) | Enabled |
| Firestore | Database created; rules deployed from `firestore.rules` |
| Hosting | Live at https://mobdeve---unisync.web.app |
| Analytics | Enabled |
| SHA-1 fingerprint | Not registered — needed only for Google Sign-In, phone auth, Dynamic Links |

Security rules restrict every `users/{uid}` subtree to that account. Redeploy after editing:

```bash
firebase deploy --only firestore:rules
```

> The Firebase BOM is pinned to the **33.x** line. `firebase-auth` 24.x is compiled with
> Kotlin 2.3 metadata, which the Kotlin 2.1 compiler rejects with "Module was compiled with
> an incompatible version of Kotlin". Raise `kotlin`/`ksp` first if you want BOM 34.x. The
> reasoning is repeated in `gradle/libs.versions.toml`.

**Not yet wired:** Google Maps (create a Maps SDK key, store it via the Secrets Gradle
plugin in `local.properties`, then replace the illustration in `fragment_campus_map.xml`
with a `SupportMapFragment`), and Crashlytics.

## Tests, lint, and CI

```bash
./gradlew testDebugUnitTest      # ViewModel + NextClassFinder
./gradlew connectedAndroidTest   # Room DAOs + migrations (needs a device/emulator)
./gradlew ktlintCheck            # ktlintFormat to auto-fix
./gradlew lintDebug
```

GitHub Actions (`.github/workflows/android.yml`) runs two jobs on every push to `main` and
every pull request:

| Job | Covers | Time |
|---|---|---|
| `build` | ktlint → unit tests → lint → `assembleDebug` | ~3 min |
| `instrumented` | Room DAO and migration tests on an API 34 emulator | ~6 min |

Each stage is a separate step, and failures are re-emitted as `::error::` annotations so the
cause is visible without downloading the full log.

The migration tests matter most: a broken migration only fails on installs that already hold
the old database, so fresh installs and unit tests all look fine while existing users crash
on launch. `MigrationTest` builds a real pre-upgrade database and opens it through Room,
which runs the migration chain and validates the result against the entities.

Run `./gradlew ktlintFormat` before committing — CI fails on style violations, and import
ordering is the usual culprit.

## Design board hosting

The UI board in `public/` deploys to Firebase Hosting. Only `public/` is published, so app
source and `google-services.json` are never uploaded — pointing the hosting root at the repo
would expose them.

```bash
firebase deploy --only hosting
```

`FIGMA_SPEC.md` is the Figma build guide (Auto Layout, components, variants, tokens).

## Known limitations

- Crowd levels and notifications are static fixtures; they need a real data source
- The campus map is an illustration, not live navigation — buildings are tappable, but there is no panning, zooming, or real-world positioning
- Check-in history is device-local and not synced
- Sign-in is restricted to `@dlsu.edu.ph`, enforced **client-side only** — it improves UX
  but is not security; real enforcement needs a Cloud Function or rules check
- Tasks created before database v3 were local-only and are not migrated to Firestore
- The UI has been verified as compiling but not visually reviewed on a device since the
  design-system work — worth an emulator pass

## Toolchain notes

- Gradle 9.0 so the daemon runs on modern JDKs (older Gradle crashed on newer Java).
- AGP 8.13.0 — the first line with official Gradle 9 support.
- Release builds are minified with R8 (`isMinifyEnabled`/`isShrinkResources`); app-specific
  keep rules go in `app/proguard-rules.pro`.
- Room exports schemas to `app/schemas/` so future migrations can be diffed; commit the
  generated JSON when you bump the database version.
- **16 KB page sizes:** Android 15+ devices can use 16 KB memory pages, and a dependency
  shipping 4 KB-aligned native libraries breaks on them (Play rejects such uploads too).
  CameraX is pinned to 1.5.3 for this reason — 1.3.4 shipped 4 KB-aligned libs, and 1.6.x
  needs `compileSdk 36`. CI runs `scripts/check_16kb_alignment.py` against the built APK,
  so a regression fails the build instead of surfacing on a device.
