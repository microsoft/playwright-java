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

package com.microsoft.playwright.fixtures;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.HttpsServer;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicInteger;

import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static org.junit.jupiter.api.Assertions.*;

public class TestPlaywrightFixtures {
  private static final AtomicInteger createdResources = new AtomicInteger();
  private static final AtomicInteger closedResources = new AtomicInteger();

  static final class Resource implements AutoCloseable {
    final Page page;

    Resource(Page page) {
      this.page = page;
      createdResources.incrementAndGet();
    }

    @Override
    public void close() {
      closedResources.incrementAndGet();
    }
  }

  private BrowserName browserNameFromBeforeEach;

  @Fixture
  Resource resource(Page page) {
    return new Resource(page);
  }

  @BeforeEach
  void rememberBrowser(BrowserName browserName) {
    browserNameFromBeforeEach = browserName;
  }

  @AfterAll
  static void allResourcesClosed() {
    assertTrue(createdResources.get() > 0);
    assertEquals(createdResources.get(), closedResources.get());
  }

  @PlaywrightTest
  void shouldInjectBuiltInFixtures(Page page, BrowserContext context, Browser browser, BrowserType browserType,
                                   Playwright playwright, Server server, HttpsServer httpsServer, BrowserName browserName) {
    assertSame(context, page.context());
    assertSame(browser, context.browser());
    assertSame(browserType, browser.browserType());
    assertEquals(browserName.toString(), browserType.name());
    assertSame(browserType, browserName.browserType(playwright));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    assertNotEquals(server.PORT, httpsServer.PORT);
    assertTrue(httpsServer.PREFIX.startsWith("https://"));
  }

  @PlaywrightTest
  void shouldCreateNewContextPerTest(BrowserContext context) {
    assertEquals(0, context.pages().size());
  }

  @PlaywrightTest
  void shouldResetServerBetweenTests(Page page, Server server) {
    // Whichever of the two tests runs first on a worker, the route set by the other one is gone.
    assertEquals(404, page.navigate(server.PREFIX + "/reset").status());
    server.setRoute("/reset", exchange -> exchange.sendResponseHeaders(200, -1));
    assertEquals(200, page.navigate(server.PREFIX + "/reset").status());
  }

  @PlaywrightTest
  void shouldResetServerBetweenTests2(Page page, Server server) {
    assertEquals(404, page.navigate(server.PREFIX + "/reset").status());
    server.setRoute("/reset", exchange -> exchange.sendResponseHeaders(200, -1));
    assertEquals(200, page.navigate(server.PREFIX + "/reset").status());
  }

  @PlaywrightTest
  void shouldInjectCustomFixture(Resource resource, Page page) {
    assertSame(page, resource.page);
  }

  @PlaywrightTest
  void shouldInjectIntoBeforeEach(BrowserName browserName) {
    assertEquals(browserName, browserNameFromBeforeEach);
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = {FIREFOX, WEBKIT}, reason = "checks the condition")
  void shouldSkipDisabledBrowsers(BrowserName browserName) {
    assertEquals(CHROMIUM, browserName);
  }

  @PlaywrightTest
  @DisabledOnBrowser(FIREFOX)
  @DisabledOnBrowser(value = WEBKIT, reason = "repeatable")
  void shouldSkipRepeatedDisabledBrowsers(BrowserName browserName) {
    assertEquals(CHROMIUM, browserName);
  }

  @PlaywrightTest
  @EnabledOnBrowser(WEBKIT)
  void shouldRunOnlyOnEnabledBrowsers(BrowserName browserName) {
    assertEquals(WEBKIT, browserName);
  }

  @Nested
  class WithTouch {
    @Fixture
    Browser.NewContextOptions contextOptions() {
      return new Browser.NewContextOptions().setHasTouch(true);
    }

    @PlaywrightTest
    void shouldUseOverriddenContextOptions(Page page, Server server, Resource resource) {
      page.navigate(server.PREFIX + "/mobile.html");
      assertEquals(true, page.evaluate("'ontouchstart' in window"));
      assertSame(page, resource.page);
    }
  }

  @Nested
  @EnabledOnBrowser(CHROMIUM)
  class ChromiumOnly {
    @PlaywrightTest
    void shouldHonorClassLevelCondition(BrowserName browserName) {
      assertEquals(CHROMIUM, browserName);
    }
  }

  @Nested
  @ParameterizedClass
  @ValueSource(ints = {400, 600})
  class WithViewportWidth {
    @Parameter
    int width;

    @Fixture
    Browser.NewContextOptions contextOptions() {
      return new Browser.NewContextOptions().setViewportSize(width, 300);
    }

    @PlaywrightTest
    void shouldUseClassParameter(Page page) {
      assertEquals(width, page.evaluate("window.innerWidth"));
    }
  }
}
