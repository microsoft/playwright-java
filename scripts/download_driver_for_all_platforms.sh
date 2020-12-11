#!/bin/bash

set -e
set +x

FILE_PREFIX=playwright-cli-0.170.0-next.1607022026758

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

cd ../driver-bundle/src/main/resources
mkdir -p driver
cd driver

for PLATFORM in mac linux win32_x64
do
  FILE_NAME=$FILE_PREFIX-$PLATFORM.zip
  if [[ -d $PLATFORM ]]; then
    echo "Skipping driver download for $PLATFORM ($(pwd)/$PLATFORM already exists)"
    continue
  fi
  mkdir $PLATFORM
  cd $PLATFORM
  echo "Downloading driver for $PLATFORM to $(pwd)"

  curl -O  https://playwright.azureedge.net/builds/cli/next/${FILE_NAME}
  unzip ${FILE_NAME} -d .
  rm $FILE_NAME

  cd -
done

