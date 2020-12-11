#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

CLI_VERSION=$(head -1 ./CLI_VERSION)
FILE_PREFIX=playwright-cli-$CLI_VERSION

cd ../driver-bundle/src/main/resources

if [[ $1 == '-f' ]]; then
  echo "Deleting existing drivers from $(pwd)"
  rm -rf driver
fi

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

