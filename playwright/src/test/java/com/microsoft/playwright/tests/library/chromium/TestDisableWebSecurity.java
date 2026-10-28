/**
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

package com.microsoft.playwright.tests.library.chromium;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/chromium/disable-web-security.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestDisableWebSecurity {
  @Fixture(scope = Scope.WORKER)
  static BrowserType.LaunchOptions launchOptions() {
    return Env.launchOptions().setArgs(Arrays.asList("--disable-web-security"));
  }

  @PlaywrightTest
  @DisplayName("CR-045 test utility world in popup w/ --disable-web-security")
  void testUtilityWorldInPopupWDisableWebSecurity(Page page, TestServer server) {
    server.setRoute("/main.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<a href=\"" + server.PREFIX + "/target.html\" target=\"_blank\">Click me</a>");
    });
    server.setRoute("/target.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<html></html>");
    });

    page.navigate(server.PREFIX + "/main.html");
    Page page1 = page.context().waitForPage(() -> page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Click me")).click());
    assertThat(page1).hasURL(Pattern.compile("target"));
  }

  @PlaywrightTest
  @DisplayName("CR-046 test init script w/ --disable-web-security")
  void testInitScriptWDisableWebSecurity(Page page, TestServer server) {
    server.setRoute("/main.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<a href=\"" + server.PREFIX + "/target.html\" target=\"_blank\">Click me</a>");
    });
    server.setRoute("/target.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<html></html>");
    });

    page.context().addInitScript("window.injected = 123");
    page.navigate(server.PREFIX + "/main.html");
    Page page1 = page.context().waitForPage(() -> page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Click me")).click());
    Object value = page1.evaluate("window.injected");
    assertEquals(123, value);
  }
}
