#!/usr/bin/env bash
# Strip or reject AI / agent attribution. Used by git hooks and CI.
set -euo pipefail

# Cursor, Claude, Codex, Copilot, Grok, OpenCode, Gemini, Devin, Windsurf, Aider.
AGENT_RE='cursor|cursoragent|claude|anthropic|openai|chatgpt|codex|copilot|grok|xai|opencode|gemini|devin|windsurf|aider'
TRAILER_RE="^[[:space:]]*(Co-authored-by|Signed-off-by):.*(${AGENT_RE})"
FOOTER_RE='Made with \[[Cc]ursor\]|CURSOR_AGENT|open-in-cursor|cursor\.com/agents|cursoragent@|Generated (with|by)[[:space:]]+(Claude|Cursor|Copilot|ChatGPT|Codex|Grok|OpenCode|Gemini)|🤖[[:space:]]*Generated'
BRANCH_RE='^(cursor|copilot|claude|codex|grok|opencode|devin|gemini|windsurf)/'

is_dirty() {
  printf '%s\n' "$1" | grep -Eiq "${TRAILER_RE}|${FOOTER_RE}"
}

strip_message() {
  local file="$1"
  local tmp
  tmp="$(mktemp)"
  grep -Eiv "${TRAILER_RE}|${FOOTER_RE}" "$file" > "$tmp" || true
  # Drop trailing empty lines.
  while [[ -s "$tmp" ]] && [[ -z "$(tail -n 1 "$tmp")" ]]; do
    sed '$d' "$tmp" > "${tmp}.n"
    mv "${tmp}.n" "$tmp"
  done
  mv "$tmp" "$file"
}

die() {
  echo "AI attribution is not allowed in this repository." >&2
  echo "$*" >&2
  exit 1
}

cmd="${1:-check}"

case "$cmd" in
  strip)
    strip_message "${2:?commit message file required}"
    if is_dirty "$(cat "$2")"; then
      die "commit message still has agent attribution after strip."
    fi
    ;;
  check)
    ROOT="$(cd "$(dirname "$0")/.." && pwd)"
    cd "$ROOT"

    if [[ -n "${GITHUB_HEAD_REF:-}" ]] && printf '%s' "$GITHUB_HEAD_REF" | grep -Eiq "$BRANCH_RE"; then
      die "branch name '${GITHUB_HEAD_REF}' looks like an agent branch. Rename it."
    fi
    if [[ -n "${GITHUB_REF_NAME:-}" ]] && printf '%s' "$GITHUB_REF_NAME" | grep -Eiq "$BRANCH_RE"; then
      die "ref '${GITHUB_REF_NAME}' looks like an agent branch. Rename it."
    fi

    if [[ -n "${GITHUB_EVENT_PATH:-}" && -f "${GITHUB_EVENT_PATH}" ]]; then
      body="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1])).get("pull_request",{}).get("body") or "")' "${GITHUB_EVENT_PATH}" 2>/dev/null || true)"
      if [[ -n "$body" ]] && is_dirty "$body"; then
        die "pull request body has agent attribution or a vendor footer."
      fi
    fi

    if git rev-parse --git-dir >/dev/null 2>&1; then
      log="$(git log --format=%B HEAD)"
      if is_dirty "$log"; then
        die "a commit on this history has agent attribution."
      fi
    fi

    if git grep -nI -Eiq "$FOOTER_RE" -- ':!LICENSE' ':!app/src/main/assets/LICENSE.txt' ':!scripts/no-ai-attribution.sh' .; then
      die "tracked files contain agent marketing or footer text."
    fi
    ;;
  *)
    echo "usage: $0 strip <commit-msg-file> | check" >&2
    exit 2
    ;;
esac
