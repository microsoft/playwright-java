#!/bin/bash
set -e
set +x

if [[ ($1 == '--help') || ($1 == '-h') || ($1 == '') || ($2 == '') ]]; then
  echo "usage: $(basename $0) {--arm64,--amd64} {focal} playwright:localbuild-focal"
  echo
  echo "Build Playwright docker image and tag it as 'playwright:localbuild-focal'."
  echo "Once image is built, you can run it with"
  echo ""
  echo "  docker run --rm -it playwright:localbuild-focal /bin/bash"
  echo ""
  echo "NOTE: this requires on Playwright PIP dependencies to be installed"
  echo ""
  exit 0
fi

function cleanup() {
  :
}

trap "cleanup; cd $(pwd -P)" EXIT
cd "$(dirname "$0")"

PLATFORM=""
if [[ "$1" == "--arm64" ]]; then
  PLATFORM="linux/arm64";
elif [[ "$1" == "--amd64" ]]; then
  PLATFORM="linux/amd64"
else
  echo "ERROR: unknown platform specifier - $1. Only --arm64 or --amd64 is supported"
  exit 1
fi

PW_TARGET_ARCH=$(echo $1 | cut -c3-)

docker build --platform "${PLATFORM}" --build-arg "PW_TARGET_ARCH=${PW_TARGET_ARCH}" -t "$3" -f "Dockerfile.$2" ../../
