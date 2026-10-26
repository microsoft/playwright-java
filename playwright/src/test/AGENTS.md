# How to port the upstream tests to Java

These rules say how to write `playwright/src/test/java/com/microsoft/playwright/tests/{page,library}`
from `tests/page` and `tests/library` in `microsoft/playwright` (the checkout in `../playwright`,
at the commit of the driver this repo uses). Use them for every new or changed upstream test.

The port is line by line, with **zero deviations**. A reviewer reads the `.spec.ts` file and
the `.java` file side by side. Each upstream statement maps to one Java statement, or to one of
the fixed patterns in this document. The test does what the upstream test does, in the same
order, with the same values, and checks the same things. Port from the upstream source in front
of you, never from memory of what the test does.

**Never change a port to make it pass.** No skip, condition, wait or assertion that upstream
does not have, for any reason: not a browser channel, not a CI image, not an operating system,
not flakiness. A port that fails shows a bug in the Java client, the driver, the test harness or
the environment: first re-read the upstream test and check that the port is faithful, then
report it with the evidence. The one allowed mark is `@Fixme("java: <reason>")` for a bug or gap
of the Java client that is not fixed yet, `@Fixme("harness: <reason>")` for a missing piece of
the test harness, or `@Fixme("driver: <reason>")` when the upstream test is newer than the driver
this repo bundles (the upstream checkout is ahead of the driver; name the upstream commit or PR
that the driver lacks). The test still runs, its failure is reported as skipped with the reason,
and it fails once it starts to pass so the mark gets removed. `@Fixme(value = "...",
browsers = WEBKIT)` limits the expectation to some browsers. A test that fails only sometimes is
reported as flaky, not marked.

The harness (fixtures, test server, helpers) is `com.microsoft.playwright.fixtures`, see its
`package-info.java`.

## 1. Files and names

- The upstream file decides the Java file: `tests/page/page-check.spec.ts` is
  `com/microsoft/playwright/tests/page/TestPageCheck.java`; `tests/library/chromium/oopif.spec.ts`
  is `tests/library/chromium/TestOopif.java`. The class name is `Test` and the file name in
  PascalCase with `-` and `.` as the word separators: `elementhandle-click` is
  `TestElementhandleClick`, `page-clock.frozen` is `TestPageClockFrozen`, `matchers.misc` is
  `TestMatchersMisc`. The class javadoc is `/** Port of tests/page/page-check.spec.ts. */`; the
  license header is the one of the upstream file. The tests keep the upstream order.
- Every upstream test has an identifier, the first token of its title
  (`it('CH-001 should check the box', ...)`). `playwright/src/test/upstream_registry.md` is a
  copy of upstream's `tests/registry.md` and lists them all. The port is a `@PlaywrightTest`
  method with `@DisplayName` holding the upstream title verbatim, identifier and `@smoke` tag
  included, and a method name that is the title in camelCase without the identifier and tags:

  ```java
  @PlaywrightTest
  @DisplayName("CH-001 should check the box @smoke")
  void shouldCheckTheBox(Page page) {
  ```

  Characters that cannot be in an identifier are dropped (`should work with $eval` is
  `shouldWorkWithEval`, `toHaveCount pass` is `toHaveCountPass`); a title ending in a number
  keeps it (`should throw when not a checkbox 2` is `shouldThrowWhenNotACheckbox2`). Method
  names must be unique in their class. The `@DisplayName` is the title of the `it()` call
  itself, with its characters as in the source (`"EH-036 should work with <select/>"`, a `"`
  escaped as `\"`): the registry shows the same title after the describe titles and a `›`,
  with markdown escapes, which are not part of it.
- `test.describe('toHaveCount', () => { ... })` is a `@Nested class ToHaveCount { ... }`, named
  after the describe title in PascalCase. A skip or `test.use()` at the top of the describe goes
  on the nested class (section 3).
- A test that a loop declares several times (`for (const kind of kinds) it(...)`) is a
  `@Nested @ParameterizedClass` with a `@ValueSource`/`@MethodSource` and a `@Parameter` field,
  holding the test; the `@DisplayName` keeps the title as written in the source, with `${kind}`.
  A `@Fixture` method of the nested class may read the field.
