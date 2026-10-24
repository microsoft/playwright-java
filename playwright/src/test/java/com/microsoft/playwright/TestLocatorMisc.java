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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorMisc{
  @PlaywrightTest
  void shouldCheckTheBoxUsingSetChecked(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    Locator input = page.locator("input");
    input.setChecked(true);
    assertEquals(true, page.evaluate("checkbox.checked"));
    input.setChecked(false);
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  void shouldWaitFor(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("div", "div => setTimeout(() => div.innerHTML = '<span>target</span>', 500)");
    locator.waitFor();
    assertTrue(locator.textContent().contains("target"));
  }

  @PlaywrightTest
  void shouldWaitForHidden(Page page) {
    page.setContent("<div><span>target</span></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("div", "div => setTimeout(() => div.innerHTML = '', 500)");
    locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
  }

  @PlaywrightTest
  void locatorsHasDoesNotEncodeUnicode(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    Locator[] locators = new Locator[]{
      page.locator("button", new Page.LocatorOptions().setHasText("Драматург")),
      page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("Драматург"))),
      page.locator("button", new Page.LocatorOptions().setHas(page.locator("text=Драматург")))
    };
    for (Locator locator: locators) {
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
        locator.click(new Locator.ClickOptions().setTimeout(100));
      });
      assertTrue(e.getMessage().contains("Драматург"), e.getMessage());
    }
  }

  @PlaywrightTest
  void shouldClearInput(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    Locator handle = page.locator("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
    handle.clear();
    assertEquals("", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  void shouldFocusAndBlurAButton(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    Locator button = page.locator("button");
    assertEquals(false, button.evaluate("button => document.activeElement === button"));

    boolean[] focused = {false};
    boolean[] blurred = {false};
    page.exposeFunction("focusEvent", e -> focused[0] = true);
    page.exposeFunction("blurEvent", e -> blurred[0] = true);
    button.evaluate("button => {\n" +
      "    button.addEventListener('focus', window['focusEvent']);\n" +
      "    button.addEventListener('blur', window['blurEvent']);\n" +
      "  }");

    button.focus();
    assertTrue(focused[0]);
    assertFalse(blurred[0]);
    assertEquals(true, button.evaluate("button => document.activeElement === button"));

    button.blur();
    assertTrue(focused[0]);
    assertTrue(blurred[0]);
    assertEquals(false, button.evaluate("button => document.activeElement === button"));
  }
  @PlaywrightTest
  void LocatorLocatorAndFrameLocatorLocatorShouldAcceptLocator(Page page) {
    page.setContent("<div><input value=outer></div>\n" +
      "    <iframe srcdoc=\"<div><input value=inner></div>\"></iframe>\n");
    Locator inputLocator = page.locator("input");
    assertEquals("outer", inputLocator.inputValue());
    assertEquals("outer", page.locator("div").locator(inputLocator).inputValue());
    assertEquals("inner", page.frameLocator("iframe").locator(inputLocator).inputValue());
    assertEquals("inner", page.frameLocator("iframe").locator("div").locator(inputLocator).inputValue());

    Locator divLocator = page.locator("div");
    assertEquals("outer", divLocator.locator("input").inputValue());
    assertEquals("inner", page.frameLocator("iframe").locator(divLocator).locator("input").inputValue());
  }

  @PlaywrightTest
  void shouldPressSequentially(Page page) {
    page.setContent("<input type='text' />");
    page.locator("input").pressSequentially("hello");
    assertEquals("hello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  public void shouldSupportFilterVisible(Page page) {
    page.setContent("<div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data0</div>\n" +
      "  <div class=\"item\">visible data1</div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data1</div>\n" +
      "  <div class=\"item\">visible data2</div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data2</div>\n" +
      "  <div class=\"item\">visible data3</div>\n" +
      "</div>");
    Locator locator = page.locator(".item").filter(new Locator.FilterOptions().setVisible(true)).nth(1);
    assertThat(locator).hasText("visible data2");
    assertThat(page.locator(".item").filter(new Locator.FilterOptions().setVisible(true)).getByText("data3")).hasText("visible data3");
    assertThat(page.locator(".item").filter(new Locator.FilterOptions().setVisible(false)).getByText("data1")).hasText("Hidden data1");
  }

  @PlaywrightTest
  void waitForFunctionShouldWaitForAnAttributeToAppear(Page page) {
    page.setContent("<button id=toggle>Menu</button>");
    page.evaluate("() => setTimeout(() => document.querySelector('#toggle').setAttribute('aria-expanded', 'true'), 500)");
    page.locator("#toggle").waitForFunction("element => element.hasAttribute('aria-expanded')");
  }

  @PlaywrightTest
  void waitForFunctionShouldReturnImmediatelyWhenAlreadyTruthy(Page page) {
    page.setContent("<div id=target>yes</div>");
    page.locator("#target").waitForFunction("element => element.textContent === 'yes'");
  }

  @PlaywrightTest
  void waitForFunctionShouldAcceptElementHandleArguments(Page page) {
    page.setContent("<div id=a></div><div id=b>value</div>");
    ElementHandle handle = page.querySelector("#b");
    page.locator("#a").waitForFunction("(element, other) => other.textContent === 'value'", handle);
  }

  @PlaywrightTest
  void waitForFunctionShouldThrowWhenPredicateThrows(Page page) {
    page.setContent("<div id=target>no</div>");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.locator("#target").waitForFunction("() => { throw new Error('oh my'); }"));
    assertTrue(e.getMessage().contains("oh my"), e.getMessage());
  }

  @PlaywrightTest
  void waitForFunctionShouldThrowOnStrictModeViolation(Page page) {
    page.setContent("<div class=x>1</div><div class=x>2</div>");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.locator("div.x").waitForFunction("() => true"));
    assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
  }

  @PlaywrightTest
  void waitForFunctionShouldRespectTimeout(Page page) {
    page.setContent("<div id=target>no</div>");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.locator("#target").waitForFunction("element => element.textContent === 'yes'", null,
        new Locator.WaitForFunctionOptions().setTimeout(500)));
    assertTrue(e.getMessage().contains("Timeout 500ms exceeded"), e.getMessage());
  }

  @PlaywrightTest
  public void shouldSupportVisible(Page page) {
    page.setContent("<div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data0</div>\n" +
      "  <div class=\"item\">visible data1</div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data1</div>\n" +
      "  <div class=\"item\">visible data2</div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data2</div>\n" +
      "  <div class=\"item\">visible data3</div>\n" +
      "</div>");
    Locator locator = page.locator(".item").visible().nth(1);
    assertThat(locator).hasText("visible data2");
    assertThat(page.locator(".item").visible().getByText("data3")).hasText("visible data3");
    assertThat(page.locator(".item").visible()).hasCount(3);
  }
}
