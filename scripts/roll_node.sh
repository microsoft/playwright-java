#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [ "$#" -gt 0 ]; then
  echo ""
  echo "Updates scripts/NODE_VERSION to the latest Node.js release of the same major version."
  echo "Run scripts/build_driver.sh after that to download the new Node.js binaries."
  echo ""
  echo "Usage: scripts/roll_node.sh"
  echo ""
  exit 1
fi

CURRENT_VERSION=$(head -1 ./NODE_VERSION)
MAJOR_VERSION=${CURRENT_VERSION%%.*}
# The checksum list of the latest release names every file after the release, e.g.
#   d38bd5e5...  node-v26.13.0-darwin-arm64.tar.gz
NEW_VERSION=$(curl --retry 5 --retry-delay 2 -fsSL "https://nodejs.org/dist/latest-v$MAJOR_VERSION.x/SHASUMS256.txt" \
  | sed -nE 's/.*node-v([0-9]+\.[0-9]+\.[0-9]+)-.*/\1/p' | head -1)
if [[ -z "$NEW_VERSION" ]]; then
  echo "Failed to determine the latest Node.js $MAJOR_VERSION.x version"
  exit 1
fi

echo "Rolling Node.js from $CURRENT_VERSION to $NEW_VERSION"
echo $NEW_VERSION > ./NODE_VERSION
