---
name: playwright-roll
description: Roll Playwright Java to a new microsoft/playwright commit. Moves the commit in scripts/DRIVER_VERSION, builds the driver from source, generates the API, and ports the upstream changes of the client.
---

# Rolling Playwright

Read `AGENTS.md` and `ROLLING.md` first.

`scripts/DRIVER_VERSION` holds one commit of `microsoft/playwright`. We build the driver from source at that commit. `.upstream/` (gitignored) is a clone of `microsoft/playwright` at that commit. Read the upstream code there. Do not edit, commit or switch branches in `.upstream/`.

A roll moves `scripts/DRIVER_VERSION` to a newer commit and ports the changes between the two commits. A roll does not use the published npm packages or their versions.

## 1. Roll

```bash
./scripts/roll_driver.sh                 # the tip of upstream main
./scripts/roll_driver.sh release-1.X     # the tip of an upstream release branch
./scripts/roll_driver.sh <commit>        # an exact commit (40 hex digits)
```

Use the target that the user gives. If the user gives no target, roll `main` to upstream `main`, and roll a `release-1.X` branch of this repository to upstream `release-1.X`.

The script writes the commit to `scripts/DRIVER_VERSION`, rolls Node.js, builds the driver from `.upstream/`, generates the API and updates the README. Then get the two commits:

```bash
old=$(git show HEAD:scripts/DRIVER_VERSION)
new=$(cat scripts/DRIVER_VERSION)
```

## 2. Find the changes to port

List the upstream commits between the two commits that changed a path the Java client depends on:

- `docs/src/api/` — the source of `api.json`. A new or removed method or option, or a changed `langs:` filter, comes from here.
- `packages/playwright-core/src/client/` — the JS client. The Java client is a port of it.
- `packages/isomorphic/` — selector engines, locator generation and parsing, and the aria snapshot code. The client and the server share this code. A change here can change client helpers such as `getByRoleSelector`.
- `packages/playwright/src/matchers/matchers.ts` — the assertion methods. A change here is usually a new option of `LocatorAssertions` or `PageAssertions`.
- `packages/protocol/spec/` — the wire protocol, one `*.yml` file for each area. A new method or event, a renamed parameter or a changed result changes the messages that the Java `*Impl` classes send and receive.

```bash
git -C .upstream log "$old..$new" --oneline --reverse -- \
  'docs/src/api/' \
  'packages/playwright-core/src/client/' \
  'packages/isomorphic/' \
  'packages/playwright/src/matchers/matchers.ts' \
  'packages/protocol/spec/'
```

The list starts with the oldest commit. Do these steps for each commit:

1. Read the commit: `git -C .upstream show <sha>`.
2. If the change is internal to JS (bundling, dispatchers, electron, mcp, dashboard, trace viewer, test runner), skip it.
3. If the commit changes `docs/src/api/` or types, read the `langs:` annotations. A feature with `langs: js` or `langs: js, python` does not apply to Java.
4. If the commit adds or changes a public method or option that applies to Java, port it. `roll_driver.sh` already generated the new types and options in the Java interfaces. Usually only the `*Impl` code is missing.
5. Make sure that a later commit did not revert the change. Look at the code at `$new` before you port.
6. Keep notes in a file (for example `/tmp/roll-notes.md`). For each upstream PR, write one line: ported, skipped or already supported, with the reason. The PR description comes from this file.

## 3. The roll PR

The PR contains:

- the new commit in `scripts/DRIVER_VERSION`, and the new `scripts/NODE_VERSION` if Node.js changed;
- the generated interfaces from `roll_driver.sh`;
- the `*Impl` code for each ported feature, the same as the upstream JS client (`.upstream/packages/playwright-core/src/client`);
- changes to the generator (import lists, special cases) if there are new types;
- one small test for each new public API: a listener for a new event, a call for a new method, a regression test for a changed return type.

Title: `chore(driver): roll to <version> (<first 10 characters of $new>)`. `<version>` is the version that the driver reports. Read it from the source, not from npm:

