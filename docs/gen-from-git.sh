#!/usr/bin/env bash
# Draft new journey.json phase entries from git log.
# Usage: bash docs/gen-from-git.sh
set -euo pipefail
echo "Recent commits (suggest appending one of these to docs/journey.json -> phases[]):"
echo
git log --format="%h | %ad | %s" --date=short -10
echo
echo "Stat for HEAD:"
git show --stat --oneline HEAD | head -30
