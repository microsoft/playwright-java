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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ElementState;
import org.junit.jupiter.api.DisplayName;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/retarget.spec.ts. */
public class TestRetarget {
  @PlaywrightTest
  @DisplayName("RE-001 element state checks should work as expected for label with zero-sized input")
  void elementStateChecksShouldWorkAsExpectedForLabelWithZeroSizedInput(Page page, TestServer server) {
    page.setContent("\n" +
      "    <label>\n" +
      "      Click me\n" +
      "      <input disabled style=\"width:0;height:0;padding:0;margin:0;border:0;\">\n" +
      "    </label>\n" +
      "  ");
    // Visible checks the label.
    assertEquals(true, page.isVisible("text=Click me"));
    assertEquals(false, page.isHidden("text=Click me"));

    // Enabled checks the input.
    assertEquals(false, page.isEnabled("text=Click me"));
    assertEquals(true, page.isDisabled("text=Click me"));
  }

  @PlaywrightTest
  @DisplayName("RE-002 should wait for enclosing disabled button")
  void shouldWaitForEnclosingDisabledButton(Page page) throws Exception {
    page.setContent("<button><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    // Java: the wait comes after the evaluate; it stays satisfied.
    span.evaluate("span => span.parentElement.disabled = true");
    span.waitForElementState(ElementState.DISABLED);
  }

  @PlaywrightTest
  @DisplayName("RE-003 should wait for enclosing button with a disabled fieldset")
  void shouldWaitForEnclosingButtonWithADisabledFieldset(Page page) throws Exception {
    page.setContent("<fieldset disabled=true><button><span>Target</span></button></div>");
    ElementHandle span = page.querySelector("text=Target");
    // Java: the wait comes after the evaluate; it stays satisfied.
    span.evaluate("span => span.parentElement.parentElement.disabled = false");
    span.waitForElementState(ElementState.ENABLED);
  }

  @PlaywrightTest
  @DisplayName("RE-004 should wait for enclosing enabled button")
  void shouldWaitForEnclosingEnabledButton(Page page, TestServer server) throws Exception {
    page.setContent("<button disabled><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    // Java: the wait comes after the evaluate; it stays satisfied.
    span.evaluate("span => span.parentElement.disabled = false");
    span.waitForElementState(ElementState.ENABLED);
  }

  @PlaywrightTest
  @DisplayName("RE-005 should check the box outside shadow dom label")
  void shouldCheckTheBoxOutsideShadowDomLabel(Page page) {
    page.setContent("<div></div>");
    page.evalOnSelector("div", "div => {\n" +
      "  const root = div.attachShadow({ mode: 'open' });\n" +
      "  const label = document.createElement('label');\n" +
      "  label.setAttribute('for', 'target');\n" +
      "  label.textContent = 'Click me';\n" +
      "  root.appendChild(label);\n" +
      "  const input = document.createElement('input');\n" +
      "  input.setAttribute('type', 'checkbox');\n" +
      "  input.setAttribute('id', 'target');\n" +
      "  root.appendChild(input);\n" +
      "}");
    page.check("label");
    assertEquals(true, page.evalOnSelector("input", "input => input.checked"));
  }

  @PlaywrightTest
  @DisplayName("RE-006 setInputFiles should work with label")
  void setInputFilesShouldWorkWithLabel(Page page) {
    page.setContent("<label for=target>Choose a file</label><input id=target type=file>");
    page.setInputFiles("text=Choose a file", asset("file-to-upload.txt"));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files?.[0].name"));
  }

  private static class Options {
    final boolean disabled;
    final boolean hidden;
    final boolean readonly;

    Options(boolean disabled, boolean hidden, boolean readonly) {
      this.disabled = disabled;
      this.hidden = hidden;
      this.readonly = readonly;
    }
  }

  private static Options disabled() {
    return new Options(true, false, false);
  }

  private static Options hidden() {
    return new Options(false, true, false);
  }

  private static Options readonly() {
    return new Options(false, false, true);
  }

  private static String optionsToAttributes(Options options) {
    return " " + (options != null && options.disabled ? "disabled" : "") + " " + (options != null && options.hidden ? "hidden" : "") + " " + (options != null && options.readonly ? "readonly" : "") + " ";
  }

  private static String domInLabel(String dom, Options options) {
    return "<label " + optionsToAttributes(options) + ">Text " + dom + "</label>";
  }

  private static String domInLabel(String dom) {
    return domInLabel(dom, null);
  }

  private static String domLabelFor(String dom, Options options) {
    return "<label " + optionsToAttributes(options) + " for=\"target\"><h1>Text</h1></label>" + dom;
  }

  private static String domLabelFor(String dom) {
    return domLabelFor(dom, null);
  }

  private static String domStandalone(String dom) {
    return dom;
  }

  private static String domInButton(String dom, Options options) {
    return "<button " + optionsToAttributes(options) + ">Button " + dom + "</button>";
  }

  private static String domInButton(String dom) {
    return domInButton(dom, null);
  }

  private static String domInLink(String dom, Options options) {
    return "<button " + optionsToAttributes(options) + ">Button " + dom + "</button>";
  }

  private static String domInLink(String dom) {
    return domInLink(dom, null);
  }

  @PlaywrightTest
  @DisplayName("RE-007 enabled/disabled retargeting")
  void enabledDisabledRetargeting(Page page) {
    Object[][] cases = {
      {domInLabel("<input id=target>"), true, "label"},
      {domLabelFor("<input id=target>"), true, "label"},
      {domStandalone("<input id=target>"), true, "input"},
      {domInButton("<input id=target>"), true, "input"},
      {domInLink("<input id=target>"), true, "input"},
      {domInButton("<input id=target>", disabled()), true, "input"},
      {domInLabel("<li role=menuitem id=target aria-disabled=false></li>"), true, "li"},

      {domInLabel("<input id=target disabled>"), false, "label"},
      {domLabelFor("<input id=target disabled>"), false, "label"},
      {domStandalone("<input id=target disabled>"), false, "input"},
      {domInButton("<input id=target disabled>"), false, "input"},
      {domInLink("<input id=target disabled>"), false, "input"},
      {domInButton("<input id=target disabled>", disabled()), false, "input"},
      {domInLabel("<li role=menuitem id=target aria-disabled=true></li>"), false, "li"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      boolean enabled = (Boolean) c[1];
      String locator = (String) c[2];
      // it.step(`"${locator}" in "${dom}" should be enabled=${enabled}`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      ElementHandle handle = page.querySelector(locator);
      assertEquals(enabled, target.isEnabled());
      assertEquals(!enabled, target.isDisabled());
      if (enabled) {
        assertThat(target).isEnabled();
        assertThat(target).not().isDisabled();
        handle.waitForElementState(ElementState.ENABLED);
      } else {
        assertThat(target).not().isEnabled();
        assertThat(target).isDisabled();
        handle.waitForElementState(ElementState.DISABLED);
      }
    }
  }

  @PlaywrightTest
  @DisplayName("RE-008 visible/hidden retargeting")
  void visibleHiddenRetargeting(Page page) {
    Object[][] cases = {
      {domInLabel("<span id=target>content</span>"), true, "label"},
      {domInLabel("<span id=target hidden>content</span>"), true, "label"},
      {domLabelFor("<span id=target>content</span>"), true, "label"},
      {domLabelFor("<span id=target hidden>content</span>"), true, "label"},
      {domStandalone("<span id=target>content</span>"), true, "span"},
      {domInButton("<span id=target>content</span>"), true, "span"},
      {domInLink("<span id=target>content</span>"), true, "span"},

      {domInLabel("<span id=target>content</span>", hidden()), false, "label"},
      {domLabelFor("<span id=target>content</span>", hidden()), false, "label"},
      {domStandalone("<span id=target hidden>content</span>"), false, "span"},
      {domInButton("<span id=target hidden>content</span>"), false, "span"},
      {domInButton("<span id=target>content</span>", hidden()), false, "span"},
      {domInLink("<span id=target hidden>content</span>"), false, "span"},
      {domInLink("<span id=target>content</span>", hidden()), false, "span"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      boolean visible = (Boolean) c[1];
      String locator = (String) c[2];
      // it.step(`"${locator}" in "${dom}" should be visible=${visible}`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      ElementHandle handle = page.querySelector(locator);
      assertEquals(visible, target.isVisible());
      assertEquals(!visible, target.isHidden());
      if (visible) {
        assertThat(target).isVisible();
        assertThat(target).not().isHidden();
        handle.waitForElementState(ElementState.VISIBLE);
      } else {
        assertThat(target).not().isVisible();
        assertThat(target).isHidden();
        handle.waitForElementState(ElementState.HIDDEN);
      }
    }
  }

  @PlaywrightTest
  @DisplayName("RE-009 editable retargeting")
  void editableRetargeting(Page page) {
    Object[][] cases = {
      {domInLabel("<input id=target>"), true, "label"},
      {domLabelFor("<input id=target>"), true, "label"},
      {domStandalone("<input id=target>"), true, "input"},
      {domInButton("<input id=target>"), true, "input"},
      {domInLink("<input id=target>"), true, "input"},
      {domInButton("<input id=target>", readonly()), true, "input"},

      {domInLabel("<input id=target readonly>"), false, "label"},
      {domLabelFor("<input id=target readonly>"), false, "label"},
      {domStandalone("<input id=target readonly>"), false, "input"},
      {domInButton("<input id=target readonly>"), false, "input"},
      {domInLink("<input id=target readonly>"), false, "input"},
      {domInButton("<input id=target readonly>", readonly()), false, "input"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      boolean editable = (Boolean) c[1];
      String locator = (String) c[2];
      // it.step(`"${locator}" in "${dom}" should be editable=${editable}`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      ElementHandle handle = page.querySelector(locator);
      assertEquals(editable, target.isEditable());
      if (editable) {
        assertThat(target).isEditable();
        handle.waitForElementState(ElementState.EDITABLE);
      } else {
        assertThat(target).not().isEditable();
      }
    }
  }

  @PlaywrightTest
  @DisplayName("RE-010 input value retargeting")
  void inputValueRetargeting(Page page) {
    Object[][] cases = {
      {domInLabel("<input id=target>"), "label"},
      {domLabelFor("<input id=target>"), "label"},
      {domStandalone("<input id=target>"), "input"},
      {domInButton("<input id=target>"), "input"},
      {domInLink("<input id=target>"), "input"},
      {domInButton("<input id=target>"), "input"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      String locator = (String) c[1];
      // it.step(`"${locator}" in "${dom}" input value`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      ElementHandle handle = page.querySelector(locator);

      assertEquals("", target.inputValue());
      assertEquals("", handle.inputValue());
      assertThat(target).hasValue("");

      target.fill("foo");
      assertEquals("foo", target.inputValue());
      assertEquals("foo", handle.inputValue());
      assertThat(target).hasValue("foo");

      page.evalOnSelector("#target", "input => input.value = 'bar'");
      assertEquals("bar", target.inputValue());
      assertEquals("bar", handle.inputValue());
      assertThat(target).hasValue("bar");

      target.selectText();
      assertEquals("bar", page.evaluate("() => window.getSelection().toString()"));
    }
  }

  @PlaywrightTest
  @DisplayName("RE-011 selection retargeting")
  void selectionRetargeting(Page page) {
    Object[][] cases = {
      {domStandalone("<div contenteditable id=target>content</div>"), "div"},
      {domInButton("<div contenteditable id=target>content</div>"), "div"},
      {domInLink("<div contenteditable id=target>content</div>"), "div"},
      {domInButton("<div contenteditable id=target>content</div>"), "div"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      String locator = (String) c[1];
      // it.step(`"${locator}" in "${dom}" text selection`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      ElementHandle handle = page.querySelector(locator);

      assertEquals(true, target.isEditable());
      assertEquals(true, handle.isEditable());
      assertThat(page.locator("#target")).hasText("content");

      target.fill("foo");
      assertThat(page.locator("#target")).hasText("foo");

      target.selectText();
      assertEquals("foo", page.evaluate("() => window.getSelection().toString()"));
    }
  }

  @PlaywrightTest
  @DisplayName("RE-012 select options retargeting")
  void selectOptionsRetargeting(Page page) {
    String select = "<select id=target multiple><option value=dog selected>Dog</option><option value=cat>Cat</option></select>";
    Object[][] cases = {
      {domInLabel(select), "label"},
      {domLabelFor(select), "label"},
      {domStandalone(select), "select"},
      {domInButton(select), "select"},
      {domInLink(select), "select"},
      {domInButton(select), "select"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      String locator = (String) c[1];
      // it.step(`"${locator}" in "${dom}" select option`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      ElementHandle handle = page.querySelector(locator);

      assertEquals("dog", target.inputValue());
      assertEquals("dog", handle.inputValue());
      assertThat(target).hasValue("dog");
      assertThat(target).hasValues(new String[]{"dog"});

      target.selectOption("cat");
      assertEquals("cat", target.inputValue());
      assertEquals("cat", handle.inputValue());
      assertThat(target).hasValue("cat");
      assertThat(target).hasValues(new String[]{"cat"});
    }
  }

  @PlaywrightTest
  @DisplayName("RE-013 direct actions retargeting")
  void directActionsRetargeting(Page page) {
    Object[][] cases = {
      {domInLabel("<div>content</div><input id=target value=oh>"), "div"},
      {domLabelFor("<div>content</div><input id=target value=oh>"), "div"},
      {domStandalone("<div>content</div>"), "div"},
      {domInButton("<div>content</div>"), "div"},
      {domInLink("<div>content</div>"), "div"},
      {domInButton("<div>content</div>"), "div"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      String locator = (String) c[1];
      // it.step(`"${locator}" in "${dom}" direct actions`)
      page.setContent(dom);
      Locator target = page.locator(locator);

      assertEquals("content", target.innerText());
      assertEquals("content", target.textContent());
      assertThat(target).hasText("content");
      assertThat(target).containsText("content");
      assertThat(target).not().isFocused();
      assertThat(target).hasCount(1);

      page.evalOnSelector("div", "div => div.foo = 'bar'");
      assertThat(target).hasJSProperty("foo", "bar");

      page.evalOnSelector("div", "div => div.classList.add('cls')");
      assertThat(target).hasClass("cls");

      page.evalOnSelector("div", "div => div.id = 'myid'");
      assertThat(target).hasId("myid");
      assertThat(target).hasAttribute("id", "myid");
      assertEquals("myid", target.getAttribute("id"));
    }
  }

  @PlaywrightTest
  @DisplayName("RE-014 check retargeting")
  void checkRetargeting(Page page) {
    Object[][] cases = {
      {domInLabel("<input type=checkbox id=target>"), "label"},
      {domLabelFor("<input type=checkbox id=target>"), "label"},
      {domStandalone("<input type=checkbox id=target>"), "input"},
      {domInButton("<input type=checkbox id=target>"), "input"},
      {domInLink("<input type=checkbox id=target>"), "input"},
      {domInButton("<input type=checkbox id=target>"), "input"},
    };
    for (Object[] c : cases) {
      String dom = (String) c[0];
      String locator = (String) c[1];
      // it.step(`"${locator}" in "${dom}" check`)
      page.setContent(dom);
      Locator target = page.locator(locator);
      assertEquals(false, target.isChecked());
      assertThat(target).not().isChecked();
      assertThat(target).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false));

      page.evalOnSelector("input", "input => input.checked = true");
      assertEquals(true, target.isChecked());
      assertThat(target).isChecked();
      assertThat(target).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(true));

      target.uncheck();
      assertEquals(false, page.evalOnSelector("input", "input => input.checked"));

      target.check();
      assertEquals(true, page.evalOnSelector("input", "input => input.checked"));

      target.setChecked(false);
      assertEquals(false, page.evalOnSelector("input", "input => input.checked"));
    }
  }

  @PlaywrightTest
  @DisplayName("RE-015 should not retarget anchor into parent label")
  void shouldNotRetargetAnchorIntoParentLabel(Page page) {
    page.setContent("\n" +
      "    <label disabled>Text<a href='#' onclick='window.__clicked=1'>Target</a></label>\n" +
      "  ");
    page.locator("a").click();
    assertEquals(1, page.evaluate("window.__clicked"));

    page.setContent("\n" +
      "    <input type=\"radio\" id=\"input-id\" checked disabled />\n" +
      "    <label for=\"input-id\">Text<a href='#' onclick='window.__clicked=2'>Target</a></label>\n" +
      "  ");
    page.locator("a").click();
    assertEquals(2, page.evaluate("window.__clicked"));
  }
}
