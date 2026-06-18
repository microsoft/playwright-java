#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [[ ($1 == '-h') || ($1 == '--help') ]]; then
  echo ""
  echo "This script downloads and assembles the Playwright driver for all platforms."
  echo "The platform-independent 'playwright-core' npm package is assembled once into the driver"
  echo "module ('driver/src/main/resources/driver/package'), and the matching Node.js binary from"
  echo "https://nodejs.org for each platform goes into the driver-bundle module"
  echo "('driver-bundle/src/main/resources/driver/<platform>'), the same way the upstream"
  echo "Playwright build does it."
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

# The platform-independent driver code (playwright-core) is assembled once into the driver module;
# the Node.js binary for each platform is assembled into the driver-bundle module. See issue #1196.
ROOT="$(cd .. && pwd)"
CORE_DEST="$ROOT/driver/src/main/resources/driver"
NODE_DEST="$ROOT/driver-bundle/src/main/resources/driver"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

# 1. playwright-core package -> driver module (once, shared by every platform).
echo "Assembling playwright-core package to $CORE_DEST/package"
rm -rf "$CORE_DEST/package"
mkdir -p "$CORE_DEST"
CORE_TGZ="$TMP_DIR/playwright-core-$DRIVER_VERSION.tgz"
download "https://registry.npmjs.org/playwright-core/-/playwright-core-$DRIVER_VERSION.tgz" "$CORE_TGZ"
# The npm tarball has a top-level package/ directory, so this creates $CORE_DEST/package.
tar -xzf "$CORE_TGZ" -C "$CORE_DEST"
rm -f "$CORE_TGZ"

# 2. Node.js binary for each platform -> driver-bundle module.
# <java platform dir>:<nodejs platform suffix>:<archive extension>
for ENTRY in \
  "mac:darwin-x64:tar.gz" \
  "mac-arm64:darwin-arm64:tar.gz" \
  "linux:linux-x64:tar.gz" \
  "linux-arm64:linux-arm64:tar.gz" \
  "win32_x64:win-x64:zip"
do
  IFS=':' read -r PLATFORM NODE_SUFFIX ARCHIVE <<< "$ENTRY"
  DEST="$NODE_DEST/$PLATFORM"
  echo "Assembling Node.js for $PLATFORM to $DEST"
  rm -rf "$DEST"
  mkdir -p "$DEST"

  # Node.js binary and its license from the official Node.js distribution.
  NODE_DIR="node-v$NODE_VERSION-$NODE_SUFFIX"
  NODE_ARCHIVE="$TMP_DIR/$NODE_DIR.$ARCHIVE"
  download "https://nodejs.org/dist/v$NODE_VERSION/$NODE_DIR.$ARCHIVE" "$NODE_ARCHIVE"
  if [[ $ARCHIVE == "zip" ]]; then
    unzip -joq "$NODE_ARCHIVE" "$NODE_DIR/node.exe" -d "$DEST"
    unzip -joq "$NODE_ARCHIVE" "$NODE_DIR/LICENSE" -d "$DEST"
  else
    tar -xzf "$NODE_ARCHIVE" -C "$DEST" --strip-components=2 "$NODE_DIR/bin/node"
    tar -xzf "$NODE_ARCHIVE" -C "$DEST" --strip-components=1 "$NODE_DIR/LICENSE"
  fi
  rm -f "$NODE_ARCHIVE"
done

echo ""
echo "All drivers have been successfully assembled."
echo ""
