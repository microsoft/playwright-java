/**
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

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDialog {
  private static Server server;
  private static Browser browser;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    Playwright playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
  }

  @BeforeAll
  static void startServer() throws IOException {
    server = new Server(8907);
  }

  @AfterAll
  static void stopServer() throws IOException {
    browser.close();
    server.stop();
    server = null;
  }

  @BeforeEach
  void setUp() {
    server.reset();
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void tearDown() {
    context.close();
    context = null;
    page = null;
  }

  @Test
  void shouldFire() {
    page.addDialogListener(dialog -> {
      assertEquals( "alert", dialog.type());
      assertEquals( "", dialog.defaultValue());
      assertEquals( "yo", dialog.message());
      dialog.accept();
    });
    page.evaluate("() => alert('yo')");
  }

  @Test
  void shouldAllowAcceptingPrompts() {
    page.addDialogListener(dialog -> {
      assertEquals("prompt", dialog.type());
      assertEquals("yes.", dialog.defaultValue());
      assertEquals("question?", dialog.message());
      dialog.accept("answer!");
    });
    Object result = page.evaluate("() => prompt('question?', 'yes.')");
    assertEquals("answer!", result);
  }

  @Test
  void shouldDismissThePrompt() {
    page.addDialogListener(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => prompt('question?')");
    assertNull(result);
  }

  @Test
  void shouldAcceptTheConfirmPrompt() {
    page.addDialogListener(dialog -> {
      dialog.accept();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(true, result);
  }

  @Test
  void shouldDismissTheConfirmPrompt() {
    page.addDialogListener(dialog -> {
      dialog.dismiss();
    });
    Object result = page.evaluate("() => confirm('boolean?')");
    assertEquals(false, result);
  }

  @Test
  void shouldBeAbleToCloseContextWithOpenAlert() {
//    test.fixme(browserName === "webkit" && platform === "darwin");
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
//    const alertPromise = page.waitForEvent("dialog");
    page.evaluate("() => {\n" +
      "    setTimeout(() => alert('hello'), 0);\n" +
      "}");
//    alertPromise;
    context.close();
  }
}
