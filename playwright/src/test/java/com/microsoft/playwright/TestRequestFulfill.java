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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRequestFulfill extends TestBase {
  @Test
  void shouldWork() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withStatus(201)
        .withContentType("text/html")
        .withHeaders(mapOf("foo", "bar"))
        .withBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(201, response.status());
    assertEquals("bar", response.headers().get("foo"));
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @Test
  void shouldWorkWithStatusCode422() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withStatus(422)
        .withBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(422, response.status());
    assertEquals("Unprocessable Entity", response.statusText());
    assertEquals("Yo, page!", page.evaluate("document.body.textContent"));
  }

  @Test
  void shouldAllowMockingBinaryResponses() {
// TODO:    test.skip(browserName === "firefox" && headful, "// Firefox headful produces a different image.");
    page.route("**/*", route -> {
      byte[] imageBuffer;
      try {
        imageBuffer = Files.readAllBytes(new File("src/test/resources/pptr.png").toPath());
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      route.fulfill(new Route.FulfillResponse()
        .withContentType("image/png")
        .withBody(imageBuffer));
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
  void shouldAllowMockingSvgWithCharset() {
    // TODO: test.skip(browserName === "firefox" && headful, "// Firefox headful produces a different image.");
    // Firefox headful produces a different image.
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withContentType("image/svg+xml ; charset=utf-8")
        .withBody("<svg width=\"50\" height=\"50\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><rect x=\"10\" y=\"10\" width=\"30\" height=\"30\" stroke=\"black\" fill=\"transparent\" stroke-width=\"5\"/></svg>"));
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


}