```bash
node -p "require('./.upstream/packages/playwright-core/package.json').version"   # for example 1.67.0-next
```

Description: list each upstream PR that you ported, each PR that you skipped (with the reason), and each PR that Java already supported.

## Mimicking the JavaScript implementation

The Java client is a port of the JS client in `.upstream/packages/playwright-core/src/client/`. When implementing a new or changed method, always read the corresponding JS file first and mirror its logic:

```
.upstream/packages/playwright-core/src/client/browserContext.ts
.upstream/packages/playwright-core/src/client/page.ts
.upstream/packages/playwright-core/src/client/tracing.ts
.upstream/packages/playwright-core/src/client/video.ts
.upstream/packages/playwright-core/src/client/locator.ts
.upstream/packages/playwright-core/src/client/network.ts
...
```

Key translation rules:

**Protocol calls** — `await this._channel.methodName(params)` → `sendMessage("methodName", params, NO_TIMEOUT)`

**Extracting a returned channel object from a result** — JS uses `SomeClass.from(result.foo)` which resolves the JS-side object for a channel reference. In Java, the object was already created when the server sent `__create__`, so extract it from the connection: `connection.getExistingObject(result.getAsJsonObject("foo").get("guid").getAsString())`

**Async/await** — all `await` calls become synchronous `sendMessage(...)` calls since the Java client is synchronous.

**`undefined` / optional params** — JS `options?.foo` checks translate to `if (options != null && options.foo != null)` null checks before adding to the params `JsonObject`.

**`_channel` fields** — the JS `this._channel.foo` maps to calling `sendMessage("foo", ...)` on `this` in the Impl class.

**Channel object references in params** — when a JS call passes a channel object as a param (e.g. `{ frame: frame._channel }`), in Java pass the guid: `params.addProperty("frame", ((FrameImpl) frame).guid)`.

**Module-level state** — JS sometimes keeps state at module level (e.g. the call id counter in `createCallIdGenerator()`). Don't translate it into a `static` field with atomics; keep it per `Connection` (or per object) as a plain field. Each connection is used from one thread, so the port should not change the threading model.

## Fixing generator and compilation errors

After running `./scripts/roll_driver.sh`, the build often fails because the generated Java interfaces reference new types or methods that the generator doesn't know how to handle yet, and the `*Impl` classes don't implement new interface methods.

### ApiGenerator.java fixes (tools/api-generator/src/main/java/com/microsoft/playwright/tools/ApiGenerator.java)

The generator has hardcoded lists that control which imports are added to each generated file. When new classes appear in the API, add them to the relevant lists in `Interface.writeTo`:
- `options.*` import list — add new classes that use types from the options package
- `java.util.*` import list — add new classes that use `List`, `Map`, etc.
- `java.util.function.Consumer` list — add new classes with `Consumer`-typed event handlers

Type mapping: when JS-only types (like `Disposable`) are used as return types in Java-compatible methods, add a mapping in `convertBuiltinType`. For example, `Disposable` → `AutoCloseable`.

Event handler generation: events with `void` type generate invalid `Consumer<void>`. Handle this case in `Event.writeListenerMethods` by emitting `Runnable` instead.

