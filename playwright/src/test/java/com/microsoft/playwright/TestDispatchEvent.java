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

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDispatchEvent extends TestBase {

  @Test
  void shouldDispatchClickEvent() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldDispatchClickEventProperties() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertNotNull(page.evaluate("bubbles"));
    assertNotNull(page.evaluate("cancelable"));
    assertNotNull(page.evaluate("composed"));
  }

  @Test
  void shouldDispatchClickSvg() {
    page.setContent("<svg height='100' width='100'>\n" +
      "  <circle onclick='javascript:window.__CLICKED=42' cx='50' cy='50' r='40' stroke='black' stroke-width='3' fill='red' />\n" +
      "</svg>");
    page.dispatchEvent("circle", "click");
    assertEquals(42, page.evaluate("() => window['__CLICKED']"));
  }

  @Test
  void shouldDispatchClickOnASpanWithAnInlineElementInside() {
    page.setContent("<style>\n" +
      "span::before {\n" +
      "  content: 'q';\n" +
      "}\n" +
      "</style>\n" +
      "<span onclick='javascript:window.CLICKED=42'></span>");
    page.dispatchEvent("span", "click");
    assertEquals(42, page.evaluate("() => window['CLICKED']"));
  }

  @Test
  void shouldDispatchClickAfterNavigation() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldDispatchClickAfterACrossOriginNavigation() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldNotFailWhenElementIsBlockedOnHover() {
    page.setContent("<style>\n" +
      "  container { display: block; position: relative; width: 200px; height: 50px; }\n" +
      "  div, button { position: absolute; left: 0; top: 0; bottom: 0; right: 0; }\n" +
      "  div { pointer-events: none; }\n" +
      "  container:hover div { pointer-events: auto; background: red; }\n" +
      "</style>\n" +
      "<container>\n" +
      "  <button onclick='window.clicked=true'>Click me</button>\n" +
      "  <div></div>\n" +
      "</container>");
    page.dispatchEvent("button", "click");
    assertNotNull(page.evaluate("() => window['clicked']"));
  }

  @Test
  void shouldDispatchClickWhenNodeIsAddedInShadowDom() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  div.attachShadow({mode: 'open'});\n" +
      "  document.body.appendChild(div);\n" +
      "}");
    page.evaluate("() => new Promise(f => setTimeout(f, 100))");
    page.evaluate("() => {\n" +
      "  const span = document.createElement('span');\n" +
      "  span.textContent = 'Hello from shadow';\n" +
      "  span.addEventListener('click', () => window['clicked'] = true);\n" +
      "  document.querySelector('div').shadowRoot.appendChild(span);\n" +
      "}");
    // TODO: do it asynchronously before evals?
    page.dispatchEvent("span", "click");
    assertEquals(true, page.evaluate("() => window['clicked']"));
  }

//  @Test
  void shouldBeAtomic() {
    // TODO: playwright.selectors.register
  }

  @Test
  void shouldDispatchDragDropEvents() {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
    page.dispatchEvent("#source", "dragstart", mapOf("dataTransfer", dataTransfer));
    page.dispatchEvent("#target", "drop", mapOf("dataTransfer", dataTransfer));
    ElementHandle source = page.querySelector("#source");
    ElementHandle target = page.querySelector("#target");
    assertEquals(true, page.evaluate("({source, target}) => {\n" +
      "  return source.parentElement === target;\n" +
      "}", mapOf("source", source,"target", target)));
  }

  @Test
  void shouldDispatchDragDropEventsOnHandle() {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
    ElementHandle source = page.querySelector("#source");
    source.dispatchEvent("dragstart", mapOf("dataTransfer", dataTransfer));
    ElementHandle target = page.querySelector("#target");
    target.dispatchEvent("drop", mapOf("dataTransfer", dataTransfer));
    assertEquals(true, page.evaluate("({source, target}) => {\n" +
      "  return source.parentElement === target;\n" +
      "}", mapOf("source", source,"target", target)));
  }

  @Test
  void shouldDispatchClickEventOnHandle() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.dispatchEvent("click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }
}
