#!/usr/bin/env bash
set -euo pipefail

git pull                # pulls the files we still need: compose.yaml, filebeat.yml, .env.example
docker compose pull	# download the images CI built on GitHub
docker compose up -d	# restart only the services whose image changed
docker image prune -f	# delete old, unused images, to prevent the disk from filling up
docker compose ps
