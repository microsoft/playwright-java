#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

CLI_VERSION=$(head -1 ./CLI_VERSION)
FILE_PREFIX=playwright-cli-$CLI_VERSION

cd ../driver-bundle/src/main/resources

if [[ -d local-driver ]]; then
  echo "$(pwd)/driver already exists, delete it first"
  exit 1;
fi

PLATFORM="unknown"
case $(uname) in
Darwin)
  PLATFORM=mac
  echo "Downloading driver for macOS"
  ;;
Linux)
  PLATFORM=linux
  echo "Downloading driver for Linux"
  ;;
MINGW32*)
  PLATFORM=win32
  echo "Downloading driver for Win32"
  ;;
MINGW64*)
  PLATFORM=win32_x64
  echo "Downloading driver for Win64"
  ;;
*)
  echo "Unknown platform '$(uname)'"
  exit 1;
  ;;
esac

mkdir -p driver
cd driver
if [[ -d $PLATFORM ]]; then
  echo "$(pwd)/$PLATFORM already exists, delete it first"
  exit 1
fi
mkdir $PLATFORM
cd $PLATFORM

FILE_NAME=$FILE_PREFIX-$PLATFORM.zip
echo "Downloading driver for $PLATFORM to $(pwd)"

curl -O  https://playwright.azureedge.net/builds/cli/next/${FILE_NAME}
unzip ${FILE_NAME} -d .
rm $FILE_NAME
./playwright-cli install
