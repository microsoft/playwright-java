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

import com.microsoft.playwright.fixtures.PlaywrightTest;

import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleClick {

  @PlaywrightTest
  void shouldWork(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  void shouldWorkWithNodeRemoved(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle button  = page.querySelector("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  void shouldWorkForShadowDOMV1(Page page, Server server) {
    page.navigate(server.PREFIX + "/shadow.html");
    ElementHandle buttonHandle = page.evaluateHandle("() => window['button']").asElement();
    buttonHandle.click();
    assertEquals(true, page.evaluate("clicked"));
  }

  @PlaywrightTest
  void shouldWorkForTextNodes(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle buttonTextNode = page.evaluateHandle("() => document.querySelector('button').firstChild").asElement();
    buttonTextNode.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  void shouldThrowForDetachedNodes(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.remove()", button);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> button.click());
    assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  void shouldThrowForHiddenNodesWithForce(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.style.display = 'none'", button);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      button.click(new ElementHandle.ClickOptions().setForce(true));
    });
    assertTrue(e.getMessage().contains("Element is not visible"));
  }

  @PlaywrightTest
  void shouldThrowForRecursivelyHiddenNodesWithForce(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.parentElement.style.display = 'none'", button);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      button.click(new ElementHandle.ClickOptions().setForce(true));
    });
    assertTrue(e.getMessage().contains("Element is not visible"));
  }

  @PlaywrightTest
  void shouldThrowForBrElementsWithForce(Page page) {
    page.setContent("hello<br>goodbye");
    ElementHandle br = page.querySelector("br");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      br.click(new ElementHandle.ClickOptions().setForce(true));
    });
    assertTrue(e.getMessage().contains("Element is outside of the viewport"));
  }

  @PlaywrightTest
  void shouldDoubleClickTheButton(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  window['double'] = false;\n" +
      "  const button = document.querySelector('button');\n" +
      "  button.addEventListener('dblclick', event => {\n" +
      "    window['double'] = true;\n" +
      "  });\n" +
      "}");
    ElementHandle button = page.querySelector("button");
    button.dblclick();
    assertEquals(true, page.evaluate("double"));
    assertEquals("Clicked", page.evaluate("result"));
  }
}
