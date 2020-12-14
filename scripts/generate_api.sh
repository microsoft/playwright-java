#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)/.."

echo "Updating api.json"
./driver-bundle/src/main/resources/driver/linux/playwright-cli print-api-json > ./api-generator/src/main/resources/api.json

mvn compile -projects api-generator

echo "Regenerating Java interfaces"
mvn exec:java --projects api-generator -Dexec.mainClass=com.microsoft.playwright.tools.ApiGenerator
