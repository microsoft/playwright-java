# How to port the JavaScript client to Java

These rules say how to write `playwright/src/main/java/com/microsoft/playwright/impl` from
`packages/playwright-core/src/client` in `microsoft/playwright`. Use them at each roll and for
every fix, for every new or changed line of the client. The upstream code is in `.upstream/`,
the checkout at the commit in `scripts/DRIVER_VERSION`. The steps of a roll are in the
`playwright-roll` skill. `ARCHITECTURE.md` at the repo root describes the driver, the protocol,
the object model and the threading model that this code runs on.

The port is line by line. A reviewer reads the `.ts` file and the `.java` file side by side.
Each JavaScript statement maps to one Java statement, or to one of the fixed patterns in this
document. The Java does what the JavaScript does, in the same order, with the same names, the
same conditions and the same error messages. Port from the upstream source in front of you,
never from memory of what the method does.

Do not improve the code while you port it: no extra check, no early return, no helper, no
reordering and no renaming that upstream does not have. When the upstream code looks wrong,
port it as it is and report it.

The existing code is the reference for every pattern below. Before you port a hunk, read the
Java code around the place it goes to, and write the new lines the same way.

## 1. Files and names

- The first line after the license header names the upstream file: `// Port of page.ts.`
  To find the port of an upstream file: `grep -rlE "^// Port of .*\bpage\.ts" playwright/src/main/java`.
  One upstream file can have several Java files, one per class (`network.ts` is `RequestImpl`,
  `ResponseImpl`, `RouteImpl`, `WebSocketImpl`, ...). A new upstream file is a new Java file
  with that header.
- The assertions are the port of `packages/playwright/src/matchers`: `matchers.ts` is
  `LocatorAssertionsImpl`, `PageAssertionsImpl` and `APIResponseAssertionsImpl`; `toBeTruthy.ts`,
  `toEqual.ts`, `toMatchText.ts`, `toMatchAriaSnapshot.ts` and `matcherHint.ts` are the classes
  of the same name; `AssertionsBase` is the call of a matcher from `expect.ts`. The `utils` of a
  matcher are `MatcherUtils`: jest is not ported, the values and the diff are printed in the same
  layout without matching jest in every case. A failure message has the upstream layout with the
  Java call: `assertThat(locator).not().hasText(expected) failed` for
  `expect(locator).not.toHaveText(expected) failed`, and the title `Assert "hasText"`.
- `isomorphic/cssParser.ts` and `cssTokenizer.ts` are not ported: `SelectorParser` keeps the
  source of a css part and does not reject an invalid css selector.
- A class that implements a public interface is `<Name>Impl` (`Page` is `PageImpl`). A
  helper class keeps the upstream name (`Waiter`, `HarRouter`, `TimeoutSettings`).
- The members keep the upstream order. A new upstream member goes where it is in the `.ts` file,
  between the same neighbors.
- The names are the upstream names without the leading underscore: `_closeReason` is
  `closeReason`, `_onRoute()` is `onRoute()`. Constants of `events.ts` are upper case:
  `Events.Page.Close` is `Events.Page.CLOSE`.
- The code targets Java 8. No `var`, no records, no `List.of()`, no text blocks.
- Do not edit a generated file: the public interfaces and options, and everything in
  `impl/protocol/`. `scripts/generate_api.sh` writes them.

## 2. Statements

| JavaScript | Java |
|---|---|
| `await x` | the plain call. The Java client is synchronous. |
| `options = {}` as the parameter default | `if (options == null) { options = new FooOptions(); }` as the first statement |
| `options.foo ?? 0`, `options.foo === undefined ? a : b` | `options.foo == null ? ... : ...` |
| `if (options.flag)` on an optional boolean | `if (options.flag != null && options.flag)` |
| `throw new Error('...')` | `throw new PlaywrightException("...")` with the same message |
| `new TargetClosedError(...)`, `TimeoutError` | the classes of the same name |
| a template string | string concatenation, the same text |
| `Promise.all(list.map(x => x.foo()))` | a loop that calls `foo()` on each one, in the list order |
| an overload on the argument type (`string \| Function \| RegExp`) | one Java overload per type, each calls a shared private method |

## 3. Protocol calls

