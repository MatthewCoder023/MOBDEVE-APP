# UniSync — Test Cases

Manual test cases for demonstrating and verifying UniSync. Every case here describes
behaviour that exists in the app; none of it is aspirational.

**Test environment**

| | |
|---|---|
| Device | Android emulator or handset, API 23+ (API 35+ recommended) |
| Build | `./gradlew :app:assembleDebug`, or Run from Android Studio |
| Network | Required for sign-in and first sync; the app works offline afterwards |
| Account | An `@dlsu.edu.ph` address. Set `REQUIRED_EMAIL_DOMAIN = null` in `AuthActivity` to demo with any address |

**How to read a result**

A case passes only if the expected result happens *and* nothing else visibly breaks
(no crash, no empty screen where data should be, no stale value left behind).

---

## 1. Authentication

| ID | Title | Steps | Expected result |
|---|---|---|---|
| AUTH-01 | Register a new account | Register tab → enter an unused `@dlsu.edu.ph` address and a 6+ character password → Create account | Account is created and the dashboard opens |
| AUTH-02 | Empty fields are rejected | Leave both fields blank → Sign in | Inline errors under both fields; no network call |
| AUTH-03 | Malformed address is rejected | Enter `not-an-email` → Sign in | "Enter a valid email address" under the email field |
| AUTH-04 | Non-DLSU address is rejected | Enter `someone@gmail.com` → Sign in | Error naming the required domain; sign-in does not proceed |
| AUTH-05 | Short password is rejected | Enter a valid address and a 3-character password → Sign in | "Password must be at least 6 characters" |
| AUTH-06 | Wrong credentials | Enter a registered address with the wrong password → Sign in | Firebase error message appears inside the card; fields stay filled |
| AUTH-07 | Loading state | Sign in with valid credentials and watch the button | Button label is replaced by a spinner, button and both fields are disabled, and the card does not change size |
| AUTH-08 | Session persists | Sign in → close the app from Recents → reopen | Dashboard opens directly; the login form is skipped |
| AUTH-09 | Sign out | Profile → Log out | Returns to the login screen; pressing Back does not re-enter the app |

## 2. Schedule

| ID | Title | Steps | Expected result |
|---|---|---|---|
| SCH-01 | Add a class | Schedule → Add class → course code, one or more day chips, a start time, a room → Save | Class appears in the list as `Days • Time • Room` |
| SCH-02 | Course code is required | Add class → pick a day but leave the code blank → Save | "Course code is required"; the dialog stays open |
| SCH-03 | A day is required | Add class → enter a code but select no day → Save | Message explaining the class would not appear in reminders; the dialog stays open |
| SCH-04 | Time is optional | Add a class with days but no time → Save | Class is saved and displays its days only |
| SCH-05 | Multiple days | Select Mon, Wed and Fri → Save | Saved text reads `Mon/Wed/Fri`, always in week order regardless of tap order |
| SCH-06 | Edit a class | Tap an existing class → change the room → Save | Row updates in place and keeps its position in the list |
| SCH-07 | Edit prefills | Tap a class with days and a time | Chips for its days are checked and the time field shows its time |
| SCH-08 | Delete and undo | Swipe a class away → tap Undo on the snackbar | Class is removed, then restored to its original position |
| SCH-09 | Empty state | Delete every class | Illustration and "No classes yet. Add your schedule." |
| SCH-10 | Schedule follows the account | Sign out → sign in on another device (or another emulator) with the same account | The same classes are listed |

## 3. Tasks

| ID | Title | Steps | Expected result |
|---|---|---|---|
| TASK-01 | Add a task | Tasks → Add task → title → pick a due date → Save | Task appears with its due date |
| TASK-02 | Title is required | Save with an empty title | Inline error; the dialog stays open |
| TASK-03 | Complete a task | Tick a task's checkbox | Title is struck through and the task sinks below the open ones |
| TASK-04 | Overdue is flagged | Add a task dated before today | Red due date and an "Overdue" chip |
| TASK-05 | Ordering | Have open, dated, undated and completed tasks in the list | Open first, soonest due date next, undated after those, completed last |
| TASK-06 | Delete and undo | Swipe a task away → Undo | Task is removed, then restored |
| TASK-07 | Due date is not off by one | Add a task due today → check the date shown | The date matches the one picked, with no one-day shift |
| TASK-08 | Empty state | Delete every task | Illustration and "No tasks yet. Add one to get started." |

