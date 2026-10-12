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

## Releasing

Pushing a `v*` tag does the whole release. The workflow builds and signs the
APK and AAB, verifies the signature, writes `SHA256SUMS`, and creates the
GitHub Release with the files attached. Nothing is uploaded by hand.

Pick the number by what a user would notice, not by how much work it was.

| | When |
|---|---|
| `0.1.1` | Fixes only. Nothing looks or behaves differently. |
| `0.2.0` | A screen, a setting, or a behaviour changed. |
| `1.0.0` | The design is settled. Not a reward for a large release. |

Before tagging:

```bash
./scripts/bump-version.sh 0.2.0
```

Then write `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` and
the `es-419` copy of it. The English file becomes the body of the GitHub
Release as well as the text F-Droid shows, so write it for a reader, not as a
commit list. Update `metadata/com.abeant.gretel.yml` with the new build block
and `CurrentVersion`. Run the build once locally:

```bash
./gradlew :app:lintRelease :app:testReleaseUnitTest :app:assembleRelease
```

Then merge to `main` and tag it:

```bash
git checkout main && git pull
git tag v0.2.0
git push origin v0.2.0
```

The workflow refuses to publish if the tag does not match `versionName`, or if
the changelog for that `versionCode` is missing, so a mislabelled or silent
release fails before anything is built.
