#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [[ ($1 == '-h') || ($1 == '--help') ]]; then
  echo ""
  echo "This script downloads and assembles the Playwright driver for all platforms."
  echo "Each driver is assembled from the 'playwright-core' npm package and the matching"
  echo "Node.js binary from https://nodejs.org, the same way the upstream Playwright build"
  echo "does it. The result is put under 'driver-bundle/src/main/resources/driver'."
  echo ""
  echo "Usage: scripts/download_driver.sh [option]"
  echo ""
  echo "Options:"
  echo "  -h, --help     display help information"
  echo ""
  exit 0
fi

# Ubuntu 24.04-arm64 emulated via qemu has a bug, so we prefer wget over curl.
# See https://github.com/microsoft/playwright-java/issues/1678.
download() {
  local url=$1
  local out=$2
  echo "Downloading $url"
  if command -v wget &> /dev/null; then
    wget -q -O "$out" "$url"
  else
    curl --retry 5 --retry-delay 2 -fL -o "$out" "$url"
  fi
}

DRIVER_VERSION=$(head -1 ./DRIVER_VERSION)

# Resolve the exact upstream commit that produced this driver version, so that the
# bundled Node.js version matches the driver exactly.
GIT_HEAD=$(npm view playwright@"$DRIVER_VERSION" gitHead)
if [[ -z "$GIT_HEAD" ]]; then
  echo "Failed to resolve upstream commit (gitHead) for playwright@$DRIVER_VERSION"
  exit 1
fi

# The Node.js version is kept in sync with the driver version in the upstream build script.
NODE_VERSION=$(curl -fsSL "https://raw.githubusercontent.com/microsoft/playwright/$GIT_HEAD/utils/build/build-playwright-driver.sh" \
  | sed -n 's/^NODE_VERSION="\([^"]*\)".*/\1/p')
if [[ -z "$NODE_VERSION" ]]; then
  echo "Failed to determine Node.js version for playwright@$DRIVER_VERSION ($GIT_HEAD)"
  exit 1
fi

echo "Driver version:  $DRIVER_VERSION"
echo "Upstream commit: $GIT_HEAD"
echo "Node.js version: $NODE_VERSION"

cd ../driver-bundle/src/main/resources

if [[ -d 'driver' ]]; then
  echo "Deleting existing drivers from $(pwd)"
  rm -rf driver
fi

mkdir -p driver
cd driver

# Download the platform-independent driver package (playwright-core) once.
CORE_TGZ="$(pwd)/playwright-core-$DRIVER_VERSION.tgz"
download "https://registry.npmjs.org/playwright-core/-/playwright-core-$DRIVER_VERSION.tgz" "$CORE_TGZ"

# <java platform dir>:<nodejs platform suffix>:<archive extension>
for ENTRY in \
  "mac:darwin-x64:tar.gz" \
  "mac-arm64:darwin-arm64:tar.gz" \
  "linux:linux-x64:tar.gz" \
  "linux-arm64:linux-arm64:tar.gz" \
  "win32_x64:win-x64:zip"
do
  IFS=':' read -r PLATFORM NODE_SUFFIX ARCHIVE <<< "$ENTRY"
  echo "Assembling driver for $PLATFORM to $(pwd)/$PLATFORM"
  mkdir "$PLATFORM"

  # 1. playwright-core package contents -> $PLATFORM/package
  tar -xzf "$CORE_TGZ" -C "$PLATFORM"

  # 2. Node.js binary and its license from the official Node.js distribution.
  NODE_DIR="node-v$NODE_VERSION-$NODE_SUFFIX"
  NODE_ARCHIVE="$NODE_DIR.$ARCHIVE"
  download "https://nodejs.org/dist/v$NODE_VERSION/$NODE_DIR.$ARCHIVE" "$NODE_ARCHIVE"
  if [[ $ARCHIVE == "zip" ]]; then
    unzip -joq "$NODE_ARCHIVE" "$NODE_DIR/node.exe" -d "$PLATFORM"
    unzip -joq "$NODE_ARCHIVE" "$NODE_DIR/LICENSE" -d "$PLATFORM"
  else
    tar -xzf "$NODE_ARCHIVE" -C "$PLATFORM" --strip-components=2 "$NODE_DIR/bin/node"
    tar -xzf "$NODE_ARCHIVE" -C "$PLATFORM" --strip-components=1 "$NODE_DIR/LICENSE"
  fi
  rm -f "$NODE_ARCHIVE"
done

rm -f "$CORE_TGZ"

echo ""
echo "All drivers have been successfully assembled."
echo ""
