# Playwright Java

The Java client is a port of the JavaScript client in `~/code/playwright/packages/playwright-core/src/client/`. When implementing or changing a method, read the corresponding JS file first and mirror its logic.

Paths such as `~/code/playwright` in this file assume that you check the repos out in `~/code`. Use the `gh` cli to interact with GitHub.

## How this repo depends on Playwright

- **The driver.** The Java client talks to the Node.js driver that `microsoft/playwright` publishes: the `playwright-core` npm package, which `scripts/download_driver.sh` unpacks into `driver/src/main/resources/driver/package/`. `scripts/DRIVER_VERSION` names its version, and `npm view playwright@<version> gitHead` prints its upstream commit. Upstream `main` is close to it. Release `1.X.0` bundles the driver of `scripts/DRIVER_VERSION` at tag `v1.X.0`.
- **Upstream code.** In `~/code/playwright`: the client in `packages/playwright-core/src/client/`, the API docs in `docs/src/api/`, the tests in `tests/page/` and `tests/library/`. Its `AGENTS.md` says how to build it and run a test.
- **Where a bug is.** When the same calls fail in a Node.js test upstream too, the cause is in the driver or a browser, and the fix lands in `microsoft/playwright`. When Node.js passes, the bug is in the Java client: compare the `*Impl.java` with its JavaScript counterpart, and fix it here.
- **The generated API.** `scripts/generate_api.sh` runs upstream's `utils/doclint/generateApiJson.js` at the commit of the driver, and `tools/api-generator` writes every interface and options class in `playwright/src/main/java/com/microsoft/playwright/`, `options/` and `assertions/`. Only `CLI.java`, `PlaywrightException.java`, `TimeoutError.java`, `assertions/PlaywrightAssertions.java` and the `package-info.java` files there are written by hand. Never edit a generated file: the next roll reverts it. A missing method or option is a change to `docs/src/api/` upstream.

## Build and test

```bash
scripts/download_driver.sh                           # the driver, once per DRIVER_VERSION
mvn -B install -D skipTests --no-transfer-progress   # build and install every module
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps" -f playwright/pom.xml --no-transfer-progress
BROWSER=chromium mvn test -f playwright/pom.xml -D test=TestPageScreenshot --no-transfer-progress
```

- `mvn test -f playwright/pom.xml` compiles the `playwright` module and its tests, so a change in `playwright/src/main/` needs no separate build step. After a change in `driver/` or `driver-bundle/`, run `mvn -B install -D skipTests --no-transfer-progress -f <module>/pom.xml` first: the `playwright` module takes those modules from the local Maven repository.
- The project targets Java 8. CI builds with JDK 8 (Zulu) and JDK 25. Do not use a newer language feature or library API.
- There is no lint or format check. The style is the Google Java Style with a two-space indent, and without comments. When you change a file under `playwright/src/test/java/com/microsoft/playwright/tests/`, run `python3 scripts/check_test_registry.py`.
- `-D test=<Class>` runs one class, `-D test=<Class>#<method>` one method. `BROWSER` is a comma-separated list of `chromium`, `firefox` and `webkit`; unset, it runs all three. `HEADED=1` runs headed. A filter that matches no test passes with `Tests run: 0`, so check the count. Each test method has a 120 second timeout, set in the root `pom.xml`. Do not set `PW_MAX_RETRIES`, which reruns a failed test; CI sets it to 3. The reports are in `playwright/target/surefire-reports/`.

## Tests

The tests are in `playwright/src/test/java/com/microsoft/playwright/`. A test is a `@PlaywrightTest` method that takes the fixtures it needs as parameters, resolved by type: `Page page`, `BrowserContext context`, `Browser browser`, `BrowserType browserType`, `Playwright playwright`, `BrowserName browserName`, `Server server`, `HttpsServer httpsServer` and `@TempDir Path`. The method runs once per selected browser, with a context and a page of its own; the browser and the servers are shared. A class overrides options with `@Fixture` methods, and skips a browser with `@DisabledOnBrowser` or `@EnabledOnBrowser`. Drive the page with `page.setContent(...)` or `page.navigate(server.PREFIX + "/...")`, and the server with `server.setRoute(...)`, `server.setRedirect(...)`, `server.futureRequest(...)` and `server.EMPTY_PAGE`.

