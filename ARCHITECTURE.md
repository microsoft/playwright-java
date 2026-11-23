# Playwright Java architecture

How the Java client is built: the driver, the wire protocol, the object model, the threading model, the generated API and the test harness. Read it before you change `playwright/src/main/java/com/microsoft/playwright/impl` or the `driver` module. The upstream checkout in `.upstream/` is the reference for client logic, protocol and tests. The rules for porting the client are in `playwright/src/main/AGENTS.md`, the rules for porting the tests in `playwright/src/test/AGENTS.md`.

## Contracts

Four things are contracts, and the tests check them; everything else is an implementation choice.

- **The public API**: `com.microsoft.playwright`, `.options` and `.assertions`. Generated from the upstream API docs; the test sources compile against it.
- **The driver and protocol**: the stock Node.js `playwright-core` driver, spoken to over the same JSON protocol as every other language port. Java never talks to a browser.
- **The execution model**: a synchronous, single-threaded API; events are delivered only while the caller is inside a Playwright call.
- **The error surface**: `PlaywrightException`, `TimeoutError`, `TargetClosedError`, and driver error text that the tests check with `contains`.

## Repository layout

| Path | Role |
| --- | --- |
| `driver/` | `Driver`, `DriverJar`, `DriverLogging` plus the resource `driver/package/` = the `playwright-core` package built from source by `scripts/build_driver.sh` (not in git) |
| `driver-bundle/` | resource `driver/<platform>/node` for `mac`, `mac-arm64`, `linux`, `linux-arm64`, `win32_x64` (same script); optional when `PLAYWRIGHT_NODEJS_PATH` is set |
| `playwright/` | the client: generated API packages, hand-written `impl`, the `junit` package, and all tests |
| `.upstream/` | gitignored checkout of `microsoft/playwright` at the commit in `scripts/DRIVER_VERSION`; the driver, the API docs and the protocol spec are all read from it |
| `scripts/` | `DRIVER_VERSION` (an upstream commit), `NODE_VERSION`, `build_driver.sh`, `generate_api.sh` (runs `generate_java_api.mts` and `generate_java_channels.js`), `roll_driver.sh`, release helpers |

Java 8 source level. Gson for all JSON, JSpecify for nullness, opentest4j for assertion errors. Roll and release steps are in `ROLLING.md` and the `playwright-roll` / `playwright-java-release` skills.

## The driver

- `scripts/DRIVER_VERSION` pins a `microsoft/playwright` commit and `scripts/NODE_VERSION` the Node.js version; `scripts/build_driver.sh` builds `playwright-core` from `.upstream/` at that commit. The package and the Node.js binaries ship as jar resources.
- Resolution (`Driver.newInstance`): `playwright.cli.dir` / `PLAYWRIGHT_DRIVER_DIR` set → use that directory as is. Otherwise the class named by `playwright.driver.impl` (default `DriverJar`) extracts the resources into a `playwright-java-*` temp dir (under `playwright.driver.tmpdir`), copes with nested Spring Boot jars and sets executable bits. Node.js comes from `PLAYWRIGHT_NODEJS_PATH` / `playwright.nodejs.path` or the bundle.
- One install per JVM (`Driver.ensureDriverInstalled`): the first `Playwright.create()` runs `node cli.js install` unless `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` or `SELENIUM_REMOTE_URL` is set. `PlaywrightImpl.createImpl(options, true)` forces a fresh driver process for tests.
- Process: `<node> package/cli.js run-driver`; env = JVM env + `CreateOptions.env` + `PW_LANG_NAME=java`, `PW_LANG_NAME_VERSION=<java major>`, `PW_CLI_DISPLAY_VERSION=<jar version>`. stdin/stdout are the pipe, stderr is inherited. `Playwright.close()` closes stdin and waits up to 30 s.
- `CLI.main` passes every subcommand to `cli.js` (`codegen`, `install`, `show-trace`, ...); only `install-driver` is handled in Java.
- `DEBUG=pw:install|pw:channel|pw:api` logs like upstream; `PLAYWRIGHT_JAVA_SRC` enables `StackTraceCollector` so traces and the inspector show Java source.

