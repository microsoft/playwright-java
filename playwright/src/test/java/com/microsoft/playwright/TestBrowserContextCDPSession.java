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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value = "com.microsoft.playwright.TestBase#isChromium", disabledReason = "Chrome Devtools Protocol supported by chromium only")
public class TestBrowserContextCDPSession extends TestBase {

  @Test
  void shouldWork() {
    CDPSession cdpSession = page.context().newCDPSession(page);
    cdpSession.send("Runtime.enable");

    JsonObject params = new JsonObject();
    params.addProperty("expression", "window.foo = 'bar'");
    cdpSession.send("Runtime.evaluate", params);

    Object foo = page.evaluate("window['foo']");
    assertEquals("bar", foo);
  }

  @Test
  void shouldSendEvents() {
    CDPSession cdpSession = page.context().newCDPSession(page);
    cdpSession.send("Network.enable");

    List<JsonElement> events = new ArrayList<>();
    cdpSession.on("Network.requestWillBeSent", events::add);
    page.navigate(server.EMPTY_PAGE);

    assertEquals(1, events.size());
  }

  @Test
  void shouldDetachSession() {
    CDPSession cdpSession = page.context().newCDPSession(page);
    cdpSession.send("Runtime.enable");

    JsonObject params = new JsonObject();
    params.addProperty("expression", "1 + 2");
    params.addProperty("returnByValue", true);

    JsonElement evaluateResult = cdpSession.send("Runtime.evaluate", params);
    assertEquals(3, evaluateResult.getAsJsonObject().getAsJsonObject("result").get("value").getAsInt());

    cdpSession.detach();

    PlaywrightException exception = assertThrows(PlaywrightException.class, () -> {
      cdpSession.send("Runtime.evaluate", params);
    });
    assertTrue(exception.getMessage().contains("Target page, context or browser has been closed"));
  }

  @Test
  void shouldThrowNiceErrors() {
    CDPSession cdpSession = page.context().newCDPSession(page);

    PlaywrightException exception = assertThrows(PlaywrightException.class, () -> {
      cdpSession.send("ThisCommand.DoesNotExist");
    });
    assertTrue(exception.getMessage().contains("'ThisCommand.DoesNotExist' wasn't found"));
  }

  @Test
  void shouldWorkWithMainFrame() {
    CDPSession cdpSession = page.context().newCDPSession(page.mainFrame());
    JsonObject params = new JsonObject();
    params.addProperty("expression", "window.foo = 'bar'");
    cdpSession.send("Runtime.evaluate", params);

    Object foo = page.evaluate("window['foo']");
    assertEquals("bar", foo);
  }

  @Test
  void shouldThrowIfTargetIsPartOfMain() {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertEquals(server.PREFIX + "/frames/one-frame.html", page.frames().get(0).url());
    assertEquals(server.PREFIX + "/frames/frame.html", page.frames().get(1).url());

    PlaywrightException exception = assertThrows(PlaywrightException.class, () -> {
      page.context().newCDPSession(page.frames().get(1));
    });
    assertTrue(exception.getMessage().contains("This frame does not have a separate CDP session, it is a part of the parent frame's session"));
  }

  @Test
  void shouldNotBreakPageClose() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    CDPSession session = page.context().newCDPSession(page);
    session.detach();
    page.close();
    context.close();
  }

  @Test
  void shouldDetachWhenPageCloses() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    CDPSession session = page.context().newCDPSession(page);
    page.close();

    PlaywrightException exception = assertThrows(PlaywrightException.class, session::detach);
    assertTrue(exception.getMessage().contains("Target page, context or browser has been closed"));
    context.close();
  }

  @Test
  void shouldAddMultipleEventListeners() {
    CDPSession cdpSession = page.context().newCDPSession(page);
    cdpSession.send("Network.enable");

    List<JsonObject> events = new ArrayList<>();
    cdpSession.on("Network.requestWillBeSent", events::add);
    cdpSession.on("Network.requestWillBeSent", events::add);

    page.navigate(server.EMPTY_PAGE);
    assertEquals(2, events.size());
  }

  @Test
  void shouldRemoveEventListeners() {
    CDPSession cdpSession = page.context().newCDPSession(page);
    cdpSession.send("Network.enable");

    List<JsonObject> events = new ArrayList<>();
    Consumer<JsonObject> listener1 = events::add;
    cdpSession.on("Network.requestWillBeSent", listener1);
    cdpSession.on("Network.requestWillBeSent", events::add);

    page.navigate(server.EMPTY_PAGE);
    assertEquals(2, events.size());

    cdpSession.off("Network.requestWillBeSent", listener1);
    events.clear();

    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, events.size());
  }
}
