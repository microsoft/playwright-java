#!/bin/bash
set -e
set +x

if [[ ($1 == '--help') || ($1 == '-h') || ($1 == '') || ($2 == '') ]]; then
  echo "usage: $(basename $0) {--arm64,--amd64} {jammy,noble,resolute} playwright:localbuild-noble"
  echo
  echo "Build Playwright docker image and tag it as 'playwright:localbuild-noble'."
  echo "Once image is built, you can run it with"
  echo ""
  echo "  docker run --rm -it playwright:localbuild-noble /bin/bash"
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

# Assemble the driver on the host where npm is available; the Dockerfile picks
# it up via `COPY . /tmp/pw-java`.
../../scripts/download_driver.sh

SECRET_ARGS=()
if [[ -n "${MAVEN_SETTINGS_SECRET:-}" ]]; then
  SECRET_ARGS+=(--secret "id=mavensettings,src=${MAVEN_SETTINGS_SECRET}")
fi

# Keep each arch image a plain single-platform manifest without the unknown/unknown platform entry.
export BUILDX_NO_DEFAULT_ATTESTATIONS=1

docker build --platform "${PLATFORM}" \
  --build-arg "PW_TARGET_ARCH=${PW_TARGET_ARCH}" \
  --build-arg ACR_CACHE_PREFIX="${ACR_CACHE_PREFIX}" \
  --build-arg UBUNTU_MIRROR_PREFIX="${UBUNTU_MIRROR_PREFIX}" \
  "${SECRET_ARGS[@]}" \
  -t "$3" -f "Dockerfile.$2" ../../
