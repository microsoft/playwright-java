#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

echo "Running TestApp..."
mvn compile exec:java -e -Dexec.mainClass=com.microsoft.playwright.testclifatjar.TestApp
