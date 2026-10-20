---
name: playwright-java-release
description: Prepare a Playwright Java release after the rolling PR has merged — cut the release branch, mark the Maven version, port the Node.js release notes to Java, draft the GitHub release, and tick the Java boxes in the internal checklist.
---

Use this skill once the `chore: roll driver to 1.X.0` PR has merged into `main` and the upstream JS `v1.X.0` is published. The rolling work itself is covered by the [[playwright-roll]] skill.

Throughout this doc, replace `X` with the minor version (e.g. `60` for `1.60.0`) and `<user>` with the fork owner (`gh api user --jq .login`).

The full release checklist lives in the private `microsoft/playwright-internal` repo as the `v1.X checklist` issue. Find its number once:

```bash
unset GITHUB_TOKEN
ISSUE=$(gh search issues --repo microsoft/playwright-internal "v1.X checklist" --json number --jq '.[0].number')
```

Tick each Java box incrementally (one PATCH per item) so the issue reflects accurate state if the flow is interrupted:

```bash
gh api repos/microsoft/playwright-internal/issues/$ISSUE --jq '.body' > /tmp/body.md
# edit /tmp/body.md to flip "- [ ]" → "- [x]" on the relevant Java item
gh api repos/microsoft/playwright-internal/issues/$ISSUE -X PATCH --field body=@/tmp/body.md
```

## 1. Cut the release branch

Push `release-1.X` from current `upstream/main` (which now contains the merged roll commit):

```bash
git fetch upstream main
git push upstream upstream/main:refs/heads/release-1.X
```

## 2. Release notes

There are two pieces of text, both written by hand:
- the `## Version 1.X` section of `docs/src/release-notes-java.md` in the upstream `playwright` repo. playwright.dev renders it.
- the body of the GitHub release, which is written from that section.

### 2a. Port the Node.js notes to `docs/src/release-notes-java.md`

Check whether the section is already upstream:

```bash
cd ~/playwright && git fetch upstream main release-1.X
git show upstream/release-1.X:docs/src/release-notes-java.md | grep -n '^## Version 1.X$'
```

If it's missing, write it by porting the `## Version 1.X` section of `docs/src/release-notes-js.md`. Do the work in a worktree off `upstream/main` so the local checkout stays untouched. If someone else already wrote it, still review it against the rules below. For example, the 1.63 Java notes went out saying "accepts an array of credentials".

Go through the JS section top to bottom and decide what to do with each item:

- **Keep** library features (Page, Frame, Locator, BrowserContext, APIRequestContext, Tracing, Screencast, assertions, …). Also keep breaking changes that affect the library, announcements about OS support and browser builds, and the Browser Versions list as is.
- **Drop** whatever exists only in the Node.js test runner or JS tooling:
  - `@playwright/test`, `playwright.config.ts`, and `npx playwright test` flags
  - `TestOptions`, `TestConfig`, `TestProject`, `FullConfig`, `TestStep` and `Fixtures`
  - reporters, the HTML report and UI mode
  - component testing
  - TypeScript-only features (generics, JSX, `tsconfig`)
  - `devices` descriptors and npm packages
- **Check `langs:`** of every linked member in `docs/src/api/` (`grep -n -A4 'method: Page.foo$' docs/src/api/class-page.md`). A member whose `langs:` doesn't include `java` doesn't exist in Java, so drop the item or reword it around the Java API.
- **Describe the Java shape** when the Java API differs from the JS one, using the names in the generated interfaces of this repo (`playwright/src/main/java/com/microsoft/playwright/`):
  - 1.63: the JS `trace.snapshots: { aria, screen }` object became the `ariaSnapshots` and `screenSnapshots` options of `Tracing.start`.
  - 1.64: the JS `recordVideo.fps` became the `recordVideoFps` option.
- **Replace config examples.** When a JS example shows a library feature through `playwright.config.ts`, show the equivalent API call instead. In 1.64, the video config became `page.screencast().start(...)` plus `showActions(...)`.
- **Add Java-only highlights**, such as 1.63's JSpecify nullability annotations. List the user-visible changes in this repo since the previous release branch was cut:
  ```bash
  git log --oneline --no-merges $(git merge-base upstream/release-1.<X-1> upstream/main)..upstream/main | grep -E '^\w+ (feat|fix)'
  ```