## 4. Dashboard

| ID | Title | Steps | Expected result |
|---|---|---|---|
| DASH-01 | Greeting | Open the app at different times of day | Greeting reads morning, afternoon or evening and includes the account's name |
| DASH-02 | Next class | Have at least one class saved | Hero card names the soonest upcoming class with its day, time and room |
| DASH-03 | Next class rolls over | Have a class earlier today that has already started | Hero shows that class's *next* occurrence, not today's |
| DASH-04 | Today's agenda | Have a class scheduled today | It appears under Today, marked "Finished" once its start time has passed |
| DASH-05 | Tasks due today | Have a task due today | It appears under Today |
| DASH-06 | Empty day | No classes today and nothing due | "No classes or deadlines today." with an illustration |
| DASH-07 | Shortcuts | Tap Crowd, QR Check-In, Alerts and Schedule in turn | Each opens its screen; Back returns to the dashboard |
| DASH-08 | Live update | Add a class from the Schedule tab, then return to the dashboard | Hero and Today reflect the new class without restarting the app |

## 5. Notifications

| ID | Title | Steps | Expected result |
|---|---|---|---|
| NOTIF-01 | Next class is listed | Have a class saved → open Alerts | "Next class: <course>" with its day, time and room |
| NOTIF-02 | Overdue tasks are listed | Have a task dated before today | It appears as overdue, above anything due today |
| NOTIF-03 | Busy rooms are listed | Record enough check-ins in one room (see CROWD-02) | The room is listed as busy |
| NOTIF-04 | Ordering | Have an overdue task, a task due today, a next class and a busy room | Overdue, then due today, then the class, then the room |
| NOTIF-05 | Empty state | New account with nothing entered | "You are all caught up." |

## 6. QR check-in

| ID | Title | Steps | Expected result |
|---|---|---|---|
| QR-01 | Simulated check-in | QR Check-In → Simulate check-in | Status line confirms the check-in and it appears under Recent check-ins |
| QR-02 | Camera permission | Tap Scan campus QR on a fresh install → Allow | Live camera preview with a corner reticle |
| QR-03 | Permission denied | Deny the camera permission → tap Scan again | Explanation with a route to app settings; no crash |
| QR-04 | Valid code | Scan a QR encoding `unisync://checkin/CCPROG3/Andrew 1404` | Check-in is recorded for that course and room |
| QR-05 | Foreign code | Scan any other QR code (a URL, for example) | Rejected without echoing its contents; no check-in is recorded |
| QR-06 | Empty state | New account, before any check-in | "No check-ins yet." |

## 7. Crowd monitoring

| ID | Title | Steps | Expected result |
|---|---|---|---|
| CROWD-01 | Activity appears | Record a check-in → open Crowd | The room is listed with its count for this hour |
| CROWD-02 | Level rises with use | Record 8 check-ins in one room, then 20 | The room moves from quiet to moderate, then to busy |
| CROWD-03 | Counts are shared, not personal | Check in from a second account | The count includes both, and no names are shown anywhere |
| CROWD-04 | The hour resets | Wait until the clock passes the hour | The room drops off the list on its own |
| CROWD-05 | Empty state | Open Crowd in an hour with no check-ins | "No check-ins yet this hour." |

## 8. Campus map

