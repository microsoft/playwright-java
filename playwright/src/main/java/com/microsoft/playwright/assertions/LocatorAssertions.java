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

package com.microsoft.playwright.assertions;

import java.util.regex.Pattern;

/**
 * The {@code LocatorAssertions} class provides assertion methods that can be used to make assertions about the {@code Locator} state
 * in the tests. A new instance of {@code LocatorAssertions} is created by calling {@link PlaywrightAssertions#assertThat
 * PlaywrightAssertions.assertThat()}:
 * <pre>{@code
 * ...
 * import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
 *
 * public class TestLocator {
 *   ...
 *   @Test
 *   void statusBecomesSubmitted() {
 *     ...
 *     page.locator("#submit-button").click();
 *     assertThat(page.locator(".status")).hasText("Submitted");
 *   }
 * }
 * }</pre>
 */
public interface LocatorAssertions {
  class IsCheckedOptions {
    public Boolean checked;
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    public IsCheckedOptions setChecked(boolean checked) {
      this.checked = checked;
      return this;
    }
    /**
     * Time to retry the assertion for.
     */
    public IsCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public IsDisabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
    public Boolean editable;
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    public IsEditableOptions setEditable(boolean editable) {
      this.editable = editable;
      return this;
    }
    /**
     * Time to retry the assertion for.
     */
    public IsEditableOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEmptyOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public IsEmptyOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
    public Boolean enabled;
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    public IsEnabledOptions setEnabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }
    /**
     * Time to retry the assertion for.
     */
    public IsEnabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsFocusedOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public IsFocusedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;
    public Boolean visible;

