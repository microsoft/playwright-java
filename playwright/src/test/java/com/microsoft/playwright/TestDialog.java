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

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.condition.OS;

import java.time.Duration;
import java.time.Instant;

import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static org.junit.jupiter.api.Assertions.*;

public class TestDialog {

  @PlaywrightTest
  void shouldFire(Page page) {
    page.onDialog(dialog -> {
      assertEquals("alert", dialog.type());
      assertEquals( "", dialog.defaultValue());
      assertEquals( "yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("() => alert('yo')");
  }

  @PlaywrightTest
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
  void shouldAcceptPromptOnce(Page page) {
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

  @PlaywrightTest
  void shouldDismissThePrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @PlaywrightTest
  void shouldAcceptTheConfirmPrompt(Page page) {
    page.onDialog(dialog -> {
      dialog.accept();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
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
  @DisabledOnBrowser(value = WEBKIT, os = OS.MAC, reason = "fixme")
  void shouldBeAbleToCloseContextWithOpenAlert(Browser browser) {
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
