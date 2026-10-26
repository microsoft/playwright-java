/*
 * Copyright 2017 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Worker;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/workers.spec.ts. */
public class TestWorkers {
  @PlaywrightTest
  @DisplayName("WO-001 Page.workers @smoke")
  void pageWorkers(Page page, TestServer server, Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    page.waitForWorker(() -> page.navigate(server.PREFIX + "/worker/worker.html"));
    Worker worker = page.workers().get(0);
    assertTrue(worker.url().contains("worker.js"));

    assertEquals("worker function result", worker.evaluate("() => self['workerFunction']()"));

    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  @DisplayName("WO-002 should emit created and destroyed events")
  @Fixme("java: the error message has no 'jsHandle.getProperty' prefix")
  void shouldEmitCreatedAndDestroyedEvents(Page page) {
    AtomicReference<JSHandle> workerObjRef = new AtomicReference<>();
    Worker worker = page.waitForWorker(() -> {
      workerObjRef.set(page.evaluateHandle("() => new Worker(URL.createObjectURL(new Blob(['1'], { type: 'application/javascript' })))"));
    });
    JSHandle workerObj = workerObjRef.get();
    JSHandle workerThisObj = worker.evaluateHandle("this");
    Worker workerDestroyed = worker.waitForClose(() -> {
      page.evaluate("workerObj => workerObj.terminate()", workerObj);
    });
    assertEquals(worker, workerDestroyed);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> workerThisObj.getProperty("self"));
    assertTrue(error.getMessage().contains("jsHandle.getProperty"));
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
  }

