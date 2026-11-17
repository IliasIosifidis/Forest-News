#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

BACKUP_DIR="${BACKUP_DIR:-$HOME/backups/forest-news}"
KEEP_DAYS=14
mkdir -p "$BACKUP_DIR"

file="$BACKUP_DIR/news-$(date +%F-%H%M).dump"

docker compose exec -T postgres pg_dump -U news -d news --format=custom > "$file.partial"
mv "$file.partial" "$file"

find "$BACKUP_DIR" -name 'news-*.dump' -mtime +"$KEEP_DAYS" -delete
echo "Backup written: $file ($(du -h "$file" | cut -f1))"
