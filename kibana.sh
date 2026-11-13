#!/usr/bin/env bash
set -euo pipefail

SERVER="${SERVER:-ilias@asus-laptop}"
URL="http://localhost:5601"

echo "Tunnel to $SERVER open. Kibana at $URL. Ctrl+c to close."

if command -v explorer.exe >/dev/null; then
	(sleep 2 && explorer.exe "$URL") & #WSL: open in windows browser
elif command -v xdg-open >/dev/null; then
	(sleep 2 && xdg-open "$URL") &	#Linux desktop
fi

ssh -N -o ExitOnForwardFailure=yes -L 5601:localhost:5601 "$SERVER"
