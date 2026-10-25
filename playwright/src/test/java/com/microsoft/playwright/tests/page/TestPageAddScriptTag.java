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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Paths;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-add-script-tag.spec.ts. */
public class TestPageAddScriptTag {
  @PlaywrightTest
  @DisplayName("AT-001 should throw an error if no options are provided")
  void shouldThrowAnErrorIfNoOptionsAreProvided(Page page, TestServer server) {
    // Java: addScriptTag takes an options object, so the string argument of upstream becomes no options.
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.addScriptTag());
    assertTrue(error.getMessage().contains("Provide an object with a `url`, `path` or `content` property"));
  }

  @PlaywrightTest
  @DisplayName("AT-002 should work with a url")
  void shouldWorkWithAUrl(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle scriptHandle = page.addScriptTag(new Page.AddScriptTagOptions().setUrl("/injectedfile.js"));
    assertNotNull(scriptHandle.asElement());
    assertEquals(42, page.evaluate("() => window['__injected']"));
  }

  @PlaywrightTest
  @DisplayName("AT-003 should work with a url and type=module")
  void shouldWorkWithAUrlAndTypemodule(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.addScriptTag(new Page.AddScriptTagOptions().setUrl("/es6/es6import.js").setType("module"));
    assertEquals(42, page.evaluate("() => window['__es6injected']"));
  }

  @PlaywrightTest
  @DisplayName("AT-004 should work with a path and type=module")
  void shouldWorkWithAPathAndTypemodule(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.addScriptTag(new Page.AddScriptTagOptions().setPath(asset("es6/es6pathimport.js")).setType("module"));
    page.waitForFunction("window.__es6injected");
    assertEquals(42, page.evaluate("() => window['__es6injected']"));
  }

  @PlaywrightTest
  @DisplayName("AT-005 should work with a content and type=module")
  void shouldWorkWithAContentAndTypemodule(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.addScriptTag(new Page.AddScriptTagOptions().setContent("import num from '/es6/es6module.js';window.__es6injected = num;").setType("module"));
    page.waitForFunction("window.__es6injected");
    assertEquals(42, page.evaluate("() => window['__es6injected']"));
  }

  @PlaywrightTest
  @DisplayName("AT-006 should throw an error if loading from url fail")
  void shouldThrowAnErrorIfLoadingFromUrlFail(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertThrows(PlaywrightException.class,
      () -> page.addScriptTag(new Page.AddScriptTagOptions().setUrl("/nonexistfile.js")));
  }

  @PlaywrightTest
  @DisplayName("AT-007 should work with a path")
  void shouldWorkWithAPath(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle scriptHandle = page.addScriptTag(new Page.AddScriptTagOptions().setPath(asset("injectedfile.js")));
    assertNotNull(scriptHandle.asElement());
    assertEquals(42, page.evaluate("() => window['__injected']"));
  }

  @PlaywrightTest
  @DisplayName("AT-008 should include sourceURL when path is provided")
  @DisabledOnBrowser(value = BrowserName.WEBKIT)
  void shouldIncludeSourceURLWhenPathIsProvided(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.addScriptTag(new Page.AddScriptTagOptions().setPath(asset("injectedfile.js")));
    String result = (String) page.evaluate("() => window['__injectedError'].stack");
    assertTrue(result.contains(Paths.get("assets", "injectedfile.js").toString()));
  }

  @PlaywrightTest
  @DisplayName("AT-009 should work with content")
  void shouldWorkWithContent(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle scriptHandle = page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 35;"));
    assertNotNull(scriptHandle.asElement());
    assertEquals(35, page.evaluate("() => window['__injected']"));
  }

  @PlaywrightTest
  @DisplayName("AT-010 should throw when added with content to the CSP page")
  void shouldThrowWhenAddedWithContentToTheCSPPage(Page page, TestServer server) {
    // Firefox fires onload for blocked script before it issues the CSP console error.
    page.navigate(server.PREFIX + "/csp.html");
    assertThrows(PlaywrightException.class,
      () -> page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 35;")));
  }

  @PlaywrightTest
  @DisplayName("AT-011 should throw when added with URL to the CSP page")
  void shouldThrowWhenAddedWithURLToTheCSPPage(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/csp.html");
    assertThrows(PlaywrightException.class,
      () -> page.addScriptTag(new Page.AddScriptTagOptions().setUrl(server.CROSS_PROCESS_PREFIX + "/injectedfile.js")));
  }

  @PlaywrightTest
  @DisplayName("AT-012 should throw a nice error when the request fails")
  void shouldThrowANiceErrorWhenTheRequestFails(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/this_does_not_exist.js";
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.addScriptTag(new Page.AddScriptTagOptions().setUrl(url)));
    assertTrue(error.getMessage().contains(url));
  }
}
