#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# Usage: ./ai_trigger.sh [date]
#   ./ai_trigger.sh                  today
#   ./ai_trigger.sh 2026-11-12       a specific day
#   ./ai_trigger.sh yesterday
#   ./ai_trigger.sh "3 days ago"

if ! day=$(date -d "${1:-today}" +%F 2>/dev/null); then
    echo "Not a valid date: ${1:-}" >&2
    echo "Usage: ./ai_trigger.sh [YYYY-MM-DD | today | yesterday | \"3 days ago\"]" >&2
    exit 1
fi

echo "Generating the digest for $day. With the 7B model this can take a while..."
start=$(date +%s)

curl -sS --fail -X POST "http://localhost:8083/dev/digests/$day"

echo "Done in $(( $(date +%s) - start )) seconds."
echo
./postgres.sh -At -c "SELECT content FROM digests.digests WHERE day = '$day';"