- `beforeEach`/`afterEach` are `@BeforeEach`/`@AfterEach` methods, which may take fixture
  parameters. The fixtures that a file defines (`test.extend({...})`) are `@Fixture` methods at
  the top of the class returning the fixture's type (a small nested class when upstream returns a
  plain object). Helper functions of the file are private static methods of the class.
- A test that can not exist in Java (a JavaScript-only API, Node.js client internals, tools that
  run in the driver, `@playwright/test` features) is not ported: it goes in
  `playwright/src/test/upstream_not_ported.txt`, with the reason. Nothing else is left out.
  `python3 scripts/check_test_registry.py` checks that every test in the registry is ported or
  listed as not ported, under the expected class and title. `--missing <code or file glob>`
  prints the names still to port.

## 2. Fixtures

Fixtures are parameters of the test method (or of a `@BeforeEach`/`@Fixture` method), resolved
by type. The upstream fixtures map to:

| Upstream | Java parameter |
|---|---|
| `page`, `context`, `browser`, `browserType`, `playwright` | `Page page`, `BrowserContext context`, `Browser browser`, `BrowserType browserType`, `Playwright playwright` |
| `browserName` | `BrowserName browserName` (enum: `CHROMIUM`, `FIREFOX`, `WEBKIT`; `browserName.toString()` is `"chromium"`) |
| `server`, `httpsServer` | `TestServer server`, `HttpsTestServer httpsServer` |
| `asset('file')` | `asset("file")` (static import of `Utils`), a `Path` |
| `testInfo`, `test.info()` | `TestInfo testInfo`: `outputPath(...)`, `expectToMatchSnapshot(...)` |
| `contextFactory(options)` | `ContextFactory contextFactory`: `contextFactory.newContext(options)` |
| `launchPersistent(options)` | `LaunchPersistent launchPersistent`: `launchPersistent.launch(options)` has `.context` and `.page` |
| `createUserDataDir()` | `CreateUserDataDir createUserDataDir`: `createUserDataDir.create()` |
| `pageWithHar(options)` | `PageWithHar pageWithHar`: `pageWithHar.create(options)` has `.page`, `.context`, `.getLog()`, `.getZip()` |
| `channel`, `headless`, `isBidi`, `platform`, `isWindows`, `isMac`, `isLinux`, `macVersion` | `Env.channel()`, `!Env.headed()`, `Env.isBidi()`, `Env.platform()`, `Env.isWindows`, `Env.isMac`, `Env.isLinux`, `Env.macVersion()` |
| `isHeadlessShell` | `Env.isHeadlessShell(browserName)` |
| `browserVersion`, `browserMajorVersion` | `browser.version()`, `browserMajorVersion(browser)` (`Utils`) |
| `isAndroid`, `isElectron`, `mode !== 'default'`, `isFrozenWebkit` | always false: a skip on them is dropped |
| `toImpl`, `startRemoteServer`, `proxyServer`, `socksPort`, `childProcess`, `daemonProcess`, `waitForPort`, `findFreePort` | not available yet: list the test as not ported with `TODO harness: needs <fixture>` |
| `kTargetClosedErrorMessage` | `kTargetClosedErrorMessage` (`Utils`) |
| `attachFrame`, `detachFrame`, `verifyViewport`, `expectedSSLError`, `chromiumVersionLessThan`, `rafraf`, `roundBox`, `unshift` | the same names in `Utils` (static import) |
| `expect(received).toContainYaml(expected)` | `expectToContainYaml(received, expected)` (`Utils`) |
| `expect(buffer).toMatchSnapshot('x.png')`, `comparePNGs` | `testInfo.expectToMatchSnapshot(buffer, "x.png")`; `comparePNGs` is a `TODO harness` |

Upstream's `browserType.launch(options)` and `browserType.launchPersistentContext(dir, options)`
add the project's launch options (headless, channel) to the given ones. In Java, build the
options on `Env.launchOptions()` or `Env.launchPersistentContextOptions()`:
`browserType.launch({ proxy: { server } })` is
`browserType.launch(Env.launchOptions().setProxy(server))`. `launchPersistent.launch(options)`
does this itself. Never mutate the `BrowserType.LaunchOptions` fixture: it is shared by the
worker.

