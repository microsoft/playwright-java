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

mvn versions:set -DnewVersion=$VERSION

cd tools/api-generator
mvn versions:set -DnewVersion=$VERSION

cd -
cd tools/update-docs-version
mvn versions:set -DnewVersion=$VERSION
