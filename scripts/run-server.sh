#!/usr/bin/env bash
set -euo pipefail

CONTROL_FILE=".server-control"
HEARTBEAT_FILE=".server-heartbeat"
STATUS_FILE=".server-status"

while true; do
  echo "running" > "$STATUS_FILE"
  java -jar paper.jar nogui
  if [[ -f "$CONTROL_FILE" ]]; then
    cmd=$(cat "$CONTROL_FILE")
    rm -f "$CONTROL_FILE"
    if [[ "$cmd" == "stop" ]]; then
      echo "stopped" > "$STATUS_FILE"
      exit 0
    fi
    if [[ "$cmd" == "restart" ]]; then
      echo "restarting" > "$STATUS_FILE"
      continue
    fi
  fi
  sleep 1
  echo "restarting" > "$STATUS_FILE"
done
