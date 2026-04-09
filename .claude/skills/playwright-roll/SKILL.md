---
name: playwright-roll
description: Roll Playwright Java to a new version
---

Help the user roll to a new version of Playwright.
ROLLING.md contains general instructions and scripts.

Start with running ./scripts/roll_driver.sh to update the version and generate the API to see the state of things.
Afterwards, work through the list of changes that need to be backported.
You can find a list of pull requests that might need to be taking into account in the issue titled "Backport changes".
Work through them one-by-one and check off the items that you have handled.
Not all of them will be relevant, some might have partially been reverted, etc. - so feel free to check with the upstream release branch.

Rolling includes:
- updating client implementation to match changes in the upstream JS implementation (see ../playwright/packages/playwright-core/src/client)
- adding a couple of new tests to verify new/changed functionality

## Mimicking the JavaScript implementation

The Java client is a port of the JS client in `../playwright/packages/playwright-core/src/client/`. When implementing a new or changed method, always read the corresponding JS file first and mirror its logic:

```
../playwright/packages/playwright-core/src/client/browserContext.ts
../playwright/packages/playwright-core/src/client/page.ts
../playwright/packages/playwright-core/src/client/tracing.ts
../playwright/packages/playwright-core/src/client/video.ts
../playwright/packages/playwright-core/src/client/locator.ts
../playwright/packages/playwright-core/src/client/network.ts
...
```

Key translation rules:

**Protocol calls** — `await this._channel.methodName(params)` → `sendMessage("methodName", params, NO_TIMEOUT)`

**Extracting a returned channel object from a result** — JS uses `SomeClass.from(result.foo)` which resolves the JS-side object for a channel reference. In Java, the object was already created when the server sent `__create__`, so extract it from the connection: `connection.getExistingObject(result.getAsJsonObject("foo").get("guid").getAsString())`

**Async/await** — all `await` calls become synchronous `sendMessage(...)` calls since the Java client is synchronous.

**`undefined` / optional params** — JS `options?.foo` checks translate to `if (options != null && options.foo != null)` null checks before adding to the params `JsonObject`.

**`_channel` fields** — the JS `this._channel.foo` maps to calling `sendMessage("foo", ...)` on `this` in the Impl class.

**Channel object references in params** — when a JS call passes a channel object as a param (e.g. `{ frame: frame._channel }`), in Java pass the guid: `params.addProperty("frame", ((FrameImpl) frame).guid)`.

## Fixing generator and compilation errors

After running `./scripts/roll_driver.sh`, the build often fails because the generated Java interfaces reference new types or methods that the generator doesn't know how to handle yet, and the `*Impl` classes don't implement new interface methods.

### ApiGenerator.java fixes (tools/api-generator/src/main/java/com/microsoft/playwright/tools/ApiGenerator.java)

The generator has hardcoded lists that control which imports are added to each generated file. When new classes appear in the API, add them to the relevant lists in `Interface.writeTo`:
- `options.*` import list — add new classes that use types from the options package
- `java.util.*` import list — add new classes that use `List`, `Map`, etc.
- `java.util.function.Consumer` list — add new classes with `Consumer`-typed event handlers

Type mapping: when JS-only types (like `Disposable`) are used as return types in Java-compatible methods, add a mapping in `convertBuiltinType`. For example, `Disposable` → `AutoCloseable`.

Event handler generation: events with `void` type generate invalid `Consumer<void>`. Handle this case in `Event.writeListenerMethods` by emitting `Runnable` instead.

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

**Protocol parameter renames** — protocol parameter names can change between versions (e.g. `wsEndpoint` → `endpoint` in `BrowserType.connect`). When a test fails with `expected string, got undefined` or similar validation errors from the driver, check `packages/protocol/src/protocol.yml` for the current parameter names and update the corresponding `params.addProperty(...)` call in the Impl class. Also check the JS client (`src/client/`) to see how it builds the params object.

## Rebuilding the driver-bundle after a roll