    /**
     * Time to retry the assertion for.
     */
    public IsVisibleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public IsVisibleOptions setVisible(boolean visible) {
      this.visible = visible;
      return this;
    }
  }
  class ContainsTextOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;
    /**
     * Whether to use {@code element.innerText} instead of {@code element.textContent} when retrieving DOM node text.
     */
    public Boolean useInnerText;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public ContainsTextOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for.
     */
    public ContainsTextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Whether to use {@code element.innerText} instead of {@code element.textContent} when retrieving DOM node text.
     */
    public ContainsTextOptions setUseInnerText(boolean useInnerText) {
      this.useInnerText = useInnerText;
      return this;
    }
  }
  class HasAttributeOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasAttributeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasClassOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasClassOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasCountOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasCountOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasCSSOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasCSSOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasIdOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasIdOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasJSPropertyOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasJSPropertyOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasTextOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;
    /**
     * Whether to use {@code element.innerText} instead of {@code element.textContent} when retrieving DOM node text.
     */
    public Boolean useInnerText;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public HasTextOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for.
     */
    public HasTextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Whether to use {@code element.innerText} instead of {@code element.textContent} when retrieving DOM node text.
     */
    public HasTextOptions setUseInnerText(boolean useInnerText) {
      this.useInnerText = useInnerText;
      return this;
    }
  }
  class HasValueOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasValueOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasValuesOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for.
     */
    public HasValuesOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Makes the assertion check for the opposite condition. For example, this code tests that the Locator doesn't contain text
   * {@code "error"}:
   * <pre>{@code
   * assertThat(locator).not().containsText("error");
   * }</pre>
   */
  LocatorAssertions not();
  /**
   * Ensures the {@code Locator} points to a checked input.
   * <pre>{@code
   * assertThat(page.locator(".subscribe")).isChecked();
   * }</pre>
   */
  default void isChecked() {
    isChecked(null);
  }
  /**
   * Ensures the {@code Locator} points to a checked input.
   * <pre>{@code
   * assertThat(page.locator(".subscribe")).isChecked();
   * }</pre>
   */
  void isChecked(IsCheckedOptions options);
  /**
   * Ensures the {@code Locator} points to a disabled element. Element is disabled if it has "disabled" attribute or is disabled
   * via <a
   * href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-disabled">'aria-disabled'</a>.
   * Note that only native control elements such as HTML {@code button}, {@code input}, {@code select}, {@code textarea}, {@code option}, {@code optgroup} can be
   * disabled by setting "disabled" attribute. "disabled" attribute on other elements is ignored by the browser.
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isDisabled();
   * }</pre>
   */
  default void isDisabled() {
    isDisabled(null);
  }
  /**
   * Ensures the {@code Locator} points to a disabled element. Element is disabled if it has "disabled" attribute or is disabled
   * via <a
   * href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-disabled">'aria-disabled'</a>.
   * Note that only native control elements such as HTML {@code button}, {@code input}, {@code select}, {@code textarea}, {@code option}, {@code optgroup} can be
   * disabled by setting "disabled" attribute. "disabled" attribute on other elements is ignored by the browser.
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isDisabled();
   * }</pre>
   */
  void isDisabled(IsDisabledOptions options);
  /**
   * Ensures the {@code Locator} points to an editable element.
   * <pre>{@code
   * assertThat(page.locator("input")).isEditable();
   * }</pre>
   */
  default void isEditable() {
    isEditable(null);
  }
  /**
   * Ensures the {@code Locator} points to an editable element.
   * <pre>{@code
   * assertThat(page.locator("input")).isEditable();
   * }</pre>
   */
  void isEditable(IsEditableOptions options);
  /**
   * Ensures the {@code Locator} points to an empty editable element or to a DOM node that has no text.
   * <pre>{@code
   * assertThat(page.locator("div.warning")).isEmpty();
   * }</pre>
   */
  default void isEmpty() {
    isEmpty(null);
  }
  /**
   * Ensures the {@code Locator} points to an empty editable element or to a DOM node that has no text.
   * <pre>{@code
   * assertThat(page.locator("div.warning")).isEmpty();
   * }</pre>
   */
  void isEmpty(IsEmptyOptions options);
  /**
   * Ensures the {@code Locator} points to an enabled element.
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isEnabled();
   * }</pre>
   */
  default void isEnabled() {
    isEnabled(null);
  }
  /**
   * Ensures the {@code Locator} points to an enabled element.
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isEnabled();
   * }</pre>
   */
  void isEnabled(IsEnabledOptions options);
  /**
   * Ensures the {@code Locator} points to a focused DOM node.
   * <pre>{@code
   * assertThat(page.locator("input")).isFocused();
   * }</pre>
   */
  default void isFocused() {
    isFocused(null);
  }
  /**
   * Ensures the {@code Locator} points to a focused DOM node.
   * <pre>{@code
   * assertThat(page.locator("input")).isFocused();
   * }</pre>
   */
  void isFocused(IsFocusedOptions options);
  /**
   * Ensures that {@code Locator} either does not resolve to any DOM node, or resolves to a <a
   * href="https://playwright.dev/java/docs/api/actionability#visible">non-visible</a> one.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isHidden();
   * }</pre>
   */
  default void isHidden() {
    isHidden(null);
  }
  /**
   * Ensures that {@code Locator} either does not resolve to any DOM node, or resolves to a <a
   * href="https://playwright.dev/java/docs/api/actionability#visible">non-visible</a> one.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isHidden();
   * }</pre>
   */
  void isHidden(IsHiddenOptions options);
  /**
   * Ensures that {@code Locator} points to an <a href="https://playwright.dev/java/docs/api/actionability#visible">attached</a>
   * and <a href="https://playwright.dev/java/docs/api/actionability#visible">visible</a> DOM node.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isVisible();
   * }</pre>
   */
  default void isVisible() {
    isVisible(null);
  }
  /**
   * Ensures that {@code Locator} points to an <a href="https://playwright.dev/java/docs/api/actionability#visible">attached</a>
   * and <a href="https://playwright.dev/java/docs/api/actionability#visible">visible</a> DOM node.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isVisible();
   * }</pre>
   */
  void isVisible(IsVisibleOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void containsText(String expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void containsText(String expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void containsText(Pattern expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void containsText(Pattern expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void containsText(String[] expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void containsText(String[] expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void containsText(Pattern[] expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. You can use regular expressions for the value
   * as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).containsText("substring");
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> Elements from a **subset** of this list contain text from the expected array, respectively.</li>
   * <li> The matching subset of elements has the same order as the expected array.</li>
   * <li> Each text value from the expected array is matched by some element from the list.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Contains the right items in the right order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 1", "Text 3", "Text 4"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Text 3", "Text 2"});
   *
   * // ✖ No item contains this text
   * assertThat(page.locator("ul > li")).containsText(new String[] {"Some 33"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).containsText(new String[] {"Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void containsText(Pattern[] expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute value.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  default void hasAttribute(String name, String value) {
    hasAttribute(name, value, (HasAttributeOptions) null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute value.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  void hasAttribute(String name, String value, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute value.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  default void hasAttribute(String name, Pattern value) {
    hasAttribute(name, value, (HasAttributeOptions) null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute value.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  void hasAttribute(String name, Pattern value, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute. The method will assert attribute presence.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("disabled");
   * assertThat(page.locator("input")).not().hasAttribute("open");
   * }</pre>
   *
   * @param name Attribute name.
   */
  default void hasAttribute(String name) {
    hasAttribute(name, (HasAttributeOptions) null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute. The method will assert attribute presence.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("disabled");
   * assertThat(page.locator("input")).not().hasAttribute("open");
   * }</pre>
   *
   * @param name Attribute name.
   */
  void hasAttribute(String name, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  default void hasClass(String expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  void hasClass(String expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  default void hasClass(Pattern expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  void hasClass(Pattern expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  default void hasClass(String[] expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  void hasClass(String[] expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  default void hasClass(Pattern[] expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. This needs to be a full match or using a relaxed
   * regular expression.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
   * assertThat(page.locator("#component")).hasClass("selected row");
   * }</pre>
   *
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   */
  void hasClass(Pattern[] expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} resolves to an exact number of DOM nodes.
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasCount(3);
   * }</pre>
   *
   * @param count Expected count.
   */
  default void hasCount(int count) {
    hasCount(count, null);
  }
  /**
   * Ensures the {@code Locator} resolves to an exact number of DOM nodes.
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasCount(3);
   * }</pre>
   *
   * @param count Expected count.
   */
  void hasCount(int count, HasCountOptions options);
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   * <pre>{@code
   * assertThat(page.locator("button")).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   */
  default void hasCSS(String name, String value) {
    hasCSS(name, value, null);
  }
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   * <pre>{@code
   * assertThat(page.locator("button")).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   */
  void hasCSS(String name, String value, HasCSSOptions options);
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   * <pre>{@code
   * assertThat(page.locator("button")).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   */
  default void hasCSS(String name, Pattern value) {
    hasCSS(name, value, null);
  }
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   * <pre>{@code
   * assertThat(page.locator("button")).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   */
  void hasCSS(String name, Pattern value, HasCSSOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   * <pre>{@code
   * assertThat(page.locator("input")).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   */
  default void hasId(String id) {
    hasId(id, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   * <pre>{@code
   * assertThat(page.locator("input")).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   */
  void hasId(String id, HasIdOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   * <pre>{@code
   * assertThat(page.locator("input")).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   */
  default void hasId(Pattern id) {
    hasId(id, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   * <pre>{@code
   * assertThat(page.locator("input")).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   */
  void hasId(Pattern id, HasIdOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given JavaScript property. Note that this property can be of a primitive
   * type as well as a plain serializable JavaScript object.
   * <pre>{@code
   * assertThat(page.locator("input")).hasJSProperty("loaded", true);
   * }</pre>
   *
   * @param name Property name.
   * @param value Property value.
   */
  default void hasJSProperty(String name, Object value) {
    hasJSProperty(name, value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given JavaScript property. Note that this property can be of a primitive
   * type as well as a plain serializable JavaScript object.
   * <pre>{@code
   * assertThat(page.locator("input")).hasJSProperty("loaded", true);
   * }</pre>
   *
   * @param name Property name.
   * @param value Property value.
   */
  void hasJSProperty(String name, Object value, HasJSPropertyOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void hasText(String expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void hasText(String expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void hasText(Pattern expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void hasText(Pattern expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void hasText(String[] expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void hasText(String[] expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  default void hasText(Pattern[] expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. You can use regular expressions for the value as well.
   * <pre>{@code
   * assertThat(page.locator(".title")).hasText("Welcome, Test User");
   * assertThat(page.locator(".title")).hasText(Pattern.compile("Welcome, .*"));
   * }</pre>
   *
   * <p> If you pass an array as an expected value, the expectations are:
   * <ol>
   * <li> Locator resolves to a list of elements.</li>
   * <li> The number of elements equals the number of expected values in the array.</li>
   * <li> Elements from the list have text matching expected array values, one by one, in order.</li>
   * </ol>
   *
   * <p> For example, consider the following list:
   *
   * <p> Let's see how we can use the assertion:
   * <pre>{@code
   * // ✓ Has the right items in the right order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   *
   * // ✖ Wrong order
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 3", "Text 2", "Text 1"});
   *
   * // ✖ Last item does not match
   * assertThat(page.locator("ul > li")).hasText(new String[] {"Text 1", "Text 2", "Text"});
   *
   * // ✖ Locator points to the outer list element, not to the list items
   * assertThat(page.locator("ul")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void hasText(Pattern[] expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the value as
   * well.
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   */
  default void hasValue(String value) {
    hasValue(value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the value as
   * well.
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   */
  void hasValue(String value, HasValueOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the value as
   * well.
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   */
  default void hasValue(Pattern value) {
    hasValue(value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the value as
   * well.
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   */
  void hasValue(Pattern value, HasValueOptions options);
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute) and the specified
   * values are selected.
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(["R", "G"]);
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   */
  default void hasValues(String[] values) {
    hasValues(values, null);
  }
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute) and the specified
   * values are selected.
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(["R", "G"]);
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   */
  void hasValues(String[] values, HasValuesOptions options);
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute) and the specified
   * values are selected.
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(["R", "G"]);
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   */
  default void hasValues(Pattern[] values) {
    hasValues(values, null);
  }
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute) and the specified
   * values are selected.
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(["R", "G"]);
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   */
  void hasValues(Pattern[] values, HasValuesOptions options);
}

