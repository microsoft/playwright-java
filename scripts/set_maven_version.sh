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
  tools/api-generator/pom.xml
  tools/update-docs-version/pom.xml
  tools/test-local-installation/pom.xml
  tools/test-spring-boot-starter/pom.xml
  examples/pom.xml
)

for name in ${POM_FILES[*]};
do
  mvn versions:set -D generateBackupPoms=false -D newVersion=$VERSION -f $name
done
