#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

TMP_DIR=$(mktemp -d)
echo "Created ${TMP_DIR}"

echo "Running TestApp..."
mvn compile exec:java -e -Dexec.mainClass=com.microsoft.playwright.testclifatjarversion.TestApp
