# Privacy

Gretel collects nothing.

- No accounts, analytics, ads, crash reporters, or telemetry.
- No Play Services, Firebase, or Crashlytics.
- No network permission and no network calls.
- No contacts, location, storage, microphone, camera, or SMS permissions.
- Package visibility is limited to apps that export `MAIN` + `LAUNCHER` (manifest `<queries>`). Gretel does not use `QUERY_ALL_PACKAGES`.
- Android backup / cloud backup is disabled (`android:allowBackup="false"`).
- Assigned app, onboarding flag, hatch window, and display theme stay on the device in private app preferences.

Gretel is a local Home app. If that ever changes, this file changes first.
