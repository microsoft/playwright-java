#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [[ ($1 == '-h') || ($1 == '--help') ]]; then
  echo ""
  echo "This script builds the Playwright driver from source and assembles it for all platforms."
  echo "It clones microsoft/playwright into '.upstream/' (gitignored), checks out the commit"
  echo "pinned in scripts/DRIVER_VERSION and builds the 'playwright-core' npm package from it."
  echo "The platform-independent package is assembled once into the driver module"
  echo "('driver/src/main/resources/driver/package'), and the Node.js binary from"
  echo "https://nodejs.org (version pinned in scripts/NODE_VERSION) for each platform goes"
  echo "into the driver-bundle module ('driver-bundle/src/main/resources/driver/<platform>')."
  echo ""
  echo "Requires git and Node.js with npm."
  echo ""
  echo "Usage: scripts/build_driver.sh [option]"
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

ROOT="$(cd .. && pwd)"
UPSTREAM_URL="https://github.com/microsoft/playwright.git"
UPSTREAM_DIR="$ROOT/.upstream"

# scripts/DRIVER_VERSION pins the microsoft/playwright commit. scripts/roll_driver.sh updates it.
DRIVER_VERSION=$(head -1 ./DRIVER_VERSION)
if [[ ! "$DRIVER_VERSION" =~ ^[0-9a-f]{40}$ ]]; then
  echo "scripts/DRIVER_VERSION must hold one microsoft/playwright commit (40 hex digits), got '$DRIVER_VERSION'"
  exit 1
fi
# scripts/NODE_VERSION pins the exact Node.js version. scripts/roll_node.sh updates it.
NODE_VERSION=$(head -1 ./NODE_VERSION)

echo "Driver commit:   $DRIVER_VERSION"
echo "Node.js version: $NODE_VERSION"

# npm uses the registry and the credentials of a root-level .npmrc when there is one (e.g.
# the one the release pipeline writes to point at the internal Azure Artifacts feed).
# Locally, NPM_CONFIG_REGISTRY=<url> can be used to the same effect.
npm_in() {
  local dir=$1
  shift
  echo "Running npm $* in $dir"
  if [[ -f "$ROOT/.npmrc" ]]; then
    (cd "$dir" && npm_config_userconfig="$ROOT/.npmrc" npm "$@")
  else
    (cd "$dir" && npm "$@")
  fi
}

upstream_has_local_changes() {
  [[ -n "$(git -C "$UPSTREAM_DIR" status --porcelain)" ]]
}

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

# 1. .upstream/ at the commit in scripts/DRIVER_VERSION.
CLONED=""
if [[ ! -e "$UPSTREAM_DIR/.git" ]]; then
  echo "Cloning $UPSTREAM_URL into $UPSTREAM_DIR"
  # The whole history, for listing the changes of a roll; file contents are fetched on demand.
  git clone --quiet --filter=blob:none --no-checkout "$UPSTREAM_URL" "$UPSTREAM_DIR"
  CLONED=1
fi
if [[ -n "$CLONED" || "$(git -C "$UPSTREAM_DIR" rev-parse HEAD)" != "$DRIVER_VERSION" ]]; then
  if [[ -z "$CLONED" ]] && upstream_has_local_changes; then
    echo "$UPSTREAM_DIR has local changes. Commit or discard them before checking out $DRIVER_VERSION."
    exit 1
  fi
  if ! git -C "$UPSTREAM_DIR" cat-file -e "$DRIVER_VERSION^{commit}" 2> /dev/null; then
    git -C "$UPSTREAM_DIR" fetch --quiet --tags origin
    # A commit that no branch or tag reaches any more, for example on a deleted branch.
    git -C "$UPSTREAM_DIR" cat-file -e "$DRIVER_VERSION^{commit}" 2> /dev/null \
      || git -C "$UPSTREAM_DIR" fetch --quiet origin "$DRIVER_VERSION"
  fi
  git -C "$UPSTREAM_DIR" checkout --quiet --detach "$DRIVER_VERSION"
fi
echo "$UPSTREAM_DIR is at $DRIVER_VERSION"

# 2. Build it, once per commit.
if upstream_has_local_changes; then
  echo "$UPSTREAM_DIR has local changes. The driver is pinned to a commit, so build it from a clean checkout."
  exit 1
fi
UPSTREAM_CORE_DIR="$UPSTREAM_DIR/packages/playwright-core"
# npm ci removes node_modules, and the stamp with it.
STAMP="$UPSTREAM_DIR/node_modules/.playwright-java-build"
if [[ "$(cat "$STAMP" 2> /dev/null)" != "$DRIVER_VERSION" || ! -d "$UPSTREAM_CORE_DIR/lib" ]]; then
  npm_in "$UPSTREAM_DIR" ci --no-audit --no-fund
  npm_in "$UPSTREAM_DIR" run build
  echo "$DRIVER_VERSION" > "$STAMP"
fi

# The platform-independent driver code (playwright-core) is assembled once into the driver module;
# the Node.js binary for each platform is assembled into the driver-bundle module. See issue #1196.
CORE_DEST="$ROOT/driver/src/main/resources/driver"
NODE_DEST="$ROOT/driver-bundle/src/main/resources/driver"

# 3. playwright-core package -> driver module (once, shared by every platform).
echo "Assembling playwright-core package to $CORE_DEST/package"
npm_in "$UPSTREAM_CORE_DIR" pack --pack-destination "$TMP_DIR" --silent > /dev/null
CORE_TGZ=$(ls "$TMP_DIR"/playwright-core-*.tgz)
rm -rf "$CORE_DEST/package"
mkdir -p "$CORE_DEST"
# The npm tarball has a top-level package/ directory, so this creates $CORE_DEST/package.
tar -xzf "$CORE_TGZ" -C "$CORE_DEST"
rm -f "$CORE_TGZ"
# Upstream copies these into the package before it publishes it (LICENCE_FILES in utils/workspace.js).
cp "$UPSTREAM_DIR/NOTICE" "$UPSTREAM_DIR/LICENSE" "$CORE_DEST/package/"

# 4. Node.js binary for each platform -> driver-bundle module.
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

UPSTREAM_VERSION=$(cd "$UPSTREAM_CORE_DIR" && node -p "require('./package.json').version")
echo ""
echo "The driver has been built from microsoft/playwright $DRIVER_VERSION; it reports version $UPSTREAM_VERSION."
echo ""
