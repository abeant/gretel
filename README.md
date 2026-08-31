<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset=".github/assets/gretel-lockup-dark.svg">
    <source media="(prefers-color-scheme: light)" srcset=".github/assets/gretel-lockup.svg">
    <img src=".github/assets/gretel-lockup.svg" width="520" alt="Gretel">
  </picture>
</p>

<p align="center"><strong>Turn an Android device into a focused, single-app device.</strong></p>

<p align="center">
  <a href="https://github.com/abeant/gretel/actions/workflows/ci.yml"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white" alt="GitHub Actions CI"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 8.0 or newer">
  <a href="LICENSE"><img src="https://img.shields.io/badge/licence-Apache--2.0-2f6fda?style=flat-square" alt="Apache 2.0 licence"></a>
</p>

Gretel is a minimal Android launcher and an anti-launcher built around one app instead of an app grid. Choose the app you want to use, make Gretel your home app, and that app becomes the place your device always returns to. There is no feed or launcher clutter competing for your attention.

It is especially useful on e-readers, including devices where you want KOReader or another reading app to feel like the main interface. It works just as well on a spare phone or tablet that you want to turn into a focused tool. Gretel does not lock the device down: its settings, Android settings, and your previous launcher remain accessible.

## What it does

- **One chosen app replaces the usual home screen.** Use your Home button or gesture and Gretel opens it.
- **Your app stays in front.** Gretel can bring it back after a restart or when the app exits. Both behaviors are optional.
- **Settings are always within reach.** Use Home twice quickly to open Gretel instead of your chosen app.
- **Designed for e-ink.** High-contrast black-and-white screens, large targets, no ripples, and no unnecessary animation.
- **Private by construction.** No account, ads, analytics, network access, or broad package visibility.
- **Easy to undo.** Choose another home app in Android settings or uninstall Gretel like any other app.
- **English and Spanish.** Android follows the device language automatically; Android 13 and newer also expose Gretel in per-app language settings.

## Made for one-purpose devices

- Make KOReader or another reading app the default experience on an Android e-reader.
- Turn an older phone or tablet into a distraction-free reading, writing, reference, or dashboard device.
- Keep the device easy to maintain: Gretel is a minimal launcher, not kiosk software, and normal Android settings remain available.

## See it on a BOOX

These screenshots were captured from Gretel running on a BOOX Nova Air.

<p align="center">
  <img src=".github/assets/welcome.png" width="45%" alt="Gretel welcome screen">
  <img src=".github/assets/choose-app.png" width="45%" alt="Choose an installed app in Gretel">
</p>

## Install

Prebuilt APKs will appear under [Releases](https://github.com/abeant/gretel/releases) when the first signed version is ready. Until then, you can build the app from source using the instructions below.

Once you have an APK:

1. Open the APK on your Android device. If Android asks, allow your browser or file manager to install unknown apps.
2. Open Gretel and choose the app you want to use.
3. Follow the prompt to set Gretel as your home app.

That is it. Use Home once to return to your chosen app. Use Home twice quickly to open Gretel's settings. The default timing is 800 ms and can be changed to 500 or 1200 ms.

> Gretel is not a kiosk or parental-control app. It is intentionally reversible and does not use Device Admin, Accessibility, overlays, or lock task mode.

## Compatibility

| | |
|---|---|
| Android | 8.0 Oreo or newer |
| Architectures | Any architecture supported by Android; Gretel contains no native code |
| Tested on | BOOX Nova Air, Android 10 |
| Package | `com.abeant.gretel` |

Android handles the home-app selection. Gretel cannot and does not make itself the default without your confirmation. Device manufacturers sometimes move that setting; if the normal prompt does not appear, open **Settings → Apps → Default apps → Home app**.

## Privacy

Gretel collects nothing and has no internet permission. Your chosen app and preferences stay on the device. See the complete [privacy policy](PRIVACY.md).

## Build from source

You will need JDK 17 and the Android SDK 36 platform and build tools.

```bash
./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Every `v*` tag builds a signed APK and Android App Bundle, verifies the APK signature, generates checksums, and publishes the files in a GitHub Release.

Contributions are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## Licence

Gretel is available under the [Apache License 2.0](LICENSE).
