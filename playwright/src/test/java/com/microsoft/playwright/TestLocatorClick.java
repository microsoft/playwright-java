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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}