Write code samples and prose in Java style:

| Node.js | Java |
|---|---|
| `await page.goto('...')` | `page.navigate("...");`: no `await`, double quotes, semicolons, typed locals (`Locator dialog = ...`) |
| `page.getByRole('button', { name: 'Save' })` | `page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"))` |
| options object `{ path: 'video.webm', fps: 60 }` | options builder `new Screencast.StartOptions().setPath(Paths.get("video.webm")).setFps(60)` |
| object literal `{ name: 'session-id', value: '42', url }` | value class `new Cookie("session-id", "42").setUrl(url)` |
| `['a', 'b']`, `{ a: 2 }` | `Arrays.asList("a", "b")`, `Map.of("a", 2)`; `new String[] {"a", "b"}` for assertion arguments |
| property `page.webmcp`, `context.tracing` | getter `page.webmcp()`, `context.tracing()` |
| event `page.on('dialogclosed', handler)` | `page.onDialogClosed(handler)` |
| `expect(locator).toHaveText(...)` | `assertThat(locator).hasText(...)`. Likewise `toBeX` → `isX`, `toHaveX` → `hasX`, `toContainX` → `containsX`, `toMatchX` → `matchesX` |
| `route.continue()` and other members with `alias-java:` | the alias, e.g. `route.resume()` (`grep -rn 'alias-java' docs/src/api/`) |
| `npx playwright install --no-remove` | `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --no-remove"` |
| "array", `undefined`, `'ai'` in prose | "list", `null`, `"ai"` |

