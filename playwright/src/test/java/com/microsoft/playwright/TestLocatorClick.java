/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.options.KeyboardModifier;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocatorClick extends TestBase {

  @Test
  void shouldWork() {
    page.navigate(server.PREFIX + "/input/button.html");
    Locator button = page.locator("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldWorkWithNodeRemoved() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window['Node']");
    Locator button  = page.locator("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldDoubleClickTheButton() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  window['double'] = false;\n" +
      "  const button = document.querySelector('button');\n" +
      "  button.addEventListener('dblclick', event => {\n" +
      "    window['double'] = true;\n" +
      "  });\n" +
      "}");
    Locator button = page.locator("button");
    button.dblclick();
    assertEquals(true, page.evaluate("double"));
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldSupportCotrolOrMetaModifier() {
    page.setContent("<a href='" + server.PREFIX + "/title.html'>Go</a>");
    Page newPage = page.context().waitForPage(() ->
      page.getByText("Go").click(new Locator.ClickOptions().setModifiers(asList(KeyboardModifier.CONTROLORMETA))));
    assertThat(newPage).hasURL(server.PREFIX + "/title.html");
  }

  @Test
  void shouldClickWithTweenedMouseMovement() {
    page.setContent(
      "<body style=\"margin: 0; padding: 0; height: 500px; width: 500px;\">\n" +
      "  <div style=\"position: relative; top: 280px; left: 150px; width: 100px; height: 40px\">Click me</div>\n" +
      "</body>"
    );

    // The test becomes flaky on WebKit without next line.
    if (isWebKit()) {
      page.evaluate("() => new Promise(requestAnimationFrame)");
    }

    page.mouse().move(100, 100);

    page.evaluate("() => {\n" +
      "  window['result'] = [];\n" +
      "  document.addEventListener('mousemove', event => {\n" +
      "    window['result'].push([event.clientX, event.clientY]);\n" +
      "  });\n" +
      "}");

    // Centerpoint at 150 + 100/2, 280 + 40/2 = 200, 300
    page.locator("div").click(new Locator.ClickOptions().setSteps(5));

    assertEquals(
      asList(
        asList(120, 140),
        asList(140, 180),
        asList(160, 220),
        asList(180, 260),
        asList(200, 300)
      ),
      page.evaluate("result")
    );
  }
}
