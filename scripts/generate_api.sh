#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

DRIVER_VERSION=$(head -1 ./scripts/DRIVER_VERSION)

# The script generates the API from the docs in .upstream/ (docs/src/api/*.md). This is the
# microsoft/playwright checkout at the commit in scripts/DRIVER_VERSION. scripts/build_driver.sh makes it.
# To try a change to the upstream docs or to the generator, set PW_SRC_DIR to a different
# checkout. Do not commit files that come from PW_SRC_DIR: CI generates the files again
# from .upstream/ and fails if they are different.
if [[ -n "$PW_SRC_DIR" ]]; then
  UPSTREAM_DIR="$PW_SRC_DIR"
  echo "Generating from $UPSTREAM_DIR (PW_SRC_DIR), not from .upstream/"
else
  UPSTREAM_DIR="./.upstream"
  if [[ "$(git -C "$UPSTREAM_DIR" rev-parse HEAD 2> /dev/null)" != "$DRIVER_VERSION" ]]; then
    echo ".upstream/ is not at $DRIVER_VERSION, the commit in scripts/DRIVER_VERSION. Run: scripts/build_driver.sh"
    exit 1
  fi
fi

# Node.js runs the TypeScript file of the generator as is. Older versions cannot.
if ! node -e 'process.exit(process.features.typescript ? 0 : 1)'; then
  echo "The API generator needs Node.js 22.18 or newer, got $(node --version)"
  exit 1
fi

echo "Regenerating Java interfaces from $UPSTREAM_DIR"
node ./scripts/generate_java_api.mts "$UPSTREAM_DIR"

# The protocol types are generated last: they avoid names of the public API types generated above.
# utils/protocol_spec.js needs the 'yaml' package. .upstream/ has it after scripts/build_driver.sh.
# A PW_SRC_DIR checkout without node_modules does not have it: install it in a temporary directory.
echo "Generating protocol channel types"
YAML_DIR=""
if [[ ! -d "$UPSTREAM_DIR/node_modules/yaml" ]]; then
  YAML_DIR=$(mktemp -d)
  npm install --prefix "$YAML_DIR" --no-audit --no-fund --silent yaml@2.9.0
  export NODE_PATH="$YAML_DIR/node_modules${NODE_PATH:+:$NODE_PATH}"
fi
node ./scripts/generate_java_channels.js "$UPSTREAM_DIR"

if [[ -n "$YAML_DIR" ]]; then
  rm -rf "$YAML_DIR"
fi