`TestServer` has the methods of upstream's `TestServer`: `PREFIX`, `CROSS_PROCESS_PREFIX`,
`EMPTY_PAGE`, `HOST`, `HOSTNAME`, `PORT`, `setRoute`, `setRedirect`, `setAuth`, `setCSP`,
`setExtraHeaders`, `enableGzip`, `setContent`, `serveFile`, `waitForRequest`,
`onceWebSocketConnection`, `waitForWebSocket`, `waitForWebSocketConnectionRequest`,
`waitForUpgrade`, `sendOnWebSocketConnection`. A route handler is
`(req, res) -> { ... }` with the Node shapes: `req.url`, `req.method`, `req.headers` (lower-case
names), `req.rawHeaders`, `req.postBody()`, `req.socket().destroy()`; `res.writeHead(200,
"Content-Type", "text/html")` (names and values alternate; or a `Map`), `res.setHeader`,
`res.statusCode = 404`, `res.write`, `res.end`, `res.flushHeaders()`, `res.socket()`. A handler
that upstream writes as `() => {}` (never answers) is `(req, res) -> {}`.
`await server.waitForRequest('/x')` is `server.waitForRequest("/x").get()`; create the future
before the action that triggers the request, like upstream. A server WebSocket (`ws` of
`onceWebSocketConnection`) has `send`, `close(code, reason)`, `onMessage(message -> ...)`
(`message.text()`, `message.data`, `message.isBinary`), `onClose((code, reason) -> ...)`,
`waitForMessage()`, `waitForClose()`.

## 3. Skips and marks

- `it.skip(browserName === 'webkit', reason)` is `@DisabledOnBrowser(value = WEBKIT, reason = "...")`;
  `browserName === 'webkit' && isMac` is `@DisabledOnBrowser(value = WEBKIT, os = OS.MAC)`;
  `!headless` adds `headed = true`. `it.skip(browserName !== 'chromium')` is
  `@EnabledOnBrowser(CHROMIUM)`. Any other condition is `assumeFalse(condition, reason)` /
  `assumeTrue(...)` (`org.junit.jupiter.api.Assumptions`) at the top of the test, with the same
  condition. A skip at the top of a file or a describe is the annotation on the class.
- `it.fixme(condition, reason)` is the same skip with the reason prefixed `fixme: `. `it.fail(...)`
  is the same skip with the reason prefixed `fail: `. A skip upstream is a skip in the port; it is
  not a deviation.
- `test.slow()`, `test.setTimeout(...)`, `test.info().annotations` and `{ annotation: ... }` are
  not ported. `test.step('name', async () => { ... })` is its body, after a `// it.step('name')`
  comment.
- Do not add `@Fixme`, `@Disabled` or an assumption that upstream does not have, except the
  `@Fixme` of the rule at the top.

## 4. Statements

- `expect(a).toBe(b)` and `toEqual(b)` are `assertEquals(b, a)`: the expected value comes first.
  `toBeTruthy()`/`toBeFalsy()` are `assertTrue`/`assertFalse` (or `assertNotNull`/`assertNull`
  for an object); `toContain(x)` is `assertTrue(a.contains(x))`; `toHaveLength(n)` is
  `assertEquals(n, a.size())`; `toBeGreaterThan(n)` is `assertTrue(a > n)`; `toMatch(/re/)` is
  `assertTrue(Pattern.compile("re").matcher(a).find())`; `toBeInstanceOf(X)` is
  `assertTrue(a instanceof X)`. A matcher on a locator, page or response
  (`await expect(locator).toHaveText(...)`) is the same matcher of the Java API:
  `assertThat(locator).hasText(...)` (`PlaywrightAssertions.assertThat`), `.not.` is `.not()`.
- `await expect(promise).rejects.toThrow('text')` and `const error = await promise.catch(e => e);
  expect(error.message).toContain('text')` are
  `PlaywrightException error = assertThrows(PlaywrightException.class, () -> ...)` then
  `assertTrue(error.getMessage().contains("text"))`. The class is the one upstream expects:
  `TimeoutError` for `playwright.errors.TimeoutError`, `TargetClosedError`
  (`com.microsoft.playwright.impl`) for `TargetClosedError`. Compare messages with `contains`,
  as upstream does; the Java message of a driver error is the serialized error, which contains the
  message text but not the `page.goto:` prefix or the call log of the Node.js client: a test
  that checks those gets `@Fixme("java: ...")`.
