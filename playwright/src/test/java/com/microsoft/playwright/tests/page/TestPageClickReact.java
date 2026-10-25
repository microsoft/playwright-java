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

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-click-react.spec.ts. */
public class TestPageClickReact {
  @PlaywrightTest
  @DisplayName("CL-106 should timeout when click opens alert")
  @Fixme("java: the error message has no 'page.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldTimeoutWhenClickOpensAlert(Page page, TestServer server) throws Exception {
    // Java: page.waitForEvent('dialog') created before the action is a handler that completes a future.
    CompletableFuture<Dialog> dialogPromise = new CompletableFuture<>();
    page.onDialog(dialog -> dialogPromise.complete(dialog));
    page.setContent("<div onclick='window.alert(123)'>Click me</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("div", new Page.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("page.click: Timeout 3000ms exceeded."));
    Dialog dialog = dialogPromise.get();
    dialog.dismiss();
  }

  @PlaywrightTest
  @DisplayName("CL-107 should not retarget when element changes on hover")
  void shouldNotRetargetWhenElementChangesOnHover(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/react.html");
    page.evaluate("() => {\n" +
      "  renderComponent(e('div', {}, [e(MyButton, { name: 'button1', renameOnHover: true }), e(MyButton, { name: 'button2' })]));\n" +
      "}");
    page.click("text=button1");
    assertEquals(true, page.evaluate("window.button1"));
    assertNull(page.evaluate("window.button2"));
  }

  @PlaywrightTest
  @DisplayName("CL-108 should not retarget when element is recycled on hover")
  void shouldNotRetargetWhenElementIsRecycledOnHover(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/react.html");
    page.evaluate("() => {\n" +
      "  function shuffle() {\n" +
      "    renderComponent(e('div', {}, [e(MyButton, { name: 'button2' }), e(MyButton, { name: 'button1' })]));\n" +
      "  }\n" +
      "  renderComponent(e('div', {}, [e(MyButton, { name: 'button1', onHover: shuffle }), e(MyButton, { name: 'button2' })]));\n" +
      "}");
    page.click("text=button1");
    assertNull(page.evaluate("window.button1"));
    assertEquals(true, page.evaluate("window.button2"));
  }
}
