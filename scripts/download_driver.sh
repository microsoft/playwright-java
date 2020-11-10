#!/bin/bash

set -e
set +x

FILE_PREFIX=playwright-cli-0.160.0-next.1604373941495

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

cd ../playwright/src/main/resources
if [[ -d driver ]]; then
  echo "$(pwd)/driver already exists, delete it first"
  exit 1;
fi

mkdir driver
cd driver
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
MINGW*)
  FILE_NAME=${FILE_PREFIX}-win32_x64.zip
  echo "Downloading driver for Windows"
  ;;
*)
  echo "Unknown platform '$(uname)'"
  exit 1;
  ;;
esac

curl -O  https://playwright.azureedge.net/builds/cli/next/${FILE_NAME}
unzip ${FILE_NAME} -d .
rm $FILE_NAME
# ./playwright-cli install