  // WO-003 should reject in-flight evaluate and emit close when page closes: see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("WO-004 should report console logs")
  void shouldReportConsoleLogs(Page page) {
    ConsoleMessage message = page.waitForConsoleMessage(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], { type: 'application/javascript' })))");
    });
    assertEquals("1", message.text());
    // Firefox's juggler had an issue that reported worker blob urls as frame urls.
    assertFalse(page.url().contains("blob"));
  }

  @PlaywrightTest
  @DisplayName("WO-005 should have timestamp on worker console messages")
  void shouldHaveTimestampOnWorkerConsoleMessages(Page page) {
    assumeFalse("webkit-wsl".equals(Env.channel()), "there is a time difference between WSL VM and host machine");

    double before = System.currentTimeMillis() - 1;  // Account for the rounding of fractional timestamps.
    ConsoleMessage message = page.waitForConsoleMessage(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(\"ts\")'], { type: 'application/javascript' })))");
    });
    double after = System.currentTimeMillis() + 1;  // Account for the rounding of fractional timestamps.
    assertEquals("ts", message.text());
    assertTrue(message.timestamp() >= before);
    assertTrue(message.timestamp() <= after);
  }

  @PlaywrightTest
  @DisplayName("WO-006 should not report console logs from workers twice")
  void shouldNotReportConsoleLogsFromWorkersTwice(Page page) {
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg.text()));
    // Java: the waits go first and the evaluate is the action of the innermost wait.
    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().equals("1")), () -> {
      page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().equals("2")), () -> {
        page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1); console.log(2);'], { type: 'application/javascript' })))");
      });
    });
    assertEquals(Arrays.asList("1", "2"), messages);
    // Firefox's juggler had an issue that reported worker blob urls as frame urls.
    assertFalse(page.url().contains("blob"));
  }

  @PlaywrightTest
  @DisplayName("WO-007 should have JSHandles for console logs")
  void shouldHaveJSHandlesForConsoleLogs(Page page, BrowserName browserName) throws Exception {
    CompletableFuture<ConsoleMessage> logPromise = new CompletableFuture<>();
    page.onConsoleMessage(logPromise::complete);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1,2,3,this)'], { type: 'application/javascript' })))");
    // Java: events are dispatched only while a Playwright call waits, so awaiting a promise that a page event settles is waitForCondition.
    page.waitForCondition(logPromise::isDone);
    ConsoleMessage log = logPromise.get();
    if (browserName != BrowserName.FIREFOX) {
      assertEquals("1 2 3 DedicatedWorkerGlobalScope", log.text());
    } else {
      assertEquals("1 2 3 JSHandle@object", log.text());
    }
    assertEquals(4, log.args().size());
    assertEquals("null", log.args().get(3).getProperty("origin").jsonValue());
  }

  @PlaywrightTest
  @DisplayName("WO-008 should evaluate")
  void shouldEvaluate(Page page, Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    Worker worker = page.waitForWorker(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], { type: 'application/javascript' })))");
    });
    assertEquals(2, worker.evaluate("1+1"));
  }

  @PlaywrightTest
  @DisplayName("WO-009 should report console event on the worker")
  void shouldReportConsoleEventOnTheWorker(Page page, Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    Worker worker = page.waitForWorker(() -> {
      page.evaluate("() => {\n" +
        "  window.worker = new Worker(URL.createObjectURL(new Blob(['42'], { type: 'application/javascript' })));\n" +
        "}");
    });
    AtomicReference<ConsoleMessage> message2 = new AtomicReference<>();
    AtomicReference<ConsoleMessage> message3 = new AtomicReference<>();
    ConsoleMessage message1 = worker.waitForConsoleMessage(() -> {
      message2.set(page.waitForConsoleMessage(() -> {
        message3.set(page.context().waitForConsoleMessage(() -> {
          worker.evaluate("() => {\n" +
            "  console.log('hello from worker');\n" +
            "}");
        }));
      }));
    });
    assertEquals("hello from worker", message1.text());
    assertEquals(message2.get(), message1);
    assertEquals(message3.get(), message1);
  }

  @PlaywrightTest
  @DisplayName("WO-010 should report console event on the worker when not listening on page or context")
  @Fixme("java: worker.waitForConsoleMessage times out with 'Timeout 30000ms exceeded' when nothing listens for console on page or context")
  void shouldReportConsoleEventOnTheWorkerWhenNotListeningOnPageOrContext(Page page, Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    Worker worker = page.waitForWorker(() -> {
      page.evaluate("() => {\n" +
        "  window.worker = new Worker(URL.createObjectURL(new Blob(['42'], { type: 'application/javascript' })));\n" +
        "}");
    });
    ConsoleMessage message = worker.waitForConsoleMessage(() -> {
      worker.evaluate("() => {\n" +
        "  console.log('hello from worker');\n" +
        "}");
    });
    assertEquals("hello from worker", message.text());
  }

  @PlaywrightTest
  @DisplayName("WO-011 should report errors")
  void shouldReportErrors(Page page) throws Exception {
    CompletableFuture<String> errorPromise = new CompletableFuture<>();
    page.onPageError(errorPromise::complete);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob([`\n" +
      "    setTimeout(() => {\n" +
      "      // Do a console.log just to check that we do not confuse it with an error.\n" +
      "      console.log('hey');\n" +
      "      throw new Error('this is my error');\n" +
      "    })\n" +
      "  `], { type: 'application/javascript' })))");
    // Java: events are dispatched only while a Playwright call waits, so awaiting a promise that a page event settles is waitForCondition.
    page.waitForCondition(errorPromise::isDone);
    String errorLog = errorPromise.get();
    assertTrue(errorLog.contains("this is my error"));
  }

  @PlaywrightTest
  @DisplayName("WO-012 should clear upon navigation")
  void shouldClearUponNavigation(TestServer server, Page page) {
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], { type: 'application/javascript' })))");
    });
    assertEquals(1, page.workers().size());
    AtomicBoolean destroyed = new AtomicBoolean(false);
    worker.onClose(new Consumer<Worker>() {
      @Override
      public void accept(Worker w) {
        worker.offClose(this);
        destroyed.set(true);
      }
    });
    page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(true, destroyed.get());
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  @DisplayName("WO-013 should clear upon cross-process navigation")
  void shouldClearUponCrossProcessNavigation(TestServer server, Page page) {
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], { type: 'application/javascript' })))");
    });
    assertEquals(1, page.workers().size());
    AtomicBoolean destroyed = new AtomicBoolean(false);
    worker.onClose(new Consumer<Worker>() {
      @Override
      public void accept(Worker w) {
        worker.offClose(this);
        destroyed.set(true);
      }
    });
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(true, destroyed.get());
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  @DisplayName("WO-014 should attribute network activity for worker inside iframe to the iframe")
  void shouldAttributeNetworkActivityForWorkerInsideIframeToTheIframe(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.FIREFOX && browserMajorVersion(browser) < 114, "https://github.com/microsoft/playwright/issues/21760");
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 149, "needs TargetInfo.parentFrameId for workers");

    page.navigate(server.PREFIX + "/empty.html");
    AtomicReference<Frame> frameRef = new AtomicReference<>();
    Worker worker = page.waitForWorker(() -> {
      frameRef.set(attachFrame(page, "frame1", server.PREFIX + "/worker/worker.html"));
    });
    Frame frame = frameRef.get();
    String url = server.PREFIX + "/one-style.css";
    Request request = page.waitForRequest(url, () -> {
      worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
    });
    assertEquals(url, request.url());
    assertEquals(frame, request.frame());
  }

  @PlaywrightTest
  @DisplayName("WO-015 should report network activity")
  void shouldReportNetworkActivity(Page page, TestServer server, BrowserName browserName, Browser browser) throws Exception {
    assumeFalse(browserName == BrowserName.FIREFOX && browserMajorVersion(browser) < 114, "https://github.com/microsoft/playwright/issues/21760");
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    Worker worker = page.waitForWorker(() -> page.navigate(server.PREFIX + "/worker/worker.html"));
    String url = server.PREFIX + "/one-style.css";
    AtomicReference<Response> responseRef = new AtomicReference<>();
    // Java: the request and response waits are created before the evaluate, so it runs in the innermost wait.
    Request request = page.waitForRequest(url, () -> {
      responseRef.set(page.waitForResponse(url, () -> {
        worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
      }));
    });
    Response response = responseRef.get();
    assertEquals(url, request.url());
    assertEquals(request, response.request());
    assertEquals(true, response.ok());
    assertEquals(new String(Files.readAllBytes(asset("one-style.css")), StandardCharsets.UTF_8), response.text());
  }

  @PlaywrightTest
  @DisplayName("WO-016 should report network activity on worker creation")
  void shouldReportNetworkActivityOnWorkerCreation(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.FIREFOX && browserMajorVersion(browser) < 114, "https://github.com/microsoft/playwright/issues/21760");
    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/one-style.css";
    AtomicReference<Response> responseRef = new AtomicReference<>();
    // Java: the request and response waits are created before the evaluate, so it runs in the innermost wait.
    Request request = page.waitForRequest(url, () -> {
      responseRef.set(page.waitForResponse(url, () -> {
        page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
          "    fetch(\"${url}\").then(response => response.text()).then(console.log);\n" +
          "  `], { type: 'application/javascript' })))", url);
      }));
    });
    Response response = responseRef.get();
    assertEquals(url, request.url());
    assertEquals(request, response.request());
    assertEquals(true, response.ok());
  }

  @PlaywrightTest
  @DisplayName("WO-017 should report worker script as network request")
  void shouldReportWorkerScriptAsNetworkRequest(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    AtomicReference<Request> request2Ref = new AtomicReference<>();
    Request request1 = page.waitForRequest(r -> r.url().contains("worker.js"), () -> {
      request2Ref.set(page.waitForRequestFinished(new Page.WaitForRequestFinishedOptions().setPredicate(r -> r.url().contains("worker.js")), () -> {
        page.evaluate("() => window.w = new Worker('/worker/worker.js')");
      }));
    });
    Request request2 = request2Ref.get();
    // Java: expect.soft is assertAll.
    assertAll(
      () -> assertEquals(server.PREFIX + "/worker/worker.js", request1.url()),
      () -> assertEquals(request2, request1));
    Response response = request1.response();
    String text = response.text();
    assertTrue(text.contains("console.log('hello from the worker');"));
  }

  @PlaywrightTest
  @DisplayName("WO-018 should report worker script as network request after redirect")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: Chromium does not report the redirect because it is not plumbed to the worker target")
  void shouldReportWorkerScriptAsNetworkRequestAfterRedirect(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    server.setRedirect("/worker.js", "/worker2.js");
    server.setRoute("/worker2.js", (req, res) -> {
      res.setHeader("Content-Type", "text/javascript");
      res.end("console.log('hello from the worker');");
    });
    Request request = page.waitForRequest(r -> r.url().contains("worker.js"), () -> {
      page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().contains("hello from the worker")), () -> {
        page.evaluate("() => window.w = new Worker('/worker.js')");
      });
    });
    assertEquals(server.PREFIX + "/worker.js", request.url());
    Request redirect = request.redirectedTo();
    assertNotNull(redirect);
    assertEquals(server.PREFIX + "/worker2.js", redirect.url());
    Response response = redirect.response();
    String text = response.text();
    assertTrue(text.contains("console.log('hello from the worker');"));
  }

  @PlaywrightTest
  @DisplayName("WO-019 should dispatch console messages when page has workers")
  void shouldDispatchConsoleMessagesWhenPageHasWorkers(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.waitForWorker(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['const x = 1;'], { type: 'application/javascript' })))");
    });
    ConsoleMessage message = page.waitForConsoleMessage(() -> {
      page.evaluate("() => console.log('foo')");
    });
    assertEquals("foo", message.text());
  }

  @PlaywrightTest
  @DisplayName("WO-020 should report and intercept network from nested worker")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: https://github.com/microsoft/playwright/issues/27376")
  void shouldReportAndInterceptNetworkFromNestedWorker(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    page.route("**/simple.json", (Route route) -> {
      // Java: route.fulfill({ json }) is a body and a content type.
      route.fulfill(new Route.FulfillOptions().setBody("{\"foo\":\"not bar\"}").setContentType("application/json"));
    });

    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/simple.json";
    List<Worker> workers = new ArrayList<>();
    List<String> messages = new ArrayList<>();

    page.onWorker(worker -> workers.add(worker));
    page.onConsoleMessage(msg -> messages.add(msg.text()));

    page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
      "    fetch(\"${url}\").then(response => response.text()).then(t => console.log(t.trim()));\n" +
      "  `], { type: 'application/javascript' })))", url);
    // Java: expect.poll is waitForCondition, which dispatches events while it polls.
    page.waitForCondition(() -> workers.size() == 1);

    workers.get(0).evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
      "    fetch(\"${url}\").then(response => response.text()).then(t => console.log(t.trim()));\n" +
      "  `], { type: 'application/javascript' })))", url);

    page.waitForCondition(() -> workers.size() == 2);

    page.waitForCondition(() -> messages.equals(Arrays.asList("{\"foo\":\"not bar\"}", "{\"foo\":\"not bar\"}")));
  }

  @PlaywrightTest
  @DisplayName("WO-021 should support extra http headers")
  void shouldSupportExtraHttpHeaders(Page page, TestServer server, BrowserName browserName, Browser browser) throws Exception {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 143, "needs workerScriptLoaded event");

    page.setExtraHTTPHeaders(Collections.singletonMap("foo", "bar"));
    CompletableFuture<TestServer.Request> request1Promise = server.waitForRequest("/worker/worker.js");
    Worker worker = page.waitForWorker(() -> page.navigate(server.PREFIX + "/worker/worker.html"));
    TestServer.Request request1 = request1Promise.get();
    CompletableFuture<TestServer.Request> request2Promise = server.waitForRequest("/one-style.css");
    worker.evaluate("url => fetch(url)", server.PREFIX + "/one-style.css");
    TestServer.Request request2 = request2Promise.get();
    assertEquals("bar", request1.headers.get("foo"));
    assertEquals("bar", request2.headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("WO-022 should support offline")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: flaky on all platforms")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: does not plumb setOffline into WorkerNavigator::OnLine")
  void shouldSupportOffline(Page page, TestServer server, BrowserName browserName) {
    Worker worker = page.waitForWorker(() -> {
      page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().contains("hello from the worker")), () -> {
        page.navigate(server.PREFIX + "/worker/worker.html");
      });
    });
    page.context().setOffline(true);
    // Java: expect.poll is waitForCondition, which dispatches events while it polls.
    page.waitForCondition(() -> Boolean.FALSE.equals(worker.evaluate("() => navigator.onLine")));
    assertEquals("error", worker.evaluate("() => fetch('/one-style.css').catch(e => 'error')"));
    page.context().setOffline(false);
    page.waitForCondition(() -> Boolean.TRUE.equals(worker.evaluate("() => navigator.onLine")));
  }

  @PlaywrightTest
  @DisplayName("WO-023 should resolve worker script allHeaders in main frame")
  void shouldResolveWorkerScriptAllHeadersInMainFrame(Page page, TestServer server, BrowserName browserName) {
    Request request = page.waitForRequestFinished(new Page.WaitForRequestFinishedOptions().setPredicate(r -> r.url().equals(server.PREFIX + "/worker/worker.js")), () -> {
      page.navigate(server.PREFIX + "/worker/worker.html");
    });
    Response response = request.response();
    Map<String, String> requestHeaders = request.allHeaders();
    assertNotNull(requestHeaders.get("host"));
    Map<String, String> responseHeaders = response.allHeaders();
    assertNotNull(responseHeaders.get("content-type"));
  }

  @PlaywrightTest
  @DisplayName("WO-024 should resolve worker script allHeaders in iframe")
  void shouldResolveWorkerScriptAllHeadersInIframe(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 151, "needs proper Network.requestWillBeSentExtraInfo");

    Request request = page.waitForRequestFinished(new Page.WaitForRequestFinishedOptions().setPredicate(r -> r.url().equals(server.PREFIX + "/worker/worker.js")), () -> {
      attachFrame(page, "frame1", server.PREFIX + "/worker/worker.html");
    });
    Response response = request.response();
    Map<String, String> requestHeaders = request.allHeaders();
    assertNotNull(requestHeaders.get("host"));
    Map<String, String> responseHeaders = response.allHeaders();
    assertNotNull(responseHeaders.get("content-type"));
  }

  @PlaywrightTest
  @DisplayName("WO-025 should resolve worker script allHeaders in nested worker inside iframe")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: cannot evaluate in nested worker")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: nested worker script request is not reported at all")
  void shouldResolveWorkerScriptAllHeadersInNestedWorkerInsideIframe(Page page, TestServer server, BrowserName browserName) {
    String url = server.PREFIX + "/worker/worker.js";
    Worker worker = page.waitForWorker(() -> {
      page.waitForRequestFinished(new Page.WaitForRequestFinishedOptions().setPredicate(r -> r.url().equals(url)), () -> {
        attachFrame(page, "frame1", server.PREFIX + "/worker/worker.html");
      });
    });

    Request request = page.waitForRequestFinished(new Page.WaitForRequestFinishedOptions().setPredicate(r -> r.url().equals(url)), () -> {
      worker.evaluate("url => {\n" +
        "  self.w = new Worker(url);\n" +
        "}", url);
    });

    Response response = request.response();
    Map<String, String> headers = response.allHeaders();
    assertNotNull(headers.get("content-type"));
  }
}
