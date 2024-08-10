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

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorAssertions2 extends TestBase {
  @Test
  void isAttachedDefault() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isAttached();
  }

  @Test
  void isAttachedWithHiddenElement() {
    page.setContent("<button style='display:none'>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isAttached();
  }

  @Test
  void isAttachedWithNot() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isAttached();
  }

  @Test
  void isAttachedWithAttachedTrue() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(true));
  }

  @Test
  void isAttachedWithAttachedFalse() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("input");
    assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(false));
  }

  @Test
  void isAttachedWithNotAndAttachedFalse() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).not().isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(false));
  }

  @Test
  void isAttachedEventually() {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
      page.evalOnSelector("div", "div => setTimeout(() => {\n" +
        "      div.innerHTML = '<span>Hello</span>'\n" +
        "    }, 100)");
    assertThat(locator).isAttached();
  }

  @Test
  void isAttachedEventuallyWithNot() {
    page.setContent("<div><span>Hello</span></div>");
    Locator locator = page.locator("span");
      page.evalOnSelector("div",  "div => setTimeout(() => {\n" +
        "      div.textContent = '';\n" +
        "    }, 0)");
    assertThat(locator).not().isAttached();
  }

  @Test
  void isAttachedFail() {
    page.setContent("<button>Hello</button>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1000)));
    assertFalse(error.getMessage().contains("locator resolved to"), error.getMessage());
  }

  @Test
  void isAttachedFailWithNot() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).not().isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("locator resolved to <input/>"), error.getMessage());
  }

  @Test
  void isAttachedWithImpossibleTimeout() {
    page.setContent("<div id=node>Text content</div>");
    assertThat(page.locator("#node")).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1));
  }

  @Test
  void isAttachedWithImpossibleTimeoutNot() {
    page.setContent("<div id=node>Text content</div>");
    assertThat(page.locator("no-such-thing")).not().isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1));
  }

  @Test
  public void toHaveAccessibleName() {
    page.setContent("<div role=\"button\" aria-label=\"Hello\"></div>");

    assertThat(page.locator("div")).hasAccessibleName("Hello");
    assertThat(page.locator("div")).not().hasAccessibleName("hello");
    assertThat(page.locator("div")).hasAccessibleName("hello", new LocatorAssertions.HasAccessibleNameOptions().setIgnoreCase(true));
    assertThat(page.locator("div")).hasAccessibleName(Pattern.compile("ell\\w"));
    assertThat(page.locator("div")).not().hasAccessibleName(Pattern.compile("hello"));
    assertThat(page.locator("div")).hasAccessibleName(Pattern.compile("hello"), new LocatorAssertions.HasAccessibleNameOptions().setIgnoreCase(true));
  }

  @Test
  public void toHaveAccessibleDescription() {
    page.setContent("<div role=\"button\" aria-description=\"Hello\"></div>");

    assertThat(page.locator("div")).hasAccessibleDescription("Hello");
    assertThat(page.locator("div")).not().hasAccessibleDescription("hello");
    assertThat(page.locator("div")).hasAccessibleDescription("hello", new LocatorAssertions.HasAccessibleDescriptionOptions().setIgnoreCase(true));
    assertThat(page.locator("div")).hasAccessibleDescription(Pattern.compile("ell\\w"));
    assertThat(page.locator("div")).not().hasAccessibleDescription(Pattern.compile("hello"));
    assertThat(page.locator("div")).hasAccessibleDescription(Pattern.compile("hello"), new LocatorAssertions.HasAccessibleDescriptionOptions().setIgnoreCase(true));
  }

  @Test
  public void toHaveRole() {
    page.setContent("<div role=\"button\">Button!</div>");

    assertThat(page.locator("div")).hasRole(AriaRole.BUTTON);
    assertThat(page.locator("div")).not().hasRole(AriaRole.CHECKBOX);
  }
}
