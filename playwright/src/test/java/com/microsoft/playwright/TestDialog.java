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

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class TestDialog extends TestBase {

  @Test
  void shouldFire() {
    page.onDialog(dialog -> {
      assertEquals("alert", dialog.type());
      assertEquals( "", dialog.defaultValue());
      assertEquals( "yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("() => alert('yo')");
  }

  @Test
  void shouldAllowAcceptingPrompts() {
    page.onDialog(dialog -> {
      assertEquals("prompt", dialog.type());
      assertEquals("yes.", dialog.defaultValue());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    Object result = page.evaluate("() => prompt('question?', 'yes.')");
    assertEquals("answer!", result);
  }

  @Test
  void shouldAcceptPromptOnce() {
    int[] callCounter = {0};
    page.onceDialog(dialog -> {
      ++callCounter[0];
      assertEquals("prompt", dialog.type());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    assertEquals("answer!", page.evaluate("prompt('question?')"));
    assertNull(page.evaluate("prompt('question?')"));
    assertEquals(1, callCounter[0]);
  }

  @Test
  void shouldDismissThePrompt() {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @Test
  void shouldAcceptTheConfirmPrompt() {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
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

  static boolean isWebKitMac() {
    return isWebKit() && Utils.getOS() == Utils.OS.MAC;
  }

  @Test
  @DisabledIf(value="isWebKitMac", disabledReason="fixme")
  void shouldBeAbleToCloseContextWithOpenAlert() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    boolean[] didShowDialog = {false};
    page.onDialog(dialog -> didShowDialog[0] = true);
    page.evaluate("() => {\n" +
      "    setTimeout(() => alert('hello'), 0);\n" +
      "}");
    Instant start = Instant.now();
    while (!didShowDialog[0]) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 30, "Timed out");
    }
    context.close();
  }
}
