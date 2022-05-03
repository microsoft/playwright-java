#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [[ ($1 == '-h') || ($1 == '--help') ]]; then
  echo ""
  echo "This script for downloading playwright-cli binaries for all platforms."
  echo "The downloaded files will be put into directory 'driver-bundle/src/main/resources/driver'."
  echo ""
  echo "Usage: scripts/download_driver_for_all_platforms.sh [option]"
  echo ""
  echo "Options:"
  echo "  -h, --help     display help information"
  echo "  -f, --force    delete existing drivers and download them again"
  echo ""
  exit 0
fi

CLI_VERSION=$(head -1 ./CLI_VERSION)
FILE_PREFIX=playwright-$CLI_VERSION

cd ../driver-bundle/src/main/resources

if [[ ($1 == '-f') || ($1 == '--force') ]]; then
  echo "Deleting existing drivers from $(pwd)"
  rm -rf driver
fi

mkdir -p driver
cd driver

for PLATFORM in mac mac-arm64 linux linux-arm64 win32_x64
do
  FILE_NAME=$FILE_PREFIX-$PLATFORM.zip
  if [[ -d $PLATFORM ]]; then
    echo "Skipping driver download for $PLATFORM ($(pwd)/$PLATFORM already exists)"
    continue
  fi
  mkdir $PLATFORM
  cd $PLATFORM
  echo "Downloading driver for $PLATFORM to $(pwd)"

  URL=https://playwright.azureedge.net/builds/driver
  if [[ "$CLI_VERSION" == *-alpha* || "$CLI_VERSION" == *-beta* || "$CLI_VERSION" == *-next* ]]; then
    URL=$URL/next
  fi
  URL=$URL/$FILE_NAME
  echo "Using url: $URL"
  curl -O $URL
  unzip $FILE_NAME -d .
  rm $FILE_NAME

  cd -
done

echo ""
echo "All drivers have been downloaded successfully, use '-f' or '--force' option to delete and download them again if you want."
echo "For more details, you can use '-h' or '--help' option, or read the CONTRIBUTING.md for reference."
echo ""