## Wire protocol

The schema is upstream `packages/protocol/spec/*.yml`; the generated `channels.d.ts` and `validator.ts` are the fastest way to look up a command. Java uses classes generated from the same spec: `scripts/generate_java_channels.js` writes `com.microsoft.playwright.impl.protocol` (one public class per file, upstream `channels.d.ts` names: `<Name>Channel` with `<Result> method(<Params>, Double timeout)`, `<Name><Method>Params`/`Result`, `<Name>Initializer`, `<Name><Event>Event`, structs). `Connection.protocolGson()` maps a `ChannelOwner` to `{guid}` and `byte[]` to base64. `Utils.toProtocol(options, Params.class)` converts an options object; the overload with an owner resolves channel references. The package is excluded from the javadoc jar.

- **Framing**: 4-byte little-endian length + UTF-8 JSON, both directions, over the driver's stdin/stdout.
- **Call**: `{id: "abcd@17", guid, method, params, metadata: {timeout?, title?, internal?, location?}}`. Other objects are referenced as `{guid}`. `timeout` never goes in `params`, only in `metadata.timeout`, and the server enforces it. The validator drops unknown keys. While tracing, the stack goes separately via `LocalUtils.addStackToTracingNoReply`.
- **Reply**: `{id, result}` or `{id, error: {error: {message, name, stack}}, errorDetails?, log?}`. `name` is `TimeoutError`, `TargetClosedError` or `Error`, mapped to `TimeoutError`, `TargetClosedError`, `DriverException`.
- **Event**: `{guid, method, params}`. Structural: `__create__ {type, guid, initializer}` (always before any reference to the guid); `__adopt__ {guid}` (a Page adopts `mainFrame`; a BrowserContext adopts `debugger`, `requestContext`, `tracing`; an APIRequestContext adopts `tracing`); `__dispose__ {reason?}` (`"gc"` = evicted; later calls throw `The object has been collected to prevent unbounded heap growth.`). Client-only: `__waitInfo__` (wait steps in the trace) and `updateSubscription {event, enabled}` for opt-in events.
- **Bootstrap**: the synthetic `Root` (guid `""`) sends `initialize {sdkLanguage: "java", supportsLoneSurrogates: true}`; the driver creates the three `BrowserType`s, `Android`, `Electron`, `LocalUtils`, then `Playwright`, and replies `{playwright: {guid}}`. A `connect()` connection bootstraps the same way and its `Playwright` carries `preLaunchedBrowser`.
- **Values** (`Serialization`, upstream `serializers.ts`): `SerializedArgument {value, handles}`; `SerializedValue` sets one of `n b s v d u bi r e a o h id/ref m se` (`ta` and `fn` are unsupported). Results: whole numbers become `Integer`, others `Double`; objects `LinkedHashMap`; arrays `ArrayList`; `null` and `undefined` both `null`. Unsupported Java types throw `PlaywrightException("Unsupported type of argument: ...")`.
- **Options**: one configured Gson instance: enums lowercased (`_` to `-`), `Optional.empty()` is an explicit null (`"no-override"`), `Map<String,String>` becomes `[{name, value}]`, `Path` a string, handles and locators their refs. A few parameters are massaged after serialization (`recordVideo`, `viewport`/`noDefaultViewport`, `acceptDownloads`, client certificates, HAR options). Binary data is base64.

## Object model

Every server object has one Java `ChannelOwner` with the same guid, holding `connection`, `parent`, `type`, `guid`, `initializer` and its children. `Connection` owns the transport, the guid registry, pending callbacks, the id counter, the tracing counter, the current step title, `env`, `isRemote` and `LocalUtils`, and instantiates objects by `type` through a factory map as `connection.ts` does (unknown type → `PlaywrightException("Unknown type ...")`; `SocksSupport`, `Android*`, `Electron` are ignored).

The tree as the driver creates it:

