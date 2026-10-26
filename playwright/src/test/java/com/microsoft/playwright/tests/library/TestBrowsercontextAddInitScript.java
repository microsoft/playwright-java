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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/library/browsercontext-add-init-script.spec.ts. */
public class TestBrowsercontextAddInitScript {
  @PlaywrightTest
  @DisplayName("IS-024 should work with browser context scripts @smoke")
  void shouldWorkWithBrowserContextScripts(BrowserContext context, TestServer server) {
    context.addInitScript("window['temp'] = 123");
    Page page = context.newPage();
    page.addInitScript("window['injected'] = window['temp']");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-025 should work without navigation, after all bindings")
  void shouldWorkWithoutNavigationAfterAllBindings(BrowserContext context) {
    CompletableFuture<Object> promise = new CompletableFuture<>();
    context.exposeFunction("woof", args -> {
      promise.complete(args[0]);
      return null;
    });

    context.addInitScript("window['woof']('hey');\n" +
      "window['temp'] = 123;");
    Page page = context.newPage();

    assertEquals(123, page.evaluate("() => window['temp']"));
    page.waitForCondition(promise::isDone);
    assertEquals("hey", promise.getNow(null));
  }

  @PlaywrightTest
  @DisplayName("IS-026 should work without navigation in popup")
  void shouldWorkWithoutNavigationInPopup(BrowserContext context) {
    context.addInitScript("window['temp'] = 123");
    Page page = context.newPage();
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['win'] = window.open()");
    });
    assertEquals(123, popup.evaluate("() => window['temp']"));
  }

  @PlaywrightTest
  @DisplayName("IS-027 should work with browser context scripts with a path")
  void shouldWorkWithBrowserContextScriptsWithAPath(BrowserContext context, TestServer server) {
    context.addInitScript(asset("injectedfile.js"));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-028 should work with browser context scripts for already created pages")
  void shouldWorkWithBrowserContextScriptsForAlreadyCreatedPages(BrowserContext context, TestServer server) {
    Page page = context.newPage();
    context.addInitScript("window['temp'] = 123");
    page.addInitScript("window['injected'] = window['temp']");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-029 should remove context init script after dispose")
  void shouldRemoveContextInitScriptAfterDispose(BrowserContext context, TestServer server) throws Exception {
    AutoCloseable disposable = context.addInitScript("window['temp'] = 123");
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['temp']"));

    disposable.close();
    page.navigate(server.PREFIX + "/tamperable.html");
    assertNull(page.evaluate("() => window['temp']"));
  }

  @PlaywrightTest
  @DisplayName("IS-030 should remove context init script and keep working in new pages")
  void shouldRemoveContextInitScriptAndKeepWorkingInNewPages(BrowserContext context, TestServer server) throws Exception {
    AutoCloseable disposable = context.addInitScript("window['temp'] = 123");
    disposable.close();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/tamperable.html");
    assertNull(page.evaluate("() => window['temp']"));
  }

  @PlaywrightTest
  @DisplayName("IS-035 init script should run only once in popup")
  void initScriptShouldRunOnlyOnceInPopup(BrowserContext context) {
    context.addInitScript("window['callCount'] = (window['callCount'] || 0) + 1;");
    Page page = context.newPage();
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank')");
    });
    assertEquals(1, popup.evaluate("callCount"));
  }
}
