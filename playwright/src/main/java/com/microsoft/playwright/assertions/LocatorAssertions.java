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
 *     page.click("#submit-button");
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
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

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
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;

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

    /**
     * Time to retry the assertion for.
     */
    public IsVisibleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ContainsTextOptions {
    /**
     * Time to retry the assertion for.
     */
    public Double timeout;
    /**
     * Whether to use {@code element.innerText} instead of {@code element.textContent} when retrieving DOM node text.
     */
    public Boolean useInnerText;

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
     * Time to retry the assertion for.
     */
    public Double timeout;
    /**
     * Whether to use {@code element.innerText} instead of {@code element.textContent} when retrieving DOM node text.
     */
    public Boolean useInnerText;

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
   * Ensures the {@code Locator} points to a hidden DOM node, which is the opposite of <a
   * href="https://playwright.dev/java/docs/api/actionability#visible">visible</a>.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isHidden();
   * }</pre>
   */
  default void isHidden() {
    isHidden(null);
  }
  /**
   * Ensures the {@code Locator} points to a hidden DOM node, which is the opposite of <a
   * href="https://playwright.dev/java/docs/api/actionability#visible">visible</a>.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isHidden();
   * }</pre>
   */
  void isHidden(IsHiddenOptions options);
  /**
   * Ensures the {@code Locator} points to a <a href="https://playwright.dev/java/docs/api/actionability#visible">visible</a> DOM
   * node.
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isVisible();
   * }</pre>
   */
  default void isVisible() {
    isVisible(null);
  }
  /**
   * Ensures the {@code Locator} points to a <a href="https://playwright.dev/java/docs/api/actionability#visible">visible</a> DOM
   * node.
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .list-item")).containsText(new String[] {"Text 1", "Text 4", "Text 5"});
   * }</pre>
   *
   * @param expected Expected substring or RegExp or a list of those.
   */
  void containsText(Pattern[] expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  default void hasAttribute(String name, String value) {
    hasAttribute(name, value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  void hasAttribute(String name, String value, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  default void hasAttribute(String name, Pattern value) {
    hasAttribute(name, value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   */
  void hasAttribute(String name, Pattern value, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * Ensures the {@code Locator} points to an element with given CSS class.
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("selected"));
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
   * <p> Note that if array is passed as an expected value, entire lists of elements can be asserted:
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasText(new String[] {"Text 1", "Text 2", "Text 3"});
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
}