```
Root > BrowserType x3, LocalUtils, Playwright (> APIRequestContext from newRequest)
BrowserType > Browser > BrowserContext > Page, Tracing, Debugger, APIRequestContext (adopted),
                                         Request, Response, Route, WebSocketRoute, BindingCall,
                                         Dialog, CDPSession, Artifact (downloads, videos)
Page > Frame (main frame adopted), Worker, WebSocket
Frame > JSHandle, ElementHandle
```

- Initializers are read lazily and may only reference guids created earlier.
- Two kinds of impl classes: channel-backed (`PageImpl`, `FrameImpl`, `RequestImpl`, `TracingImpl`, `Stream`, ...) and pure client objects without a guid (`LocatorImpl`, `KeyboardImpl`, `MouseImpl`, `DownloadImpl` wrapping an `ArtifactImpl`, `ConsoleMessageImpl`, `SelectorsImpl`, `RouteHandler`, the `*AssertionsImpl`, ...).
- Same guid, same instance: tests compare `Frame`, `Page`, `Request` and `BrowserContext` with `==` and `assertEquals`.
- State only the client holds: listener maps and `updateSubscription`; `Page.frames`, `Frame.childFrames` and `loadStates` fed by `frameAttached`/`frameDetached`/`navigated`/`loadstate`; the route handler lists (`RouteHandler`, `WebSocketRouteHandler`, `HarRouter`); binding maps; `TimeoutSettings`; close reasons and `isClosed`/`isDetached`; `Selectors` registrations sent with every `newContext`; tracing chunk and HAR bookkeeping; `Browser.contexts` and `BrowserContext.pages`.

## Threading model

Only `PipeTransport` has threads of its own:

```
API thread --serialize--> outgoing queue --> Writer thread --> driver stdin
API thread <--poll 10 ms-- incoming queue <-- Reader thread <-- driver stdout
```

- **API thread**: the caller. It serializes the message, enqueues it and pumps until its reply arrives.
- **Reader thread**: reads frames from stdout into a bounded `ArrayBlockingQueue(1000)`. Never interrupted on close, so the driver can flush and exit. A closed pipe surfaces as `PlaywrightException("Failed to read message from driver, pipe closed.")`.
- **Writer thread**: takes strings from a bounded queue, writes frames, flushes when the queue is empty. `close()` closes stdin and interrupts only this thread.

No timer thread: timeouts are `System.nanoTime` deadlines polled by the loop. The loop is `ChannelOwner.runUntil(code, waitable)`: run `code`, then `while (!waitable.isDone()) connection.processOneMessage()`, which polls for at most 10 ms and dispatches one message (a reply completes its `WaitableResult`; `__create__`/`__adopt__`/`__dispose__` edit the tree; anything else is `handleEvent`).

Rules the harness relies on:

1. Events are delivered only inside a Playwright call. State set by a handler is awaited with `page.waitForCondition(...)`; `future.get()` hangs.
2. Handlers are re-entrant: a route handler that calls `route.fulfill()` runs a nested loop that may complete the outer call and deliver more events first.
3. Wire order is dispatch order, one message per iteration. Listener lists are copied before notification, so a handler may remove itself.
4. An event wait is `WaitableRace(WaitableEvent, close helper → TargetClosedError, WaitableTimeout)`. The event subscribes before the callback runs, so `waitForPopup(() -> click())` never misses.
5. `console`, `dialog`, `dialogClosed`, `request`, `response`, `requestFinished`, `requestFailed` and `fileChooser` are opt-in: `updateSubscription` is sent asynchronously when the first listener is added. A dialog with no listener is dismissed client-side (accepted for `beforeunload`).
6. `sendMessageAsync` (route actions, `updateSubscription`, `BindingCall` replies) does not pump; `sendMessageNoReply` registers no callback.
7. Driver errors are re-thrown on the API thread as a new exception of the same class, so the stack trace points at the caller. `isSafeCloseError` filters the "has been closed" family in close paths.
8. One `Playwright` per worker, not per thread: the harness creates fixtures on a fork-join thread and runs the test body on a separate timeout thread. Strictly sequential use from different threads must work: no `ThreadLocal`, no thread affinity.
9. `connect()` layers a second `Connection` over a `JsonPipe` of the first; the same thread pumps both.
10. A hung driver is caught by a `timeout + 10 s` guard: `TimeoutError("Timeout <n>ms exceeded while waiting for the driver to respond to <method>. The driver is not responding.")`.

