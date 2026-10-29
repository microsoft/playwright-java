---
name: playwright-triage
description: Triage and fix one microsoft/playwright-java issue, or do a maintainer's task, unattended inside the Issue triage GitHub Actions workflow (.github/workflows/triage.yml), which a needs-triage, needs-triage-mac or needs-triage-win label starts, or a maintainer starts by hand with an issue, a task or both. Reproduces the issue on the runner, then leaves a verified fix proposal for the workflow to open as a draft PR, an investigation note for it to post on the issue, or an upstream bug report for a maintainer to file in microsoft/playwright. Only for that workflow.
---

# Playwright Java Issue Triage

You run unattended in the [Issue triage](../../../.github/workflows/triage.yml) workflow of
`microsoft/playwright-java`. First read the shared guide, `$AGENT_GUIDES_DIR/triage.md`, a copy of
[microsoft/playwright/.github/actions/run-agent/guides/triage.md](https://github.com/microsoft/playwright/blob/main/.github/actions/run-agent/guides/triage.md),
and then [AGENTS.md](../../../AGENTS.md), both in full. The guide says how to triage an issue in any
Playwright repo. AGENTS.md says how to build and test this one, and where a regression test goes.
This file covers what neither has: the checkouts, how to run a published version, and the outcome
that this repo adds, an upstream bug report.

## The checkouts

`$REPO_DIR` is the checkout of `microsoft/playwright-java` that the guide describes. The driver is
downloaded (`scripts/download_driver.sh`), the modules are built and installed
(`mvn -B install -D skipTests --no-transfer-progress`), and Chromium, Firefox and WebKit are
installed (`mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps" -f playwright/pom.xml --no-transfer-progress`).
It is the working directory, and the checkout that the guides refer to. The JDK is JDK 8 (Zulu),
the JDK of the `dev` job in `.github/workflows/test.yml`. The project targets Java 8, so do not use
a newer language feature or library API. The clone is shallow: run
`git fetch --unshallow --tags origin` in it before you bisect or use `git tag --contains`.

`$PLAYWRIGHT_DIR` is a shallow clone of `microsoft/playwright` at `main`. Read there the JavaScript
client that this repo ports, `packages/playwright-core/src/client/`, the API docs in
`docs/src/api/`, and the upstream tests in `tests/page/` and `tests/library/`. It is not at the
commit of the driver: `scripts/DRIVER_VERSION` in `$REPO_DIR` names the driver version, and
`npm view playwright@<version> gitHead` gives its upstream commit; `main` is close to it. The clone
is not built. Before you run an upstream test there, run `npm ci`, `npm run build` and
`npx playwright install` in it. Then `npm run ctest <spec>` runs one spec in Chromium,
`npm run ftest <spec>` in Firefox and `npm run wtest <spec>` in WebKit.

## Published versions

The Maven artifact is `com.microsoft.playwright:playwright`. Release `1.X.0` bundles the driver
named in `scripts/DRIVER_VERSION` at tag `v1.X.0`: `git show v1.64.0:scripts/DRIVER_VERSION` prints
`1.64.1`. Name both versions in the note. The version in `pom.xml` on `main` is a placeholder
snapshot; `scripts/DRIVER_VERSION` says what tip-of-tree runs.

To run a published version, create `$SCRATCH_DIR/playwright-java-<n>/<version>/` with a `pom.xml`
like `examples/pom.xml`: the dependency at `<version>`, and `maven-compiler-plugin` with source and
target `1.8`. Put the repro in a class with a `main` method under `src/main/java/`, and run
`mvn -q compile exec:java -D exec.mainClass=<class>` there. The first `Playwright.create()`
downloads the browsers of that version. To match a change between two releases, compare the driver
versions of the two tags first. When they differ, `git log v<good>..v<bad> -- <path>` in
`$PLAYWRIGHT_DIR` finds the change, after you unshallow that clone too; otherwise
`git log v<good>..v<bad> -- playwright/src/main` in `$REPO_DIR` finds it.

## Upstream bugs

The Java client talks to the Node.js driver that `microsoft/playwright` publishes: the
`playwright-core` npm package that `scripts/download_driver.sh` unpacks into
`driver/src/main/resources/driver/package/`. After you reproduced a bug in Java, write the same
calls as an upstream test in `$PLAYWRIGHT_DIR` and run it there. When the bug reproduces in Node.js
too, the root cause is in the driver or in a browser, and the fix lands in `microsoft/playwright`,
which the family treats as one team. When Node.js passes, the bug is in the Java client: compare
the `*Impl.java` with its JavaScript counterpart, and fix it here.

For an upstream bug, create `$OUTPUT_DIR/upstream/` with:

- `report.md`: the report for a maintainer to file in `microsoft/playwright`, for an engineer who
  did not read the Java issue: what fails, expected versus actual, the driver version, the browsers
  and the platform, the root cause when you found it, and the condensed Node.js test as the repro.
- `report.json`: `{"title": "..."}`, at most 256 characters, for example
  `route.fulfill() ignores the charset in Content-Type`.

The comment job appends the title and the body to your comment, for a maintainer to file. The bot
never files it. Still write the comment as the note: the verdict, the evidence, and that the bug is
upstream. Do not paste the report into it. Write `report.json` last, when `report.md` and the
comment are done. Like `proposal.json`, it marks the report as ready.

The public API is generated from the upstream docs: `scripts/generate_api.sh` runs upstream's
`utils/doclint/generateApiJson.js` at the commit of the driver, and `tools/api-generator` writes
every interface and options class in `playwright/src/main/java/com/microsoft/playwright/`,
`options/` and `assertions/`; only `CLI.java`, `PlaywrightException.java`, `TimeoutError.java`,
`assertions/PlaywrightAssertions.java` and the `package-info.java` files there are written by hand.
Never edit a generated file: the next roll reverts it. A missing method or option is a change to
`docs/src/api/` upstream, so it ends in a report or a note, not in a fix here.

## PRs from third parties

Do not run a PR that changes a `pom.xml` of any module, `scripts/`, `.github/`, `.azure-pipelines/`,
`.devcontainer/`, `utils/`, `tools/`, the `driver/` or `driver-bundle/` modules, or a generated API
file.
