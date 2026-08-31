# Contributing

## Identity

- App name: Gretel. Wordmark is the text `GRETEL`, not part of the launcher mark.
- Package / namespace: `com.abeant.gretel`
- License: Apache-2.0.

## Hard nos

No Device Admin, lock-task, screen pinning as the product, Accessibility Service, `SYSTEM_ALERT_WINDOW`, overlay, `QUERY_ALL_PACKAGES`, Play Services, Firebase, Crashlytics, ads, analytics, or Compose. Do not silently set the default Home app.

## Development

1. JDK 17, Android SDK 36.
2. `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`
3. Views / XML only. `minSdk 26`, `targetSdk 36`.
4. Unit-test hatch-window timing, KOReader preference, and preference persistence when you touch those paths.
5. Keep taps ≥ 48 dp and text ≥ 16 sp. White default, optional true black. No ripples.

Changes to the Home dispatch path, persistence, package visibility, or background behavior should explain their compatibility and lifecycle impact in the pull request.

## Pull requests

Open a PR against `main`. Keep the repo private until the owner publishes it. Do not push a `v*` tag unless a release is intended.

## Version bumps

```bash
./scripts/bump-version.sh 0.1.1
```

Add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` when you bump `versionCode`.
