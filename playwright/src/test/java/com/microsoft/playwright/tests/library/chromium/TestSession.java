/**
 * Copyright 2018 Google Inc. All rights reserved.
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

package com.microsoft.playwright.tests.library.chromium;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/chromium/session.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestSession {
  private static JsonObject json(Object... keysAndValues) {
    JsonObject object = new JsonObject();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      Object value = keysAndValues[i + 1];
      if (value instanceof Boolean)
        object.addProperty((String) keysAndValues[i], (Boolean) value);
      else if (value instanceof JsonElement)
        object.add((String) keysAndValues[i], (JsonElement) value);
      else
        object.addProperty((String) keysAndValues[i], (String) value);
    }
    return object;
  }

  @PlaywrightTest
  @DisplayName("CD-037 should work")
  void shouldWork(Page page) {
    CDPSession client = page.context().newCDPSession(page);

    // Java: Promise.all of two sends, in the order they are sent.
    client.send("Runtime.enable");
    client.send("Runtime.evaluate", json("expression", "window.foo = \"bar\""));
    Object foo = page.evaluate("() => window['foo']");
    assertEquals("bar", foo);
  }

  @PlaywrightTest
  @DisplayName("CD-039 should send events")
  void shouldSendEvents(Page page, TestServer server) {
    CDPSession client = page.context().newCDPSession(page);
    client.send("Network.enable");
    List<JsonObject> events = new ArrayList<>();
    client.on("Network.requestWillBeSent", event -> events.add(event));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, events.size());
  }

  @PlaywrightTest
  @DisplayName("CD-042 should be able to detach session")
  void shouldBeAbleToDetachSession(Page page) {
    CDPSession client = page.context().newCDPSession(page);
    client.send("Runtime.enable");
    JsonObject evalResponse = client.send("Runtime.evaluate", json("expression", "1 + 2", "returnByValue", true));
    assertEquals(3, evalResponse.getAsJsonObject("result").get("value").getAsInt());
    client.detach();
    PlaywrightException error = null;
    try {
      client.send("Runtime.evaluate", json("expression", "3 + 1", "returnByValue", true));
    } catch (PlaywrightException e) {
      error = e;
    }
    assertTrue(error.getMessage().contains("Target page, context or browser has been closed"));
  }

  @PlaywrightTest
  @DisplayName("CD-044 should work with main frame")
  void shouldWorkWithMainFrame(Page page) {
    CDPSession client = page.context().newCDPSession(page.mainFrame());

    // Java: Promise.all of two sends, in the order they are sent.
    client.send("Runtime.enable");
    client.send("Runtime.evaluate", json("expression", "window.foo = \"bar\""));
    Object foo = page.evaluate("() => window['foo']");
    assertEquals("bar", foo);
  }

  @PlaywrightTest
  @DisplayName("CD-045 should throw if target is part of main")
  void shouldThrowIfTargetIsPartOfMain(TestServer server, Page page) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertTrue(page.frames().get(0).url().contains("/frames/one-frame.html"));
    assertTrue(page.frames().get(1).url().contains("/frames/frame.html"));

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.context().newCDPSession(page.frames().get(1)));
    assertTrue(error.getMessage().contains("This frame does not have a separate CDP session, it is a part of the parent frame's session"));
  }

  @PlaywrightTest
  @DisplayName("CD-046 should not break page.close()")
  void shouldNotBreakPageClose(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    CDPSession session = page.context().newCDPSession(page);
    session.detach();
    page.close();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("CD-047 should detach when page closes")
  void shouldDetachWhenPageCloses(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    CDPSession session = context.newCDPSession(page);
    page.close();
    PlaywrightException error = null;
    try {
      session.detach();
    } catch (PlaywrightException e) {
      error = e;
    }
    assertNotNull(error);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("CD-048 should reject protocol calls when page closes")
  void shouldRejectProtocolCallsWhenPageCloses(Browser browser) throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    CDPSession session = context.newCDPSession(page);
    // Java: the promise is a send() on a thread of its own.
    CompletableFuture<JsonObject> promise = async(() -> session.send("Runtime.evaluate", json("expression", "new Promise(() => {})", "awaitPromise", true)));
    page.close();
    Throwable error1 = cause(promise);
    assertTrue(error1.getMessage().contains(kTargetClosedErrorMessage));
    PlaywrightException error2 = assertThrows(PlaywrightException.class,
      () -> session.send("Runtime.evaluate", json("expression", "new Promise(() => {})", "awaitPromise", true)));
    assertTrue(error2.getMessage().contains("Target page, context or browser has been closed"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("CD-049 should emit event for each CDP event")
  void shouldEmitEventForEachCDPEvent(Page page, TestServer server) {
    CDPSession client = page.context().newCDPSession(page);
    client.send("Network.enable");
    List<JsonObject> events = new ArrayList<>();
    client.on("event", event -> events.add(event));
    page.navigate(server.EMPTY_PAGE);
    assertTrue(events.size() > 0);
    JsonObject requestEvent = null;
    for (JsonObject e : events) {
      if (e.get("method").getAsString().equals("Network.requestWillBeSent")) {
        requestEvent = e;
        break;
      }
    }
    assertNotNull(requestEvent);
    assertEquals(server.EMPTY_PAGE, requestEvent.getAsJsonObject("params").getAsJsonObject("request").get("url").getAsString());
  }

  @PlaywrightTest
  @DisplayName("CD-050 should emit close event when session is detached")
  void shouldEmitCloseEventWhenSessionIsDetached(Page page) {
    CDPSession client = page.context().newCDPSession(page);
    CDPSession[] closedSession = {null};
    client.onClose(session -> closedSession[0] = session);
    client.detach();
    assertSame(client, closedSession[0]);
  }

  @PlaywrightTest
  @DisplayName("CD-051 should emit close event when page closes")
  void shouldEmitCloseEventWhenPageCloses(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    CDPSession session = context.newCDPSession(page);
    CompletableFuture<CDPSession> closePromise = new CompletableFuture<>();
    session.onClose(closePromise::complete);
    page.close();
    context.waitForCondition(closePromise::isDone);
    CDPSession closedSession = closePromise.join();
    assertSame(session, closedSession);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("CD-052 should work with newBrowserCDPSession")
  void shouldWorkWithNewBrowserCDPSession(Browser browser) {
    CDPSession session = browser.newBrowserCDPSession();

    JsonObject version = session.send("Browser.getVersion");
    assertTrue(version.get("userAgent").getAsString().length() > 0);

    boolean[] gotEvent = {false};
    session.on("Target.targetCreated", params -> gotEvent[0] = true);
    session.send("Target.setDiscoverTargets", json("discover", true));
    Page page = browser.newPage();
    assertEquals(true, gotEvent[0]);
    page.close();

    session.detach();
  }
}
