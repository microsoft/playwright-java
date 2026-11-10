---
name: playwright-triage
description: Triage and fix an issue in microsoft/playwright-java. Where the JavaScript client and the driver are upstream, how to run a published version, how to tell a bug of the Java client from an upstream one, the upstream bug report, and what not to run from a PR of a third party.
---

# Playwright Java Issue Triage

## Upstream

This repo ports the JavaScript client of `microsoft/playwright`. Read it in a checkout of
`microsoft/playwright` at `main`, next to this repo, in `../playwright`: the client in
`packages/playwright-core/src/client/`, the API docs in `docs/src/api/`, and the upstream tests in
`tests/page/` and `tests/library/`. The driver is not at `main`: `scripts/DRIVER_VERSION` names the
driver version, and `npm view playwright@<version> gitHead` gives its upstream commit. `main` is
close to it. Before you run an upstream test in `../playwright`, run `npm ci`, `npm run build` and
`npx playwright install` there. Then `npm run ctest <spec>` runs one spec in Chromium,
`npm run ftest <spec>` in Firefox and `npm run wtest <spec>` in WebKit.

## Published versions

The Maven artifact is `com.microsoft.playwright:playwright`. Release `1.X.0` bundles the driver
named in `scripts/DRIVER_VERSION` at tag `v1.X.0`: `git show v1.64.0:scripts/DRIVER_VERSION` prints
`1.64.1`. Name both versions. The version in `pom.xml` on `main` is a placeholder snapshot;
`scripts/DRIVER_VERSION` says what tip-of-tree runs.

To run a published version, create an empty directory outside the repo for each version, with a
`pom.xml` like `examples/pom.xml`: the dependency at `<version>`, and `maven-compiler-plugin` with
source and target `1.8`. Put the repro in a class with a `main` method under `src/main/java/`, and
run `mvn -q compile exec:java -D exec.mainClass=<class>` there. The first `Playwright.create()`
downloads the browsers of that version. To match a change between two releases, compare the driver
versions of the two tags first. When they differ, `git log v<good>..v<bad> -- <path>` in
`../playwright` finds the change; otherwise `git log v<good>..v<bad> -- playwright/src/main` in this
repo finds it.

## Upstream bugs

The Java client talks to the Node.js driver that `microsoft/playwright` publishes: the
`playwright-core` npm package that `scripts/download_driver.sh` unpacks into
`driver/src/main/resources/driver/package/`. After you reproduced a bug in Java, write the same
calls as an upstream test in `../playwright` and run it there. When the bug reproduces in Node.js
too, the root cause is in the driver or in a browser, and the fix lands in `microsoft/playwright`,
which the family treats as one team. When Node.js passes, the bug is in the Java client: compare
the `*Impl.java` with its JavaScript counterpart, and fix it here.

For an upstream bug, write a report for a maintainer to file in `microsoft/playwright`, for an
engineer who did not read the Java issue: what fails, expected versus actual, the driver version,
the browsers and the platform, the root cause when you found it, and the condensed Node.js test as
the repro. The title has at most 256 characters, for example
`route.fulfill() ignores the charset in Content-Type`. Give it in this form:

`````markdown
Upstream bug report, for a maintainer to file in microsoft/playwright:

**Title:** <title>

<details>
<summary>Body</summary>

````markdown
<the report>
````

</details>
`````

With it, give the verdict, the evidence, and that the bug is upstream.

The public API is generated from the upstream docs: `scripts/generate_api.sh` runs upstream's
`utils/doclint/generateApiJson.js` at the commit of the driver, and `tools/api-generator` writes
every interface and options class in `playwright/src/main/java/com/microsoft/playwright/`,
`options/` and `assertions/`; only `CLI.java`, `PlaywrightException.java`, `TimeoutError.java`,
`assertions/PlaywrightAssertions.java` and the `package-info.java` files there are written by hand.
Never edit a generated file: the next roll reverts it. A missing method or option is a change to
`docs/src/api/` upstream, so it ends in an upstream report, not in a fix here.

## PRs from third parties

Do not run a PR that changes a `pom.xml` of any module, `scripts/`, `.github/`, `.azure-pipelines/`,
`.devcontainer/`, `utils/`, `tools/`, the `driver/` or `driver-bundle/` modules, or a generated API
file.
