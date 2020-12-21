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

import java.io.OutputStreamWriter;

import static com.microsoft.playwright.BrowserContext.EventType.PAGE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBrowserContextBasic extends TestBase {
  @Test
  void shouldNotReportFramelessPagesOnError() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    server.setRoute("/empty.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<a href='" + server.EMPTY_PAGE + "' target='_blank'>Click me</a>");
      }
    });
    Page[] popup = {null};
    context.addListener(PAGE, event -> popup[0] = (Page) event.data());
    page.navigate(server.EMPTY_PAGE);
    page.click("'Click me'");
    context.close();
    if (popup[0] != null) {
      // This races on Firefox :/
      assertTrue(popup[0].isClosed());
      assertNotNull(popup[0].mainFrame());
    }
  }
}
