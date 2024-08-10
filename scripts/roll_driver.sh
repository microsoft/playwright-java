#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [ "$#" -ne 1 ]; then
  echo ""
  echo "Usage: scripts/roll_driver.sh [new version]"
  echo ""
  exit 1
fi

NEW_VERSION=$1
CURRENT_VERSION=$(head -1 ./DRIVER_VERSION)

if [[ "$CURRENT_VERSION" == "$NEW_VERSION" ]]; then
  echo "Current version is up to date. Skipping driver download.";
else
  echo $NEW_VERSION > ./DRIVER_VERSION
  ./download_driver.sh
fi;

./generate_api.sh
./update_readme.sh