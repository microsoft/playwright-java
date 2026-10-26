/**
 * Copyright 2018 Google Inc. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.BindingCallback;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-expose-function.spec.ts. */
public class TestBrowsercontextExposeFunction {
  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  // Java: expect.poll(() => value).toContain(expected); waitForCondition() dispatches the events while waiting.
  private static void expectPollToContain(Page page, List<String> value, String expected) {
    try {
      page.waitForCondition(() -> value.contains(expected), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertTrue(value.contains(expected));
  }

  @PlaywrightTest
  @DisplayName("EF-022 expose binding should work")
  void exposeBindingShouldWork(BrowserContext context) {
    BindingCallback.Source[] bindingSource = {null};
    context.exposeBinding("add", (source, args) -> {
      bindingSource[0] = source;
      return (Integer) args[0] + (Integer) args[1];
    });
    Page page = context.newPage();
    Object result = page.evaluate("add(5, 6)");
    assertEquals(context, bindingSource[0].context());
    assertEquals(page, bindingSource[0].page());
    assertEquals(page.mainFrame(), bindingSource[0].frame());
    assertEquals(11, result);
  }

  @PlaywrightTest
  @DisplayName("EF-023 should work")
  void shouldWork(BrowserContext context, TestServer server) {
    context.exposeFunction("add", args -> (Integer) args[0] + (Integer) args[1]);
    Page page = context.newPage();
    page.exposeFunction("mul", args -> (Integer) args[0] * (Integer) args[1]);
    context.exposeFunction("sub", args -> (Integer) args[0] - (Integer) args[1]);
    context.exposeBinding("addHandle", (source, args) -> {
      JSHandle handle = source.frame().evaluateHandle("([a, b]) => a + b", Arrays.asList(args[0], args[1]));
      return handle;
    });
    Object result = page.evaluate("(async () => ({ mul: await mul(9, 4), add: await add(9, 4), sub: await sub(9, 4), addHandle: await addHandle(5, 6) }))()");
    assertEquals(mapOf("mul", 36, "add", 13, "sub", 5, "addHandle", 11), result);
  }

  @PlaywrightTest
  @DisplayName("EF-024 should dispose")
  void shouldDispose(BrowserContext context, TestServer server) throws Exception {
    AutoCloseable binding = context.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });
    Page page = context.newPage();
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
    binding.close();

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}"));
    assertTrue(e.getMessage().contains("is not a function"));
  }

  @PlaywrightTest
  @DisplayName("EF-025 should throw for duplicate registrations")
  void shouldThrowForDuplicateRegistrations(BrowserContext context, TestServer server) {
    context.exposeFunction("foo", args -> null);
    context.exposeFunction("bar", args -> null);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.exposeFunction("foo", args -> null));
    assertTrue(error.getMessage().contains("Function \"foo\" has been already registered"));
    Page page = context.newPage();
    error = assertThrows(PlaywrightException.class, () -> page.exposeFunction("foo", args -> null));
    assertTrue(error.getMessage().contains("Function \"foo\" has been already registered in the browser context"));
    page.exposeFunction("baz", args -> null);
    error = assertThrows(PlaywrightException.class, () -> context.exposeFunction("baz", args -> null));
    assertTrue(error.getMessage().contains("Function \"baz\" has been already registered in one of the pages"));
  }

  @PlaywrightTest
  @DisplayName("EF-026 should be callable from-inside addInitScript")
  void shouldBeCallableFromInsideAddInitScript(BrowserContext context, TestServer server) {
    List<String> args = new CopyOnWriteArrayList<>();
    context.exposeFunction("woof", a -> {
      args.add((String) a[0]);
      return null;
    });
    context.addInitScript("window[\"woof\"](\"context\")");
    Page page = context.newPage();
    page.evaluate("undefined");
    expectPollToEqual(page, () -> args, Arrays.asList("context"));
    // Java: args = [] is args.clear(), the exposed function appends to the same list.
    args.clear();
    page.addInitScript("window[\"woof\"](\"page\")");
    page.reload();
    expectPollToEqual(page, () -> args, Arrays.asList("context", "page"));
  }

  @PlaywrightTest
  @DisplayName("EF-027 should work with CSP")
  void shouldWorkWithCSP(Page page, BrowserContext context, TestServer server) {
    server.setCSP("/empty.html", "default-src \"self\"");
    page.navigate(server.EMPTY_PAGE);
    boolean[] called = {false};
    context.exposeBinding("hi", (source, args) -> called[0] = true);
    page.evaluate("() => window.hi()");
    assertEquals(true, called[0]);
  }

  @PlaywrightTest
  @DisplayName("EF-028 should call binding from pagehide handler when navigating away")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: Chromium drops CDP events from the old document after the frame host swap")
  void shouldCallBindingFromPagehideHandlerWhenNavigatingAway(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    List<String> calls = new CopyOnWriteArrayList<>();
    context.exposeBinding("reportFromPage", (source, args) -> {
      calls.add((String) args[0]);
      return null;
    });
    java.util.function.Function<String, String> html = body ->
      "<!doctype html>" + body + "<script>addEventListener('pagehide', () => window.reportFromPage('pagehide of ' + location.pathname));</script>";
    server.setRoute("/a.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end(html.apply("<a id=\"next\" href=\"/b.html\">next</a>"));
    });
    server.setRoute("/b.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end(html.apply("<p>b</p>"));
    });
    page.navigate(server.PREFIX + "/a.html");
    page.click("#next");
    page.waitForURL("**/b.html");
    expectPollToContain(page, calls, "pagehide of /a.html");
  }
}
