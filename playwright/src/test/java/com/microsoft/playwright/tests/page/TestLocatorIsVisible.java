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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-is-visible.spec.ts. */
public class TestLocatorIsVisible {
  @PlaywrightTest
  @DisplayName("LO-115 isVisible and isHidden should work")
  void isVisibleAndIsHiddenShouldWork(Page page) {
    page.setContent("<div>Hi</div><span></span>");

    Locator div = page.locator("div");
    assertEquals(true, div.isVisible());
    assertEquals(false, div.isHidden());
    assertEquals(true, page.isVisible("div"));
    assertEquals(false, page.isHidden("div"));

    Locator span = page.locator("span");
    assertEquals(false, span.isVisible());
    assertEquals(true, span.isHidden());
    assertEquals(false, page.isVisible("span"));
    assertEquals(true, page.isHidden("span"));

    assertEquals(false, page.isVisible("no-such-element"));
    assertEquals(true, page.isHidden("no-such-element"));
  }

  @PlaywrightTest
  @DisplayName("LO-116 isVisible should be true for opacity:0")
  void isVisibleShouldBeTrueForOpacity0(Page page) {
    page.setContent("<div style=\"opacity:0\">Hi</div>");
    assertThat(page.locator("div")).isVisible();
  }

  @PlaywrightTest
  @DisplayName("LO-117 isVisible should be true for element outside view")
  void isVisibleShouldBeTrueForElementOutsideView(Page page) {
    page.setContent("<div style=\"position: absolute; left: -1000px\">Hi</div>");
    assertThat(page.locator("div")).isVisible();
  }

  @PlaywrightTest
  @DisplayName("LO-118 isVisible and isHidden should work with details")
  void isVisibleAndIsHiddenShouldWorkWithDetails(Page page) {
    page.setContent("<details>\n" +
      "    <summary>click to open</summary>\n" +
      "      <ul>\n" +
      "        <li>hidden item 1</li>\n" +
      "        <li>hidden item 2</li>\n" +
      "        <li>hidden item 3</li>\n" +
      "      </ul\n" +
      "  </details>");

    assertThat(page.locator("ul")).isHidden();
  }

  @PlaywrightTest
  @DisplayName("LO-119 isVisible and isHidden should work with nested details")
  void isVisibleAndIsHiddenShouldWorkWithNestedDetails(Page page) {
    page.setContent("<details>\n" +
      "    <summary>outer</summary>\n" +
      "    <details open>\n" +
      "      <summary>inner</summary>\n" +
      "      <button>hidden button</button>\n" +
      "    </details>\n" +
      "  </details>");

    assertThat(page.locator("summary", new Page.LocatorOptions().setHasText("outer"))).isVisible();
    assertThat(page.locator("summary", new Page.LocatorOptions().setHasText("inner"))).isHidden();
    assertThat(page.locator("button")).isHidden();
    assertThat(page.getByRole(AriaRole.BUTTON)).hasCount(0);
  }

  @PlaywrightTest
  @DisplayName("LO-120 isVisible and isHidden should work with unslotted content")
  void isVisibleAndIsHiddenShouldWorkWithUnslottedContent(Page page) {
    page.setContent("\n" +
      "    <div id=unslotted><span>hidden 1</span><div><span>hidden 2</span></div></div>\n" +
      "    <div id=slotted><span>visible</span></div>\n" +
      "    <script>\n" +
      "      unslotted.attachShadow({ mode: 'open' }).innerHTML = 'nothing to see here';\n" +
      "      slotted.attachShadow({ mode: 'open' }).innerHTML = '<slot></slot>';\n" +
      "    </script>\n" +
      "  ");

    assertThat(page.locator("span", new Page.LocatorOptions().setHasText("hidden 1"))).isHidden();
    assertThat(page.locator("span", new Page.LocatorOptions().setHasText("hidden 2"))).isHidden();
    assertThat(page.locator("span", new Page.LocatorOptions().setHasText("visible"))).isVisible();
  }

  @PlaywrightTest
  @DisplayName("LO-121 isVisible and isHidden should work with list box options")
  void isVisibleAndIsHiddenShouldWorkWithListBoxOptions(Page page) {
    page.setContent("\n" +
      "    <select multiple>\n" +
      "      <option>visible 1</option>\n" +
      "      <optgroup label=\"group\"><option>visible 2</option></optgroup>\n" +
      "      <option hidden>hidden 1</option>\n" +
      "      <optgroup label=\"hidden group\" style=\"display:none\"><option>hidden 2</option></optgroup>\n" +
      "    </select>\n" +
      "    <select size=3><option>visible 3</option></select>\n" +
      "    <select multiple style=\"display:none\"><option>hidden 3</option></select>\n" +
      "  ");

    for (String name : new String[]{"visible 1", "visible 2", "visible 3"}) {
      assertThat(page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(name).setExact(true))).isVisible();
    }
    for (String text : new String[]{"hidden 1", "hidden 2", "hidden 3"}) {
      assertThat(page.locator("option", new Page.LocatorOptions().setHasText(text))).isHidden();
    }
  }

  @PlaywrightTest
  @DisplayName("LO-122 isVisible inside a button")
  void isVisibleInsideAButton(Page page) {
    page.setContent("<button><span></span>a button</button>");
    Locator span = page.locator("span");
    assertEquals(false, span.isVisible());
    assertEquals(true, span.isHidden());
    assertEquals(false, page.isVisible("span"));
    assertEquals(true, page.isHidden("span"));
    assertThat(span).not().isVisible();
    assertThat(span).isHidden();
    span.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    page.locator("button").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
  }

  @PlaywrightTest
  @DisplayName("LO-123 isVisible inside a role=button")
  void isVisibleInsideARoleButton(Page page) {
    page.setContent("<div role=button><span></span>a button</div>");
    Locator span = page.locator("span");
    assertEquals(false, span.isVisible());
    assertEquals(true, span.isHidden());
    assertEquals(false, page.isVisible("span"));
    assertEquals(true, page.isHidden("span"));
    assertThat(span).not().isVisible();
    assertThat(span).isHidden();
    span.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    page.locator("[role=button]").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
  }

  @PlaywrightTest
  @DisplayName("LO-124 isVisible during navigation should not throw")
  void isVisibleDuringNavigationShouldNotThrow(Page page, TestServer server) {
    for (int i = 0; i < 20; i++) {
      try {
        page.setContent("\n" +
          "      <script>\n" +
          "        setTimeout(() => {\n" +
          "          window.location.href = \"" + server.EMPTY_PAGE + "\";\n" +
          "        }, Math.random(50));\n" +
          "      </script>\n" +
          "    ");
      } catch (PlaywrightException e) {
        // Avoid page.setContent throwing because of scheduled navigation.
      }
      assertEquals(false, page.locator("div").isVisible());
    }
  }

  @PlaywrightTest
  @DisplayName("LO-125 isVisible with invalid selector should throw")
  void isVisibleWithInvalidSelectorShouldThrow(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("hey=what").isVisible());
    assertTrue(error.getMessage().contains("Unknown engine \"hey\" while parsing selector hey=what"));
  }
}
