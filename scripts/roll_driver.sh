#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [[ "$#" -gt 1 || ($1 == '-h') || ($1 == '--help') ]]; then
  echo ""
  echo "Rolls the driver to a microsoft/playwright branch (main, release-1.66), tag (v1.66.0)"
  echo "or commit: pins its commit in scripts/DRIVER_VERSION, rolls Node.js, builds the driver"
  echo "from source and regenerates the API. The default is the tip of main."
  echo ""
  echo "Usage: scripts/roll_driver.sh [<ref>]"
  echo ""
  exit 1
fi

UPSTREAM_URL="https://github.com/microsoft/playwright.git"
REF=${1:-main}
if [[ "$REF" =~ ^[0-9a-f]{40}$ ]]; then
  NEW_VERSION=$REF
else
  # "^{}" is the commit that an annotated tag points to.
  REFS=$(git ls-remote "$UPSTREAM_URL" "refs/tags/$REF^{}" "refs/tags/$REF" "refs/heads/$REF")
  NEW_VERSION=""
  for NAME in "refs/tags/$REF^{}" "refs/tags/$REF" "refs/heads/$REF"; do
    NEW_VERSION=$(echo "$REFS" | awk -v name="$NAME" '$2 == name { print $1 }')
    if [[ -n "$NEW_VERSION" ]]; then
      break
    fi
  done
  if [[ -z "$NEW_VERSION" ]]; then
    echo "$REF is not a branch, a tag or a full commit of $UPSTREAM_URL"
    exit 1
  fi
fi
CURRENT_VERSION=$(head -1 ./DRIVER_VERSION)
echo "Rolling driver from $CURRENT_VERSION to $NEW_VERSION"

echo $NEW_VERSION > ./DRIVER_VERSION
# Each roll also moves the driver to the latest Node.js release of the pinned major version.
./roll_node.sh
./build_driver.sh

./generate_api.sh
./update_readme.sh

# The version that the driver reports: 1.66.0 on a release tag, 1.67.0-next on main.
PACKAGE_VERSION=$(node -p "require('../.upstream/packages/playwright-core/package.json').version")
node -e "$(cat <<EOF2
  const [majorVersion, minorVersion] = process.argv[1].split('-')[0].split('.').map(part => parseInt(part, 10));
  const exampleVersion = majorVersion + '.' + minorVersion + '.0';
  fs.writeFileSync('../examples/pom.xml', fs.readFileSync('../examples/pom.xml', 'utf8')
    .replace(/<playwright\.version>.*<\/playwright\.version>/, '<playwright\.version>' + exampleVersion + '</playwright\.version>')
  );
EOF2
)" $PACKAGE_VERSION

echo ""
echo "Rolled to microsoft/playwright $NEW_VERSION; the driver reports version $PACKAGE_VERSION."
echo "The upstream commits since the previous pin:"
echo "  git -C .upstream log --oneline --reverse $CURRENT_VERSION..$NEW_VERSION"
echo ""
