#!/usr/bin/env bash
# Verify a published Gretel release without an Android SDK.
#
# Downloads the release artifacts with gh (or uses a local directory), then
# checks SHA256SUMS and zip archive integrity. apksigner and aapt checks run
# only when those tools are on PATH; otherwise they are reported as skipped.
# Set GRETEL_VERIFY_SKIP_SDK=1 to force-skip the SDK tool checks (used by the
# fixture tests so they behave the same on machines with Android build-tools).
#
# Usage:
#   ./scripts/verify-release.sh v0.2.0        # fetch and verify a tag
#   ./scripts/verify-release.sh --dir PATH    # verify files already on disk
set -euo pipefail

FAIL=0
SKIP=0

say() { printf '%s\n' "$*"; }
fail() { say "FAIL: $*"; FAIL=$((FAIL + 1)); }
skip() { say "SKIP: $*"; SKIP=$((SKIP + 1)); }
ok() { say "OK: $*"; }

if [[ "${1:-}" == "--dir" ]]; then
  WORK="${2:?usage: $0 --dir PATH}"
elif [[ "${1:-}" =~ ^v[0-9] ]]; then
  WORK="$(mktemp -d "${TMPDIR:-/tmp}/gretel-release-XXXXXXXX")"
  gh release download "$1" --repo abeant/gretel \
    --pattern 'gretel.apk' --pattern 'gretel.aab' --pattern 'SHA256SUMS' \
    --dir "${WORK}" --clobber
  say "downloaded $1 artifacts to ${WORK}"
else
  say "usage: $0 <vX.Y.Z> | --dir PATH" >&2
  exit 2
fi

cd "${WORK}"

EXPECTED="gretel.apk gretel.aab"

if [[ ! -f SHA256SUMS ]]; then
  fail "SHA256SUMS missing"
else
  # The manifest must list exactly the expected bare filenames: no extra
  # entries, no omissions, no paths.
  MANIFEST_OK=1
  mapfile -t entries < <(awk 'NF {print $NF}' SHA256SUMS)
  if [[ "${#entries[@]}" -ne 2 ]]; then
    MANIFEST_OK=0
  fi
  for entry in "${entries[@]}"; do
    case "${entry}" in
      gretel.apk|gretel.aab) ;;
      *) MANIFEST_OK=0 ;;
    esac
  done
  for wanted in ${EXPECTED}; do
    grep -Eq "^[0-9a-fA-F]{64}  ${wanted}$" SHA256SUMS || MANIFEST_OK=0
  done
  if [[ "${MANIFEST_OK}" -eq 0 ]]; then
    fail "SHA256SUMS must list exactly: ${EXPECTED} (no paths or extra entries)"
  elif sha256sum -c SHA256SUMS; then
    ok "all checksums in SHA256SUMS match"
  else
    fail "checksum mismatch (see output above)"
  fi
fi

for artifact in ${EXPECTED}; do
  if [[ ! -f "${artifact}" ]]; then
    fail "${artifact} missing"
    continue
  fi
  if unzip -t "${artifact}" >/dev/null 2>&1; then
    ok "${artifact} is a readable zip archive"
  else
    fail "${artifact} fails zip integrity check"
  fi
done

if [[ -n "${GRETEL_VERIFY_SKIP_SDK:-}" ]]; then
  skip "GRETEL_VERIFY_SKIP_SDK set; apksigner and aapt checks skipped"
else
  if command -v apksigner >/dev/null 2>&1; then
    if apksigner verify --verbose --print-certs gretel.apk; then
      ok "apksigner signature check passed"
    else
      fail "apksigner signature check failed"
    fi
  else
    skip "apksigner not on PATH; signature verification must run on a machine with Android build-tools"
  fi

  AAPT=""
  for t in aapt2 aapt; do
    if command -v "${t}" >/dev/null 2>&1; then AAPT="${t}"; break; fi
  done
  if [[ -n "${AAPT}" ]]; then
    if BADGING="$("${AAPT}" dump badging gretel.apk 2>/dev/null)" \
      && grep -q '^package:' <<<"${BADGING}"; then
      grep -E "^package:|^application-label:" <<<"${BADGING}" || true
      ok "manifest badging printed above"
    else
      fail "${AAPT} could not read package metadata from gretel.apk"
    fi
  else
    skip "aapt/aapt2 not on PATH; package and version metadata unchecked"
  fi
fi

say "-----"
say "failures: ${FAIL}, skipped: ${SKIP}"
[[ "${FAIL}" -eq 0 ]]
