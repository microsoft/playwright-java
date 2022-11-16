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

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorFrame extends TestBase {
  private static void routeIframe(Page page) {
    page.route("**/empty.html", route -> route.fulfill(new Route.FulfillOptions()
      .setBody("<iframe src='iframe.html'></iframe>").setContentType("text/html")));
    page.route("**/iframe.html", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html>\n" +
        "  <div>\n" +
        "    <button data-testid=\"buttonId\">Hello iframe</button>\n" +
        "    <iframe src=\"iframe-2.html\"></iframe>\n" +
        "  </div>\n" +
        "  <span>1</span>\n" +
        "  <span>2</span>\n" +
        "  <label for=target>Name</label><input id=target type=text placeholder=Placeholder title=Title alt=Alternative>\n" +
        "</html>").setContentType("text/html"));
    });
    page.route("**/iframe-2.html", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html><button>Hello nested iframe</button></html>").setContentType("text/html"));
    });
  }

  private static void routeAmbiguous(Page page) {
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setBody("<iframe src='iframe-1.html'></iframe>\n" +
          "<iframe src='iframe-2.html'></iframe>\n" +
          "<iframe src='iframe-3.html'></iframe>")
        .setContentType("text/html"));
    });
    page.route("**/iframe-*", route -> {
      try {
        String path = new URL(route.request().url()).getPath().substring(1);
        route.fulfill(new Route.FulfillOptions()
          .setBody("<html><button>Hello from " + path + "</button></html>")
          .setContentType("text/html"));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test
  void shouldWorkForIframe() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").locator("button");
    button.waitFor();
    assertEquals("Hello iframe", button.innerText());
    assertThat(button).hasText("Hello iframe");
    button.click();
  }

  @Test
  void shouldWorkForNestedIframe() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").frameLocator("iframe").locator("button");
    button.waitFor();
    assertEquals("Hello nested iframe", button.innerText());
    assertThat(button).hasText("Hello nested iframe");
    button.click();
  }

  @Test
  void shouldWorkForAnd() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator locator = page.frameLocator("iframe").locator("button");
    assertThat(locator).hasText("Hello iframe");
    assertEquals("Hello iframe", locator.innerText());
    Locator spans = page.frameLocator("iframe").locator("span");
    assertThat(spans).hasCount(2);
  }

  @Test
  void shouldWaitForFrame() {
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.frameLocator("iframe").locator("span").click(new Locator.ClickOptions().setTimeout(300));
    });
    assertTrue(e.getMessage().contains("waiting for frameLocator(\"iframe\")"), e.getMessage());
  }

  @Test
  void shouldWaitForFrame2() {
    routeIframe(page);
    page.evaluate("url => setTimeout(() => location.href = url, 300)", server.EMPTY_PAGE);
    page.frameLocator("iframe").locator("button").click();
  }

  void shouldWaitForFrameToGo() {
  }

  @Test
  void shouldNotWaitForFrame() {
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator("iframe").locator("span")).isHidden();
  }

  @Test
  void shouldNotWaitForFrame2() {
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator("iframe").locator("span")).not().isVisible();
  }

  @Test
  void shouldNotWaitForFrame3() {
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator("iframe").locator("span")).hasCount(0);
  }

  @Test
  void shouldClickInLazyIframe() {
    page.route("**/iframe.html", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html><button>Hello iframe</button></html>").setContentType("text/html"));
    });
    // empty pge
    page.navigate(server.EMPTY_PAGE);

    // add blank iframe
    page.evaluate("setTimeout(() => {\n" +
      "      const iframe = document.createElement('iframe');\n" +
      "      document.body.appendChild(iframe);\n" +
      "      // navigate iframe\n" +
      "      setTimeout(() => iframe.src = 'iframe.html', 500);\n" +
      "    }, 500);");
    // Click in iframe
    Locator button = page.frameLocator("iframe").locator("button");
    button.click();
    assertThat(button).hasText("Hello iframe");
    assertEquals("Hello iframe", button.innerText());
  }

  @Test
  void waitForShouldSurviveFrameReattach() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").locator("button:has-text('Hello nested iframe')");
    page.evaluate("setTimeout(() => {\n" +
      "        document.querySelector('iframe').remove();\n" +
      "        setTimeout(() => {\n" +
      "          const iframe = document.createElement('iframe');\n" +
      "          iframe.src = 'iframe-2.html';\n" +
      "          document.body.appendChild(iframe);\n" +
      "        }, 500);\n" +
      "      }, 500);");
    button.waitFor();
  }

  @Test
  void clickShouldSurviveFrameReattach() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").locator("button:has-text('Hello nested iframe')");

    page.evaluate("setTimeout(() => {\n" +
      "        document.querySelector('iframe').remove();\n" +
      "        setTimeout(() => {\n" +
      "          const iframe = document.createElement('iframe');\n" +
      "          iframe.src = 'iframe-2.html';\n" +
      "          document.body.appendChild(iframe);\n" +
      "        }, 500);\n" +
      "      }, 500);");
    button.click();
  }

  @Test
  void clickShouldSurviveIframeNavigation() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").locator("button:has-text('Hello nested iframe')");
    page.evaluate("setTimeout(() => {\n" +
      "        document.querySelector('iframe').src = 'iframe-2.html';\n" +
      "      }, 500);");
    button.click();
  }

  @Test
  void shouldNonWorkForNonFrame() {
    routeIframe(page);
    page.setContent("<div></div>");
    Locator button = page.frameLocator("div").locator("button");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> button.waitFor());
    assertTrue(e.getMessage().contains("<div></div>"), e.getMessage());
    assertTrue(e.getMessage().contains("<iframe> was expected"), e.getMessage());
  }

  @Test
  void locatorFrameLocatorShouldWorkForIframe() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("body").frameLocator("iframe").locator("button");
    button.waitFor();
    assertThat(button).hasText("Hello iframe");
    assertEquals("Hello iframe", button.innerText());
    button.click();
  }

  @Test
  void locatorFrameLocatorShouldThrowOnAmbiguity() {
    routeAmbiguous(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("body").frameLocator("iframe").locator("button");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> button.waitFor());
    assertTrue(e.getMessage().contains("Error: strict mode violation: locator(\"body\").locator(\"iframe\") resolved to 3 elements"), e.getMessage());
  }

  @Test
  void locatorFrameLocatorShouldNotThrowOnFirstLastNth() {
    routeAmbiguous(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button1 = page.locator("body").frameLocator("iframe").first().locator("button");
    assertThat(button1).hasText("Hello from iframe-1.html");
    Locator button2 = page.locator("body").frameLocator("iframe").nth(1).locator("button");
    assertThat(button2).hasText("Hello from iframe-2.html");
    Locator button3 = page.locator("body").frameLocator("iframe").last().locator("button");
    assertThat(button3).hasText("Hello from iframe-3.html");
  }

  @Test
  void getByCoverage() {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button1 = page.frameLocator("iframe").getByRole(AriaRole.BUTTON);
    Locator button2 = page.frameLocator("iframe").getByText("Hello");
    Locator button3 = page.frameLocator("iframe").getByTestId("buttonId");
    assertThat(button1).hasText("Hello iframe");
    assertThat(button2).hasText("Hello iframe");
    assertThat(button3).hasText("Hello iframe");
    Locator input1 = page.frameLocator("iframe").getByLabel("Name");
    assertThat(input1).hasValue("");
    Locator input2 = page.frameLocator("iframe").getByPlaceholder("Placeholder");
    assertThat(input2).hasValue("");
    Locator input3 = page.frameLocator("iframe").getByAltText("Alternative");
    assertThat(input3).hasValue("");
    Locator input4 = page.frameLocator("iframe").getByTitle("Title");
    assertThat(input4).hasValue("");
  }

}
