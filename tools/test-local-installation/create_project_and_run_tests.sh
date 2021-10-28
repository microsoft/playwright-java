#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"


LOG_DIR=$(pwd)/../../logs
PROJECT_DIR=$(mktemp -d)
echo "Creating project in $PROJECT_DIR"
cp -R . $PROJECT_DIR
cp -R ../../assertions/src/test/ $PROJECT_DIR/src/
cp -R ../../driver-bundle/src/test/ $PROJECT_DIR/src/
cp -R ../../playwright/src/test/ $PROJECT_DIR/src/
cd $PROJECT_DIR

mvn test --no-transfer-progress -D test=*TestPageRoute*

cp -R target/surefire-reports $LOG_DIR

rm -rf $PROJECT_DIR
