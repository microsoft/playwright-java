#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

DRIVER_PATH=./driver-bundle/src/main/resources/driver
case $(uname) in
Darwin)
  PLAYWRIGHT_CLI=$DRIVER_PATH/mac/playwright.sh
  ;;
Linux|MINGW32*|MINGW64*)
  PLAYWRIGHT_CLI=$DRIVER_PATH/linux/playwright.sh
  ;;
esac

echo "Updating api.json from $($PLAYWRIGHT_CLI --version)"

$PLAYWRIGHT_CLI print-api-json > ./tools/api-generator/src/main/resources/api.json

mvn compile -f ./tools/api-generator --no-transfer-progress

echo "Regenerating Java interfaces"
mvn exec:java --f ./tools/api-generator -D exec.mainClass=com.microsoft.playwright.tools.ApiGenerator
