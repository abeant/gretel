#!/usr/bin/env bash
# Copy repo hooks into .git/hooks so AI trailers are stripped on commit.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOOKS="$(git -C "${ROOT}" rev-parse --git-path hooks)"
mkdir -p "${HOOKS}"
cp "${ROOT}/.githooks/commit-msg" "${HOOKS}/commit-msg"
cp "${ROOT}/.githooks/prepare-commit-msg" "${HOOKS}/prepare-commit-msg"
chmod +x "${HOOKS}/commit-msg" "${HOOKS}/prepare-commit-msg"
echo "Installed commit-msg and prepare-commit-msg hooks."
