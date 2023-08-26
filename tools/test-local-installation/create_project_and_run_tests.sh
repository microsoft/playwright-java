#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"


PROJECT_DIR=$(mktemp -d)
echo "Creating project in $PROJECT_DIR"
cp -R . $PROJECT_DIR
cp -R ../../driver-bundle/src/test/ $PROJECT_DIR/src/
cp -R ../../playwright/src/test/ $PROJECT_DIR/src/
cd $PROJECT_DIR

mvn test --no-transfer-progress

rm -rf $PROJECT_DIR