## Generated API

- `scripts/generate_api.sh` requires `.upstream/` at the pinned commit (or `PW_SRC_DIR`), runs `scripts/generate_java_api.mts`, which parses `docs/src/api/*.md` with the upstream parser, and then `scripts/generate_java_channels.js` for the protocol types. The markdown docs are the source of truth; every Java-specific name comes from `langs:` / `alias-java:` annotations there. `verify_api.yml` fails CI on any diff.
- Generated: one interface per class in `com.microsoft.playwright` (with nested `XxxOptions`), `assertions/*`, every enum and object type in `options/`, `ScreencastFrame`. Hand-written: `CLI`, `PlaywrightException`, `TimeoutError`, the `package-info.java` files (`@NullMarked`), `Annotate`, `BindingCallback`, `FunctionCallback`, `BrowserChannel`. Generated code calls `PlaywrightImpl.create`, `FormDataImpl`, `RequestOptionsImpl`, the three `*AssertionsImpl` and `AssertionsTimeout` by name.
- Shape rules: optional parameters produce `default` overloads from last to first; a union parameter produces one overload per member (`String | Pattern | Predicate<String>`); waits are `waitForX(Options, Runnable)`, there is no `waitForEvent`; events are `onX`/`offX(Consumer<T>)`; option fields are public and boxed, `T|null` fields are `Optional<T>`, setters are fluent. `Playwright`, `Browser`, `BrowserContext` and `Page` are `AutoCloseable`.
- The `@Nullable` annotations are part of the compiled contract: NullAway consumers break without them.

## Mapping to upstream

Each client class is a line-by-line translation of one file of `packages/playwright-core/src/client/`: the same member order, the upstream names without the leading underscore, the same statements. A ported file starts with `// Port of <file>.ts.` and marks each Java-specific spot with `// java: <why>`. To find the port of a file: `grep -rlE "^// Port of .*\bpage\.ts" playwright/src/main/java`. The rules are in `playwright/src/main/AGENTS.md`.

What does not follow from the file names:

- `network.ts` is one class per upstream class: `RequestImpl`, `ResponseImpl`, `RouteImpl`, `WebSocketImpl`, `WebSocketRouteImpl`, `RawHeaders`, `RouteHandler`, `WebSocketRouteHandler`. `fetch.ts` is `APIRequestImpl`, `APIRequestContextImpl`, `APIResponseImpl`.
- `connect.ts` is `Connect` (static `connectToBrowser`, `connectToEndpoint`); `JsonPipe` is also the transport (`JsonPipeTransport` upstream). `PlaywrightImpl` has the Java-only driver start-up at the bottom.
- `packages/protocol/src/serializers.ts` is the first part of `Serialization` (`parseSerializedValue`, `serializeValue`, ...); its Gson configuration and adapters below are Java-only.
- The helpers of `packages/isomorphic/` without client state are in the subpackage `impl.isomorphic` (`Headers`, `StringUtils`, `MimeType`, `Time`, `LocatorUtils`). `manualPromise.ts` is `LongStandingScope` and `urlMatch.ts` is `UrlMatcher`, both in `impl`: the first is built on `ChannelOwner` and `Waitable`, the second has the driver convert globs (`LocalUtils.globToRegex`).
- `packages/playwright/src/matchers/*.ts` is `AssertionsBase`, the `*AssertionsImpl` and `AssertionsTimeout`. They are not a line-by-line port.
- `android.ts` and `electron.ts` are not ported.
- `Utils` holds only helpers with no upstream counterpart (Gson `convertType`/`clone`, `toProtocol`, `writeToFile`, regex flag conversion).

Deliberate divergences that the tests encode: callback-style `waitForX`; `waitForCondition` on page and context; no `page.goto:` prefix in error messages (tests that need it are `@Fixme`); locator actions that need an element resolve a handle, act and dispose it; `selectors()` is a per-`Playwright` client registry; close methods swallow safe close errors; enums go through Gson adapters.