- `const [popup] = await Promise.all([page.waitForEvent('popup'), page.click('a')])` is
  `Page popup = page.waitForPopup(() -> page.click("a"))`; the same for `waitForRequest`,
  `waitForResponse`, `waitForNavigation`, `waitForDownload`, `waitForFileChooser`,
  `waitForConsoleMessage`, `waitForWebSocket`, `waitForWorker`, `context.waitForPage` and
  `waitForEvent('x', predicate)` (`page.waitForX(predicate, () -> ...)`). The promises after the
  first go in the callback, in order. `page.on('x', ...)` is `page.onX(...)`, `page.off` is
  `page.offX`; `page.once('x', h)` is an `onX` handler that calls `offX` on itself first.
- A promise that is created before and awaited later, where the action can not go in a callback
  (`const promise = frame.goto(url); await server.waitForRequest(...); await promise`), is
  `CompletableFuture<Response> promise = async(() -> frame.navigate(url))` (`Utils.async`) and
  `promise.get()`; the error of a failed one is `cause(promise)`. Two independent actions in one
  `Promise.all` run the same way: one in `async`, the other inline. The client dispatches the
  driver's messages in order when several threads call it (a fair lock in `Connection`), but
  Playwright objects are not otherwise thread-safe: use `async` only when no callback form
  exists, mark the statement `// Java:`, and report every test that fails with
  `Object doesn't exist: ...` as "concurrent"; do not mark it `@Fixme`.
- A promise settled by an event handler (`new Promise(f => page.once('load', f))`, awaited
  later) is a `CompletableFuture` that the `onLoad` handler completes, awaited with
  `page.waitForCondition(future::isDone)`: the Java client only delivers events while the test
  thread is inside a Playwright call, so `future.get()` would hang forever.
- `page.evaluate(() => ...)`: the function stays JavaScript, in a string, with the same body.
  A function with arguments takes one argument: `page.evaluate("([a, b]) => a + b", asList(a, b))`
  or `page.evaluate("x => x", 7)`. A Node.js value that goes to the page is the Java value:
  `undefined` and `null` are `null`, an object is a `Map` (`mapOf("k", v, ...)` in `Utils`), an
  array is a `List` (`asList`), a `Buffer` is `byte[]`, a `RegExp` is a `Pattern`, a `Date` is
  a `Date`. A value from the page is a `String`, a `Boolean`, an `Integer` (a whole number) or a
  `Double`, a `List<Object>`, a `Map<String, Object>` or `null`: `expect(await page.evaluate(() =>
  7)).toBe(7)` is `assertEquals(7, page.evaluate("() => 7"))`, an object is
  `assertEquals(mapOf("foo", "bar"), ...)`, an array is `assertEquals(asList(1, 2), ...)`.
- The Java API names: `page.goto` is `page.navigate`, `page.$` is `querySelector`, `page.$$` is
  `querySelectorAll`, `page.$eval` is `evalOnSelector`, `page.$$eval` is `evalOnSelectorAll`,
  `page.mouse` is `page.mouse()`, `page.keyboard` is `page.keyboard()`, `page.clock` is
  `page.clock()`, `browser.newPage()` and the rest are the same. Options are the options object
  of the call: `{ waitUntil: 'load', timeout: 5000 }` is
  `new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD).setTimeout(5000)`. The
  signatures are in `playwright/src/main/java/com/microsoft/playwright/*.java`; read the file
  rather than guessing a name. The keys of the data that the page or the driver returns stay as
  they are (`request.headers().get("content-type")`).
- `new Promise(resolve => ...)` that the test settles itself is a `CompletableFuture` that the
  test completes; `new Promise(f => setTimeout(f, 100))` is `page.waitForTimeout(100)` when a
  page is at hand, else `Thread.sleep(100)`.
- `console.log` and comments stay, as comments. A comment that explains an upstream workaround
  stays with the code it explains.
- The template strings of `setContent` keep their newlines and indentation, as `"...\n" +`
  concatenation.

## 5. Marks

The port has one mark of its own, `// Java:`, on the line before a statement whose form the
rules above change in a way that is not obvious: a value that Java can not express as upstream
does, a helper that stands for a JavaScript construct. Do not mark a plain rename or a plain
`async`. A mark is not a deviation: the test still does what upstream does.

## 6. Running

From `playwright/`: `mvn -o -q test -Dtest=TestPageCheck` runs the class in the three browsers
(`BROWSER=chromium,firefox` to pick some). The reports are in `target/surefire-reports`,
`TEST-*.xml` names each test case after the upstream title. `mvn -o -q test-compile` only
compiles.
