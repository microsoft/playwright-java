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

for name in ${POM_FILES[*]};
do
  mvn versions:set -D generateBackupPoms=false -D newVersion=$VERSION -f $name
done
