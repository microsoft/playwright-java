# Upstream rewrite: evaluate promises that are awaited later

A prompt for an agent working in the Playwright repository (`../playwright`, specs under
`tests/page` and `tests/library`).

The Java port of these specs (microsoft/playwright-java, `playwright/src/test/java/com/microsoft/playwright/tests`)
runs on a synchronous API: `page.evaluate()` returns only when the promise of the page function
has settled, and a Playwright instance is used from one thread only. A test that starts an
`evaluate` whose promise stays pending, makes other Playwright calls, and awaits that promise
later can not be ported line by line. The tests below are listed in
`playwright/src/test/upstream_not_ported.txt` with the reason
`upstream: evaluate promise awaited later` until they are rewritten upstream as described here;
the rewritten tests are then ported as they are. Tests whose point is the pending call itself
(BR-005, BT-004, PA-042, CD-048, EV-093) are not rewritten and stay listed as `java: concurrent`.

## The change

Rewrite each test so that no `evaluate` promise stays pending across other Playwright calls,
without changing what the test checks. Only tests where the pending evaluate is a vehicle, not
the thing under test, qualify: a test whose point is that a pending call gets rejected when its
page, frame or browser goes away (such as `should reject all promises when page is closed`)
stays as it is.

The usual shapes:

- The page-side work is started by an `evaluate` whose function keeps the promise on the
  window object and returns nothing, `window.__promise = new Promise(...)`, or by
  `evaluateHandle(() => new Promise(...))`, which keeps a `JSHandle` to it. Where the test
  awaited the pending promise, it awaits a second call, `await page.evaluate(() =>
  window.__promise)` or `await handle.jsonValue()`.
- A call that only has to span other calls is a `page.waitForEvent(...)` whose trigger comes
  after those calls. The synchronous ports express that with the callback form of the wait,
  `page.waitForConsoleMessage(() -> { ...other calls...; page.evaluate("console.log(1)"); })`.

Keep the title and the `XX-nnn` identifier of each test as they are, the identifiers
cross-reference the registry of the ports. Keep every assertion that still holds, adjust the
ones that describe the pending call, and say in a comment when the rewritten test checks less
than the original did. The tests must keep passing in Node.js in the three browsers. Do not
change the other tests in these files.

## Tests to rewrite

### TR-041 `tests/library/tracing.spec.ts`, "should not emit after w/o before"

The test checks that a call whose `before` event is in one chunk does not get its `after`
event into the next chunk. The spanning call is
`page.evaluate(() => { console.log('started'); return new Promise(f => window.callback = f); })`,
kept pending across `tracing.stopChunk()` and `tracing.startChunk()` and resolved with
`page.evaluateHandle(() => window.callback())` in the second chunk.

Rewrite: the spanning call becomes `const consolePromise = page.waitForEvent('console')`,
started in the first chunk after `tracing.start()`, followed by a completed call in that chunk,
`await page.evaluate(() => 1)`. After `stopChunk` and `startChunk`, the second chunk runs
`await page.evaluate(() => console.log('started'))`, which fires the event, and then `await
consolePromise`. The expected events of trace1 become the `before` of the wait (with its title
`Wait for event "console"`) and the `before` and `after` of the evaluate, in that order; those of
trace2 the `before` and `after` of the second evaluate only, with the same call id, greater than
the call ids of the first chunk. The `after` of the wait must not appear in trace2, which is what
the original checked for the evaluate.

## Already ported in Java with this shape

These tests were ported with the same change made on the Java side; rewriting them upstream the
same way would make the ports line by line again, but nothing waits on it: RO-012 and RO-013
(`tests/page/page-route.spec.ts`, the status of the sync XHR and of the fetch kept on window),
RO-100 (`tests/page/page-request-fulfill.spec.ts`, the fetch started without `return`), DD-019
(`tests/page/page-drag.spec.ts`, the drop promise kept on window), CA-011
(`tests/library/capabilities.spec.ts`, `showDirectoryPicker()` started without awaiting),
EV-063 (`tests/page/page-evaluate.spec.ts`, the reload scheduled from the page) and NA-078
(`tests/page/page-history.spec.ts`, the second `pushState` scheduled from the page).
