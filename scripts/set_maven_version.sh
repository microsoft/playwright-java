#!/bin/bash

set -e
set +x

if [[ $# == 0 ]]; then
  echo "Missing version parameter."
  echo "Usage:"
  echo "  $(basename $0) 0.170.3-SNAPSHOT"
  exit 1
fi

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)/.."

VERSION=$1
POM_FILES=(
  pom.xml
  tools/*/pom.xml
  examples/pom.xml
)

node -e "$(cat <<EOF
  const parts = process.argv[1].split('.').map(part => parseInt(part, 10));
  parts[1]--;
  const previousMajorVersion = parts.join('.');
  fs.writeFileSync('examples/pom.xml', fs.readFileSync('examples/pom.xml', 'utf8')
    .replace(/<playwright-version>.*<\/playwright-version>/, '<playwright-version>' + previousMajorVersion + '</playwright-version>')
  );
EOF
)" $VERSION
