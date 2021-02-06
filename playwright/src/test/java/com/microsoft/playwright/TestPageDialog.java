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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestPageDialog extends TestBase {
  @Test
  void shouldFire() {
    page.onDialog(dialog -> {
      assertEquals("alert", dialog.type());
      assertEquals("", dialog.defaultValue());
      assertEquals("yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("alert('yo')");
  }

  @Test
  void shouldAllowAcceptingPrompts() {
    page.onDialog(dialog -> {
      assertEquals("prompt", dialog.type());
      assertEquals("yes.", dialog.defaultValue());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    Object result = page.evaluate("prompt('question?', 'yes.')");
    assertEquals("answer!", result);
  }

  @Test
  void shouldDismissThePrompt() {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("prompt('question?')");
    assertNull(result);
  }

  @Test
  void shouldAcceptTheConfirmPrompt() {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    Object result = page.evaluate("confirm('boolean?')");
    assertEquals(true, result);
  }

  @Test
  void shouldDismissTheConfirmPrompt() {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(false, result);
  }

  @Test
  void shouldBeAbleToCloseContextWithOpenAlert() {
    Page page = context.newPage();
    boolean[] didShowDialog = {false};
    page.onDialog(dialog -> didShowDialog[0] = true);
    page.evaluate("() => setTimeout(() => alert('hello'), 0)");
    while (!didShowDialog[0]) {
      page.waitForTimeout(100);
    }
  }

  @Test
  void shouldHandleMultipleAlerts() {
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

  @Test
  void shouldHandleMultipleConfirms() {
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

  @Test
  void shouldAutoDismissThePromptWithoutListeners() {
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @Test
  void shouldAutoDismissTheAlertWithoutListeners() {
    page.setContent("<div onclick='window.alert(123); window._clicked=true'>Click me</div>");
    page.click("div");
    assertEquals(true, page.evaluate("window._clicked"));
  }

}
