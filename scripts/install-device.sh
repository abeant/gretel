#!/usr/bin/env bash
# Install Gretel on a USB-attached device and open Hatch.
#
# On a BOOX (or any first-time adb host): enable USB debugging, then accept
# the RSA fingerprint prompt ON THE E-INK SCREEN. `adb devices` must show
# "device", not "unauthorized".
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

VARIANT="debug"
if [[ "${1:-}" == "--release" ]]; then
  VARIANT="release"
elif [[ "${1:-}" != "" ]]; then
  echo "usage: $0 [--release]" >&2
  exit 2
fi

if ! command -v adb >/dev/null 2>&1; then
  if [[ -n "${ANDROID_HOME:-}" && -x "${ANDROID_HOME}/platform-tools/adb" ]]; then
    export PATH="${ANDROID_HOME}/platform-tools:${PATH}"
  elif [[ -n "${ANDROID_SDK_ROOT:-}" && -x "${ANDROID_SDK_ROOT}/platform-tools/adb" ]]; then
    export PATH="${ANDROID_SDK_ROOT}/platform-tools:${PATH}"
  fi
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "adb not on PATH. Install platform-tools or set ANDROID_HOME." >&2
  exit 1
fi

echo "USB debugging must be on. Accept the RSA prompt on the e-ink screen if asked."
adb start-server
STATE="$(adb get-state 2>/dev/null || true)"
if [[ "${STATE}" != "device" ]]; then
  echo "No authorized device (adb get-state=${STATE:-none})." >&2
  echo "If the device is plugged in, check the RSA dialog on the panel." >&2
  adb devices >&2 || true
  exit 1
fi

if [[ "${VARIANT}" == "release" ]]; then
  ./gradlew :app:installRelease
else
  ./gradlew :app:installDebug
fi

adb shell monkey -p com.abeant.gretel -c android.intent.category.LAUNCHER 1
echo "Hatch should be in front. Package: com.abeant.gretel"
