#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# "$@" passes along whatever you add after the script name:
# ./db.sh gives you interactive psql
# ./db.sh -c "SELECT count(*) FROM articles;" runs one query and exits
docker compose exec postgres psql -U news -d news "$@"
