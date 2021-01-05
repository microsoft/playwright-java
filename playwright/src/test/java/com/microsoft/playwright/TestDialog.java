/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.microsoft.playwright.Dialog.Type.ALERT;
import static com.microsoft.playwright.Dialog.Type.PROMPT;
import static com.microsoft.playwright.Page.EventType.DIALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDialog extends TestBase {

  @Test
  void shouldFire() {
    page.addListener(DIALOG, event -> {
      Dialog dialog = (Dialog) event.data();
      assertEquals(ALERT, dialog.type());
      assertEquals( "", dialog.defaultValue());
      assertEquals( "yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("() => alert('yo')");
  }

  @Test
  void shouldAllowAcceptingPrompts() {
    page.addListener(DIALOG, event -> {
      Dialog dialog = (Dialog) event.data();
      assertEquals(PROMPT, dialog.type());
      assertEquals("yes.", dialog.defaultValue());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    Object result = page.evaluate("() => prompt('question?', 'yes.')");
    assertEquals("answer!", result);
  }

  @Test
  void shouldDismissThePrompt() {
    page.addListener(DIALOG, event -> {
      Dialog dialog = (Dialog) event.data();
      dialog.dismiss();
    });
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @Test
  void shouldAcceptTheConfirmPrompt() {
    page.addListener(DIALOG, event -> {
      Dialog dialog = (Dialog) event.data();
      dialog.accept();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(true, result);
  }

  @Test
  void shouldDismissTheConfirmPrompt() {
    page.addListener(DIALOG, event -> {
      Dialog dialog = (Dialog) event.data();
      dialog.dismiss();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(false, result);
  }

  static boolean isWebKitMac() {
    return isWebKit() && Utils.getOS() == Utils.OS.MAC;
  }

  @Test
  @DisabledIf(value="isWebKitMac", disabledReason="fixme")
  void shouldBeAbleToCloseContextWithOpenAlert() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
//    const alertPromise = page.futureEvent("dialog");
    page.evaluate("() => {\n" +
      "    setTimeout(() => alert('hello'), 0);\n" +
      "}");
//    alertPromise;
    context.close();
  }
}
