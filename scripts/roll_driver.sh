#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

usage() {
  echo ""
  echo "Rolls the driver to a commit of microsoft/playwright."
  echo ""
  echo "Usage: scripts/roll_driver.sh [main|release-1.<minor>|<commit>]"
  echo ""
  echo "  main               the tip of the main branch (default)"
  echo "  release-1.<minor>  the tip of a release branch, for example release-1.66"
  echo "  <commit>           a full commit hash (40 hex digits)"
  echo ""
  echo "The script writes the commit to scripts/DRIVER_VERSION, rolls Node.js, builds the"
  echo "driver from source and generates the API."
  echo ""
}

if [[ "$#" -gt 1 || ($1 == '-h') || ($1 == '--help') ]]; then
  usage
  exit 1
fi

UPSTREAM_URL="https://github.com/microsoft/playwright.git"
REF=${1:-main}
if [[ "$REF" =~ ^[0-9a-f]{40}$ ]]; then
  NEW_COMMIT=$REF
elif [[ "$REF" == "main" || "$REF" =~ ^release-1\.[0-9]+$ ]]; then
  NEW_COMMIT=$(git ls-remote "$UPSTREAM_URL" "refs/heads/$REF" | cut -f1)
  if [[ -z "$NEW_COMMIT" ]]; then
    echo "$UPSTREAM_URL has no branch $REF"
    exit 1
  fi
else
  echo "Unknown ref: $REF"
  usage
  exit 1
fi
OLD_COMMIT=$(head -1 ./DRIVER_VERSION)
echo "Rolling the driver from $OLD_COMMIT to $NEW_COMMIT"

echo $NEW_COMMIT > ./DRIVER_VERSION
# Each roll also moves Node.js to the latest release of the pinned major version.
./roll_node.sh
./build_driver.sh

./generate_api.sh
./update_readme.sh

# The playwright module takes the driver from the local Maven repository.
(cd .. && mvn -B clean install -D skipTests --no-transfer-progress -pl driver,driver-bundle)

echo ""
echo "Rolled the driver from $OLD_COMMIT to $NEW_COMMIT."
echo "To list the upstream commits of the roll, run:"
echo "  git -C .upstream log --oneline --reverse $OLD_COMMIT..$NEW_COMMIT"
echo ""