## Timeouts and errors

- `TimeoutSettings`: 30 000 ms for actions and navigations, 180 000 ms for launch and connect; a page chains to its context. Order: explicit option, `setDefaultNavigationTimeout` (navigation only), `setDefaultTimeout`, parent, constant. `APIRequestContext` has its own instance. Timeout `0` waits forever.
- Server-enforced timeouts travel in `metadata.timeout` and fail with the server's message (`Timeout 30000ms exceeded.` plus the call log). Client-side waits go through `Waiter` as upstream (`createForEvent`, `rejectOnEvent`, `rejectOnTimeout`, `waitForEvent`, `dispose`); the timeout message names the event, `Timeout <n>ms exceeded while waiting for event "<event>"`. `connect()` races the Playwright initialization against its timeout. Assertions default to 5 000 ms (`setDefaultAssertionTimeout`).
- Waitables: `WaitableResult` (a reply), `WaitableEvent` (a listener hit), `WaitablePredicate` (polled), `WaitableTimeout`, `WaitableNever`, `WaitableRace`; plus the page close helper (`TargetClosedError`), the frame detach helper (`Navigating frame was detached!`) and the frame navigation and load-state helpers.
- Exceptions: `PlaywrightException` (base) and `TimeoutError` are public; `impl.TargetClosedError` is public with the default message `Target page, context or browser has been closed`; `DriverException` wraps other server errors; `ServerErrorWithDetails` carries `errorDetails` for `expect`; assertions throw `AssertionFailedError`.
- A driver error message is `SerializedError.Error.toString()` (`Error {\n  message='...'\n  name='...'\n  stack='...'\n}`) followed by `\nCall log:\n- ...`. Tests `contains`-check the upstream text inside `message=`. Quirk: log lines carry a doubled `-   - ` prefix.

## Assertions

`assertThat(locator | page | response)`; `.not()` flips `isNot`. Locator and page matchers send one `Frame.expect` command with `FrameExpectOptions {selector, expression, expressionArg, expectedText[], expectedNumber, expectedValue, useInnerText, isNot, timeout}` under the title `Assert "hasText"`, and the server polls. The expression ids and the `ExpectedTextValue` flags (`normalizeWhiteSpace`, `ignoreCase`, `matchSubstring`) are copied from upstream `matchers.ts` (`to.have.text`, `to.be.visible`, `to.have.url`, `to.match.aria`, ...). A failure becomes `AssertionFailedError` with `<prefix>` (`Locator expected to have text`, or `expected not to`), then `\nExpected: ...\nReceived: ...`, then `\nCall log:` when present, with expected and actual passed as `ValueWrapper`s; the ported `expect-matcher-result` tests snapshot these strings. `APIResponseAssertions.isOK()` is client-side: `Response status expected to be within [200..299] range, was <status>` plus the call log from `fetchLog` and `\nResponse text:` for textual MIME types.

## Client-side subsystems

Each is a thin layer over a server feature, with sequencing the tests observe:

- **Routing**: `route()` prepends a `RouteHandler` to the list of the page or context and sends `setNetworkInterceptionPatterns`; on a `route` event the page router runs before the context router, newest handler first, honouring `times` and `fallback()`; when nothing handles it, `route.resume()`. Globs are compiled by `LocalUtils.globToRegex`; `unroute` compares `UrlMatcher`s.
- **HAR**: `routeFromHAR` replays through `LocalUtils.harOpen`/`harLookup` or records through `TracingImpl` (`harStart`/`harExport`).
- **WebSocket routing**: the `WebSocketRouteHandler` list plus `setWebSocketInterceptionPatterns`; `WebSocketRouteImpl` models both ends and sends `ensureOpened` when the handler did not connect to the server.
- **Bindings**: `exposeBinding {name, needsHandle}`; duplicates throw `Function "x" has been already registered`; `bindingCall` events are answered with `resolve`/`reject` asynchronously.
- **Downloads, videos, uploads**: `Artifact` commands (`pathAfterFinished`, `saveAs`, `stream`, ...); over `connect()`, `path()` throws and content streams through `Stream`/`WritableStream`; uploads send `localPaths` locally and `createTempFiles` plus streams remotely; payloads are size-checked.
- **Dialogs**: may arrive before their page exists; auto-dismissed when nobody listens.
- **Tracing**: `tracingStart` (option names rewritten), `tracingStartChunk`, `LocalUtils.tracingStarted` for a `stacksId`; `Connection.tracingCount` turns on stack shipping; `tracingStopChunk {mode}` then `LocalUtils.zip` locally or artifact save plus append remotely.
- **Selectors**: `SelectorsImpl` keeps engines and `testIdAttributeName` per `Playwright` and sends them with every context; locator builders only concatenate `internal:` selector strings.
- **Clock, CDP, screencast, storage, credentials, debugger, WebMCP**: thin command wrappers; the screencast acknowledges every frame with `screencastFrameAck`.

