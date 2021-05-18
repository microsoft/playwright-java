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

import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBrowserContextClearCookies extends TestBase {
  @Test
  void shouldClearCookies() {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(
      new Cookie("cookie1", "1").setUrl(server.EMPTY_PAGE)));
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    context.clearCookies();
    assertEquals(emptyList(), context.cookies());
    page.reload();
    assertEquals("", page.evaluate("document.cookie"));
  }

  @Test
  void shouldIsolateCookiesWhenClearing() {
    BrowserContext anotherContext = browser.newContext();
    context.addCookies(asList(
      new Cookie("page1cookie", "page1value").setUrl(server.EMPTY_PAGE)));
    anotherContext.addCookies(asList(
      new Cookie("page2cookie", "page2value").setUrl(server.EMPTY_PAGE)));

    assertEquals(1, (context.cookies()).size());
    assertEquals(1, (anotherContext.cookies()).size());

    context.clearCookies();
    assertEquals(0, (context.cookies()).size());
    assertEquals(1, (anotherContext.cookies()).size());

    anotherContext.clearCookies();
    assertEquals(0, (context.cookies()).size());
    assertEquals(0, (anotherContext.cookies()).size());
    anotherContext.close();
  }
}
