#!/usr/bin/env bash
# Opens Grafana, Prometheus and Kibana from the server through one SSH tunnel.
# Run it on your PC (WSL). Ctrl+C closes the tunnel.
set -euo pipefail

SERVER="${1:-ilias@forest-news}"

echo "Grafana:    http://localhost:3000"
echo "Prometheus: http://localhost:9090"
echo "Kibana:     http://localhost:5601"
echo "Ctrl+C to close the tunnel."

# open the browser tabs once the tunnel has had a moment to start
(
    sleep 3
    explorer.exe "http://localhost:3000" || true
    explorer.exe "http://localhost:9090" || true
) &

ssh -N -o ExitOnForwardFailure=yes \
    -L 3000:localhost:3000 \
    -L 9090:localhost:9090 \
    -L 5601:localhost:5601 \
    "$SERVER"