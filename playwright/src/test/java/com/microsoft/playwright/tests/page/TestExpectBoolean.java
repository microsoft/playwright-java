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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.opentest4j.AssertionFailedError;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/expect-boolean.spec.ts. */
public class TestExpectBoolean {
  @Nested
  class ToBeChecked {
    @PlaywrightTest
    @DisplayName("EX-001 default")
    void testDefault(Page page) {
      page.setContent("<input type=checkbox checked></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isChecked();
    }

    @PlaywrightTest
    @DisplayName("EX-002 with checked:true")
    void withCheckedTrue(Page page) {
      page.setContent("<input type=checkbox checked></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(true));
    }

    @PlaywrightTest
    @DisplayName("EX-003 with checked:false")
    void withCheckedFalse(Page page) {
      page.setContent("<input type=checkbox checked></input>");
      Locator locator = page.locator("input");
      assertThat(locator).not().isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false));
    }

    @PlaywrightTest
    @DisplayName("EX-004 with indeterminate:true")
    void withIndeterminateTrue(Page page) {
      page.setContent("<input type=checkbox></input>");
      page.locator("input").evaluate("e => e.indeterminate = true");
      Locator locator = page.locator("input");
      assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setIndeterminate(true));
    }

    @PlaywrightTest
    @DisplayName("EX-005 with indeterminate:true and checked")
    void withIndeterminateTrueAndChecked(Page page) {
      page.setContent("<input type=checkbox></input>");
      page.locator("input").evaluate("e => e.indeterminate = true");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setIndeterminate(true).setChecked(false)));
      assertTrue(error.getMessage().contains("Can't assert indeterminate and checked at the same time"));
    }

    @PlaywrightTest
    @DisplayName("EX-006 fail")
    @Fixme("java: the message is the Java assertion format, 'Locator expected to be\\nExpected: checked\\nReceived: unchecked\\n\\nCall log:\\n  - Assert \"isChecked\" locator(\"input\") with timeout 1000ms ...', not the 'expect(locator).toBeChecked() failed' message of the Node.js client")
    void fail(Page page) {
      page.setContent("<input type=checkbox></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toBeChecked() failed\n" +
        "\n" +
        "Locator:  locator('input')\n" +
        "Expected: checked\n" +
        "Received: unchecked\n" +
        "Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toBeChecked\" locator('input') with timeout 1000ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-007 with not")
    void withNot(Page page) {
      page.setContent("<input type=checkbox></input>");
      Locator locator = page.locator("input");
      assertThat(locator).not().isChecked();
    }

    @PlaywrightTest
    @DisplayName("EX-008 with not and checked:false")
    void withNotAndCheckedFalse(Page page) {
      page.setContent("<input type=checkbox></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false));
    }

    @PlaywrightTest
    @DisplayName("EX-009 fail with not")
    @Fixme("java: the message is the Java assertion format, 'Locator expected to be\\nExpected: checked\\nReceived: unchecked\\n\\nCall log:\\n  - Assert \"isChecked\" locator(\"input\") with timeout 1000ms ...', not the 'expect(locator).not.toBeChecked() failed' message of the Node.js client")
    void failWithNot(Page page) {
      page.setContent("<input type=checkbox checked></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toBeChecked() failed\n" +
        "\n" +
        "Locator:  locator('input')\n" +
        "Expected: not checked\n" +
        "Received: checked\n" +
        "Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"not toBeChecked\" locator('input') with timeout 1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("locator resolved to <input checked type=\"checkbox\"/>"));
    }

    @PlaywrightTest
    @DisplayName("EX-010 fail with checked:false")
    @Fixme("java: the message is the Java assertion format, 'Locator expected to be\\nExpected: checked\\nReceived: unchecked\\n\\nCall log:\\n  - Assert \"isChecked\" locator(\"input\") with timeout 1000ms ...', not the 'expect(locator).toBeChecked({ checked: false }) failed' message of the Node.js client")
    void failWithCheckedFalse(Page page) {
      page.setContent("<input type=checkbox checked></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false).setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toBeChecked({ checked: false }) failed\n" +
        "\n" +
        "Locator:  locator('input')\n" +
        "Expected: unchecked\n" +
        "Received: checked\n" +
        "Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toBeChecked\" locator('input') with timeout 1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("locator resolved to <input checked type=\"checkbox\"/>"));
    }

    @PlaywrightTest
    @DisplayName("EX-011 fail with indeterminate: true")
    @Fixme("java: the message is the Java assertion format, 'Locator expected to be\\nExpected: checked\\nReceived: unchecked\\n\\nCall log:\\n  - Assert \"isChecked\" locator(\"input\") with timeout 1000ms ...', not the 'expect(locator).toBeChecked({ indeterminate: true }) failed' message of the Node.js client")
    void failWithIndeterminateTrue(Page page) {
      page.setContent("<input type=checkbox></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setIndeterminate(true).setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toBeChecked({ indeterminate: true }) failed\n" +
        "\n" +
        "Locator:  locator('input')\n" +
        "Expected: indeterminate\n" +
        "Received: unchecked\n" +
        "Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toBeChecked\" locator('input') with timeout 1000ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-012 fail missing")
    @Fixme("java: the message is the Java assertion format, 'Locator expected to be\\nExpected: checked\\nReceived: unchecked\\n\\nCall log:\\n  - Assert \"isChecked\" locator(\"input\") with timeout 1000ms ...', not the 'expect(locator).not.toBeChecked() failed' message of the Node.js client")
    void failMissing(Page page) {
      page.setContent("<div>no inputs here</div>");
      Locator locator2 = page.locator("input2");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator2).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toBeChecked() failed\n" +
        "\n" +
        "Locator: locator('input2')\n" +
        "Expected: not checked\n" +
        "Timeout: 1000ms\n" +
        "Error: element(s) not found\n" +
        "\n" +
        "Call log:\n" +
        "  - Expect \"not toBeChecked\" locator('input2') with timeout 1000ms\n" +
        "  - waiting for locator('input2')\n"));
    }

    @PlaywrightTest
    @DisplayName("EX-013 with role")
    void withRole(Page page) {
      for (String role : new String[]{"checkbox", "menuitemcheckbox", "option", "radio", "switch", "menuitemradio", "treeitem"}) {
        // it.step(`role=${role}`)
        page.setContent("<div role=" + role + " aria-checked=true>I am checked</div>");
        Locator locator = page.locator("div");
        assertThat(locator).isChecked();
      }
    }

    @PlaywrightTest
    @DisplayName("EX-014 friendly log")
    void friendlyLog(Page page) {
      page.setContent("<input type=checkbox></input>");
      AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
        () -> assertThat(page.locator("input")).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000)));
      String message1 = error1.getMessage();
      assertTrue(message1.contains("unexpected value \"unchecked\""));

      page.setContent("<input type=checkbox checked></input>");
      AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
        () -> assertThat(page.locator("input")).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false).setTimeout(1000)));
      String message2 = error2.getMessage();
      assertTrue(message2.contains("unexpected value \"checked\""));
    }

    @PlaywrightTest
    @DisplayName("EX-015 with impossible timeout")
    void withImpossibleTimeout(Page page) {
      page.setContent("<input type=checkbox checked></input>");
      assertThat(page.locator("input")).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1));
    }

    @PlaywrightTest
    @DisplayName("EX-016 with impossible timeout .not")
    void withImpossibleTimeoutNot(Page page) {
      page.setContent("<input type=checkbox></input>");
      assertThat(page.locator("input")).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1));
    }
  }

  @Nested
  class ToBeEditable {
    @PlaywrightTest
    @DisplayName("EX-017 default")
    void testDefault(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isEditable();
    }

    @PlaywrightTest
    @DisplayName("EX-018 with not")
    void withNot(Page page) {
      page.setContent("<input readonly></input>");
      Locator locator = page.locator("input");
      assertThat(locator).not().isEditable();
    }

    @PlaywrightTest
    @DisplayName("EX-019 with editable:true")
    void withEditableTrue(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(true));
    }

    @PlaywrightTest
    @DisplayName("EX-020 with editable:false")
    void withEditableFalse(Page page) {
      page.setContent("<input readonly></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false));
    }

    @PlaywrightTest
    @DisplayName("EX-021 with not and editable:false")
    void withNotAndEditableFalse(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      assertThat(locator).not().isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false));
    }

    @PlaywrightTest
    @DisplayName("EX-022 throws")
    void throwsTest(Page page) {
      page.setContent("<button>");
      Locator locator = page.locator("button");
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(locator).isEditable());
      assertTrue(error.getMessage().contains("Element is not an <input>, <textarea>, <select> or [contenteditable] and does not have a role allowing [aria-readonly]"));
    }
  }

  @Nested
  class ToBeEnabled {
    @PlaywrightTest
    @DisplayName("EX-023 default")
    void testDefault(Page page) {
      page.setContent("<button>Text</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isEnabled();
    }

    @PlaywrightTest
    @DisplayName("EX-024 with enabled:true")
    void withEnabledTrue(Page page) {
      page.setContent("<button>Text</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(true));
    }

    @PlaywrightTest
    @DisplayName("EX-025 with enabled:false")
    void withEnabledFalse(Page page) {
      page.setContent("<button disabled>Text</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false));
    }

    @PlaywrightTest
    @DisplayName("EX-026 failed")
    void failed(Page page) {
      page.setContent("<button disabled>Text</button>");
      Locator locator = page.locator("button");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("locator resolved to <button disabled>Text</button>"));
    }

    @PlaywrightTest
    @DisplayName("EX-027 eventually")
    void eventually(Page page) {
      page.setContent("<button disabled>Text</button>");
      Locator locator = page.locator("button");
      page.waitForTimeout(500);
      try {
        locator.evaluate("e => e.removeAttribute('disabled')");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).isEnabled();
    }

    @PlaywrightTest
    @DisplayName("EX-028 eventually with not")
    void eventuallyWithNot(Page page) {
      page.setContent("<button>Text</button>");
      Locator locator = page.locator("button");
      page.waitForTimeout(500);
      try {
        locator.evaluate("e => e.setAttribute('disabled', '')");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).not().isEnabled();
    }

    @PlaywrightTest
    @DisplayName("EX-029 with not and enabled:false")
    void withNotAndEnabledFalse(Page page) {
      page.setContent("<button>Text</button>");
      Locator locator = page.locator("button");
      assertThat(locator).not().isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false));
    }

    @PlaywrightTest
    @DisplayName("EX-030 toBeDisabled")
    void toBeDisabled(Page page) {
      page.setContent("<button disabled>Text</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isDisabled();
    }
  }

  @PlaywrightTest
  @DisplayName("EX-031 toBeEmpty input")
  void toBeEmptyInput(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEmpty();
  }

  @PlaywrightTest
  @DisplayName("EX-032 not.toBeEmpty")
  void notToBeEmpty(Page page) {
    page.setContent("<input value=text></input>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isEmpty();
  }

  @PlaywrightTest
  @DisplayName("EX-033 toBeEmpty div")
  void toBeEmptyDiv(Page page) {
    page.setContent("<div style=\"width: 50; height: 50px\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).isEmpty();
  }

  @PlaywrightTest
  @DisplayName("EX-034 toBeDisabled with value")
  void toBeDisabledWithValue(Page page) {
    page.setContent("<button disabled=\"yes\">Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isDisabled();
  }

  @PlaywrightTest
  @DisplayName("EX-035 toBeChecked with value")
  void toBeCheckedWithValue(Page page) {
    page.setContent("<input type=checkbox checked=\"yes\"></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isChecked();
  }

  @PlaywrightTest
  @DisplayName("EX-036 toBeHidden with value")
  void toBeHiddenWithValue(Page page) {
    page.setContent("<input type=checkbox hidden=\"of course\"></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isHidden();
  }

  @PlaywrightTest
  @DisplayName("EX-037 not.toBeDisabled div")
  void notToBeDisabledDiv(Page page) {
    page.setContent("<div disabled=\"yes\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).not().isDisabled();
  }

  @Nested
  class ToBeVisible {
    @PlaywrightTest
    @DisplayName("EX-038 default")
    void testDefault(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isVisible();
    }

    @PlaywrightTest
    @DisplayName("EX-039 with not")
    void withNot(Page page) {
      page.setContent("<button style=\"display: none\">hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).not().isVisible();
    }

    @PlaywrightTest
    @DisplayName("EX-040 with visible:true")
    void withVisibleTrue(Page page) {
      page.setContent("<button>hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(true));
    }

    @PlaywrightTest
    @DisplayName("EX-041 with visible:false")
    void withVisibleFalse(Page page) {
      page.setContent("<button hidden>hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false));
    }

    @PlaywrightTest
    @DisplayName("EX-042 with not and visible:false")
    void withNotAndVisibleFalse(Page page) {
      page.setContent("<button>hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false));
    }

    @PlaywrightTest
    @DisplayName("EX-043 eventually")
    void eventually(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("span");
      try {
        page.evalOnSelector("div", "div => div.innerHTML = '<span>Hello</span>'");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).isVisible();
    }

    @PlaywrightTest
    @DisplayName("EX-044 eventually with not")
    void eventuallyWithNot(Page page) {
      page.setContent("<div><span>Hello</span></div>");
      Locator locator = page.locator("span");
      try {
        page.evalOnSelector("span", "span => span.textContent = ''");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).not().isVisible();
    }

    @PlaywrightTest
    @DisplayName("EX-045 fail")
    void fail(Page page) {
      page.setContent("<button style=\"display: none\"></button>");
      Locator locator = page.locator("button");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("locator resolved to <button></button>"));
    }

    @PlaywrightTest
    @DisplayName("EX-046 fail with not")
    void failWithNot(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("locator resolved to <input/>"));
    }

    @PlaywrightTest
    @DisplayName("EX-047 with impossible timeout")
    void withImpossibleTimeout(Page page) {
      page.setContent("<div id=node>Text content</div>");
      assertThat(page.locator("#node")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1));
    }

    @PlaywrightTest
    @DisplayName("EX-048 with impossible timeout .not")
    void withImpossibleTimeoutNot(Page page) {
      page.setContent("<div id=node>Text content</div>");
      assertThat(page.locator("no-such-thing")).not().isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1));
    }

    // EX-049 with frameLocator: not ported, see upstream_not_ported.txt.

    // EX-050 with frameLocator 2: not ported, see upstream_not_ported.txt.

    // EX-051 over navigation: not ported, see upstream_not_ported.txt.
  }

  @Nested
  class ToBeHidden {
    @PlaywrightTest
    @DisplayName("EX-052 default")
    void testDefault(Page page) {
      page.setContent("<button style=\"display: none\"></button>");
      Locator locator = page.locator("button");
      assertThat(locator).isHidden();
    }

    @PlaywrightTest
    @DisplayName("EX-053 when nothing matches")
    void whenNothingMatches(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("button");
      assertThat(locator).isHidden();
    }

    @PlaywrightTest
    @DisplayName("EX-054 with not")
    void withNot(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      assertThat(locator).not().isHidden();
    }

    @PlaywrightTest
    @DisplayName("EX-055 eventually with not")
    void eventuallyWithNot(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("span");
      try {
        page.evalOnSelector("div", "div => div.innerHTML = '<span>Hello</span>'");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).not().isHidden();
    }

    @PlaywrightTest
    @DisplayName("EX-056 eventually")
    void eventually(Page page) {
      page.setContent("<div><span>Hello</span></div>");
      Locator locator = page.locator("span");
      try {
        page.evalOnSelector("span", "span => span.textContent = ''");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).isHidden();
    }

    @PlaywrightTest
    @DisplayName("EX-057 fail")
    void fail(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(3000)));
      assertTrue(error.getMessage().contains("locator resolved to <input/>"));
    }

    @PlaywrightTest
    @DisplayName("EX-058 fail with not")
    void failWithNot(Page page) {
      page.setContent("<button style=\"display: none\"></button>");
      Locator locator = page.locator("button");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(3000)));
      assertTrue(error.getMessage().contains("locator resolved to <button></button>"));
    }

    @PlaywrightTest
    @DisplayName("EX-059 fail with not when nothing matching")
    @Fixme("java: the message is 'Locator expected not to be hidden\\nError: element(s) not found\\nCall log:\\n  - Assert \"isHidden\" locator(\"button\") with timeout 1000ms ...', not the 'expect(locator).not.toBeHidden() failed' message of the Node.js client")
    void failWithNotWhenNothingMatching(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("button");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toBeHidden() failed\n" +
        "\n" +
        "Locator: locator('button')\n" +
        "Expected: not hidden\n" +
        "Timeout: 1000ms\n" +
        "Error: element(s) not found\n" +
        "\n" +
        "Call log:\n" +
        "  - Expect \"not toBeHidden\" locator('button') with timeout 1000ms\n"));
    }

    @PlaywrightTest
    @DisplayName("EX-060 with impossible timeout .not")
    void withImpossibleTimeoutNot(Page page) {
      page.setContent("<div id=node>Text content</div>");
      assertThat(page.locator("#node")).not().isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1));
    }

    @PlaywrightTest
    @DisplayName("EX-061 with impossible timeout")
    void withImpossibleTimeout(Page page) {
      page.setContent("<div id=node>Text content</div>");
      assertThat(page.locator("no-such-thing")).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1));
    }
  }

  @PlaywrightTest
  @DisplayName("EX-062 toBeFocused")
  void toBeFocused(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    locator.focus();
    assertThat(locator).isFocused();
  }

  @PlaywrightTest
  @DisplayName("EX-063 toBeFocused with shadow elements")
  void toBeFocusedWithShadowElements(Page page) {
    page.setContent("\n" +
      "    <div id=\"app\">\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      const root = document.querySelector('div');\n" +
      "      const shadowRoot = root.attachShadow({ mode: 'open' });\n" +
      "      const input = document.createElement('input');\n" +
      "      input.id = \"my-input\"\n" +
      "      shadowRoot.appendChild(input);\n" +
      "    </script>\n" +
      "  ");

    page.locator("input").focus();
    assertEquals("my-input", page.evaluate("() => document.activeElement.shadowRoot.activeElement.id"));
    assertThat(page.locator("#app")).isFocused();
    assertThat(page.locator("input")).isFocused();
  }

  @PlaywrightTest
  @DisplayName("EX-064 should print unknown engine error")
  void shouldPrintUnknownEngineError(Page page) {
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.locator("row=\"row\"")).isVisible());
    assertTrue(error.getMessage().contains("Unknown engine \"row\" while parsing selector row=\"row\""));
  }

  @PlaywrightTest
  @DisplayName("EX-065 should print selector syntax error")
  void shouldPrintSelectorSyntaxError(Page page) {
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.locator("row]")).isVisible());
    assertTrue(error.getMessage().contains("Unexpected token \"]\" while parsing css selector \"row]\""));
  }

  @PlaywrightTest
  @DisplayName("EX-066 toBeOK")
  void toBeOK(Page page, TestServer server) {
    APIResponse res = page.request().get(server.EMPTY_PAGE);
    assertThat(res).isOK();
  }

  @PlaywrightTest
  @DisplayName("EX-067 not.toBeOK")
  void notToBeOK(Page page, TestServer server) {
    APIResponse res = page.request().get(server.PREFIX + "/unknown");
    assertThat(res).not().isOK();
  }

  @Nested
  class ToBeOKShouldPrintResponseWithTextContentTypeWhenFails {
    @BeforeEach
    void beforeEach(TestServer server) {
      server.setRoute("/text-content-type", (req, res) -> {
        res.statusCode = 404;
        res.setHeader("Content-type", "text/plain");
        res.end("Text error");
      });
      server.setRoute("/no-content-type", (req, res) -> {
        res.statusCode = 404;
        res.end("No content type error");
      });
      server.setRoute("/binary-content-type", (req, res) -> {
        res.statusCode = 404;
        res.setHeader("Content-type", "image/bmp");
        res.end("Image content type error");
      });
    }

    @PlaywrightTest
    @DisplayName("EX-070 text content type")
    @Fixme("java: the message is 'Response status expected to be within [200..299] range, was 404\\nCall log:\\n→ GET ...', without the 'expect(response).toBeOK() failed' line of the Node.js client")
    void textContentType(Page page, TestServer server) {
      APIResponse res = page.request().get(server.PREFIX + "/text-content-type");
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(res).isOK());
      assertTrue(stripAnsi(error.getMessage()).contains("expect(response).toBeOK() failed"));
      assertTrue(error.getMessage().contains("→ GET " + server.PREFIX + "/text-content-type"));
      assertTrue(error.getMessage().contains("← 404 Not Found"));
      assertTrue(stripAnsi(error.getMessage()).contains("Response text:\nText error"));
    }

    @PlaywrightTest
    @DisplayName("EX-071 no content type")
    void noContentType(Page page, TestServer server) {
      APIResponse res = page.request().get(server.PREFIX + "/no-content-type");
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(res).isOK());
      assertTrue(error.getMessage().contains("→ GET " + server.PREFIX + "/no-content-type"));
      assertTrue(error.getMessage().contains("← 404 Not Found"));
      assertFalse(error.getMessage().contains("No content type error"));
    }

    @PlaywrightTest
    @DisplayName("EX-072 image content type")
    void imageContentType(Page page, TestServer server) {
      APIResponse res = page.request().get(server.PREFIX + "/image-content-type");
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(res).isOK());
      assertTrue(error.getMessage().contains("→ GET " + server.PREFIX + "/image-content-type"));
      assertTrue(error.getMessage().contains("← 404 Not Found"));
      assertFalse(error.getMessage().contains("Image content type error"));
    }
  }

  @Nested
  class ToBeAttached {
    @PlaywrightTest
    @DisplayName("EX-073 default")
    void testDefault(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      assertThat(locator).isAttached();
    }

    @PlaywrightTest
    @DisplayName("EX-074 with hidden element")
    void withHiddenElement(Page page) {
      page.setContent("<button style=\"display:none\">hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isAttached();
    }

    @PlaywrightTest
    @DisplayName("EX-075 with not")
    void withNot(Page page) {
      page.setContent("<button>hello</button>");
      Locator locator = page.locator("input");
      assertThat(locator).not().isAttached();
    }

    @PlaywrightTest
    @DisplayName("EX-076 with attached:true")
    void withAttachedTrue(Page page) {
      page.setContent("<button>hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(true));
    }

    @PlaywrightTest
    @DisplayName("EX-077 with attached:false")
    void withAttachedFalse(Page page) {
      page.setContent("<button>hello</button>");
      Locator locator = page.locator("input");
      assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(false));
    }

    @PlaywrightTest
    @DisplayName("EX-078 with not and attached:false")
    void withNotAndAttachedFalse(Page page) {
      page.setContent("<button>hello</button>");
      Locator locator = page.locator("button");
      assertThat(locator).not().isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(false));
    }

    @PlaywrightTest
    @DisplayName("EX-079 eventually")
    void eventually(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("span");
      try {
        page.evalOnSelector("div", "div => div.innerHTML = '<span>Hello</span>'");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).isAttached();
    }

    @PlaywrightTest
    @DisplayName("EX-080 eventually with not")
    void eventuallyWithNot(Page page) {
      page.setContent("<div><span>Hello</span></div>");
      Locator locator = page.locator("span");
      try {
        page.evalOnSelector("div", "div => div.textContent = ''");
      } catch (PlaywrightException e) {
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).not().isAttached();
    }

    @PlaywrightTest
    @DisplayName("EX-081 fail")
    void fail(Page page) {
      page.setContent("<button>Hello</button>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1000)));
      assertFalse(error.getMessage().contains("locator resolved to"));
    }

    @PlaywrightTest
    @DisplayName("EX-082 fail with not")
    void failWithNot(Page page) {
      page.setContent("<input></input>");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(3000)));
      assertTrue(error.getMessage().contains("locator resolved to <input/>"));
    }

    @PlaywrightTest
    @DisplayName("EX-083 with impossible timeout")
    void withImpossibleTimeout(Page page) {
      page.setContent("<div id=node>Text content</div>");
      assertThat(page.locator("#node")).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1));
    }

    @PlaywrightTest
    @DisplayName("EX-084 with impossible timeout .not")
    void withImpossibleTimeoutNot(Page page) {
      page.setContent("<div id=node>Text content</div>");
      assertThat(page.locator("no-such-thing")).not().isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1));
    }

    // EX-085 with frameLocator: not ported, see upstream_not_ported.txt.

    // EX-086 over navigation: not ported, see upstream_not_ported.txt.
  }
}
