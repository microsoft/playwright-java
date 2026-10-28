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
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/page-dialog.spec.ts. */
public class TestPageDialog {
  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("DI-001 should fire")
  void shouldFire(Page page, TestServer server) {
    page.onDialog(dialog -> {
      assertEquals("alert", dialog.type());
      assertEquals("", dialog.defaultValue());
      assertEquals("yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("() => alert('yo')");
  }

  @PlaywrightTest
  @DisplayName("DI-002 should fire dialogclosed when dialog is accepted")
  void shouldFireDialogclosedWhenDialogIsAccepted(Page page) {
    List<Dialog> closed = new ArrayList<>();
    page.onDialogClosed(dialog -> closed.add(dialog));
    Dialog[] opened = {null};
    page.onDialog(dialog -> {
      opened[0] = dialog;
      dialog.accept();
    });
    page.evaluate("() => alert('yo')");
    expectPollToEqual(page, () -> closed.size(), 1);
    assertEquals(opened[0], closed.get(0));
    // Perform some roundtrips to ensure the event does not fire twice.
    page.evaluate("() => 1");
    page.evaluate("() => 1");
    assertEquals(1, closed.size());
  }

  @PlaywrightTest
  @DisplayName("DI-003 should fire dialogclosed when dialog is dismissed")
  void shouldFireDialogclosedWhenDialogIsDismissed(Page page) throws Exception {
    // Java: page.waitForEvent('dialogclosed') created before the action is a handler that completes a future.
    CompletableFuture<Dialog> closedPromise = new CompletableFuture<>();
    page.onDialogClosed(dialog -> closedPromise.complete(dialog));
    page.onDialog(dialog -> dialog.dismiss());
    page.evaluate("() => confirm('boolean?')");
    // Java: waitForCondition() dispatches the events while waiting.
    page.waitForCondition(() -> closedPromise.isDone());
    Dialog dialog = closedPromise.get();
    assertEquals("confirm", dialog.type());
    assertEquals("boolean?", dialog.message());
  }

  @PlaywrightTest
  @DisplayName("DI-004 should fire dialogclosed for auto-dismissed dialogs")
  void shouldFireDialogclosedForAutoDismissedDialogs(Page page) throws Exception {
    // Java: page.waitForEvent('dialogclosed') created before the action is a handler that completes a future.
    CompletableFuture<Dialog> closedPromise = new CompletableFuture<>();
    page.onDialogClosed(dialog -> closedPromise.complete(dialog));
    page.evaluate("() => alert('yo')");
    // Java: waitForCondition() dispatches the events while waiting.
    page.waitForCondition(() -> closedPromise.isDone());
    Dialog dialog = closedPromise.get();
    assertEquals("yo", dialog.message());
  }

  @PlaywrightTest
  @DisplayName("DI-005 should allow accepting prompts @smoke")
  void shouldAllowAcceptingPrompts(Page page) {
    page.onDialog(dialog -> {
      assertEquals("prompt", dialog.type());
      assertEquals("yes.", dialog.defaultValue());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    Object result = page.evaluate("() => prompt('question?', 'yes.')");
    assertEquals("answer!", result);
  }

  @PlaywrightTest
  @DisplayName("DI-006 should dismiss the prompt")
  void shouldDismissThePrompt(Page page) {
    page.onDialog(dialog -> dialog.dismiss());
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @PlaywrightTest
  @DisplayName("DI-007 should accept the confirm prompt")
  void shouldAcceptTheConfirmPrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(true, result);
  }

  @PlaywrightTest
  @DisplayName("DI-008 should dismiss the confirm prompt")
  void shouldDismissTheConfirmPrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(false, result);
  }

  @PlaywrightTest
  @DisplayName("DI-009 should be able to close context with open alert")
  void shouldBeAbleToCloseContextWithOpenAlert(Page page) throws Exception {
    // Java: page.waitForEvent('dialog') created before the action is a handler that completes a future.
    CompletableFuture<Dialog> alertPromise = new CompletableFuture<>();
    page.onDialog(dialog -> alertPromise.complete(dialog));
    page.evaluate("() => {\n" +
      "  window.builtins.setTimeout(() => alert('hello'), 0);\n" +
      "}");
    // Java: waitForCondition() dispatches the events while waiting.
    page.waitForCondition(() -> alertPromise.isDone());
    alertPromise.get();
  }

  @PlaywrightTest
  @DisplayName("DI-010 should handle multiple alerts")
  void shouldHandleMultipleAlerts(Page page) {
    page.onDialog(dialog -> {
      try {
        dialog.accept();
      } catch (PlaywrightException e) {
      }
    });
    page.setContent("\n" +
      "    <p>Hello World</p>\n" +
      "    <script>\n" +
      "      alert('Please dismiss this dialog');\n" +
      "      alert('Please dismiss this dialog');\n" +
      "      alert('Please dismiss this dialog');\n" +
      "    </script>\n" +
      "  ");
    assertEquals("Hello World", page.textContent("p"));
  }

  @PlaywrightTest
  @DisplayName("DI-011 should handle multiple confirms")
  void shouldHandleMultipleConfirms(Page page) {
    page.onDialog(dialog -> {
      try {
        dialog.accept();
      } catch (PlaywrightException e) {
      }
    });
    page.setContent("\n" +
      "    <p>Hello World</p>\n" +
      "    <script>\n" +
      "      confirm('Please confirm me?');\n" +
      "      confirm('Please confirm me?');\n" +
      "      confirm('Please confirm me?');\n" +
      "    </script>\n" +
      "  ");
    assertEquals("Hello World", page.textContent("p"));
  }

  @PlaywrightTest
  @DisplayName("DI-012 should auto-dismiss the prompt without listeners")
  void shouldAutoDismissThePromptWithoutListeners(Page page) {
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @PlaywrightTest
  @DisplayName("DI-013 should auto-dismiss the alert without listeners")
  void shouldAutoDismissTheAlertWithoutListeners(Page page) {
    page.setContent("<div onclick=\"window.alert(123); window._clicked=true\">Click me</div>");
    page.click("div");
    assertEquals(true, page.evaluate("window._clicked"));
  }
}
