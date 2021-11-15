#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT

cd "$(dirname $0)/.."

# Remove artifacts from previous driver (for local builds).
mvn clean

# Built from source and do local install.
mvn install --no-transfer-progress -D skipTests

echo "Updating browser versions in README.md"
mvn compile exec:java --f ./tools/update-docs-version -D exec.mainClass=com.microsoft.playwright.tools.UpdateBrowserVersions
