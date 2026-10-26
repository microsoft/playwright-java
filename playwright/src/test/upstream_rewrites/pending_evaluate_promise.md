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
the rewritten tests will then be ported as they are.

## The change

Rewrite each test so that no `evaluate` promise stays pending across other Playwright calls:

1. Start the page-side work with an `evaluate` whose function keeps the promise on the window
   object and returns nothing, `window.__promise = new Promise(...)`, or keep a `JSHandle` to the
   promise with `evaluateHandle(() => new Promise(...))`.
2. Make the other calls as before.
3. Where the test awaited the pending promise, await it with a second call:
   `await page.evaluate(() => window.__promise)`, or `await handle.jsonValue()` for a handle.
   An error that the original expected from the pending promise (a closed page, a detached
   frame) comes from this second call.

Keep the title and the `XX-nnn` identifier of each test as they are, the identifiers
cross-reference the registry of the ports. Keep every assertion that still holds, adjust the
ones that describe the pending call, and say in a comment when the rewritten test checks less
than the original did. The tests must keep passing in Node.js in the three browsers. Do not
change the other tests in these files.

## Tests to rewrite

### TR-041 `tests/library/tracing.spec.ts`, "should not emit after w/o before"

`page.evaluate(() => { console.log('started'); return new Promise(f => window.callback = f); })`
stays pending across `tracing.stopChunk()` and `tracing.startChunk()`, so that the `after`
event of the call lands in the second chunk without its `before`.

Rewrite: the function keeps the promise, `window.promise = new Promise(f => window.callback = f)`,
and returns; the `page.waitForEvent('console')` goes into a `Promise.all` with that evaluate.
After `startChunk`, resolve it with `page.evaluateHandle(() => window.callback())` as now, and
await it with `page.evaluate(() => window.promise)`. The events of trace1 become the `before`
and `after` of the evaluate and of the console wait, those of trace2 the `before` and `after` of
the two calls; the test then checks that every `after` of a chunk has its `before` in the same
chunk and that the call ids of the second chunk are greater than those of the first.

### EV-093 `tests/page/page-evaluate.spec.ts`, "should throw when frame is detached"

`frame.evaluate(() => new Promise(() => {}))` stays pending while the frame is detached, and
rejects with `Frame was detached` or `Execution context was destroyed`.

Rewrite: keep the never-resolving promise on the window of the frame, detach the frame, then
`await frame.evaluate(() => window.promise)`, which rejects with one of the same messages
because the frame is gone.

### BR-005 `tests/library/browser.spec.ts`, "should dispatch page.on(close) upon browser.close and reject evaluate"
### BT-004 `tests/library/browsertype-launch.spec.ts`, "should reject all promises when browser is closed"
### PA-042 `tests/library/page-close.spec.ts`, "should reject all promises when page is closed"

`page.evaluate(() => new Promise(() => {}))` stays pending while the browser or the page closes,
and rejects with the target closed message.

Rewrite: keep the promise on window, close as now (`await page.close()` in PA-042 instead of
the `Promise.all`), then `await page.evaluate(() => window.promise)` and expect the same error.

### CD-048 `tests/library/chromium/session.spec.ts`, "should reject protocol calls when page closes"

`session.send('Runtime.evaluate', { expression: 'new Promise(() => {})', awaitPromise: true })`
stays pending while the page closes.

Rewrite: send the expression that keeps the promise on window without `awaitPromise`, close
the page, then send the `Runtime.evaluate` that awaits `window.promise`, which rejects with the
target closed message like the second `send` of the test already does.

## Already ported in Java with this shape

These tests were ported with the same change made on the Java side; rewriting them upstream the
same way would make the ports line by line again, but nothing waits on it: RO-012 and RO-013
(`tests/page/page-route.spec.ts`, the status of the sync XHR and of the fetch kept on window),
RO-100 (`tests/page/page-request-fulfill.spec.ts`, the fetch started without `return`), DD-019
(`tests/page/page-drag.spec.ts`, the drop promise kept on window), CA-011
(`tests/library/capabilities.spec.ts`, `showDirectoryPicker()` started without awaiting),
EV-063 (`tests/page/page-evaluate.spec.ts`, the reload scheduled from the page) and NA-078
(`tests/page/page-history.spec.ts`, the second `pushState` scheduled from the page).
