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

package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

// Compares the fixtures of @BeforeAll with those of the tests, which are per thread.
@Execution(ExecutionMode.SAME_THREAD)
@FixtureTest
@UsePlaywright(TestFixtureNewContextOptions.CustomOptions.class)
public class TestFixtureNewContextOptions {
  // Only checked as an option value, never navigated to.
  private static final String BASE_URL = "http://localhost:1234/base/";
  private static Browser.NewContextOptions contextOptionsFromBeforeAll;

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options()
        .setBaseUrl(BASE_URL)
        .setDeviceName("iPhone 14")
        .setIgnoreHTTPSErrors(true)
        .setContextOptions(new Browser.NewContextOptions().setLocale("de-DE"));
    }
  }

  private static void assertConfiguredFromOptionsFactory(Browser.NewContextOptions contextOptions) {
    assertNotNull(contextOptions);
    assertEquals(BASE_URL, contextOptions.baseURL);
    assertEquals(true, contextOptions.ignoreHTTPSErrors);
    assertEquals("de-DE", contextOptions.locale);
    assertEquals(390, contextOptions.viewportSize.get().width);
    assertEquals(664, contextOptions.viewportSize.get().height);
    assertEquals(true, contextOptions.isMobile);
    assertEquals(true, contextOptions.hasTouch);
    assertTrue(contextOptions.userAgent.contains("iPhone"), contextOptions.userAgent);
  }

  @BeforeAll
  static void beforeAll(Browser browser, Browser.NewContextOptions contextOptions) {
    assertConfiguredFromOptionsFactory(contextOptions);
    contextOptionsFromBeforeAll = contextOptions;

    BrowserContext context = browser.newContext(contextOptions);
    Page page = context.newPage();
    assertEquals(390, page.viewportSize().width);
    assertEquals(664, page.viewportSize().height);
    assertEquals("de-DE", page.evaluate("() => navigator.language"));
    assertTrue(((String) page.evaluate("() => navigator.userAgent")).contains("iPhone"));
    context.close();

    // Modifying the options must not affect other tests.
    contextOptions.setBaseURL("http://localhost:1234/modified/").setLocale("fr-FR");
  }

  @AfterAll
  static void afterAll(Browser.NewContextOptions contextOptions) {
    assertConfiguredFromOptionsFactory(contextOptions);
  }

  @Test
  void shouldBeConfiguredFromOptionsFactory(Browser.NewContextOptions contextOptions) {
    assertConfiguredFromOptionsFactory(contextOptions);
  }

  @Test
  void shouldReturnNewInstanceEveryTime(Browser.NewContextOptions contextOptions1, Browser.NewContextOptions contextOptions2) {
    assertNotSame(contextOptionsFromBeforeAll, contextOptions1);
    assertNotSame(contextOptions1, contextOptions2);
    contextOptions1.setBaseURL("http://localhost:1234/modified/").setLocale("fr-FR");
    assertConfiguredFromOptionsFactory(contextOptions2);
  }

  @Test
  void shouldNotAffectBrowserContextFixture(Page page) {
    assertEquals(390, page.viewportSize().width);
    assertEquals("de-DE", page.evaluate("() => navigator.language"));
  }
}
