#!/usr/bin/env bash
set -euo pipefail
if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi
exec java -jar target/message-logger-1.0.0.jar --config config/application.yaml "$@"
