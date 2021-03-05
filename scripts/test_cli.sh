#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

VERSION=$(mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -f playwright/pom.xml  -Dexec.args=-V | grep Version)

if [[ $VERSION == Version* ]]; then
  echo "[SUCCESS] got cli version: $VERSION"
else
  echo "[FAIL] failed to get cli version: $VERSION"
  exit 1
fi
