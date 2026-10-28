/*
 * Copyright 2018 Google Inc. All rights reserved.
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/elementhandle-click.spec.ts. */
public class TestElementhandleClick {
  @PlaywrightTest
  @DisplayName("EH-010 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("EH-011 should work with Node removed")
  void shouldWorkWithNodeRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle button = page.querySelector("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("EH-012 should work for Shadow DOM v1")
  void shouldWorkForShadowDOMV1(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/shadow.html");
    // Java: evaluateHandle returns a JSHandle, which is an ElementHandle for a node.
    ElementHandle buttonHandle = page.evaluateHandle("() => window['button']").asElement();
    buttonHandle.click();
    assertEquals(true, page.evaluate("clicked"));
  }

  @PlaywrightTest
  @DisplayName("EH-013 should work for TextNodes")
  void shouldWorkForTextNodes(Page page) {
    page.setContent("\n" +
      "    <div id='outer' onclick=\"window['result'] = 'Clicked ' + event.target.id;\">\n" +
      "      <div id='inner' style=\"max-width: 50px\">Lorem ipsum dolor sit amet consectetur adipiscing elit proin, integer curabitur imperdiet rhoncus cursus tincidunt bibendum, consequat sed magnis laoreet luctus mollis tellus. Nisl parturient mus accumsan feugiat sem laoreet magnis nisi, aptent per sollicitudin gravida orci ac blandit, viverra eros praesent auctor vivamus semper bibendum. Consequat sed habitasse luctus dictumst gravida platea semper phasellus, nascetur ridiculus purus est varius quisque et scelerisque, id vehicula eleifend montes sollicitudin dis velit. Pellentesque ridiculus per natoque et eleifend taciti nunc, laoreet auctor at condimentum imperdiet ante, conubia mi cubilia scelerisque sociosqu sem.</div>\n" +
      "      Custom Text.\n" +
      "    </div>\n" +
      "  ");
    // Java: evaluateHandle returns a JSHandle, which is an ElementHandle for a node.
    ElementHandle buttonTextNode = page.evaluateHandle("() => document.querySelector('#outer').lastChild").asElement();
    buttonTextNode.click();
    assertEquals("Clicked outer", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("EH-014 should throw for detached nodes")
  void shouldThrowForDetachedNodes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.remove()", button);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.click());
    assertTrue(error.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  @DisplayName("EH-015 should throw for hidden nodes with force")
  void shouldThrowForHiddenNodesWithForce(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.style.display = 'none'", button);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.click(new ElementHandle.ClickOptions().setForce(true)));
    assertTrue(error.getMessage().contains("Element is not visible"));
  }

  @PlaywrightTest
  @DisplayName("EH-016 should throw for recursively hidden nodes with force")
  void shouldThrowForRecursivelyHiddenNodesWithForce(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.parentElement.style.display = 'none'", button);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.click(new ElementHandle.ClickOptions().setForce(true)));
    assertTrue(error.getMessage().contains("Element is not visible"));
  }

  @PlaywrightTest
  @DisplayName("EH-017 should throw for <br> elements with force")
  void shouldThrowForBrElementsWithForce(Page page, TestServer server) {
    page.setContent("hello<br>goodbye");
    ElementHandle br = page.querySelector("br");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> br.click(new ElementHandle.ClickOptions().setForce(true)));
    assertTrue(error.getMessage().contains("Element is outside of the viewport"));
  }

  @PlaywrightTest
  @DisplayName("EH-018 should double click the button")
  void shouldDoubleClickTheButton(Page page, TestServer server) {
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
