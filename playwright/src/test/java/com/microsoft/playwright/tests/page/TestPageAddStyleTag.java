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
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Paths;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-add-style-tag.spec.ts. */
public class TestPageAddStyleTag {
  @PlaywrightTest
  @DisplayName("AT-013 should throw an error if no options are provided")
  void shouldThrowAnErrorIfNoOptionsAreProvided(Page page, TestServer server) {
    // Java: addStyleTag takes an options object, so the string argument of upstream becomes no options.
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.addStyleTag());
    assertTrue(error.getMessage().contains("Provide an object with a `url`, `path` or `content` property"));
  }

  @PlaywrightTest
  @DisplayName("AT-014 should work with a url @smoke")
  void shouldWorkWithAUrl(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle styleHandle = page.addStyleTag(new Page.AddStyleTagOptions().setUrl("/injectedstyle.css"));
    assertNotNull(styleHandle.asElement());
    assertEquals("rgb(255, 0, 0)", page.evaluate("window.getComputedStyle(document.querySelector('body')).getPropertyValue('background-color')"));
  }

  @PlaywrightTest
  @DisplayName("AT-015 should throw an error if loading from url fail")
  void shouldThrowAnErrorIfLoadingFromUrlFail(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertThrows(PlaywrightException.class,
      () -> page.addStyleTag(new Page.AddStyleTagOptions().setUrl("/nonexistfile.js")));
  }

  @PlaywrightTest
  @DisplayName("AT-016 should work with a path")
  void shouldWorkWithAPath(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle styleHandle = page.addStyleTag(new Page.AddStyleTagOptions().setPath(asset("injectedstyle.css")));
    assertNotNull(styleHandle.asElement());
    assertEquals("rgb(255, 0, 0)", page.evaluate("window.getComputedStyle(document.querySelector('body')).getPropertyValue('background-color')"));
  }

  @PlaywrightTest
  @DisplayName("AT-017 should include sourceURL when path is provided")
  void shouldIncludeSourceURLWhenPathIsProvided(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.addStyleTag(new Page.AddStyleTagOptions().setPath(asset("injectedstyle.css")));
    ElementHandle styleHandle = page.querySelector("style");
    String styleContent = (String) page.evaluate("style => style.innerHTML", styleHandle);
    assertTrue(styleContent.contains(Paths.get("assets", "injectedstyle.css").toString()));
  }

  @PlaywrightTest
  @DisplayName("AT-018 should work with content")
  void shouldWorkWithContent(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle styleHandle = page.addStyleTag(new Page.AddStyleTagOptions().setContent("body { background-color: green; }"));
    assertNotNull(styleHandle.asElement());
    assertEquals("rgb(0, 128, 0)", page.evaluate("window.getComputedStyle(document.querySelector('body')).getPropertyValue('background-color')"));
  }

  @PlaywrightTest
  @DisplayName("AT-019 should throw when added with content to the CSP page")
  void shouldThrowWhenAddedWithContentToTheCSPPage(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/csp.html");
    assertThrows(PlaywrightException.class,
      () -> page.addStyleTag(new Page.AddStyleTagOptions().setContent("body { background-color: green; }")));
  }

  @PlaywrightTest
  @DisplayName("AT-020 should throw when added with URL to the CSP page")
  void shouldThrowWhenAddedWithURLToTheCSPPage(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/csp.html");
    assertThrows(PlaywrightException.class,
      () -> page.addStyleTag(new Page.AddStyleTagOptions().setUrl(server.CROSS_PROCESS_PREFIX + "/injectedstyle.css")));
  }
}
