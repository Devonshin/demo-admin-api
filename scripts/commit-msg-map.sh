#!/usr/bin/env bash
# Commit message mapper: cleans tags, converts literal \n to real newlines, and appends a summary derived from diff
# Usage: used by git filter-branch --msg-filter
# Requirements: git, python3
set -euo pipefail

# Read the original commit message from stdin
ORIG_MSG=$(cat || true)
export ORIG_MSG

# Determine root commit to optionally skip modifying it
ROOT_SHA=$(git rev-list --max-parents=0 HEAD | head -n1 || true)
CUR_SHA=${GIT_COMMIT:-}

# If no commit context (e.g., standalone run), just transform text without summary
NO_COMMIT_CTX=0
if [[ -z "${CUR_SHA}" || -z "${ROOT_SHA}" ]]; then
  NO_COMMIT_CTX=1
fi

# If this is the root commit, return the original message unchanged
if [[ ${NO_COMMIT_CTX} -eq 0 && "${CUR_SHA}" == "${ROOT_SHA}" ]]; then
  printf "%s" "${ORIG_MSG}"
  exit 0
fi

# Use Python to:
# - remove [KO] and [FR] tags
# - convert literal \n sequences into actual newlines
# - trim trailing spaces on each line and collapse excessive blank lines
cleaned=$(python3 - "$@" << 'PY'
import sys, re, os
text = os.environ.get('ORIG_MSG', '')
# Remove [KO] and [FR] (case-insensitive, with or without spaces)
text = re.sub(r"\[(?:KO|FR)\]\s*", "", text, flags=re.IGNORECASE)
# Convert escaped \n into actual newlines
text = text.replace("\\n", "\n")

# Split sentences onto separate lines while keeping existing line breaks.
# We consider sentence-ending punctuation: . ! ? … and CJK variants 。 ！ ？
# Avoid splitting common Markdown list/code lines.
lines = [ln.rstrip() for ln in text.splitlines()]
segmented = []
for ln in lines:
    if not ln:
        segmented.append("")
        continue
    lns = ln.lstrip()
    if lns.startswith(('-', '*', '>')) or ln.strip().startswith('```') or ln.startswith('    '):
        segmented.append(ln.rstrip())
        continue
    # Insert a newline after sentence-ending punctuation followed by whitespace
    # and a non-space next token (letter/number), then split.
    split_ln = re.sub(r"(?<=[\.!?…。！？])\s+(?=\S)", "\n", ln)
    segmented.extend([part.rstrip() for part in split_ln.split('\n')])

# Normalize blank lines: collapse multiple consecutive empty lines to one and drop trailing blanks
normalized = []
blank = False
for ln in segmented:
    if ln == "":
        if not blank:
            normalized.append(ln)
        blank = True
    else:
        normalized.append(ln)
        blank = False
while normalized and normalized[-1] == "":
    normalized.pop()
print("\n".join(normalized))
PY
<<< "${ORIG_MSG}")

# Build a concise diff-based summary without listing filenames
ADDED=0
REMOVED=0
HAS_CODE=0
HAS_TEST=0
HAS_DOC=0
HAS_BUILD=0

if [[ ${NO_COMMIT_CTX} -eq 0 ]]; then
  # Gather numstat and categorize by path without echoing filenames
  while IFS=$'\t' read -r a d path; do
    # Skip empty lines
    [[ -z "$a" || -z "$d" || -z "$path" ]] && continue
    # Handle binary changes marked with '-'
    if [[ "$a" == "-" ]]; then a=0; fi
    if [[ "$d" == "-" ]]; then d=0; fi
    # Sum totals
    if [[ "$a" =~ ^[0-9]+$ ]]; then ADDED=$((ADDED + a)); fi
    if [[ "$d" =~ ^[0-9]+$ ]]; then REMOVED=$((REMOVED + d)); fi
    # Categorize by path (do not expose names in the message)
    p="$path"
    if [[ "$p" =~ (^|/)test(|s)(/|$) || "$p" =~ (^|/)src/test(/|$) ]]; then HAS_TEST=1; fi
    if [[ "$p" =~ (^|/)doc(|s)(/|$) || "$p" =~ (^|/)README|\.md$ ]]; then HAS_DOC=1; fi
    if [[ "$p" =~ (^|/)build(/|$) || "$p" =~ \\.gradle(|\.kts)$ || "$p" =~ (^|/)gradle(/|$) || "$p" =~ (^|/)Dockerfile$ || "$p" =~ (^|/)docker-compose\.yml$ ]]; then HAS_BUILD=1; fi
    # Treat source changes as code when under src/main or kotlin/java directories
    if [[ "$p" =~ (^|/)src/main(/|$) || "$p" =~ \\.kt$ || "$p" =~ \\.java$ ]]; then HAS_CODE=1; fi
  done < <(git show --numstat --no-renames --format= "${CUR_SHA}")
fi

# Compose bilingual summary lines without filenames
summary_ko="요약: 라인 +${ADDED}, -${REMOVED} | 범주: 코드=${HAS_CODE}, 테스트=${HAS_TEST}, 문서=${HAS_DOC}, 빌드=${HAS_BUILD}"
summary_fr="Synthèse: lignes +${ADDED}, -${REMOVED} | Catégories: code=${HAS_CODE}, tests=${HAS_TEST}, docs=${HAS_DOC}, build=${HAS_BUILD}"

# Append summary to the cleaned message. Ensure a blank line separation when needed
if [[ -n "${cleaned}" ]]; then
  printf "%s\n\n%s\n%s\n" "${cleaned}" "${summary_ko}" "${summary_fr}"
else
  printf "%s\n%s\n" "${summary_ko}" "${summary_fr}"
fi
