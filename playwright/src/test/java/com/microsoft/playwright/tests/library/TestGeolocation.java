/**
 * Copyright 2017 Google Inc. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Geolocation;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


/** Port of tests/library/geolocation.spec.ts. */
public class TestGeolocation {
  private static final String GET_CURRENT_POSITION = "() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
    "    resolve({ latitude: position.coords.latitude, longitude: position.coords.longitude });\n" +
    "  }))";

  @PlaywrightTest
  @DisplayName("GE-001 should work @smoke")
  void shouldWork(TestServer server, ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    context.grantPermissions(Arrays.asList("geolocation"));
    page.navigate(server.EMPTY_PAGE);
    context.setGeolocation(new Geolocation(10, 10));
    Object geolocation = page.evaluate(GET_CURRENT_POSITION);
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
  }

  @PlaywrightTest
  @DisplayName("GE-002 should throw when invalid longitude")
  void shouldThrowWhenInvalidLongitude(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.setGeolocation(new Geolocation(10, 200)));
    assertTrue(error.getMessage().contains("geolocation.longitude: precondition -180 <= LONGITUDE <= 180 failed."));
  }

  @PlaywrightTest
  @DisplayName("GE-003 should isolate contexts")
  void shouldIsolateContexts(TestServer server, ContextFactory contextFactory, Browser browser) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    context.grantPermissions(Arrays.asList("geolocation"));
    context.setGeolocation(new Geolocation(10, 10));
    page.navigate(server.EMPTY_PAGE);

    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions()
      .setPermissions(Arrays.asList("geolocation"))
      .setGeolocation(new Geolocation(20, 20)));
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);

    Object geolocation = page.evaluate(GET_CURRENT_POSITION);
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);

    Object geolocation2 = page2.evaluate(GET_CURRENT_POSITION);
    assertEquals(mapOf("latitude", 20, "longitude", 20), geolocation2);

    context2.close();
  }

  // GE-004 should throw with missing latitude: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("GE-005 should not modify passed default options object")
  void shouldNotModifyPassedDefaultOptionsObject(Browser browser) {
    Geolocation geolocation = new Geolocation(10, 10);
    Browser.NewContextOptions options = new Browser.NewContextOptions().setGeolocation(geolocation);
    BrowserContext context = browser.newContext(options);
    context.setGeolocation(new Geolocation(20, 20));
    assertSame(geolocation, options.geolocation);
    context.close();
  }

  // GE-006 should throw with missing longitude in default options: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("GE-007 should use context options")
  void shouldUseContextOptions(Browser browser, TestServer server) {
    Browser.NewContextOptions options = new Browser.NewContextOptions()
      .setGeolocation(new Geolocation(10, 10))
      .setPermissions(Arrays.asList("geolocation"));
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Object geolocation = page.evaluate(GET_CURRENT_POSITION);
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("GE-008 watchPosition should be notified")
  void watchPositionShouldBeNotified(TestServer server, ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    context.grantPermissions(Arrays.asList("geolocation"));
    page.navigate(server.EMPTY_PAGE);
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(message -> messages.add(message.text()));

    context.setGeolocation(new Geolocation(0, 0));
    page.evaluate("() => {\n" +
      "    navigator.geolocation.watchPosition(pos => {\n" +
      "      const coords = pos.coords;\n" +
      "      console.log(`lat=${coords.latitude} lng=${coords.longitude}`);\n" +
      "    }, err => {});\n" +
      "  }");
    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().contains("lat=0 lng=10")),
      () -> context.setGeolocation(new Geolocation(0, 10)));
    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().contains("lat=20 lng=30")),
      () -> context.setGeolocation(new Geolocation(20, 30)));
    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().contains("lat=40 lng=50")),
      () -> context.setGeolocation(new Geolocation(40, 50)));

    String allMessages = String.join("|", messages);
    assertTrue(allMessages.contains("lat=0 lng=10"));
    assertTrue(allMessages.contains("lat=20 lng=30"));
    assertTrue(allMessages.contains("lat=40 lng=50"));
  }

  @PlaywrightTest
  @DisplayName("GE-009 should use context options for popup")
  void shouldUseContextOptionsForPopup(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    context.grantPermissions(Arrays.asList("geolocation"));
    context.setGeolocation(new Geolocation(10, 10));
    Page popup = page.waitForPopup(() -> page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/geolocation.html"));
    popup.waitForLoadState();
    Object geolocation = popup.evaluate("() => window['geolocationPromise']");
    assertEquals(mapOf("longitude", 10, "latitude", 10), geolocation);
  }
}
