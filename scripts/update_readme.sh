#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

DRIVER_VERSION=$(head -1 ./scripts/DRIVER_VERSION)

if [[ -n "$PW_SRC_DIR" ]]; then
  UPSTREAM_DIR="$PW_SRC_DIR"
else
  UPSTREAM_DIR="./.upstream"
  if [[ "$(git -C "$UPSTREAM_DIR" rev-parse HEAD 2> /dev/null)" != "$DRIVER_VERSION" ]]; then
    echo ".upstream/ is not at $DRIVER_VERSION, the commit in scripts/DRIVER_VERSION. Run: scripts/build_driver.sh"
    exit 1
  fi
fi

echo "Updating browser versions in README.md"
node ./scripts/update_readme.js "$UPSTREAM_DIR"
