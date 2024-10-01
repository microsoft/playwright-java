#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [[ ($1 == '-h') || ($1 == '--help') ]]; then
  echo ""
  echo "This script for downloading playwright driver for all platforms."
  echo "The downloaded files will be put under 'driver-bundle/src/main/resources/driver'."
  echo ""
  echo "Usage: scripts/download_driver.sh [option]"
  echo ""
  echo "Options:"
  echo "  -h, --help     display help information"
  echo ""
  exit 0
fi

DRIVER_VERSION=$(head -1 ./DRIVER_VERSION)
FILE_PREFIX=playwright-$DRIVER_VERSION

cd ../driver-bundle/src/main/resources

if [[ -d 'driver' ]]; then
  echo "Deleting existing drivers from $(pwd)"
  rm -rf driver
fi

mkdir -p driver
cd driver

for PLATFORM in mac mac-arm64 linux linux-arm64 win32_x64
do
  FILE_NAME=$FILE_PREFIX-$PLATFORM.zip
  mkdir $PLATFORM
  cd $PLATFORM
  echo "Downloading driver for $PLATFORM to $(pwd)"

  URL=https://playwright.azureedge.net/builds/driver
  if [[ "$DRIVER_VERSION" == *-alpha* || "$DRIVER_VERSION" == *-beta* || "$DRIVER_VERSION" == *-next* ]]; then
    URL=$URL/next
  fi
  URL=$URL/$FILE_NAME
  echo "Using url: $URL"
  # Ubuntu 24.04-arm64 emulated via qemu has a bug, so we prefer wget over curl.
  # See https://github.com/microsoft/playwright-java/issues/1678.
  if command -v wget &> /dev/null; then
      wget $URL
  else
      curl -O $URL
  fi
  unzip $FILE_NAME -d .
  rm $FILE_NAME

  cd -
done

echo ""
echo "All drivers have been successfully downloaded."
echo ""
