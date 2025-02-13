#!/bin/bash

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)"

if [ "$#" -ne 1 ]; then
  echo ""
  echo "Usage: scripts/roll_driver.sh [new version]"
  echo ""
  exit 1
fi

NEW_VERSION=$1
CURRENT_VERSION=$(head -1 ./DRIVER_VERSION)

if [[ "$CURRENT_VERSION" == "$NEW_VERSION" ]]; then
  echo "Current version is up to date. Skipping driver download.";
else
  echo $NEW_VERSION > ./DRIVER_VERSION
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
