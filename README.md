<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset=".github/assets/gretel-lockup-dark.svg">
    <source media="(prefers-color-scheme: light)" srcset=".github/assets/gretel-lockup.svg">
    <img src=".github/assets/gretel-lockup.svg" width="420" alt="Gretel">
  </picture>
</p>

<p align="center"><strong>The anti-launcher. Your home screen is one app.</strong></p>

<p align="center"><a href="https://abeant.github.io/gretel/">abeant.github.io/gretel</a></p>

<p align="center">
  <a href="https://github.com/abeant/gretel/actions/workflows/ci.yml"><img src="https://github.com/abeant/gretel/actions/workflows/ci.yml/badge.svg" alt="CI status"></a>
  <a href="https://github.com/abeant/gretel/releases/latest"><img src="https://img.shields.io/github/v/release/abeant/gretel?style=flat-square&color=2f6fda&label=release" alt="Latest release"></a>
  <a href="https://github.com/abeant/gretel/releases"><img src="https://img.shields.io/github/downloads/abeant/gretel/total?style=flat-square&color=3DDC84&label=downloads" alt="Total downloads"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 8.0 or newer">
  <a href="LICENSE"><img src="https://img.shields.io/badge/licence-Apache--2.0-2f6fda?style=flat-square" alt="Apache 2.0 licence"></a>
  <img src="https://img.shields.io/badge/tracking-none-000000?style=flat-square" alt="No tracking">
</p>

<!--
Add these once each channel is live:
<a href="https://f-droid.org/packages/com.abeant.gretel/"><img src="https://img.shields.io/f-droid/v/com.abeant.gretel?style=flat-square&logo=fdroid&logoColor=white&label=F-Droid" alt="F-Droid version"></a>
<a href="https://play.google.com/store/apps/details?id=com.abeant.gretel"><img src="https://img.shields.io/badge/Google%20Play-available-3DDC84?style=flat-square&logo=googleplay&logoColor=white" alt="Google Play"></a>
-->

Every Android launcher hands you a grid of apps and asks you to choose again, every time you press Home. Gretel asks once.

Pick the app you actually want to use, make Gretel your home app, and that app becomes the place your device always returns to. No grid, no feed, nothing else competing for your attention. Press Home twice quickly and Gretel's settings appear, so you are never stuck.

It was built for e-readers, where KOReader or another reading app should simply *be* the device. It works just as well on a spare phone or tablet you want to turn into a single-purpose tool.

<p align="center">
  <img src=".github/assets/welcome.png" width="42%" alt="Gretel welcome screen on a BOOX Nova Air">
  <img src=".github/assets/choose-app.png" width="42%" alt="Choosing an installed app in Gretel">
</p>
<p align="center"><sub>Captured on a BOOX Nova Air.</sub></p>

## Install

<a href="https://github.com/abeant/gretel/releases/latest"><img src="https://img.shields.io/badge/Download%20APK-GitHub%20Releases-2f6fda?style=for-the-badge&logo=github&logoColor=white" alt="Download the APK from GitHub Releases"></a>

1. Download `gretel.apk` from [Releases](https://github.com/abeant/gretel/releases/latest) and open it on your Android device. If Android asks, allow your browser or file manager to install unknown apps.
2. Open Gretel and choose the app you want to use.
3. Follow the prompt to set Gretel as your home app.

Verify the download against `SHA256SUMS` in the same release if you want to confirm the file is intact.

That is it. Home once returns you to your chosen app. Home twice quickly opens Gretel's settings. The default double-Home timing is 800 ms and can be set to 500 or 1200 ms.

Gretel is also headed to F-Droid. Watch [Releases](https://github.com/abeant/gretel/releases) or star the repo to hear about it.

## What it does

- **One chosen app replaces the home screen.** Your Home button or gesture opens it, every time.
- **Your app stays in front.** Gretel can bring it back after a restart or when the app exits. Both behaviors are optional.
- **Settings are always within reach.** Home twice quickly opens Gretel instead of your chosen app.
- **Designed for e-ink.** High-contrast black-and-white screens, large targets, no ripples, no unnecessary animation.
- **Private by construction.** No account, ads, analytics, network permission, or broad package visibility.
- **Easy to undo.** Choose another home app in Android settings, or uninstall Gretel like any other app.
- **English and Spanish.** Android follows the device language automatically. Android 13 and newer also expose Gretel in per-app language settings.

## Made for one-purpose devices

- Make KOReader or another reading app the default experience on an Android e-reader.
- Turn an older phone or tablet into a distraction-free reading, writing, reference, or dashboard device.
- Give someone a device that opens to exactly one thing, without kiosk software or an admin console.

## How it compares

| | Stock launcher | Minimal launchers | Kiosk / MDM apps | **Gretel** |
|---|---|---|---|---|
| Home opens | An app grid | A short app list | A locked app | **The one app you chose** |
| Choices per Home press | Many | A few | None | **None** |
| Reversible by the user | Yes | Yes | No | **Yes** |
| Device Admin, Accessibility, or overlays | No | Sometimes | Yes | **Never** |
| Built for e-ink | No | Rarely | No | **Yes** |
| Network permission | Usually | Sometimes | Yes | **None** |

> Gretel is not a kiosk or parental-control app. It is intentionally reversible and does not use Device Admin, Accessibility, overlays, or lock task mode. If you need to stop someone from leaving an app, Gretel is the wrong tool.

## Compatibility

| Item | Detail |
|---|---|
| Android | 8.0 Oreo or newer |
| Architectures | Any architecture supported by Android. Gretel contains no native code. |
| Tested on | BOOX Nova Air, Android 10 |
| Package | `com.abeant.gretel` |
| Size | Under 1 MB, no bundled SDKs, no native code |

Android handles the home-app selection. Gretel cannot and does not make itself the default without your confirmation. Device manufacturers sometimes move that setting. If the normal prompt does not appear, open **Settings, Apps, Default apps, Home app**.

## Questions

**Does it lock the device?**
No. Gretel is a home app, not a cage. Android settings, your previous launcher, and Gretel's own settings all stay reachable.

**How do I get back to Gretel once my app is in front?**
Press Home twice quickly. That opens Gretel's settings instead of your chosen app.

**What if I uninstall the app I chose?**
Gretel notices and offers to pick a different one instead of leaving you on a blank screen.

**Does it run in the background or drain the battery?**
No. There is no foreground service, persistent notification, boot receiver, alarm, or wake lock. Gretel only runs when Android delivers a Home press.

**Does it work on a Kindle?**
No. Amazon's Fire tablets and Kindle e-readers do not expose Android's home-app setting. Gretel needs a device where that setting is available, which covers BOOX, most other Android e-readers, and ordinary phones and tablets.

**Can I switch back?**
Any time. Open **Settings, Apps, Default apps, Home app** and pick another launcher, or just uninstall Gretel.

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

<p align="center"><sub>If Gretel makes your device quieter, a star helps other people find it.</sub></p>