`impl/protocol/` has the generated types of `packages/protocol/spec/*.yml`: one
`<Class>Channel` per object, and for each method `<Class><Method>Params` and
`<Class><Method>Result` (`PageReloadParams`, `CDPSessionSendResult`); for each event
`<Class><Event>Event` (`PageDownloadEvent`).

- `this._channel.foo(params, timeout)` is `channel.foo(params, timeout)`. Each class has
  `final <Class>Channel channel = new <Class>Channel(this);`.
- An object literal is the params class, one assignment per field, in the upstream order:

  ```ts
  await this._channel.send({ method, params }, { timeout: options.timeout ?? 0 });
  ```
  ```java
  CDPSessionSendParams args = new CDPSessionSendParams();
  args.params = params;
  args.method = method;
  ```
- `{ ...options, waitUntil }` is `toProtocol(options, PageReloadParams.class)`, then one
  assignment per extra field.
- `kNoTimeout` is `NO_TIMEOUT`. `this._timeoutSettings.timeout(options)` is
  `timeoutSettings.timeout(options.timeout)`; the same for `navigationTimeout`.
- `Foo.from(result.foo)` and `Foo.fromNullable(result.foo)` are `result.foo`: the generated
  result already holds the impl object. `from` is not ported.
- A channel object in the params (`{ frame: frame._channel }`) is the impl object itself,
  assigned to the field.
- `this._wrapApiCall(async () => { ... }, { internal: true })` is
  `wrapApiCall(() -> { ... }, title, internal)`.

## 4. Events

- `this._channel.on('foo', ...)` in the upstream constructor is a `case "foo":` in the
  `handleEvent(String event, JsonObject params)` override of the class. The cases keep the
  order of the constructor. Parse the params with
  `connection.protocolGson().fromJson(params, PageFooEvent.class)`, then write the body of
  the upstream listener.
- `this.emit(Events.Page.Close, this)` is `emit(Events.Page.CLOSE, this)`.
- `_setEventToSubscriptionMapping(new Map([...]))` is `setEventToSubscriptionMapping(mapping)`
  with the same entries.
- Each event of the public interface has `onFoo(handler)` and `offFoo(handler)`, which call
  `on(Events.Page.FOO, handler)` and `off(...)`. They are at the end of the class.
- `waitForEvent(event, optionsOrPredicate)` is one typed `waitForFoo(options, callback)` per
  event of the public interface. It calls the private `waitForEvent(event, predicate, timeout,
  code)` of the class, which is the port of the upstream method on `Waiter`.

## 5. Promises

The Java client has one thread and no promises. A call that waits pumps the connection.

- A promise that upstream awaits at once is a plain call.
- A promise that upstream keeps and awaits later is a `Waitable`: `WaitableResult` for a
  `ManualPromise`, `WaitableRace` for `Promise.race`, `sendMessageAsync(...)` for a channel
  call that is not awaited. `runUntil(code, waitable)` runs the code and pumps until the
  waitable is done.
- Module-level state (a counter, a cache) is a plain field of the `Connection` or of the
  object. No `static` state, no atomics, no `synchronized`, no locks: each connection is used
  from one thread.

## 6. A new class

For a new `ChannelOwner` upstream (`Debugger`): write `<Name>Impl extends ChannelOwner
implements <Name>` with the `channel` field and the constructor
`(ChannelOwner parent, String type, String guid, JsonObject initializer)`, and add it to the
`factories` map of `Connection.java`, where the other types are.

## 7. Deviations

When the Java cannot be the same as the JavaScript, add a `// java: <reason>` comment at that
line. Say what differs and why, in one or two lines. Use it for:

- code with no counterpart upstream: `// java: helpers with no counterpart upstream.`
- an upstream member that is left out: `// java: from is not ported, generated results already hold the impl object.`
- a different type or shape: `// java: the parsed JSON is a JsonElement.`

Do not mark a plain rename, a plain `await` or one of the patterns above. Write no other
comments: upstream comments are not copied.

## 8. What is not ported

- A member with `* langs: js` (or a list without `java`) in `docs/src/api/*.md`. Check the
  docs, not the name.
- `android.ts`, `electron.ts`, `channels.d.ts`, `types.ts`.
- `AbortSignal` and the `signal` options.
- `[Symbol.asyncDispose]`: a public disposable is `AutoCloseable.close()`.
- `assertMaxArguments` and `isFunction`: a Java expression is always a `String`.
- JavaScript-only plumbing: the `Proxy` objects and the protocol validators.