Type names: give every new object type an explicit alias upstream. When a roll brings in a new object type (an option or return value written as `<[Object]>` in `docs/src/api/`), it should usually have its own `* alias: <Name>` line. Without one, each language's generator makes up a name, and the Java one can be vague (e.g. `Style`) or differ between Java and .NET.
- Prefer a plain `alias:` so every language gets the same name. Use `alias-java:` / `alias-csharp:` only when a language really needs a different name.
- Add the aliases in a small upstream docs PR (e.g. microsoft/playwright#43164, `Style` → `ScreencastActionStyle`). Don't special-case names in `ApiGenerator`.
- After the upstream change lands, roll to a driver that includes it. Don't hand-edit the generated files.

After editing the generator, recompile and re-run it:
```
mvn -f tools/api-generator/pom.xml compile -q
mvn -f tools/api-generator/pom.xml exec:java -Dexec.mainClass=com.microsoft.playwright.tools.ApiGenerator
```

### Impl class fixes (playwright/src/main/java/com/microsoft/playwright/impl/)

After regenerating, compile `playwright/` to find what's missing:
```
mvn -f playwright/pom.xml compile 2>&1 | grep "ERROR"
```

Common patterns:

**Return type changed (e.g. `void` → `AutoCloseable`):** Update the method signature in the Impl class and return an appropriate `AutoCloseable`. Check the JS client to see what kind of disposable is used:
- If JS returns `DisposableObject.from(result.disposable)` — the server created a disposable channel object. Extract its guid from the protocol result and return `connection.getExistingObject(guid)` (a `DisposableObject`).
- If JS returns `new DisposableStub(() => this.someCleanup())` — it's a local callback. Return `new DisposableStub(this::someCleanup)` in Java.
- Examples: `addInitScript`/`exposeBinding`/`exposeFunction` → `DisposableObject`; `route(...)` → `DisposableStub(() -> unroute(...))`; `Tracing.group` → `DisposableStub(this::groupEnd)`; `Video.start` → `DisposableStub(this::stop)`.

**New method missing:** Add a stub implementation. Common patterns:
- Simple protocol message: `sendMessage("methodName", params, NO_TIMEOUT)`
- New property accessor (e.g. from initializer): `return initializer.get("fieldName").getAsString()`
- Delegation to mainFrame (for Page methods): `return mainFrame.locator(":root").method(...)`

**New interface entirely (e.g. `Debugger`):** Create a new `*Impl` class extending `ChannelOwner`, implement the interface, and register the type in `Connection.java`'s switch statement. Initialize the field from the parent's initializer in the parent's constructor (e.g. `connection.getExistingObject(initializer.getAsJsonObject("debugger").get("guid").getAsString())`).

**Field visibility:** If a field needs to be accessed from a sibling Impl class (e.g. setting `existingResponse` on `RequestImpl` from `BrowserContextImpl`), change it from `private` to package-private.

**`ListenerCollection` only supports `Consumer<T>`, not `Runnable`.** For void events that use `Runnable` handlers, maintain a plain `List<Runnable>` instead.

**Protocol changes that remove events** — when a method's response now returns an object directly instead of via a subsequent event, update the Impl to capture it from the `sendMessage` result and remove the old event handler. Example: `videoStart` used to fire a `"video"` page event to deliver the artifact; it now returns the artifact directly in the response. Check git history of the upstream JS client when tests hang unexpectedly.

**Protocol parameter renames** — protocol parameter names can change between versions (e.g. `wsEndpoint` → `endpoint` in `BrowserType.connect`). When a test fails with `expected string, got undefined` or similar validation errors from the driver, check `packages/protocol/spec/*.yml` for the current parameter names and update the corresponding `params.addProperty(...)` call in the Impl class. Also check the JS client (`src/client/`) to see how it builds the params object.

## Rebuilding the driver modules after a roll

`./scripts/roll_driver.sh` does the full roll. It moves `scripts/DRIVER_VERSION` to the new commit. It builds the driver from `.upstream/` into `driver/src/main/resources/driver/package/` and `driver-bundle/src/main/resources/driver/<platform>/`. It generates `api.json` and the Java interfaces, and it updates the README. If all steps pass, the next `mvn` command uses the new driver.

If a step fails, the script stops before Maven installs the new driver. The most frequent cause is the API generator, which fails on a new type (see *Fixing generator and compilation errors*). After that, the test JVM loads the **old** driver from `target/classes` or from the installed jar, although the source resources already hold the new driver.

To correct this, install the two driver modules one time before you run the tests again:
```
mvn -f driver/pom.xml install -DskipTests
mvn -f driver-bundle/pom.xml install -DskipTests
```

If `scripts/generate_api.sh` prints `.upstream/ is not at ...`, run `scripts/build_driver.sh`.

## Porting and verifying tests

**Before porting an upstream test file, check the API exists in Java.** The upstream repo may have test files for brand-new APIs that haven't been added to the Java interface yet (e.g., `screencast.spec.ts` tests `page.screencast` which may not be in the generated `Page.java`). Check `git diff main --name-only` to see what interfaces were added this roll, and verify the method exists in the generated Java interface before porting.

**Java test file names don't always match upstream spec names.** `TestScreencast.java` tests `recordVideo` video-file recording (which corresponds to `video.spec.ts`), not the newer `page.screencast` streaming API (`screencast.spec.ts`). When comparing coverage, check test *content*, not just file names.

**Remove tests for behavior that was removed upstream.** When the JS client drops a client-side error check (e.g., "Page is not yet closed before saveAs", "Page did not produce any video frames"), delete the corresponding Java tests rather than trying to keep them passing. Check the upstream `tests/library/` spec to confirm the behavior is gone.

**Run the full suite to catch regressions, re-run flaky failures in isolation.** Some tests (e.g., `TestClientCertificates#shouldKeepSupportingHttp`) time out only under heavy parallel load. Run the failing test alone to confirm it's flaky before investigating further.

**Don't change the driver environment per test.** The driver is a JVM-wide singleton, so `Playwright.CreateOptions.env` (e.g. `PWTEST_UNDER_TEST`) only applies to the first `Playwright.create()` in the JVM. Forcing a new driver instance from a test breaks unrelated tests (`ClosedFileSystemException`). Test through what the public API exposes instead — e.g. screencast action decorations render in a closed shadow root, so the style test checks screencast frames for the decoration's pixels rather than querying the DOM.

## Reviewing the diff before the PR

Before opening the PR (and before pushing follow-ups), read `git diff upstream/main` end to end:
- **Removed generated methods** — `git diff upstream/main -- playwright/src/main/java/com/microsoft/playwright/*.java | grep '^-.*;'`. A return type change like `void` → `AutoCloseable` still compiles for callers but breaks code compiled against the previous jar. Call these out in the PR.
- **Behavior beyond upstream** — client-side checks or fixes not in the JS client (e.g. an option that was never sent). Keep them only if deliberate, and list them in the PR.
- **Stray files** — local config such as `.claude/settings.json`, or unrelated edits like `examples/pom.xml`, must not be committed.

## Diagnosing hanging tests

When `mvn test` hangs and surefire eventually times the JVM out, it writes thread dumps to `playwright/target/surefire-reports/<timestamp>-jvmRun*.dump`. To find the stuck test:

```
grep "com.microsoft.playwright.Test" playwright/target/surefire-reports/*-jvmRun1.dump | sort -u
```

Each line is a stack frame inside a test method — typically you'll see one or two test methods blocked on a `Future.get()`, `waitForCondition`, or similar. That's the hanging test.

When you've identified a hanging test:
1. Run it in isolation: `mvn -f playwright/pom.xml test -Dtest='TestClass#testMethod'`. If it passes alone, it's a parallel-load flake — note it but move on.
2. If it still hangs in isolation, look for a recent fix in the upstream repo for the *same* test name. Use `git -C .upstream log --oneline -- tests/library/<spec>.spec.ts`. Upstream fixes for client-side hangs are often small and portable (e.g. `about:blank` → `server.EMPTY_PAGE` from microsoft/playwright#39840 fixed `route-web-socket.spec.ts` arraybuffer hangs — apparently some browser changed the WebSocket origin policy on `about:blank`).
3. When porting an upstream fix, mirror the helper signature change rather than hard-coding workarounds. E.g. if upstream added a `server` parameter to `setupWS`, do the same in Java by injecting `Server server` via the JUnit fixture (`@FixtureTest` already wires up `ServerLifecycle`, so adding `Server server` to the test method signature is enough — no class-level boilerplate). Watch for local-variable shadowing when you add a `Server server` parameter to a method that already has a `WebSocketRoute server` local; rename the local.
