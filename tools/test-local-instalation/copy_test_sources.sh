#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

cp -R ../../driver-bundle/src/test/ src/
cp -R ../../playwright/src/test/ src/
mvn test --no-transfer-progress