| Directory | Use for |
|-----------|---------|
| `com/microsoft/playwright/` | the Java-only tests, one class per feature (`TestPageScreenshot`, `TestBrowserContextHar`). A regression test for a bug of the Java client goes here, in the class that owns the feature, as one added case. |
| `com/microsoft/playwright/tests/page/`, `tests/library/` | line-by-line ports of upstream `tests/page` and `tests/library`, one class per spec file (`TestPageCheck` for `page-check.spec.ts`), each test titled `@DisplayName("CH-001 ...")` with the upstream identifier. Do not add a test or an assertion that upstream does not have. When the port that covers a bug carries `@Fixme("java: ...")`, the fix removes the mark, and that port is the regression test. `playwright/src/test/AGENTS.md` has the rules. |
| `com/microsoft/playwright/junit/` | the public JUnit extension, `@UsePlaywright` |

Tag a regression test with the issue as the Java-only tests do: a comment with the full URL on the first line of the test body, `// https://github.com/microsoft/playwright-java/issues/<n>`. Good models: `TestBrowserContextExposeFunction`, `shouldNotDispatchBindingOnAClosedPage` (a binding, a popup and a context of its own in a try-with-resources block); `TestTracing`, `shouldRecoverTracingAfterAFailedStop` (a tagged test with `@TempDir` on `context.tracing()`).

## Commit Convention

Semantic commit messages: `label(scope): description`

Labels: `fix`, `feat`, `chore`, `docs`, `test`, `devops`

```bash
git checkout -b fix-39562
# ... make changes ...
git add <changed-files>
git commit -m "$(cat <<'EOF'
fix(proxy): handle SOCKS proxy authentication

Fixes: https://github.com/microsoft/playwright-java/issues/39562
EOF
)"
# **Never `git push` without an explicit instruction to push.**
git push origin fix-39562
gh pr create --repo microsoft/playwright-java --head <user>:fix-39562 \
  --title "fix(proxy): handle SOCKS proxy authentication" \
  --body "$(cat <<'EOF'
## Summary
- <describe the change very! briefly>

Fixes https://github.com/microsoft/playwright-java/issues/39562
EOF
)"
```

Never add Co-Authored-By agents in commit message.
Never add "Generated with" in commit message.
Never add test plan to PR description. Keep PR description short — a few bullet points at most.
Branch naming for issue fixes: `fix-<issue-number>`.

**Never amend commits.** Always create a new commit for follow-up changes, even when iterating on an open PR. Amending rewrites history and forces a force-push, losing the incremental review trail. Only amend if the user explicitly says so.

**Never `git push` without an explicit instruction to push.** Applies even when a PR is already open for the branch — additional commits are immediately visible to reviewers. Commit locally, report what was committed, and wait. Only push when the user's message contains "push", "upload", "create PR", "ship it", or equivalent.

## Skills

- **playwright-roll** (`.claude/skills/playwright-roll/SKILL.md`) — roll Playwright Java to a new upstream version: bump the driver, regenerate the API, and port relevant upstream changes.
- **playwright-java-release** (`.claude/skills/playwright-java-release/SKILL.md`) — prepare a release after the rolling PR merges: cut the release branch, mark the Maven version, and draft the GitHub release.
- **playwright-cherry-pick** (upstream, `~/code/playwright/.claude/skills/playwright-cherry-pick/SKILL.md`) — cherry-pick a merged PR into a `release-1.X` branch as `cherry-pick(#<pr>): <original title>`. Use the same rules for this repo's release branches.
