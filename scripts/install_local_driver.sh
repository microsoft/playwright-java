#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

CLI_VERSION=$(head -1 ./CLI_VERSION)
FILE_PREFIX=playwright-cli-$CLI_VERSION

cd ../driver/src/main/resources
if [[ -d local-driver ]]; then
  echo "$(pwd)/driver already exists, delete it first"
  exit 1;
fi

mkdir local-driver
cd local-driver
echo "Created directory: $(pwd)"

FILE_NAME="unknown"
case $(uname) in
Darwin)
  FILE_NAME=${FILE_PREFIX}-mac.zip
  echo "Downloading driver for macOS"
  ;;
Linux)
  FILE_NAME=${FILE_PREFIX}-linux.zip
  echo "Downloading driver for Linux"
  ;;
MINGW32*)
  FILE_NAME=${FILE_PREFIX}-win32.zip
  echo "Downloading driver for Windows"
  ;;
MINGW64*)
  FILE_NAME=${FILE_PREFIX}-win32_x64.zip
  echo "Downloading driver for Windows"
*)
  echo "Unknown platform '$(uname)'"
  exit 1;
  ;;
esac

curl -O  https://playwright.azureedge.net/builds/cli/next/${FILE_NAME}
unzip ${FILE_NAME} -d .
rm $FILE_NAME
./playwright-cli install