## Test harness

- `com.microsoft.playwright.fixtures` is a JUnit 5 port of upstream `browserTest.ts`: `@PlaywrightTest` runs once per browser in `BROWSER`, fixtures resolve by parameter type with `@Fixture(scope = TEST | WORKER)` overrides, and `TestServer` is a hand-written HTTP/1.1 and WebSocket server. The port rules are in `playwright/src/test/AGENTS.md`.
- Workers (`WorkerPool`) each own one `Playwright`; a worker serves one test at a time but not one thread (`separate_thread` timeout mode).
- `@Fixme("java: ..." | "driver: ..." | "harness: ...")` is a strict expected failure: a test that starts passing fails with `Test marked @Fixme passed, remove the annotation`. Most marks are for the missing `page.goto:` prefix and the JavaScript-style `Locator.toString()`.
- `upstream_base.txt` pins the ported upstream commit; `upstream_not_ported.txt` lists the exclusions, concurrent-call tests among them.

Internals the tests reach into:

| Symbol | Used by |
| --- | --- |
| `impl.PlaywrightImpl.createImpl(CreateOptions, boolean forceNewDriver)` | tests that need a fresh driver |
| `((PlaywrightImpl) pw).localUtils().globToRegex(glob, baseURL, webSocketUrl)` | interception tests |
| `impl.TargetClosedError` and its default message | 13 files |
| a `Process` field `driverProcess` on the class `Playwright.create()` returns | `TestUnresponsiveDriver` |
| a superclass `ChannelOwner` with a field `guid` | a connect test |
| `impl.driver.Driver.ensureDriverInstalled`, `createAndInstall`, `driverDir()`, `createProcessBuilder()`; `impl.driver.jar.DriverJar` with a no-arg constructor and `installDriverTo(Path)`; the properties `playwright.cli.dir`, `playwright.driver.tmpdir`, `playwright.nodejs.path`, `playwright.driver.impl`; env `PLAYWRIGHT_DOWNLOAD_HOST`, `PLAYWRIGHT_BROWSERS_PATH`, `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD`; the message `Failed to create driver` | driver and server tests |
| `impl.junit.BrowserContextOptionsExtension.getContextOptions`, `PlaywrightExtension.getOrCreatePlaywright` | junit tests |
| `StackTraceCollector`: exactly one `src/<sha1>.java` entry per trace; `PLAYWRIGHT_JAVA_SRC` read from `CreateOptions.env` before the process env | tracing tests |
| public option fields (`LaunchOptions.headless`/`channel`, `CreateOptions.env`, `ConnectOptions.headers`, `BoundingBox`, `ViewportSize`, `Cookie.url`) | harness |

Exact strings: `Locator@<selector>`, `JSHandle@<preview>`, `selectors.register: Selector engine name may only contain [a-zA-Z0-9_] characters`, `Connecting over CDP is only supported in Chromium and WebKit.`, `Malformed endpoint. Did you use launchServer method?`, `Timeout 100ms exceeded while waiting for the driver to respond to waitForFunction`. An exception thrown by a handler propagates out of the call that dispatched it.

Quirks that no test depends on: the doubled call-log prefix, `metadata.wallTime` (ignored by the server), `__abort__` never sent, `ta`/`fn` values unsupported.
