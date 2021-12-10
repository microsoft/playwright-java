#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

TMP_DIR=$(mktemp -d)
echo "Created ${TMP_DIR}"

echo "Running CLI..."
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="--version" 2>&1 | tee ${TMP_DIR}/cli.txt

echo "Running TestApp..."
mvn compile exec:java -e -Dexec.mainClass=com.microsoft.playwright.testcliversion.TestApp 2>&1 | tee ${TMP_DIR}/app.txt

CLI_VERSION=$(cat ${TMP_DIR}/cli.txt | tail -n 1 | cut -d\  -f2)
PACKAGE_VERSION=$(cat ${TMP_DIR}/app.txt | grep ImplementationVersion | cut -d\  -f2)

rm -rf $TMP_DIR

echo "Comparing versions: ${CLI_VERSION} and ${PACKAGE_VERSION}"

if [[ "$CLI_VERSION" == "$PACKAGE_VERSION" ]];
then
  echo "SUCCESS.";
else
  echo "FAIL.";
  exit 1;
fi;

