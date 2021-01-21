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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.microsoft.playwright.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.*;

public class TestWorkers extends TestBase {

  @Test
  void pageWorkers() {
    Worker worker = page.waitForWorker(() ->
      page.navigate(server.PREFIX + "/worker/worker.html"));
    assertTrue(worker.url().contains("worker.js"));
    assertEquals("worker function result", worker.evaluate("() => self['workerFunction']()"));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.workers().size());
  }

  @Test
  void shouldEmitCreatedAndDestroyedEvents() {
    JSHandle[] workerObj = {null};
    Worker worker = page.waitForWorker(() -> {
      workerObj[0] = page.evaluateHandle(
        "() => new Worker(URL.createObjectURL(new Blob(['1'], {type: 'application/javascript'})))");
    });
    JSHandle workerThisObj = worker.evaluateHandle("() => this");
    Worker closedWorker = worker.waitForClose(() ->
      page.evaluate("workerObj => workerObj.terminate()", workerObj[0]));
    assertEquals(worker, closedWorker);
    try {
      workerThisObj.getProperty("self");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Most likely the worker has been closed."));
    }
  }

  @Test
  void shouldReportConsoleLogs() {
    ConsoleMessage message = page.waitForConsole(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals("1", message.text());
  }

  @Test
  void shouldHaveJSHandlesForConsoleLogs() {
    ConsoleMessage log = page.waitForConsole(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1,2,3,this)'], {type: 'application/javascript'})))"));
    assertEquals("1 2 3 JSHandle@object", log.text());
    assertEquals(4, log.args().size());
    assertEquals("null", (log.args().get(3).getProperty("origin")).jsonValue());
  }

  @Test
  void shouldEvaluate() {
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals(2, worker.evaluate("1+1"));
  }

  @Test
  void shouldReportErrors() {
    Page.Error errorLog = page.waitForPageError(() -> {
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob([`\n" +
        "  setTimeout(() => {\n" +
        "    // Do a console.log just to check that we do not confuse it with an error.\n" +
        "    console.log('hey');\n" +
        "    throw new Error('this is my error');\n" +
        "  })\n" +
        "`], {type: 'application/javascript'})))");
    });
    assertTrue(errorLog.message().contains("this is my error"));
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="flaky upstream")
  void shouldClearUponNavigation() {
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals(1, page.workers().size());
    Worker destroyed = worker.waitForClose(() -> page.navigate(server.PREFIX + "/one-style.html"));
    assertEquals(worker, destroyed);
    assertEquals(0, page.workers().size());
  }

  @Test
  void shouldClearUponCrossProcessNavigation() {
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

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="fixme")
  void shouldAttributeNetworkActivityForWorkerInsideIframeToTheIframe() {
    page.navigate(server.PREFIX + "/empty.html");
    Frame[] frame = {null};
    Worker worker = page.waitForWorker(() -> {
      frame[0] = attachFrame(page, "frame1", server.PREFIX + "/worker/worker.html");
    });
    assertNotNull(frame[0]);
    String url = server.PREFIX + "/one-style.css";
    Request request = page.waitForRequest(() -> {
      worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
    }, url);
    assertEquals(url, request.url());
    assertEquals(frame[0], request.frame());
  }

  @Test
  void shouldReportNetworkActivity() {
    Worker worker = page.waitForWorker(() -> page.navigate(server.PREFIX + "/worker/worker.html"));
    String url = server.PREFIX + "/one-style.css";
    Request[] request = {null};
    Response response = page.waitForResponse(() -> {
      request[0] = page.waitForRequest(() -> {
        worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
      }, url);
      assertEquals(url, request[0].url());
    }, url);
    assertEquals(request[0], response.request());
    assertTrue(response.ok());
  }

  @Test
  void shouldReportNetworkActivityOnWorkerCreation() {
    // Chromium needs waitForDebugger enabled for this one.
    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/one-style.css";
    Request[] request = {null};
    Response response = page.waitForResponse(() -> {
      request[0] = page.waitForRequest(() -> {
        page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
          "  fetch('${url}').then(response => response.text()).then(console.log);\n" +
          "`], {type: 'application/javascript'})))", url);
      }, url);
      assertEquals(url, request[0].url());
    }, url);
    assertEquals(request[0], response.request());
    assertTrue(response.ok());
  }

  @Test
  void shouldFormatNumberUsingContextLocale() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withLocale("ru-RU"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> page.evaluate(
      "() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))"));
    assertEquals("10\u00A0000,2", worker.evaluate("() => (10000.20).toLocaleString()"));
    context.close();
  }
}