| ID | Title | Steps | Expected result |
|---|---|---|---|
| MAP-01 | Buildings are labelled | Open Map | Every block on the illustration shows its name |
| MAP-02 | Opens on the next class | Have a class in a room like `Andrew 1404` → open Map | Andrew is highlighted and the caption reads "<course> at <time> is in Andrew Building" |
| MAP-03 | Unknown room | Set the next class's room to `Online` → open Map | Caption says the room is not on the map; no building is wrongly highlighted |
| MAP-04 | Tap a building | Tap any block | It is highlighted, the caption shows its name and description, and its card in the list is outlined |
| MAP-05 | List follows the map | Tap a building whose card is below the fold | The page scrolls just enough to bring that card into view |
| MAP-06 | A tap wins | Tap a building, then wait | The selection stays put and is not replaced by the next-class building |
| MAP-07 | Crowd on the map | Record 8+ check-ins in `Gokongwei 305` → open Map | Gokongwei is tinted and its card reads "… • N check-ins this hour" |
| MAP-08 | Quiet buildings stay plain | Look at buildings with no check-ins | They keep the illustration's own colour |
| MAP-09 | Selection survives rotation | Select a building → rotate the device | The same building is still selected |

## 9. Profile and settings

| ID | Title | Steps | Expected result |
|---|---|---|---|
| PROF-01 | Identity | Open Profile | Name, email and monogram match the signed-in account |
| PROF-02 | Preferences persist | Toggle both switches off → leave the screen and return | Both are still off |
| PROF-03 | Reminder permission | On API 33+, turn reminders on for the first time → Allow | Switch stays on and the daily job is scheduled |
| PROF-04 | Permission denied | Turn reminders on → Deny | Switch returns to off and a message explains why |
| PROF-05 | Log out is not the loudest button | Look at the screen | Log out is outlined, not filled green |

## 10. Cross-cutting

| ID | Title | Steps | Expected result |
|---|---|---|---|
| GEN-01 | Dark mode | Switch the system theme to dark | Every screen is legible; no white-on-white or black-on-black text |
| GEN-02 | Offline reads | Turn off the network → reopen the app | Classes and tasks are still listed from the offline cache |
| GEN-03 | Offline writes | Offline, add a class → restore the network | The class is still there and reaches the account once a connection returns |
| GEN-04 | Rotation | Rotate on each tab | No crash and no lost input; open dialogs keep what was typed |
| GEN-05 | Back behaviour | From a dashboard shortcut screen, press Back | Returns to the dashboard, not out of the app |
| GEN-06 | Two accounts stay separate | Sign out → sign in as a different account | Only the second account's schedule and tasks are shown |

---

## Suggested demo path

A run through the app in about five minutes, in an order where each step sets up the next.

1. **Sign in** — mention the DLSU domain restriction and show the disabled state while it works (AUTH-04, AUTH-07).
2. **Add a class for today**, picking days as chips and the time from the picker. Point out that days are picked rather than typed, so a class can always be placed on a calendar (SCH-01, SCH-03).
3. **Dashboard** — the class is already in the hero card and Today's agenda, from that one entry (DASH-02, DASH-04).
4. **Add a task due today**, then show it under Today and in Alerts (TASK-01, NOTIF-02).
5. **QR check-in** — simulate a few check-ins (QR-01).
6. **Crowd** — the room now shows activity for this hour, aggregated across accounts and with no names attached (CROWD-01, CROWD-03).
7. **Map** — it opens on the building of the next class, the busy building from step 5 is tinted, and tapping any building scrolls its card into view (MAP-02, MAP-07, MAP-05).
8. **Profile** — preferences, then log out (PROF-02, AUTH-09).

Worth saying out loud during the demo: the dashboard, alerts and map are all reading the *same* schedule and check-in data rather than keeping their own copies, which is why one entry in step 2 shows up in four places.

## Automated coverage

These run in CI on every push and cover the logic behind the cases above.

| Command | Covers |
|---|---|
| `./gradlew testDebugUnitTest` | Schedule parsing and formatting, next-class selection, the Today agenda, alert ordering, due-date maths, room-to-building matching, crowd aggregation |
| `./gradlew connectedDebugAndroidTest` | Room DAOs, every database migration, first-launch state, and the upload of a device-local schedule to an account |
| `./gradlew ktlintCheck lintDebug` | Formatting and Android lint |

The round-trip test in `ScheduleFormatterTest` is the one worth mentioning if anyone asks
about regressions: it asserts that every day and time the picker can produce can be read
back, which is the property whose absence used to make classes disappear from reminders.
