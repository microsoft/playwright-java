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

import com.microsoft.playwright.options.Geolocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestGeolocation extends TestBase {
  @Test
  void shouldWork() {
    context.grantPermissions(asList("geolocation"));
    page.navigate(server.EMPTY_PAGE);
    context.setGeolocation(new Geolocation(10, 10));
    Object geolocation = page.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});\n" +
      "}))");
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
  }

  @Test
  void shouldThrowWhenInvalidLongitude() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      context.setGeolocation(new Geolocation(10, 200));
    });
    assertTrue(e.getMessage().contains("geolocation.longitude: precondition -180 <= LONGITUDE <= 180 failed."));
  }

  @Test
  void shouldIsolateContexts() {
    context.grantPermissions(asList("geolocation"));
    context.setGeolocation(new Geolocation(10, 10));
    page.navigate(server.EMPTY_PAGE);

    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions()
      .setPermissions(asList("geolocation"))
      .setGeolocation(new Geolocation(20, 20)));
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);

    Object geolocation = page.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});\n" +
      "}))");
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);

    Object geolocation2 = page2.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});\n" +
      "}))");
    assertEquals(mapOf("latitude", 20, "longitude", 20), geolocation2);
    context2.close();
  }

  void shouldThrowWithMissingLatitude() {
    // Not applicable in Java.
  }

  @Test
  void shouldNotModifyPassedDefaultOptionsObject() {
    Geolocation geolocation = new Geolocation(10, 10);
    Browser.NewContextOptions options = new Browser.NewContextOptions().setGeolocation(geolocation);
    BrowserContext context = browser.newContext(options);
    context.setGeolocation(new Geolocation(20, 20));
    assertEquals(geolocation, options.geolocation);
    context.close();
  }

  void shouldThrowWithMissingLongitudeInDefaultOptions() {
    // Not applicable in Java.
  }

  @Test
  void shouldUseContextOptions() {
    Browser.NewContextOptions options = new Browser.NewContextOptions()
      .setGeolocation(new Geolocation(10, 10))
      .setPermissions(asList("geolocation"));
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object geolocation = page.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});\n" +
      "}))");
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
    context.close();
  }

  @Test
  void watchPositionShouldBeNotified() {
    context.grantPermissions(asList("geolocation"));
    page.navigate(server.EMPTY_PAGE);
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(message -> messages.add(message.text()));
    context.setGeolocation(new Geolocation(0, 0));
    page.evaluate("() => {\n" +
      "  navigator.geolocation.watchPosition(pos => {\n" +
      "    const coords = pos.coords;\n" +
      "    console.log(`lat=${coords.latitude} lng=${coords.longitude}`);\n" +
      "  }, err => {});\n" +
      "}");
    {
      ConsoleMessage message = page.waitForConsoleMessage(() -> context.setGeolocation(new Geolocation(0, 10)));
      // Location change events come several times so we loop until expected one is received.
      while (!message.text().contains("lat=0 lng=10")) {
        message = page.waitForConsoleMessage(() -> {});
      }
      assertTrue(message.text().contains("lat=0 lng=10"), message.text());
    }
    {
      ConsoleMessage message = page.waitForConsoleMessage(() -> context.setGeolocation(new Geolocation(20, 30)));
      while (!message.text().contains("lat=20 lng=30")) {
        message = page.waitForConsoleMessage(() -> {});
      }
      assertTrue(message.text().contains("lat=20 lng=30"), message.text());
    }
    {
      ConsoleMessage message = page.waitForConsoleMessage(() -> context.setGeolocation(new Geolocation(40, 50)));
      while (!message.text().contains("lat=40 lng=50")) {
        message = page.waitForConsoleMessage(() -> {});
      }
      assertTrue(message.text().contains("lat=40 lng=50"), message.text());
    }
    assertTrue(messages.contains("lat=0 lng=10"));
    assertTrue(messages.contains("lat=20 lng=30"));
    assertTrue(messages.contains("lat=40 lng=50"));
  }

  @Test
  void shouldUseContextOptionsForPopup() {
    context.grantPermissions(asList("geolocation"));
    context.setGeolocation(new Geolocation(10, 10));
    Page popup = page.waitForPopup(() -> page.evaluate(
      "url => window['_popup'] = window.open(url)", server.PREFIX + "/geolocation.html"));
    popup.waitForLoadState();
    Object geolocation = popup.evaluate("window['geolocationPromise']");
    assertEquals(mapOf("longitude", 10, "latitude", 10), geolocation);
  }
}
