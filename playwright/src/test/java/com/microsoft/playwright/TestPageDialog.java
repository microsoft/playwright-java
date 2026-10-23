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

import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestPageDialog {
  @PlaywrightTest
  void shouldFire(Page page) {
    page.onDialog(dialog -> {
      assertEquals("alert", dialog.type());
      assertEquals("", dialog.defaultValue());
      assertEquals("yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("alert('yo')");
  }

  @PlaywrightTest
  void shouldAllowAcceptingPrompts(Page page) {
    page.onDialog(dialog -> {
      assertEquals("prompt", dialog.type());
      assertEquals("yes.", dialog.defaultValue());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    Object result = page.evaluate("prompt('question?', 'yes.')");
    assertEquals("answer!", result);
  }

  @PlaywrightTest
  void shouldDismissThePrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("prompt('question?')");
    assertNull(result);
  }

  @PlaywrightTest
  void shouldAcceptTheConfirmPrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    Object result = page.evaluate("confirm('boolean?')");
    assertEquals(true, result);
  }

  @PlaywrightTest
  void shouldDismissTheConfirmPrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(false, result);
  }

  @PlaywrightTest
  void shouldBeAbleToCloseContextWithOpenAlert(BrowserContext context) {
    Page page = context.newPage();
    boolean[] didShowDialog = {false};
    page.onDialog(dialog -> didShowDialog[0] = true);
    page.evaluate("() => setTimeout(() => alert('hello'), 0)");
    while (!didShowDialog[0]) {
      page.waitForTimeout(100);
    }
  }

  @PlaywrightTest
  void shouldHandleMultipleAlerts(Page page) {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    page.setContent("<p>Hello World</p>\n" +
      "<script>\n" +
      "  alert('Please dismiss this dialog');\n" +
      "  alert('Please dismiss this dialog');\n" +
      "  alert('Please dismiss this dialog');\n" +
      "</script>\n");
    assertEquals("Hello World", page.textContent("p"));
  }

  @PlaywrightTest
  void shouldHandleMultipleConfirms(Page page) {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    page.setContent("<p>Hello World</p>\n" +
      "<script>\n" +
      "  confirm('Please confirm me?');\n" +
      "  confirm('Please confirm me?');\n" +
      "  confirm('Please confirm me?');\n" +
      "</script>\n");
    assertEquals("Hello World", page.textContent("p"));
  }

  @PlaywrightTest
  void shouldAutoDismissThePromptWithoutListeners(Page page) {
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @PlaywrightTest
  void shouldAutoDismissTheAlertWithoutListeners(Page page) {
    page.setContent("<div onclick='window.alert(123); window._clicked=true'>Click me</div>");
    page.click("div");
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  void shouldFireDialogClosedWhenDialogIsAccepted(Page page) {
    List<Dialog> closed = new ArrayList<>();
    page.onDialogClosed(closed::add);
    Dialog[] opened = {null};
    page.onDialog(dialog -> {
      opened[0] = dialog;
      dialog.accept();
    });
    page.evaluate("alert('yo')");
    page.waitForCondition(() -> closed.size() == 1);
    assertEquals(opened[0], closed.get(0));
    // Perform some roundtrips to ensure the event does not fire twice.
    page.evaluate("1");
    page.evaluate("1");
    assertEquals(1, closed.size());
  }

  @PlaywrightTest
  void shouldFireDialogClosedWhenDialogIsDismissed(Page page) {
    Dialog[] closed = {null};
    page.onDialogClosed(dialog -> closed[0] = dialog);
    page.onDialog(Dialog::dismiss);
    page.evaluate("confirm('boolean?')");
    page.waitForCondition(() -> closed[0] != null);
    assertEquals("confirm", closed[0].type());
    assertEquals("boolean?", closed[0].message());
  }

  @PlaywrightTest
  void shouldFireDialogClosedForAutoDismissedDialogs(Page page) {
    Dialog[] closed = {null};
    page.onDialogClosed(dialog -> closed[0] = dialog);
    page.evaluate("alert('yo')");
    page.waitForCondition(() -> closed[0] != null);
    assertEquals("yo", closed[0].message());
  }
}
