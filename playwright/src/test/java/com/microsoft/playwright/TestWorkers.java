/**
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

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.gson.internal.bind.TypeAdapters.URL;
import static com.microsoft.playwright.Page.EventType.*;
import static com.microsoft.playwright.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestWorkers {
  private static Playwright playwright;
  private static Server server;
  private static Browser browser;
  private static boolean isChromium;
  private static boolean isWebKit;
  private static boolean headful;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
    isChromium = true;
    isWebKit = false;
    headful = false;
  }

  @BeforeAll
  static void startServer() throws IOException {
    server = new Server(8907);
  }

  @AfterAll
  static void stopServer() throws IOException {
    browser.close();
    server.stop();
    server = null;
  }

  @BeforeEach
  void setUp() {
    server.reset();
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void tearDown() {
    context.close();
    context = null;
    page = null;
  }

  @Test
  void pageWorkers() {
    Deferred<Event<Page.EventType>> workerEvent = page.waitForEvent(WORKER);
    page.navigate(server.PREFIX + "/worker/worker.html");
    workerEvent.get();
    Worker worker = page.workers().get(0);
    assertTrue(worker.url().contains("worker.js"));
    assertEquals("worker function result", worker.evaluate("() => self['workerFunction']()"));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.workers().size());
  }

  @Test
  void shouldEmitCreatedAndDestroyedEvents() {
    Deferred<Event<Page.EventType>> workerCreatedPromise = page.waitForEvent(WORKER);
    JSHandle workerObj = page.evaluateHandle("() => new Worker(URL.createObjectURL(new Blob(['1'], {type: 'application/javascript'})))");
    Worker worker = (Worker) workerCreatedPromise.get().data();
    JSHandle workerThisObj = worker.evaluateHandle("() => this");
    Deferred<Event<Worker.EventType>> workerDestroyedPromise = worker.waitForEvent(Worker.EventType.CLOSE);
    page.evaluate("workerObj => workerObj.terminate()", workerObj);
    assertEquals(worker, workerDestroyedPromise.get().data());
    try {
      workerThisObj.getProperty("self");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Most likely the worker has been closed."));
    }
  }

  @Test
  void shouldReportConsoleLogs() {
    Deferred<Event<Page.EventType>> consoleEvent = page.waitForEvent(CONSOLE);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))");
    assertEquals("1", ((ConsoleMessage) consoleEvent.get().data()).text());
  }

  @Test
  void shouldHaveJSHandlesForConsoleLogs() {
    Deferred<Event<Page.EventType>> consoleEvent = page.waitForEvent(CONSOLE);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1,2,3,this)'], {type: 'application/javascript'})))");
    ConsoleMessage log = (ConsoleMessage) consoleEvent.get().data();
    assertEquals("1 2 3 JSHandle@object", log.text());
    assertEquals(4, log.args().size());
    assertEquals("null", (log.args().get(3).getProperty("origin")).jsonValue());
  }

  @Test
  void shouldEvaluate() {
    Deferred<Event<Page.EventType>> workerCreatedPromise = page.waitForEvent(WORKER);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))");
    Worker worker = (Worker) workerCreatedPromise.get().data();
    assertEquals(2, worker.evaluate("1+1"));
  }

  @Test
  void shouldReportErrors() {
    Deferred<Event<Page.EventType>> errorPromise = page.waitForEvent(PAGEERROR);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob([`\n" +
      "  setTimeout(() => {\n" +
      "    // Do a console.log just to check that we do not confuse it with an error.\n" +
      "    console.log('hey');\n" +
      "    throw new Error('this is my error');\n" +
      "  })\n" +
      "`], {type: 'application/javascript'})))");
    Page.Error errorLog = (Page.Error) errorPromise.get().data();
    assertTrue(errorLog.message().contains("this is my error"));
  }

  @Test
  void shouldClearUponNavigation() {
    page.navigate(server.EMPTY_PAGE);
    Deferred<Event<Page.EventType>> workerCreatedPromise = page.waitForEvent(WORKER);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))");
    Worker worker = (Worker) workerCreatedPromise.get().data();
    assertEquals(1, page.workers().size());
    boolean[] destroyed = {false};
    worker.addListener(Worker.EventType.CLOSE, event -> destroyed[0] = true);
    page.navigate(server.PREFIX + "/one-style.html");
    assertTrue(destroyed[0]);
    assertEquals(0, page.workers().size());
  }

  @Test
  void shouldClearUponCrossProcessNavigation() {
    page.navigate(server.EMPTY_PAGE);
    Deferred<Event<Page.EventType>> workerCreatedPromise = page.waitForEvent(WORKER);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))");
    Worker worker = (Worker) workerCreatedPromise.get().data();
    assertEquals(1, page.workers().size());
    boolean[] destroyed = {false};
    worker.addListener(Worker.EventType.CLOSE, event -> destroyed[0] = true);
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertTrue(destroyed[0]);
    assertEquals(0, page.workers().size());
  }

  void shouldAttributeNetworkActivityForWorkerInsideIframeToTheIframe() {
    // TODO: test.fixme(browserName === "firefox" || browserName === "chromium");
    page.navigate(server.PREFIX + "/empty.html");
    Deferred<Event<Page.EventType>> workerEvent = page.waitForEvent(WORKER);
    Frame frame = attachFrame(page, "frame1", server.PREFIX + "/worker/worker.html");
    String url = server.PREFIX + "/one-style.css";
    Deferred<Request> request = page.waitForRequest(url);
    Worker worker = (Worker) workerEvent.get().data();

    worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);

    assertEquals(url, request.get().url());
    assertEquals(frame, request.get().frame());
  }

  @Test
  void shouldReportNetworkActivity() {
    Deferred<Event<Page.EventType>> workerEvent = page.waitForEvent(WORKER);
    page.navigate(server.PREFIX + "/worker/worker.html");
    Worker worker = (Worker) workerEvent.get().data();
    String url = server.PREFIX + "/one-style.css";
    Deferred<Request> requestPromise = page.waitForRequest(url);
    Deferred<Response> responsePromise = page.waitForResponse(url);
    worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url);
    Request request = requestPromise.get();
    Response response = responsePromise.get();
    assertEquals(url, request.url());
    assertEquals(request, response.request());
    assertTrue(response.ok());
  }

  @Test
  void shouldReportNetworkActivityOnWorkerCreation() {
    // Chromium needs waitForDebugger enabled for this one.
    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/one-style.css";
    Deferred<Request> requestPromise = page.waitForRequest(url);
    Deferred<Response> responsePromise = page.waitForResponse(url);
    page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
      "  fetch('${url}').then(response => response.text()).then(console.log);\n" +
      "`], {type: 'application/javascript'})))", url);
    Request request = requestPromise.get();
    Response response = responsePromise.get();
    assertEquals(url, request.url());
    assertEquals(request, response.request());
    assertTrue(response.ok());
  }

  @Test
  void shouldFormatNumberUsingContextLocale() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withLocale("ru-RU"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Deferred<Event<Page.EventType>> workerEvent = page.waitForEvent(WORKER);
    page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))");
    Worker worker = (Worker) workerEvent.get().data();
    assertEquals("10\u00A0000,2", worker.evaluate("() => (10000.20).toLocaleString()"));
    context.close();
  }
}

