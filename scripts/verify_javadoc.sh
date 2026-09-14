#!/bin/bash

# Checks that the javadoc jar produced by `mvn package` documents the public API.
# Javadoc errors don't fail the build (failOnError is false), so a misconfiguration
# can silently produce an empty jar.

set -e
set +x

trap "cd $(pwd -P)" EXIT
cd "$(dirname $0)/.."

JARS=(playwright/target/playwright-*-javadoc.jar)
if [[ ${#JARS[@]} -ne 1 || ! -f "${JARS[0]}" ]]; then
  echo "ERROR: expected exactly one javadoc jar, found: ${JARS[*]}"
  exit 1
fi
JAR=${JARS[0]}
ENTRIES=$(jar tf "$JAR")

FAILED=0

# Class pages, excluding class-use/ and package-summary/tree/use pages.
CLASS_PAGES=$(echo "$ENTRIES" | grep -E '^com/microsoft/playwright/.*\.html$' | grep -v -E '/class-use/|/package-[a-z]+\.html$' | wc -l)
MIN_CLASS_PAGES=300
echo "$JAR: $CLASS_PAGES class pages"
if [[ $CLASS_PAGES -lt $MIN_CLASS_PAGES ]]; then
  echo "ERROR: expected at least $MIN_CLASS_PAGES class pages"
  FAILED=1
fi

for page in \
    index.html \
    com/microsoft/playwright/Page.html \
    com/microsoft/playwright/Locator.html \
    com/microsoft/playwright/options/Cookie.html \
    com/microsoft/playwright/assertions/PlaywrightAssertions.html \
    com/microsoft/playwright/junit/UsePlaywright.html; do
  if ! echo "$ENTRIES" | grep -q -x -F "$page"; then
    echo "ERROR: missing $page"
    FAILED=1
  fi
done

IMPL_PAGES=$(echo "$ENTRIES" | grep -E '^com/microsoft/playwright/impl/[^/]+\.html$' || true)
if [[ -n "$IMPL_PAGES" ]]; then
  echo "ERROR: com.microsoft.playwright.impl should be excluded, found:"
  echo "$IMPL_PAGES"
  FAILED=1
fi

exit $FAILED
