#!/usr/bin/env bash
# Exercise verify-release.sh against the fixtures in scripts/fixtures.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERIFY="${ROOT}/scripts/verify-release.sh"
GOOD="${ROOT}/scripts/fixtures/release-ok"

# Keep the run hermetic: fake zip fixtures would confuse a real apksigner or
# aapt on an Android developer machine, so SDK tool checks are force-skipped.
export GRETEL_VERIFY_SKIP_SDK=1

expect_pass() {
  if ! "${VERIFY}" --dir "$1" >/dev/null; then
    echo "  expected success, got failure: $1" >&2
    exit 1
  fi
}

expect_fail() {
  if "${VERIFY}" --dir "$1" >/dev/null 2>&1; then
    echo "  expected failure, got success: $1" >&2
    exit 1
  fi
}

copy_fixture() {
  local dst
  dst="$(mktemp -d)"
  cp "${GOOD}/"* "${dst}/"
  printf '%s' "${dst}"
}

echo "case 1: intact fixture passes"
expect_pass "${GOOD}"

echo "case 2: truncated artifact fails checksum and zip checks"
D="$(copy_fixture)"; truncate -s 10 "${D}/gretel.apk"
expect_fail "${D}"; rm -rf "${D}"

echo "case 3: manifest missing an entry fails"
D="$(copy_fixture)"; grep 'gretel.apk' "${GOOD}/SHA256SUMS" > "${D}/SHA256SUMS"
expect_fail "${D}"; rm -rf "${D}"

echo "case 4: manifest with a path traversal entry fails"
D="$(copy_fixture)"
printf '%s  %s\n' "$(sha256sum "${D}/gretel.apk" | cut -d' ' -f1)" "../gretel.apk" > "${D}/SHA256SUMS"
printf '%s  %s\n' "$(sha256sum "${D}/gretel.aab" | cut -d' ' -f1)" "gretel.aab" >> "${D}/SHA256SUMS"
expect_fail "${D}"; rm -rf "${D}"

echo "case 5: missing artifact fails"
D="$(copy_fixture)"; rm "${D}/gretel.aab"
sed -i '/gretel.aab/d' "${D}/SHA256SUMS"
expect_fail "${D}"; rm -rf "${D}"

echo "case 6: checksum mismatch fails"
D="$(copy_fixture)"
printf '%064d  %s\n' 0 "gretel.apk" > "${D}/SHA256SUMS"
grep 'gretel.aab' "${GOOD}/SHA256SUMS" >> "${D}/SHA256SUMS"
expect_fail "${D}"; rm -rf "${D}"

echo "all verify-release tests passed"
