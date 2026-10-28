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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.util.Arrays;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.relativeToCwd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-misc-1.spec.ts. */
public class TestLocatorMisc1 {
  @PlaywrightTest
  @DisplayName("LO-127 should hover @smoke")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "headed messes up with hover")
  void shouldHover(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    Locator button = page.locator("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("LO-128 should hover when Node is removed")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "headed messes up with hover")
  void shouldHoverWhenNodeIsRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evaluate("() => delete window['Node']");
    Locator button = page.locator("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("LO-129 should fill input")
  void shouldFillInput(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    Locator handle = page.locator("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("LO-130 should fill input when Node is removed")
  void shouldFillInputWhenNodeIsRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evaluate("() => delete window['Node']");
    Locator handle = page.locator("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("LO-131 should clear input")
  void shouldClearInput(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    Locator handle = page.locator("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
    handle.clear();
    assertEquals("", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("LO-132 should check the box")
  void shouldCheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    Locator input = page.locator("input");
    input.check();
    assertEquals(true, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  @DisplayName("LO-133 should check the box using setChecked")
  void shouldCheckTheBoxUsingSetChecked(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    Locator input = page.locator("input");
    input.setChecked(true);
    assertEquals(true, page.evaluate("checkbox.checked"));
    input.setChecked(false);
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  @DisplayName("LO-134 should uncheck the box")
  void shouldUncheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    Locator input = page.locator("input");
    input.uncheck();
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  @DisplayName("LO-135 should select single option")
  void shouldSelectSingleOption(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    Locator select = page.locator("select");
    select.selectOption("blue");
    assertEquals(Arrays.asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(Arrays.asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("LO-136 should focus and blur a button")
  void shouldFocusAndBlurAButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    Locator button = page.locator("button");
    assertEquals(false, button.evaluate("button => document.activeElement === button"));

    boolean[] focused = {false};
    boolean[] blurred = {false};
    page.exposeFunction("focusEvent", args -> focused[0] = true);
    page.exposeFunction("blurEvent", args -> blurred[0] = true);
    button.evaluate("button => {\n" +
      "  button.addEventListener('focus', window['focusEvent']);\n" +
      "  button.addEventListener('blur', window['blurEvent']);\n" +
      "}");

    button.focus();
    assertEquals(true, focused[0]);
    assertEquals(false, blurred[0]);
    assertEquals(true, button.evaluate("button => document.activeElement === button"));

    button.blur();
    assertEquals(true, focused[0]);
    assertEquals(true, blurred[0]);
    assertEquals(false, button.evaluate("button => document.activeElement === button"));
  }

  @PlaywrightTest
  @DisplayName("LO-137 focus should respect strictness")
  void focusShouldRespectStrictness(Page page, TestServer server) {
    page.setContent("<div>A</div><div>B</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("div").focus());
    assertTrue(error.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  @DisplayName("LO-138 should dispatch click event via ElementHandles")
  void shouldDispatchClickEventViaElementHandles(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    Locator button = page.locator("button");
    button.dispatchEvent("click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("LO-139 should upload the file")
  void shouldUploadTheFile(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path filePath = relativeToCwd(asset("file-to-upload.txt"));
    Locator input = page.locator("input[type=file]");
    input.setInputFiles(filePath);
    assertEquals("file-to-upload.txt", page.evaluate("e => e.files[0].name", input.elementHandle()));
  }
}
