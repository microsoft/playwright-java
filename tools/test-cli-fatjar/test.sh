#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

echo "Running CLI..."
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

echo "Running TestApp..."
mvn compile exec:java -e -Dexec.mainClass=com.microsoft.playwright.testclifatjar.TestApp
