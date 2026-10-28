/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/hit-target.spec.ts. */
public class TestHitTarget {
  @PlaywrightTest
  @DisplayName("HT-001 should block all events when hit target is wrong")
  @Fixme("java: the error message is 'Timeout 3000ms exceeded.' and the call log, without the 'page.click: ' prefix of the Node.js client")
  void shouldBlockAllEventsWhenHitTargetIsWrong(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  const blocker = document.createElement('div');\n" +
      "  blocker.style.position = 'absolute';\n" +
      "  blocker.style.width = '400px';\n" +
      "  blocker.style.height = '400px';\n" +
      "  blocker.style.left = '0';\n" +
      "  blocker.style.top = '0';\n" +
      "  document.body.appendChild(blocker);\n" +
      "\n" +
      "  const allEvents = [];\n" +
      "  window.allEvents = allEvents;\n" +
      "  for (const name of ['mousedown', 'mouseup', 'click', 'dblclick', 'auxclick', 'contextmenu', 'pointerdown', 'pointerup']) {\n" +
      "    window.addEventListener(name, e => allEvents.push(e.type));\n" +
      "    blocker.addEventListener(name, e => allEvents.push(e.type));\n" +
      "  }\n" +
      "}");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.click("button", new Page.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("page.click: Timeout 3000ms exceeded."));

