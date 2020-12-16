#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)/.."

PLAYWRIGHT_CLI=./driver-bundle/src/main/resources/driver/linux/playwright-cli
echo "Updating api.json from $($PLAYWRIGHT_CLI --version)"

$PLAYWRIGHT_CLI print-api-json > ./api-generator/src/main/resources/api.json

mvn compile -projects api-generator --no-transfer-progress

echo "Regenerating Java interfaces"
mvn exec:java --projects api-generator -Dexec.mainClass=com.microsoft.playwright.tools.ApiGenerator
