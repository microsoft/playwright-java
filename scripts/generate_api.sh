#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

PLAYWRIGHT_CLI="unknown"
case $(uname) in
Darwin)
  PLAYWRIGHT_CLI=./driver-bundle/src/main/resources/driver/mac/package/cli.js
  ;;
Linux)
  PLAYWRIGHT_CLI=./driver-bundle/src/main/resources/driver/linux/package/cli.js
  ;;
MINGW64*)
  PLAYWRIGHT_CLI=./driver-bundle/src/main/resources/driver/win32_x64/package/cli.js
  ;;
*)
  echo "Unknown platform '$(uname)'"
  exit 1;
  ;;
esac

echo "Updating api.json from $($PLAYWRIGHT_CLI --version)"

node $PLAYWRIGHT_CLI print-api-json > ./tools/api-generator/src/main/resources/api.json

mvn compile -f ./tools/api-generator --no-transfer-progress

echo "Regenerating Java interfaces"
mvn exec:java --f ./tools/api-generator -D exec.mainClass=com.microsoft.playwright.tools.ApiGenerator
