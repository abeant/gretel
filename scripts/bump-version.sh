#!/usr/bin/env bash
# Bump versionName and increment versionCode in app/build.gradle.kts.
# Usage: ./scripts/bump-version.sh 0.1.1
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FILE="${ROOT}/app/build.gradle.kts"

if [[ $# -ne 1 ]]; then
  echo "usage: $0 <versionName>" >&2
  exit 2
fi

NEW_NAME="$1"
if [[ ! "${NEW_NAME}" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-].*)?$ ]]; then
  echo "versionName should look like 0.1.1" >&2
  exit 2
fi

CURRENT_CODE="$(sed -n 's/.*versionCode = \([0-9][0-9]*\).*/\1/p' "${FILE}" | head -n1)"
if [[ -z "${CURRENT_CODE}" ]]; then
  echo "could not read versionCode from ${FILE}" >&2
  exit 1
fi
NEW_CODE=$((CURRENT_CODE + 1))

# Portable in-place replace (GNU and BSD sed).
tmp="$(mktemp)"
sed \
  -e "s/versionName = \"[^\"]*\"/versionName = \"${NEW_NAME}\"/" \
  -e "s/versionCode = ${CURRENT_CODE}/versionCode = ${NEW_CODE}/" \
  "${FILE}" > "${tmp}"
mv "${tmp}" "${FILE}"

echo "versionName ${NEW_NAME}"
echo "versionCode ${NEW_CODE}"
echo "Add fastlane/metadata/android/en-US/changelogs/${NEW_CODE}.txt before you tag."
