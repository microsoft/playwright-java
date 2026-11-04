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
  echo "NOTE: this requires a JDK, Maven and npm on the host: the client is built here"
  echo "and the image only installs the result."
  echo ""
  exit 0
fi

function cleanup() {
  rm -rf dist
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

# The release pipeline cannot reach Maven Central directly: it provides a settings.xml
# that mirrors it to an internal feed (see .azure-pipelines/publish-docker.yml).
MVN=(mvn --no-transfer-progress)
if [[ -n "${MAVEN_SETTINGS_SECRET:-}" ]]; then
  MVN+=(-s "${MAVEN_SETTINGS_SECRET}")
fi

# Like the other language ports, build the client on the host, where npm, a JDK and
# Maven are available, and stage what the Dockerfile copies into the image under dist/.
# The build context is this directory, so nothing else reaches the image.
../../scripts/download_driver.sh
"${MVN[@]}" -B install -D skipTests -f ../../pom.xml

PW_VERSION=$("${MVN[@]}" -q -f ../../pom.xml help:evaluate -D expression=project.version -D forceStdout)
LOCAL_REPO=$("${MVN[@]}" -q -f ../../pom.xml help:evaluate -D expression=settings.localRepository -D forceStdout)
rm -rf dist

# dist/m2: the playwright-java artifacts of this build, for the local Maven repository of
# the image. Projects built in the image then resolve them without downloading.
for MODULE in parent-pom driver driver-bundle playwright; do
  mkdir -p "dist/m2/com/microsoft/playwright/$MODULE"
  cp -R "$LOCAL_REPO/com/microsoft/playwright/$MODULE/$PW_VERSION" "dist/m2/com/microsoft/playwright/$MODULE/"
done

# dist/cli: the client jar and its runtime dependencies, which the Dockerfile runs the
# CLI from to install the driver, the browsers and their dependencies.
"${MVN[@]}" -q -f ../../playwright/pom.xml dependency:copy-dependencies \
  -D includeScope=runtime -D outputDirectory="$PWD/dist/cli"
cp "../../playwright/target/playwright-$PW_VERSION.jar" dist/cli/

# The jammy and noble images install this Maven (see the Dockerfiles).
MAVEN_VERSION=3.9.12
"${MVN[@]}" -q dependency:copy -D artifact="org.apache.maven:apache-maven:${MAVEN_VERSION}:tar.gz:bin" \
  -D outputDirectory=maven

# Keep each arch image a plain single-platform manifest without the unknown/unknown platform entry.
export BUILDX_NO_DEFAULT_ATTESTATIONS=1

# arm64 images are cross-built under QEMU user-mode emulation, where Ubuntu 22.04's
# ldconfig segfaults intermittently at startup (tonistiigi/binfmt#298, every binfmt
# build since QEMU 8.1.4). apt's libc-bin trigger runs ldconfig, so a crash fails the
# whole `docker build`. Retry: BuildKit keeps the layers that already succeeded, so a
# retry re-runs only the failed RUN step.
MAX_ATTEMPTS=1
if [[ "${PLATFORM}" == "linux/arm64" ]]; then
  MAX_ATTEMPTS=3
fi

for ((attempt = 1; attempt <= MAX_ATTEMPTS; attempt++)); do
  if docker build --platform "${PLATFORM}" \
      --build-arg "PW_TARGET_ARCH=${PW_TARGET_ARCH}" \
      --build-arg "MAVEN_VERSION=${MAVEN_VERSION}" \
      --build-arg ACR_CACHE_PREFIX="${ACR_CACHE_PREFIX}" \
      --build-arg UBUNTU_MIRROR_PREFIX="${UBUNTU_MIRROR_PREFIX}" \
      -t "$3" -f "Dockerfile.$2" .; then
    exit 0
  fi
  if (( attempt < MAX_ATTEMPTS )); then
    echo "docker build failed (attempt ${attempt}/${MAX_ATTEMPTS}), retrying..." >&2
  fi
done
echo "ERROR: docker build failed after ${MAX_ATTEMPTS} attempt(s)" >&2
exit 1
