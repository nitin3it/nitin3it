# KidSafe — Parental app-usage monitor (Android)

KidSafe lets a parent see which apps their child uses and for how long.
The same app is installed on both phones; on first launch you pick **Parent** or **Child**.

| | |
|---|---|
| **Child phone** | Pick *Child* → sign in with Google or mobile number (OTP) → a unique **Child ID** (e.g. `K7QM-29TX`) is shown → allow *Usage access*. App usage is uploaded every ~15 minutes in the background (and whenever the app is opened). |
| **Parent phone** | Pick *Parent* → sign in with Google or mobile number → you get your own **Parent ID** → tap *Enter Child ID* and type the child's ID → the dashboard shows the child's screen time per app (e.g. *Instagram 1h 20m, Chrome 2h*), a 7-day chart, and how many times each app was opened. |

Every account gets a separate ID per role, so the same Google account or number can be used
as a parent on one phone and a child on another.

## Tech stack

- Kotlin, Jetpack Compose, Material 3 (custom light & dark theme)
- Firebase Authentication — Google (Credential Manager) and phone/OTP
- Cloud Firestore — profiles and daily usage, live updates on the parent dashboard
- `UsageStatsManager` + WorkManager — collects and uploads usage on the child phone

## Setup (one time, ~5 minutes)

1. Create a project at <https://console.firebase.google.com>.
2. **Add an Android app** with package name `com.nitin3it.kidsafe`.
   Add your debug **SHA-1** (Android Studio → Gradle → *signingReport*, or
   `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android`).
   SHA-1 is required for both Google sign-in and phone OTP.
3. Download `google-services.json` and put it in `app/`.
4. **Authentication → Sign-in method**: enable **Google** and **Phone**.
5. **Firestore Database**: create a database, then paste the contents of [`firestore.rules`](firestore.rules)
   into the *Rules* tab and publish.
6. Open the project in Android Studio and run it on two devices (or a device + emulator).

Without `google-services.json` the app still builds, and shows a reminder to finish this setup.

## Build from the command line

```bash
./gradlew assembleDebug        # APK in app/build/outputs/apk/debug/
```

## Project layout

```
app/src/main/java/com/nitin3it/kidsafe/
├── data/            Firebase repositories, models, ID generation, role preference
├── usage/           UsageCollector (reads usage events), UsageSyncer, UsageSyncWorker
└── ui/
    ├── onboarding/  Parent / Child role selection
    ├── auth/        Google + mobile OTP login
    ├── child/       Child home: Child ID, permission, sync status, today's usage
    ├── parent/      Parent dashboard: children, weekly chart, per-app usage
    └── components/  Theme-aware shared widgets
```

## Data model (Firestore)

```
profiles/{uid}_{role}
  publicId, role, displayName, contact, deviceModel,
  linkedChildren[]  (parent), lastSyncAt (child)
profiles/{childProfileId}/usage/{yyyy-MM-dd}
  date, totalMs, updatedAt,
  apps[] { packageName, appName, totalMs, launches, lastUsed }
```

Usage documents can only be read by the child who wrote them and by parents who have linked that child.

## Notes

- Android only allows reading app usage after the user turns on *Usage access* for KidSafe
  (Settings → Usage access). The child screen guides through this.
- Usage is counted from the child phone's own clock and timezone, per calendar day.