`./scripts/roll_driver.sh` does the whole roll pipeline end-to-end: bumps `DRIVER_VERSION`, downloads new driver files into `driver-bundle/src/main/resources/driver/<platform>/`, regenerates `api.json` and the Java interfaces, and updates the README. When all of that succeeds, the next `mvn` invocation that touches `driver-bundle` will pick up the new files and you don't need to think about it.

But if any step in the pipeline fails (the very common case is the API generator throwing on a new type — see *Fixing generator and compilation errors*), the run aborts before `driver-bundle/target/classes/` has been refreshed. From that point on, until you manually rebuild `driver-bundle`, the test JVM will load the **old** driver from the cached `target/classes`/installed jar even though the source resources have already been swapped to the new version.

Fix — rebuild `driver-bundle` once before re-running tests:
```
mvn -f driver-bundle/pom.xml install -DskipTests
```

## Porting and verifying tests

**Before porting an upstream test file, check the API exists in Java.** The upstream repo may have test files for brand-new APIs that haven't been added to the Java interface yet (e.g., `screencast.spec.ts` tests `page.screencast` which may not be in the generated `Page.java`). Check `git diff main --name-only` to see what interfaces were added this roll, and verify the method exists in the generated Java interface before porting.

**Java test file names don't always match upstream spec names.** `TestScreencast.java` tests `recordVideo` video-file recording (which corresponds to `video.spec.ts`), not the newer `page.screencast` streaming API (`screencast.spec.ts`). When comparing coverage, check test *content*, not just file names.

**Remove tests for behavior that was removed upstream.** When the JS client drops a client-side error check (e.g., "Page is not yet closed before saveAs", "Page did not produce any video frames"), delete the corresponding Java tests rather than trying to keep them passing. Check the upstream `tests/library/` spec to confirm the behavior is gone.

**Run the full suite to catch regressions, re-run flaky failures in isolation.** Some tests (e.g., `TestClientCertificates#shouldKeepSupportingHttp`) time out only under heavy parallel load. Run the failing test alone to confirm it's flaky before investigating further.

## Diagnosing hanging tests

When `mvn test` hangs and surefire eventually times the JVM out, it writes thread dumps to `playwright/target/surefire-reports/<timestamp>-jvmRun*.dump`. To find the stuck test:

```
grep "com.microsoft.playwright.Test" playwright/target/surefire-reports/*-jvmRun1.dump | sort -u
```

Each line is a stack frame inside a test method — typically you'll see one or two test methods blocked on a `Future.get()`, `waitForCondition`, or similar. That's the hanging test.

When you've identified a hanging test:
1. Run it in isolation: `mvn -f playwright/pom.xml test -Dtest='TestClass#testMethod'`. If it passes alone, it's a parallel-load flake — note it but move on.
2. If it still hangs in isolation, look for a recent fix in the upstream repo for the *same* test name. Use `git log --oneline tests/library/<spec>.spec.ts` in `~/playwright`. Upstream fixes for client-side hangs are often small and portable (e.g. `about:blank` → `server.EMPTY_PAGE` from microsoft/playwright#39840 fixed `route-web-socket.spec.ts` arraybuffer hangs — apparently some browser changed the WebSocket origin policy on `about:blank`).
3. When porting an upstream fix, mirror the helper signature change rather than hard-coding workarounds. E.g. if upstream added a `server` parameter to `setupWS`, do the same in Java by injecting `Server server` via the JUnit fixture (`@FixtureTest` already wires up `ServerLifecycle`, so adding `Server server` to the test method signature is enough — no class-level boilerplate). Watch for local-variable shadowing when you add a `Server server` parameter to a method that already has a `WebSocketRoute server` local; rename the local.

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
git push origin fix-39562
gh pr create --repo microsoft/playwright-java --head username:fix-39562 \
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
Branch naming for issue fixes: `fix-<issue-number>`

## Tips & Tricks
- Project checkouts are in the parent directory (`../`).
- When updating checkboxes, store the issue content into /tmp and edit it there, then update the issue based on the file
- use the "gh" cli to interact with GitHub
