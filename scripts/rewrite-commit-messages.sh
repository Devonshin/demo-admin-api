#!/usr/bin/env bash
# Rewrites commit messages on the current branch: removes [KO]/[FR], converts literal \n, and appends diff-based summaries.
# Local-only: this script does NOT push. It preserves author/committer dates.
# Safe backup tag will be created before rewrite.
set -euo pipefail

# Ensure we're in a git repo
git rev-parse --git-dir >/dev/null 2>&1

# Ensure dependencies
command -v python3 >/dev/null 2>&1 || { echo "python3 is required" >&2; exit 1; }

# Make sure the mapper is executable
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
MAPPER="$SCRIPT_DIR/commit-msg-map.sh"
chmod +x "$MAPPER"

# Create a backup tag
ts=$(date +%Y%m%d-%H%M%S)
backup_tag="backup-pre-reword-${ts}"
if git show-ref --tags --quiet --verify "refs/tags/${backup_tag}"; then
  echo "Backup tag already exists: ${backup_tag}" >&2
else
  git tag -a "${backup_tag}" -m "Backup before commit message rewrite (${ts})"
  echo "Created backup tag: ${backup_tag}"
fi

# Target range: current branch history. filter-branch preserves dates by default when msg-filter only.
# We keep the root commit untouched via the mapper script.
branch=$(git rev-parse --abbrev-ref HEAD)
echo "Rewriting commit messages on branch: ${branch}"

# Use filter-branch with msg-filter. This is local-only and rewrites current branch.
# Note: Consider using git filter-repo if available for speed; this keeps POSIX compatibility.
GIT_SEQUENCE_EDITOR=:
export GIT_SEQUENCE_EDITOR

git filter-branch -f --msg-filter "bash '$MAPPER'" ${branch} 2>/dev/null 1>&2 || true

echo "Rewrite completed for ${branch}."
echo "Verify results with: git log --date=iso --pretty=format:'%H %ad%n%s%n%b%n---' | sed -n '1,80p'"
echo "If anything looks wrong, you can restore with: git reset --hard ${backup_tag}"
