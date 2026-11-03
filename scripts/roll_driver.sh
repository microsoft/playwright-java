#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [ "$#" -gt 1 ]; then
  echo ""
  echo "Usage: scripts/roll_driver.sh [next|beta|<version>]"
  echo ""
  exit 1
fi

ARG=${1:-next}
if [[ "$ARG" == "next" ]]; then
  NEW_VERSION=$(npm view playwright@next version)
elif [[ "$ARG" == "beta" ]]; then
  NEW_VERSION=$(npm view playwright@beta version)
else
  NEW_VERSION=$ARG
fi
CURRENT_VERSION=$(head -1 ./DRIVER_VERSION)
echo "Rolling driver from $CURRENT_VERSION to $NEW_VERSION"

# Pin the major version of the latest Node.js LTS, the policy of the upstream Docker images
# (see upstream utils/build/update-playwright-node.mjs).
CURRENT_NODE_VERSION=$(head -1 ./NODE_VERSION)
NEW_NODE_VERSION=$(curl -fsSL "https://nodejs.org/dist/index.json" \
  | node -e "let s='';process.stdin.on('data',d=>s+=d).on('end',()=>console.log(JSON.parse(s).find(r=>r.lts).version.slice(1).split('.')[0]))")
if [[ -z "$NEW_NODE_VERSION" ]]; then
  echo "Failed to determine the latest Node.js LTS version"
  exit 1
fi
echo "Rolling Node.js from $CURRENT_NODE_VERSION to $NEW_NODE_VERSION"

if [[ "$CURRENT_VERSION" == "$NEW_VERSION" && "$CURRENT_NODE_VERSION" == "$NEW_NODE_VERSION" ]]; then
  echo "Current versions are up to date. Skipping driver download.";
else
  echo $NEW_VERSION > ./DRIVER_VERSION
  echo $NEW_NODE_VERSION > ./NODE_VERSION
  ./download_driver.sh
fi;

./generate_api.sh
./update_readme.sh

node -e "$(cat <<EOF
  let [majorVersion, minorVersion] = process.argv[1].split('-')[0].split('.').map(part => parseInt(part, 10));
  minorVersion[1]--;
  const previousMajorVersion = majorVersion + '.' + minorVersion + '.0';
  fs.writeFileSync('../examples/pom.xml', fs.readFileSync('../examples/pom.xml', 'utf8')
    .replace(/<playwright\.version>.*<\/playwright\.version>/, '<playwright\.version>' + previousMajorVersion + '</playwright\.version>')
  );
EOF
)" $NEW_VERSION