In the markdown, keep API references as links in the docs format (``[`method: Locator.within`]``, ``[`property: Page.webmcp`]``, ``[`option: Browser.newContext.recordVideoFps`]``, `[Locator]`) instead of writing out names. playwright.dev renders them with the Java names. Use ```` ```java ```` code fences. Every API a sample uses must exist in the generated Java interfaces, so grep for each method and option setter.

Send the change as an upstream PR titled `docs: release notes for v1.X Java`. The upstream `infra` workflow checks the syntax of `java` snippets, so make sure it passes. Once the PR merges, ask the user to get it cherry-picked to `release-1.X`: upstream maintainers push `cherry-pick(#NNNN): ...` commits straight to the release branch. Then tick the Java "write release notes" box and set its PR link.

### 2b. Write the GitHub release body

Write `/tmp/v1.X.0-release-notes.md` by hand from the `## Version 1.X` section of `upstream/release-1.X:docs/src/release-notes-java.md`:

- Drop the `## Version 1.X` heading and move every other heading up one level (`###` → `##`, `####` → `###`).
- Replace each docs-format link with a playwright.dev link whose text is the Java name from the table above:

| Markdown | GitHub release |
|---|---|
| `[Locator]` | `[Locator](https://playwright.dev/java/docs/api/class-locator)` |
| ``[`method: Locator.within`]`` | `[locator.within()](https://playwright.dev/java/docs/api/class-locator#locator-within)` |
| ``[`property: Page.webmcp`]`` | `[page.webmcp()](https://playwright.dev/java/docs/api/class-page#page-webmcp)` |
| ``[`event: Page.dialogClosed`]`` | `[page.onDialogClosed()](https://playwright.dev/java/docs/api/class-page#page-event-dialog-closed)` |
| ``[`method: LocatorAssertions.toBeVisible`]`` | `[assertThat(locator).isVisible()](https://playwright.dev/java/docs/api/class-locatorassertions#locator-assertions-to-be-visible)` |
| ``[`option: Screencast.start.fps`]`` | ``[`fps`](https://playwright.dev/java/docs/api/class-screencast#screencast-start-option-fps)`` |
| `[Playwright MCP](./getting-started-mcp.md)` | `[Playwright MCP](https://playwright.dev/java/docs/getting-started-mcp)` |
| `![alt](./images/foo.png)` | `![alt](https://github.com/microsoft/playwright/blob/main/docs/src/images/foo.png?raw=true)` |

How to build the links:
- **Link text:** start with the class name in lower camel case (`page`, `browserContext`, `apiRequestContext`). Methods and properties get `()` with no arguments, events become `on<Event>()`, and assertions start with `assertThat(locator).`, `assertThat(page).` or `assertThat(response).`.
- **Page path:** `class-<class name in lowercase>`.
- **Anchor:** built from the names in the docs, not the Java names: `<kebab(Class)>-<kebab(member)>`. Insert `-event-` for events and append `-option-<kebab(option)>` for options. kebab-case splits at every case change, so `APIRequestContext.addCookies` → `class-apirequestcontext#api-request-context-add-cookies`.

Then check that every link resolves. The GitHub draft is written after the JS release, so playwright.dev already documents the new APIs. playwright.dev serves minified HTML, so the anchor may appear without quotes (`id=page-webmcp>`):

```bash
grep -oE 'https://playwright\.dev/[^) ]+' /tmp/v1.X.0-release-notes.md | sort -u | while read -r url; do
  page=${url%%#*}
  html=$(curl -sfL "$page") || { echo "MISSING PAGE   $url"; continue; }
  if [[ "$url" == *"#"* ]]; then
    anchor=${url#*#}
    grep -qE "id=\"?${anchor}[\" >]" <<<"$html" || echo "MISSING ANCHOR $url"
  fi
done
```

Scan for anything that wasn't converted. Both commands should print nothing:

```bash
grep -nE '\[`(method|property|event|option|param): |\]\(\./|\[[A-Z][A-Za-z]*\]([^(]|$)' /tmp/v1.X.0-release-notes.md
grep -nE "\.on\('|\b(toBe|toHave|toContain|toMatch)[A-Z]|\bawait\b|\barray\b|\bundefined\b|\[[a-z][A-Za-z]*\.[a-z][A-Za-z]*\]\(" /tmp/v1.X.0-release-notes.md
```

If the body exposes a problem in the markdown itself, such as a JS-only feature or a JS-style sample, fix it upstream as well (2a). Otherwise playwright.dev keeps the problem.

Create the draft directly against `release-1.X`. Drafting against `main` and retargeting later is fragile, because every `gh release edit` rotates the `untagged-<hash>` ID:

```bash
gh release create v1.X.0 --repo microsoft/playwright-java --draft \
  --title "v1.X.0" --notes-file /tmp/v1.X.0-release-notes.md --target release-1.X
```

## 3. Bump the Maven version on the release branch

Cut `mark-v-1.X.0` off `upstream/release-1.X`, run `set_maven_version.sh`, and PR back to the release branch:

```bash
git checkout -b mark-v-1.X.0 upstream/release-1.X
./scripts/set_maven_version.sh 1.X.0
git add -u
git commit -m "chore: mark 1.X.0"
git push -u origin mark-v-1.X.0
gh pr create --repo microsoft/playwright-java --head <user>:mark-v-1.X.0 --base release-1.X \
  --title "chore: mark 1.X.0" \
  --body "Updates Maven version in all modules to \`1.X.0\` for the v1.X release."
```

`set_maven_version.sh` only invokes `mvn versions:set` on `pom.xml`, `tools/*/pom.xml`, and `examples/pom.xml`, but the root invocation cascades through the reactor, so the expected diff is 11 poms: root + `driver/` + `driver-bundle/` + `playwright/` (from the reactor cascade) + 6 under `tools/` + `examples/`, all flipping `1.<prev>.0-SNAPSHOT` → `1.X.0`. Any other file in the diff is a red flag.

## 4. Publish

The user publishes the draft release manually once the `mark-v-1.X.0` PR is merged. After publishing, the `v1.X.0` tag triggers the Azure DevOps pipelines that push the artifacts to Maven Central (`.azure-pipelines/publish.yml`) and the Docker images to ACR (`.azure-pipelines/publish-docker.yml`).
