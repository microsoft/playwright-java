/*
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/page-add-init-script.spec.ts. */
public class TestPageAddInitScript {
  @PlaywrightTest
  @DisplayName("IS-001 should evaluate before anything else on the page")
  void shouldEvaluateBeforeAnythingElseOnThePage(Page page, TestServer server) {
    // Java: addInitScript() takes the source of the script, here the body of the function
    page.addInitScript("window['injected'] = 123;");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-002 should work with a path")
  void shouldWorkWithAPath(Page page, TestServer server) {
    page.addInitScript(asset("injectedfile.js"));
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-003 should work with content @smoke")
  void shouldWorkWithContent(Page page, TestServer server) {
    page.addInitScript("window[\"injected\"] = 123");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-005 should work with trailing comments")
  void shouldWorkWithTrailingComments(Page page) {
    page.addInitScript("// comment");
    page.addInitScript("window.secret = 42;");
    page.navigate("data:text/html,<html></html>");
    assertEquals(42, page.evaluate("secret"));
  }

  @PlaywrightTest
  @DisplayName("IS-006 should support multiple scripts")
  void shouldSupportMultipleScripts(Page page, TestServer server) {
    page.addInitScript("window['script1'] = 1;");
    page.addInitScript("window['script2'] = 2;");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(1, page.evaluate("() => window['script1']"));
    assertEquals(2, page.evaluate("() => window['script2']"));
  }

  @PlaywrightTest
  @DisplayName("IS-007 should work with CSP")
  void shouldWorkWithCSP(Page page, TestServer server) {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.addInitScript("window['injected'] = 123;");
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(123, page.evaluate("() => window['injected']"));

    // Make sure CSP works.
    try {
      page.addScriptTag(new Page.AddScriptTagOptions().setContent("window.e = 10;"));
    } catch (PlaywrightException e) {
      // void e
    }
    assertNull(page.evaluate("() => window['e']"));
  }

  @PlaywrightTest
  @DisplayName("IS-008 should work after a cross origin navigation")
  void shouldWorkAfterACrossOriginNavigation(Page page, TestServer server) {
    page.navigate(server.CROSS_PROCESS_PREFIX);
    page.addInitScript("window['injected'] = 123;");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-009 should remove init script after dispose")
  void shouldRemoveInitScriptAfterDispose(Page page, TestServer server) throws Exception {
    AutoCloseable disposable = page.addInitScript("window['injected'] = 123;");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(123, page.evaluate("() => window['result']"));

    disposable.close();
    page.navigate(server.PREFIX + "/tamperable.html");
    assertNull(page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("IS-010 should remove one of multiple init scripts after dispose")
  void shouldRemoveOneOfMultipleInitScriptsAfterDispose(Page page, TestServer server) throws Exception {
    AutoCloseable disposable1 = page.addInitScript("window['script1'] = 1;");
    page.addInitScript("window['script2'] = 2;");
    page.navigate(server.PREFIX + "/tamperable.html");
    assertEquals(1, page.evaluate("() => window['script1']"));
    assertEquals(2, page.evaluate("() => window['script2']"));

    disposable1.close();
    page.navigate(server.PREFIX + "/tamperable.html");
    assertNull(page.evaluate("() => window['script1']"));
    assertEquals(2, page.evaluate("() => window['script2']"));
  }

  @PlaywrightTest
  @DisplayName("IS-011 init script should run only once in iframe")
  void initScriptShouldRunOnlyOnceInIframe(Page page, TestServer server, BrowserName browserName) {
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(event -> {
      if (event.text().startsWith("init script:")) {
        messages.add(event.text());
      }
    });
    page.addInitScript("console.log('init script:', location.pathname || 'no url yet')");
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertEquals(asList(
      "init script: /frames/one-frame.html",
      "init script: " + (browserName == BrowserName.FIREFOX && !Env.isBidi() ? "no url yet" : "/frames/frame.html")),
      messages);
  }
}
