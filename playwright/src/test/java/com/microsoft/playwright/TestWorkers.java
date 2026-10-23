/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.Assumptions;

import java.time.Duration;
import java.time.Instant;

import static com.microsoft.playwright.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static org.junit.jupiter.api.Assertions.*;

public class TestWorkers {

  private int browserMajorVersion(Browser browser) {
    String version = browser.version();
    return Integer.parseInt(version.split("\\.")[0]);
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = CHROMIUM, reason = "Started failing since last driver roll, flaky upstream")
  void pageWorkers(Page page, Server server) {
    Worker worker = page.waitForWorker(() -> page.navigate(server.PREFIX + "/worker/worker.html"));
    assertTrue(worker.url().contains("worker.js"));
    assertEquals("worker function result", worker.evaluate("() => self['workerFunction']()"));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  void shouldEmitCreatedAndDestroyedEvents(Page page) {
    JSHandle[] workerObj = {null};
    Worker worker = page.waitForWorker(() -> {
      workerObj[0] = page.evaluateHandle(
        "() => new Worker(URL.createObjectURL(new Blob(['1'], {type: 'application/javascript'})))");
    });
    JSHandle workerThisObj = worker.evaluateHandle("() => this");
    Worker closedWorker = worker.waitForClose(() ->
      page.evaluate("workerObj => workerObj.terminate()", workerObj[0]));
    assertEquals(worker, closedWorker);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      workerThisObj.getProperty("self");
    });
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed") || e.getMessage().contains("Worker was closed"), e.getMessage());

  }

  @PlaywrightTest
  void shouldReportConsoleLogs(Page page) {
    ConsoleMessage message = page.waitForConsoleMessage(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals("1", message.text());
  }

  @PlaywrightTest
  void shouldHaveJSHandlesForConsoleLogs(Page page, BrowserName browserName) {
    ConsoleMessage log = page.waitForConsoleMessage(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1,2,3,this)'], {type: 'application/javascript'})))"));
    if (browserName == FIREFOX) {
      assertEquals("1 2 3 JSHandle@object", log.text());
    } else {
      assertEquals("1 2 3 DedicatedWorkerGlobalScope", log.text());
    }
    assertEquals(4, log.args().size());
    assertEquals("null", (log.args().get(3).getProperty("origin")).jsonValue());
  }

  @PlaywrightTest
  void shouldEvaluate(Page page) {
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals(2, worker.evaluate("1+1"));
  }

  @PlaywrightTest
  void shouldReportErrors(Page page) {
    String[] errorLog = {null};
    page.onPageError(e -> errorLog[0] = e);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob([`\n" +
      "  setTimeout(() => {\n" +
      "    // Do a console.log just to check that we do not confuse it with an error.\n" +
      "    console.log('hey');\n" +
      "    throw new Error('this is my error');\n" +
      "  })\n" +
      "`], {type: 'application/javascript'})))");
    Instant start = Instant.now();
    while (errorLog[0] == null) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 30, "Timed out");
    }
    assertTrue(errorLog[0].contains("this is my error"));
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, reason = "flaky upstream")
  void shouldClearUponNavigation(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals(1, page.workers().size());
    Worker destroyed = worker.waitForClose(() -> page.navigate(server.PREFIX + "/one-style.html"));
    assertEquals(worker, destroyed);
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  void shouldClearUponCrossProcessNavigation(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals(1, page.workers().size());
    boolean[] destroyed = {false};
    worker.onClose(worker1 -> destroyed[0] = true);
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertTrue(destroyed[0]);
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  @EnabledOnBrowser(value = WEBKIT, reason = "fixme")
  void shouldAttributeNetworkActivityForWorkerInsideIframeToTheIframe(Page page, Server server) {
    page.navigate(server.PREFIX + "/empty.html");
    Frame[] frame = {null};
    Worker worker = page.waitForWorker(() -> {
      frame[0] = attachFrame(page, "frame1", server.PREFIX + "/worker/worker.html");
    });
    assertNotNull(frame[0]);
    String url = server.PREFIX + "/one-style.css";
    Request request = page.waitForRequest(url, () -> {
      worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
    });
    assertEquals(url, request.url());
    assertEquals(frame[0], request.frame());
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = CHROMIUM, reason = "Started failing since last driver roll, flaky upstream")
  void shouldReportNetworkActivity(Page page, Browser browser, Server server, BrowserName browserName) {
    Assumptions.assumeFalse(browserName == FIREFOX && browserMajorVersion(browser) < 114);
    Worker worker = page.waitForWorker(() -> page.navigate(server.PREFIX + "/worker/worker.html"));
    String url = server.PREFIX + "/one-style.css";
    Request[] request = {null};
    Response response = page.waitForResponse(url, () -> {
      request[0] = page.waitForRequest(url, () -> {
        worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
      });
      assertEquals(url, request[0].url());
    });
    assertEquals(request[0], response.request());
    assertTrue(response.ok());
  }

  @PlaywrightTest
  void shouldReportNetworkActivityOnWorkerCreation(Page page, Browser browser, Server server, BrowserName browserName) {
    Assumptions.assumeFalse(browserName == FIREFOX && browserMajorVersion(browser) < 114);
    // Chromium needs waitForDebugger enabled for this one.
    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/one-style.css";
    Request[] request = {null};
    Response response = page.waitForResponse(url, () -> {
      request[0] = page.waitForRequest(url, () -> {
        page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
          "  fetch('${url}').then(response => response.text()).then(console.log);\n" +
          "`], {type: 'application/javascript'})))", url);
      });
      assertEquals(url, request[0].url());
    });
    assertEquals(request[0], response.request());
    assertTrue(response.ok());
  }

  @PlaywrightTest
  void shouldFormatNumberUsingContextLocale(Browser browser, Server server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("ru-RU"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals("10\u00A0000,2", worker.evaluate("() => (10000.20).toLocaleString()"));
    context.close();
  }

  @PlaywrightTest
  void shouldReportConsoleEventOnTheWorker(Page page) {
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => { window.worker = new Worker(URL.createObjectURL(new Blob(['42'], {type: 'application/javascript'}))); }"
    ));

    ConsoleMessage[] message2 = {null};
    ConsoleMessage[] message3 = {null};

    page.onConsoleMessage(msg -> message2[0] = msg);
    page.context().onConsoleMessage(msg -> message3[0] = msg);

    ConsoleMessage message1 = worker.waitForConsoleMessage(() -> {
      worker.evaluate("() => console.log('hello from worker')");
    });

    assertEquals("hello from worker", message1.text());
    assertSame(message1, message2[0]);
    assertSame(message1, message3[0]);
  }
}

