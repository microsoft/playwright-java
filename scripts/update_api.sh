#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT

cd "$(dirname $0)/.."

./scripts/generate_api.sh

# Built from source and do local install.
mvn clean install --no-transfer-progress -D skipTests

echo "Updating browser versions in README.md"
mvn compile exec:java --f ./tools/update-docs-version -D exec.mainClass=com.microsoft.playwright.tools.UpdateBrowserVersions
