#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

DRIVER_VERSION=$(head -1 ./scripts/DRIVER_VERSION)

# api.json is generated from .upstream/, the microsoft/playwright checkout at the commit in
# scripts/DRIVER_VERSION that the driver is built from (scripts/build_driver.sh). PW_SRC_DIR
# generates it from another checkout, to try a change to the docs or to the generator. The
# committed files must come from .upstream/: CI generates them again and fails when they differ.
if [[ -n "$PW_SRC_DIR" ]]; then
  UPSTREAM_DIR="$PW_SRC_DIR"
  echo "Generating from $UPSTREAM_DIR (PW_SRC_DIR), not from .upstream/"
else
  UPSTREAM_DIR="./.upstream"
  if [[ "$(git -C "$UPSTREAM_DIR" rev-parse HEAD 2> /dev/null)" != "$DRIVER_VERSION" ]]; then
    echo ".upstream/ is not at $DRIVER_VERSION, the commit in scripts/DRIVER_VERSION. Run: scripts/build_driver.sh"
    exit 1
  fi
fi

echo "Updating api.json from $UPSTREAM_DIR"
API_JSON_MODE=1 node "$UPSTREAM_DIR/utils/doclint/generateApiJson.js" \
  > ./tools/api-generator/src/main/resources/api.json

mvn compile -f ./tools/api-generator --no-transfer-progress

echo "Regenerating Java interfaces"
mvn exec:java --f ./tools/api-generator -D exec.mainClass=com.microsoft.playwright.tools.ApiGenerator
