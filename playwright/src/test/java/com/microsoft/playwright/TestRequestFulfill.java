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
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestRequestFulfill extends TestBase {
  @Test
  void shouldWork() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(201)
        .setContentType("text/html")
        .setHeaders(mapOf("foo", "bar"))
        .setBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(201, response.status());
    assertEquals("bar", response.headers().get("foo"));
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @Test
  void shouldWorkWithStatusCode422() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(422)
        .setBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(422, response.status());
    assertEquals("Unprocessable Entity", response.statusText());
    assertEquals("Yo, page!", page.evaluate("document.body.textContent"));
  }

  static boolean isFirefoxHeadful() {
    return isFirefox() && isHeadful();
  }

  @Test
  @DisabledIf(value="isFirefoxHeadful", disabledReason="skip")
  void shouldAllowMockingBinaryResponses() {
    page.route("**/*", route -> {
      byte[] imageBuffer;
      try {
        imageBuffer = Files.readAllBytes(Paths.get("src/test/resources/pptr.png"));
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      route.fulfill(new Route.FulfillOptions()
        .setContentType("image/png")
        .setBodyBytes(imageBuffer));
    });
    page.evaluate("PREFIX => {\n" +
      "  const img = document.createElement('img');\n" +
      "  img.src = PREFIX + '/does-not-exist.png';\n" +
      "  document.body.appendChild(img);\n" +
      "  return new Promise(fulfill => img.onload = fulfill);\n" +
      "}", server.PREFIX);
    ElementHandle img = page.querySelector("img");
//    expect(img.screenshot()).toMatchSnapshot("mock-binary-response.png");
  }

  @Test
  @DisabledIf(value="isFirefoxHeadful", disabledReason="skip")
  void shouldAllowMockingSvgWithCharset() {
    // Firefox headful produces a different image.
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("image/svg+xml ; charset=utf-8")
        .setBody("<svg width=\"50\" height=\"50\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><rect x=\"10\" y=\"10\" width=\"30\" height=\"30\" stroke=\"black\" fill=\"transparent\" stroke-width=\"5\"/></svg>"));
    });
    page.evaluate("PREFIX => {\n" +
      "  const img = document.createElement('img');\n" +
      "  img.src = PREFIX + '/does-not-exist.svg';\n" +
      "  document.body.appendChild(img);\n" +
      "  return new Promise((f, r) => { img.onload = f; img.onerror = r; });\n" +
      "}", server.PREFIX);
    ElementHandle img = page.querySelector("img");
//    expect(img.screenshot()).toMatchSnapshot("mock-svg.png");
  }


  @Test
  void fulfillShouldThrowIfHandledTwice() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.route("**/*", route -> {
        route.fulfill();
        route.fulfill();
      });
      page.navigate(server.EMPTY_PAGE);
    });
    assertTrue(e.getMessage().contains("Route is already handled!"), e.getMessage());
  }

  @Test
  void abortShouldThrowIfHandledTwice() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.route("**/*", route -> {
        route.abort();
        route.abort();
      });
      page.navigate(server.EMPTY_PAGE);
    });
    assertTrue(e.getMessage().contains("Route is already handled!"), e.getMessage());
  }

  @Test
  void resumeShouldThrowIfHandledTwice() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.route("**/*", route -> {
        route.resume();
        route.resume();
      });
      page.navigate(server.EMPTY_PAGE);
    });
    assertTrue(e.getMessage().contains("Route is already handled!"), e.getMessage());
  }
}
