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

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Page.EventType.CONSOLE;
import static com.microsoft.playwright.Page.EventType.POPUP;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    try {
      context.setGeolocation(new Geolocation(10, 200));
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("geolocation.longitude: precondition -180 <= LONGITUDE <= 180 failed."));
    }
  }

  @Test
  void shouldIsolateContexts() {
    context.grantPermissions(asList("geolocation"));
    context.setGeolocation(new Geolocation(10, 10));
    page.navigate(server.EMPTY_PAGE);

    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions()
      .withPermissions(asList("geolocation"))
      .withGeolocation(new Geolocation(20, 20)));
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
    Browser.NewContextOptions options = new Browser.NewContextOptions().withGeolocation(geolocation);
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
      .withGeolocation(new Geolocation(10, 10))
      .withPermissions(asList("geolocation"));
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
    page.addListener(CONSOLE, event -> messages.add(((ConsoleMessage) event.data()).text()));
    context.setGeolocation(new Geolocation());
    page.evaluate("() => {\n" +
      "  navigator.geolocation.watchPosition(pos => {\n" +
      "    const coords = pos.coords;\n" +
      "    console.log(`lat=${coords.latitude} lng=${coords.longitude}`);\n" +
      "  }, err => {});\n" +
      "}");
    {
      Deferred<Event<Page.EventType>> deferred = page.waitForEvent(CONSOLE, event -> ((ConsoleMessage) event.data()).text().contains("lat=0 lng=10"));
      context.setGeolocation(new Geolocation(0, 10));
      deferred.get();
    }
    {
      Deferred<Event<Page.EventType>> deferred = page.waitForEvent(CONSOLE, event -> ((ConsoleMessage) event.data()).text().contains("lat=20 lng=30"));
      context.setGeolocation(new Geolocation(20, 30));
      deferred.get();
    }
    {
      Deferred<Event<Page.EventType>> deferred = page.waitForEvent(CONSOLE, event -> ((ConsoleMessage) event.data()).text().contains("lat=40 lng=50"));
      context.setGeolocation(new Geolocation(40, 50));
      deferred.get();
    }
    assertTrue(messages.contains("lat=0 lng=10"));
    assertTrue(messages.contains("lat=20 lng=30"));
    assertTrue(messages.contains("lat=40 lng=50"));
  }

  @Test
  void shouldUseContextOptionsForPopup() {
    context.grantPermissions(asList("geolocation"));
    context.setGeolocation(new Geolocation(10, 10));
    Deferred<Event<Page.EventType>> popupEvent = page.waitForEvent(POPUP);
    page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/geolocation.html");
    Page popup = (Page) popupEvent.get().data();
    popup.waitForLoadState().get();
    Object geolocation = popup.evaluate("window['geolocationPromise']");
    assertEquals(mapOf("longitude", 10, "latitude", 10), geolocation);
  }
}
