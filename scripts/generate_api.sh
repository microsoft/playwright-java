#!/bin/bash

set -e
set +x

trap 'cd $(pwd -P)' EXIT
cd "$(dirname "$0")/.."

DRIVER_VERSION=$(head -1 ./scripts/DRIVER_VERSION)

# api.json is generated from the upstream Playwright source at the exact commit
# that produced this driver version. Set PW_SRC_DIR to reuse an existing upstream
# checkout, otherwise a minimal one is fetched into a temporary directory.
GIT_HEAD=$(npm view playwright@"$DRIVER_VERSION" gitHead)
if [[ -z "$GIT_HEAD" ]]; then
  echo "Failed to resolve upstream commit (gitHead) for playwright@$DRIVER_VERSION"
  exit 1
fi

CLONED_UPSTREAM=""
if [[ -n "$PW_SRC_DIR" ]]; then
  UPSTREAM_DIR="$PW_SRC_DIR"
  echo "Using upstream Playwright checkout at $UPSTREAM_DIR (PW_SRC_DIR)"
else
  UPSTREAM_DIR=$(mktemp -d)
  CLONED_UPSTREAM="$UPSTREAM_DIR"
  echo "Fetching upstream Playwright source at $GIT_HEAD"
  # generateApiJson.js only needs utils/ and docs/, the protocol generator also reads packages/protocol/spec.
  git clone --quiet --filter=blob:none --no-checkout https://github.com/microsoft/playwright.git "$UPSTREAM_DIR"
  git -C "$UPSTREAM_DIR" sparse-checkout init --cone
  git -C "$UPSTREAM_DIR" sparse-checkout set utils docs packages/protocol/spec
  git -C "$UPSTREAM_DIR" checkout --quiet "$GIT_HEAD"
fi

echo "Updating api.json from upstream playwright@$DRIVER_VERSION ($GIT_HEAD)"
API_JSON_MODE=1 node "$UPSTREAM_DIR/utils/doclint/generateApiJson.js" \
  > ./tools/api-generator/src/main/resources/api.json

mvn compile -f ./tools/api-generator --no-transfer-progress

echo "Regenerating Java interfaces"
mvn exec:java --f ./tools/api-generator -D exec.mainClass=com.microsoft.playwright.tools.ApiGenerator

# The protocol types are generated last: they avoid names of the public API types generated above.
# utils/protocol_spec.js needs the 'yaml' package, which a sparse upstream checkout does not have.
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
if [[ -n "$CLONED_UPSTREAM" ]]; then
  rm -rf "$CLONED_UPSTREAM"
fi
