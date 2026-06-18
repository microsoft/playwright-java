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
  echo "does it. Each platform is written to its own module under"
  echo "'driver-bundle-<platform>/src/main/resources/driver/<platform>'."
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

# Each platform's driver is assembled into its own Maven module
# (driver-bundle-<platform>/src/main/resources/driver/<platform>), so that a consumer build
# only pulls in the driver for the host platform. See issue #1196. The platform token uses an
# <os>-<arch> scheme (e.g. mac-x64) that matches both the artifact name and the directory that
# DriverJar.platformDir() looks up.
ROOT="$(cd .. && pwd)"

# Download once into a temporary directory shared across platforms.
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
CORE_TGZ="$TMP_DIR/playwright-core-$DRIVER_VERSION.tgz"
download "https://registry.npmjs.org/playwright-core/-/playwright-core-$DRIVER_VERSION.tgz" "$CORE_TGZ"

# <java platform dir>:<nodejs platform suffix>:<archive extension>
for ENTRY in \
  "mac-x64:darwin-x64:tar.gz" \
  "mac-arm64:darwin-arm64:tar.gz" \
  "linux-x64:linux-x64:tar.gz" \
  "linux-arm64:linux-arm64:tar.gz" \
  "win-x64:win-x64:zip"
do
  IFS=':' read -r PLATFORM NODE_SUFFIX ARCHIVE <<< "$ENTRY"
  DEST="$ROOT/driver-bundle-$PLATFORM/src/main/resources/driver"
  if [[ -d "$DEST" ]]; then
    echo "Deleting existing driver from $DEST"
    rm -rf "$DEST"
  fi
  mkdir -p "$DEST/$PLATFORM"
  echo "Assembling driver for $PLATFORM to $DEST/$PLATFORM"

  # 1. playwright-core package contents -> $PLATFORM/package
  tar -xzf "$CORE_TGZ" -C "$DEST/$PLATFORM"

  # 2. Node.js binary and its license from the official Node.js distribution.
  NODE_DIR="node-v$NODE_VERSION-$NODE_SUFFIX"
  NODE_ARCHIVE="$TMP_DIR/$NODE_DIR.$ARCHIVE"
  download "https://nodejs.org/dist/v$NODE_VERSION/$NODE_DIR.$ARCHIVE" "$NODE_ARCHIVE"
  if [[ $ARCHIVE == "zip" ]]; then
    unzip -joq "$NODE_ARCHIVE" "$NODE_DIR/node.exe" -d "$DEST/$PLATFORM"
    unzip -joq "$NODE_ARCHIVE" "$NODE_DIR/LICENSE" -d "$DEST/$PLATFORM"
  else
    tar -xzf "$NODE_ARCHIVE" -C "$DEST/$PLATFORM" --strip-components=2 "$NODE_DIR/bin/node"
    tar -xzf "$NODE_ARCHIVE" -C "$DEST/$PLATFORM" --strip-components=1 "$NODE_DIR/LICENSE"
  fi
  rm -f "$NODE_ARCHIVE"
done

echo ""
echo "All drivers have been successfully assembled."
echo ""