    // Give it some time, just in case.
    page.waitForTimeout(1000);
    Object allEvents = page.evaluate("() => window.allEvents");
    assertEquals(Collections.emptyList(), allEvents);
  }

  @PlaywrightTest
  @DisplayName("HT-002 should block click when mousedown fails")
  void shouldBlockClickWhenMousedownFails(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.addEventListener('mousemove', () => {\n" +
      "    button.style.marginLeft = '100px';\n" +
      "  });\n" +
      "\n" +
      "  const allEvents = [];\n" +
      "  window.allEvents = allEvents;\n" +
      "  for (const name of ['mousemove', 'mousedown', 'mouseup', 'click', 'dblclick', 'auxclick', 'contextmenu', 'pointerdown', 'pointerup'])\n" +
      "    button.addEventListener(name, e => allEvents.push(e.type));\n" +
      "}");

    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
    Object allEvents = page.evaluate("() => window.allEvents");
    assertEquals(Arrays.asList(
      // First attempt failed.
      "mousemove",
      // Second attempt succeeded.
      "mousemove", "pointerdown", "mousedown", "pointerup", "mouseup", "click"), allEvents);
  }

  @PlaywrightTest
  @DisplayName("HT-003 should click when element detaches in mousedown")
  void shouldClickWhenElementDetachesInMousedown(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.addEventListener('mousedown', () => {\n" +
      "    window.result = 'Mousedown';\n" +
      "    button.remove();\n" +
      "  });\n" +
      "}");

    page.click("button", new Page.ClickOptions().setTimeout(15000));
    assertEquals("Mousedown", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("HT-004 should block all events when hit target is wrong and element detaches")
  @Fixme("java: the error message is 'Timeout 3000ms exceeded.' and the call log, without the 'page.click: ' prefix of the Node.js client")
  void shouldBlockAllEventsWhenHitTargetIsWrongAndElementDetaches(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  const blocker = document.createElement('div');\n" +
      "  blocker.style.position = 'absolute';\n" +
      "  blocker.style.width = '400px';\n" +
      "  blocker.style.height = '400px';\n" +
      "  blocker.style.left = '0';\n" +
      "  blocker.style.top = '0';\n" +
      "  document.body.appendChild(blocker);\n" +
      "\n" +
      "  window.addEventListener('mousemove', () => button.remove());\n" +
      "\n" +
      "  const allEvents = [];\n" +
      "  window.allEvents = allEvents;\n" +
      "  for (const name of ['mousedown', 'mouseup', 'click', 'dblclick', 'auxclick', 'contextmenu', 'pointerdown', 'pointerup']) {\n" +
      "    window.addEventListener(name, e => allEvents.push(e.type));\n" +
      "    blocker.addEventListener(name, e => allEvents.push(e.type));\n" +
      "  }\n" +
      "}");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.click("button", new Page.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("page.click: Timeout 3000ms exceeded."));

    // Give it some time, just in case.
    page.waitForTimeout(1000);
    Object allEvents = page.evaluate("() => window.allEvents");
    assertEquals(Collections.emptyList(), allEvents);
  }

  @PlaywrightTest
  @DisplayName("HT-005 should not block programmatic events")
  void shouldNotBlockProgrammaticEvents(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.addEventListener('mousemove', () => {\n" +
      "    button.style.marginLeft = '100px';\n" +
      "    button.dispatchEvent(new MouseEvent('click'));\n" +
      "  });\n" +
      "\n" +
      "  const allEvents = [];\n" +
      "  window.allEvents = allEvents;\n" +
      "  button.addEventListener('click', e => {\n" +
      "    if (!e.isTrusted)\n" +
      "      allEvents.push(e.type);\n" +
      "  });\n" +
      "}");

    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
    Object allEvents = page.evaluate("() => window.allEvents");
    // We should get one programmatic click on each attempt.
    assertEquals(Arrays.asList(
      "click", "click"), allEvents);
  }

  @PlaywrightTest
  @DisplayName("HT-006 should click the button again after document.write")
  void shouldClickTheButtonAgainAfterDocumentWrite(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));

    page.evaluate("() => {\n" +
      "  document.open();\n" +
      "  document.write('<button onclick=\"window.result2 = true\"></button>');\n" +
      "  document.close();\n" +
      "}");
    page.click("button");
    assertEquals(true, page.evaluate("result2"));
  }

  @PlaywrightTest
  @DisplayName("HT-007 should work with mui select")
  void shouldWorkWithMuiSelect(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/mui.html");
    page.evaluate("() => {\n" +
      "  renderComponent(e(MaterialUI.FormControl, { fullWidth: true }, [\n" +
      "    e(MaterialUI.InputLabel, { id: 'demo-simple-select-label' }, ['Age']),\n" +
      "    e(MaterialUI.Select, {\n" +
      "      labelId: 'demo-simple-select-label',\n" +
      "      id: 'demo-simple-select',\n" +
      "      value: 10,\n" +
      "      label: 'Age',\n" +
      "    }, [\n" +
      "      e(MaterialUI.MenuItem, { value: 10 }, ['Ten']),\n" +
      "      e(MaterialUI.MenuItem, { value: 20 }, ['Twenty']),\n" +
      "      e(MaterialUI.MenuItem, { value: 30 }, ['Thirty']),\n" +
      "    ]),\n" +
      "  ]));\n" +
      "}");
    page.click("div.MuiFormControl-root:has-text(\"Age\")");
    assertThat(page.locator("text=Thirty")).isVisible();
  }

  @PlaywrightTest
  @DisplayName("HT-008 should work with drag and drop that moves the element under cursor")
  void shouldWorkWithDragAndDropThatMovesTheElementUnderCursor(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/drag-n-drop-manual.html");
    page.dragAndDrop("#from", "#to");
    assertThat(page.locator("#to")).hasText("Dropped");
  }

  @PlaywrightTest
  @DisplayName("HT-009 should work with block inside inline")
  void shouldWorkWithBlockInsideInline(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div>\n" +
      "      <span>\n" +
      "        <div id=\"target\" onclick=\"window._clicked=true\">\n" +
      "          Romimine\n" +
      "        </div>\n" +
      "      </span>\n" +
      "    </div>\n" +
      "  ");
    page.locator("#target").click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-010 should work with block-block-block inside inline-inline")
  void shouldWorkWithBlockBlockBlockInsideInlineInline(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div>\n" +
      "      <a href=\"#ney\">\n" +
      "        <div>\n" +
      "          <span>\n" +
      "            <a href=\"#yay\">\n" +
      "              <div>\n" +
      "                <h3 id=\"target\">\n" +
      "                  Romimine\n" +
      "                </h3>\n" +
      "              </div>\n" +
      "            </a>\n" +
      "          </span>\n" +
      "        </div>\n" +
      "      </a>\n" +
      "    </div>\n" +
      "  ");
    page.locator("#target").click();
    assertThat(page).hasURL(server.EMPTY_PAGE + "#yay");
  }

  @PlaywrightTest
  @DisplayName("HT-011 should work with block inside inline in shadow dom")
  void shouldWorkWithBlockInsideInlineInShadowDom(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      const root = document.querySelector('div');\n" +
      "      const shadowRoot = root.attachShadow({ mode: 'open' });\n" +
      "      const span = document.createElement('span');\n" +
      "      shadowRoot.appendChild(span);\n" +
      "      const div = document.createElement('div');\n" +
      "      span.appendChild(div);\n" +
      "      div.id = 'target';\n" +
      "      div.addEventListener('click', () => window._clicked = true);\n" +
      "      div.textContent = 'Hello';\n" +
      "    </script>\n" +
      "  ");
    page.locator("#target").click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-012 should not click iframe overlaying the target")
  void shouldNotClickIframeOverlayingTheTarget(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <button style=\"position: absolute; left: 250px;bottom: 0;height: 40px;width: 200px;\" onclick=\"window._clicked=1\">\n" +
      "      click-me\n" +
      "    </button>\n" +
      "    <div style=\"background: transparent; bottom: 0px; left: 0px; margin: 0px; padding: 0px; position: fixed; z-index: 2147483647;\">\n" +
      "      <iframe srcdoc=\"<body onclick='window.top._clicked=2' style='background-color:red;height:40px;'></body>\" style=\"display: block; border: 0px; width: 100vw; height: 48px;\"></iframe>\n" +
      "    </div>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.click("text=click-me", new Page.ClickOptions().setTimeout(3000)));
    assertNull(page.evaluate("window._clicked"));
    assertTrue(error.getMessage().contains("<iframe srcdoc=\"<body onclick='window.top._clicked=2' style='background-color:red;height:40px;'></body>\"></iframe> from <div>…</div> subtree intercepts pointer events"));
  }

  @PlaywrightTest
  @DisplayName("HT-013 should not click an element overlaying iframe with the target")
  void shouldNotClickAnElementOverlayingIframeWithTheTarget(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div onclick='window.top._clicked=1'>padding</div>\n" +
      "    <iframe width=600 height=600 srcdoc=\"<iframe srcdoc='<div onclick=&quot;window.top._clicked=2&quot;>padding</div><div onclick=&quot;window.top._clicked=3&quot;>inner</div>'></iframe><div onclick='window.top._clicked=4'>outer</div>\"></iframe>\n" +
      "    <div onclick='window.top._clicked=5' style=\"position: absolute; left: 0; right: 0; top: 0; bottom: 0; background: rgba(255, 0, 0, 0.1); padding: 200px;\">PINK OVERLAY</div>\n" +
      "  ");

    Locator target = page.frameLocator("iframe").frameLocator("iframe").locator("text=inner");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> target.click(new Locator.ClickOptions().setTimeout(3000)));
    assertNull(page.evaluate("window._clicked"));
    assertTrue(error.getMessage().contains("<div onclick=\"window.top._clicked=5\">PINK OVERLAY</div> intercepts pointer events"));

    page.locator("text=overlay").evaluate("e => e.style.display = 'none'");

    target.click();
    assertEquals(3, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-014 should click into frame inside closed shadow root")
  void shouldClickIntoFrameInsideClosedShadowRoot(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div id=framecontainer>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      const iframe = document.createElement('iframe');\n" +
      "      iframe.setAttribute('name', 'myframe');\n" +
      "      iframe.setAttribute('srcdoc', '<div onclick=\"window.top.__clicked = true\">click me</div>');\n" +
      "      const div = document.getElementById('framecontainer');\n" +
      "      const host = div.attachShadow({ mode: 'closed' });\n" +
      "      host.appendChild(iframe);\n" +
      "    </script>\n" +
      "  ");

    com.microsoft.playwright.Frame frame = page.frames().get(1);
    frame.locator("text=click me").click();
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-015 should click an element inside closed shadow root")
  void shouldClickAnElementInsideClosedShadowRoot(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div id=container>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      const span = document.createElement('span');\n" +
      "      span.textContent = 'click me';\n" +
      "      span.addEventListener('click', () => window.__clicked = true);\n" +
      "      const div = document.getElementById('container');\n" +
      "      const host = div.attachShadow({ mode: 'closed' });\n" +
      "      host.appendChild(span);\n" +
      "      window.__target = span;\n" +
      "    </script>\n" +
      "  ");

    JSHandle handle = page.evaluateHandle("window.__target");
    handle.asElement().click();
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-016 should detect overlay from another shadow root")
  void shouldDetectOverlayFromAnotherShadowRoot(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <style>\n" +
      "      div > div {\n" +
      "        position: absolute;\n" +
      "        top: 0;\n" +
      "        left: 0;\n" +
      "        width: 10px;\n" +
      "        height: 10px;\n" +
      "      }\n" +
      "      span {\n" +
      "        display: block;\n" +
      "        position: absolute;\n" +
      "        left: 0;\n" +
      "        top: 0;\n" +
      "        width: 300px;\n" +
      "        height: 300px;\n" +
      "      }\n" +
      "    </style>\n" +
      "    <div style=\"position:relative; width:300px; height:300px\">\n" +
      "      <div id=container1></div>\n" +
      "      <div id=container2></div>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      for (const id of ['container1', 'container2']) {\n" +
      "        const span = document.createElement('span');\n" +
      "        span.id = id + '-span';\n" +
      "        span.textContent = 'click me';\n" +
      "        span.style.display = 'block';\n" +
      "        span.style.position = 'absolute';\n" +
      "        span.style.left = '20px';\n" +
      "        span.style.top = '20px';\n" +
      "        span.style.width = '300px';\n" +
      "        span.style.height = '300px';\n" +
      "        span.addEventListener('click', () => window.__clicked = id);\n" +
      "        const div = document.getElementById(id);\n" +
      "        const host = div.attachShadow({ mode: 'open' });\n" +
      "        host.appendChild(span);\n" +
      "      }\n" +
      "    </script>\n" +
      "  ");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("#container1 >> text=click me").click(new Locator.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("<div id=\"container2\"></div> intercepts pointer events"));
  }

  @PlaywrightTest
  @DisplayName("HT-017 should detect overlaid element in a transformed iframe")
  void shouldDetectOverlaidElementInATransformedIframe(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; }\n" +
      "      iframe {\n" +
      "        border: 4px solid black;\n" +
      "        background: gray;\n" +
      "        margin-left: 33px;\n" +
      "        margin-top: 24px;\n" +
      "        width: 400px;\n" +
      "        height: 400px;\n" +
      "        transform: scale(1.2);\n" +
      "      }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { margin-left: 10px; margin-top: 20px; width: 2px; height: 2px; }\n" +
      "        section { position: absolute; top: 0; left: 0; bottom: 0; right: 0; }\n" +
      "      </style>\n" +
      "      <div>Target</div>\n" +
      "      <section>Overlay</section>\n" +
      "      <script>\n" +
      "        document.querySelector('div').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("div");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> locator.click(new Locator.ClickOptions().setTimeout(5000)));
    assertTrue(error.getMessage().contains("<section>Overlay</section> intercepts pointer events"));
  }

  @PlaywrightTest
  @DisplayName("HT-018 should click in iframe with padding")
  void shouldClickInIframeWithPadding(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; box-sizing: border-box; }\n" +
      "      iframe { background: gray; width: 200px; height: 200px; padding-top: 100px; }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { height: 100px; }\n" +
      "      </style>\n" +
      "      <div>Non-target</div>\n" +
      "      <div id=target>Target</div>\n" +
      "      <div>Non-target</div>\n" +
      "      <script>\n" +
      "        document.querySelector('#target').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("#target");
    locator.click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-019 should click in iframe with padding 2")
  void shouldClickInIframeWithPadding2(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; box-sizing: content-box; }\n" +
      "      iframe { background: gray; width: 200px; height: 200px; padding-top: 100px; }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { height: 100px; }\n" +
      "      </style>\n" +
      "      <div>Non-target</div>\n" +
      "      <div id=target>Target</div>\n" +
      "      <div>Non-target</div>\n" +
      "      <script>\n" +
      "        document.querySelector('#target').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("#target");
    locator.click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-020 should click in custom element")
  void shouldClickInCustomElement(Page page) {
    page.setContent("\n" +
      "    <html>\n" +
      "      <body>\n" +
      "        <my-input></my-input>\n" +
      "        <script>\n" +
      "          class MyInput extends HTMLElement {\n" +
      "            connectedCallback() {\n" +
      "              this.attachShadow({mode:'open'});\n" +
      "              this.shadowRoot.innerHTML = '<div><span><input type=\"text\" /></span></div>';\n" +
      "              this.shadowRoot.querySelector('input').addEventListener('click', () => window.__clicked = true);\n" +
      "            }\n" +
      "          }\n" +
      "          customElements.define('my-input', MyInput);\n" +
      "        </script>\n" +
      "      </body>\n" +
      "    </html>\n" +
      "  ");
    page.locator("input").click();
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-021 should click in nested custom elements")
  void shouldClickInNestedCustomElements(Page page) {
    // issue: https://github.com/microsoft/playwright/issues/43118
    page.setContent("\n" +
      "    <outer-host></outer-host>\n" +
      "    <script>\n" +
      "      customElements.define('inner-host', class extends HTMLElement {\n" +
      "        connectedCallback() {\n" +
      "          this.attachShadow({ mode: 'open' }).innerHTML = '<inner-most></inner-most>';\n" +
      "        }\n" +
      "      });\n" +
      "      customElements.define('inner-most', class extends HTMLElement {\n" +
      "        connectedCallback() {\n" +
      "          this.attachShadow({ mode: 'open' }).innerHTML = '<input>';\n" +
      "          this.shadowRoot.querySelector('input').addEventListener('click', () => window.__clicked = true);\n" +
      "        }\n" +
      "      });\n" +
      "      customElements.define('outer-host', class extends HTMLElement {\n" +
      "        connectedCallback() {\n" +
      "          this.attachShadow({ mode: 'open' }).innerHTML = '<inner-host></inner-host>';\n" +
      "        }\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
    page.locator("input").click();
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("HT-022 should click an element inside nested closed shadow roots")
  void shouldClickAnElementInsideNestedClosedShadowRoots(Page page) {
    // issue: https://github.com/microsoft/playwright/issues/43118
    page.setContent("\n" +
      "    <div id=container></div>\n" +
      "    <script>\n" +
      "      const input = document.createElement('input');\n" +
      "      input.addEventListener('click', () => window.__clicked = true);\n" +
      "      const outer = document.getElementById('container').attachShadow({ mode: 'closed' });\n" +
      "      const inner = document.createElement('div');\n" +
      "      outer.appendChild(inner);\n" +
      "      inner.attachShadow({ mode: 'closed' }).appendChild(input);\n" +
      "      window.__target = input;\n" +
      "    </script>\n" +
      "  ");
    JSHandle handle = page.evaluateHandle("window.__target");
    handle.asElement().click();
    assertEquals(true, page.evaluate("window.__clicked"));
  }
}
