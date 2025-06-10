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

import java.util.*;
import java.util.regex.Pattern;
import com.microsoft.playwright.options.AriaRole;

/**
 * The {@code LocatorAssertions} class provides assertion methods that can be used to make assertions about the {@code
 * Locator} state in the tests.
 * <pre>{@code
 * // ...
 * import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
 *
 * public class TestLocator {
 *   // ...
 *   @Test
 *   void statusBecomesSubmitted() {
 *     // ...
 *     page.getByRole(AriaRole.BUTTON).click();
 *     assertThat(page.locator(".status")).hasText("Submitted");
 *   }
 * }
 * }</pre>
 */
public interface LocatorAssertions {
  class IsAttachedOptions {
    public Boolean attached;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    public IsAttachedOptions setAttached(boolean attached) {
      this.attached = attached;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsAttachedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsCheckedOptions {
    /**
     * Provides state to assert for. Asserts for input to be checked by default. This option can't be used when {@code
     * indeterminate} is set to true.
     */
    public Boolean checked;
    /**
     * Asserts that the element is in the indeterminate (mixed) state. Only supported for checkboxes and radio buttons. This
     * option can't be true when {@code checked} is provided.
     */
    public Boolean indeterminate;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Provides state to assert for. Asserts for input to be checked by default. This option can't be used when {@code
     * indeterminate} is set to true.
     */
    public IsCheckedOptions setChecked(boolean checked) {
      this.checked = checked;
      return this;
    }
    /**
     * Asserts that the element is in the indeterminate (mixed) state. Only supported for checkboxes and radio buttons. This
     * option can't be true when {@code checked} is provided.
     */
    public IsCheckedOptions setIndeterminate(boolean indeterminate) {
      this.indeterminate = indeterminate;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsDisabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
    public Boolean editable;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    public IsEditableOptions setEditable(boolean editable) {
      this.editable = editable;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsEditableOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEmptyOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsEmptyOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
    public Boolean enabled;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    public IsEnabledOptions setEnabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsEnabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsFocusedOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsFocusedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsInViewportOptions {
    /**
     * The minimal ratio of the element to intersect viewport. If equals to {@code 0}, then element should intersect viewport
     * at any positive ratio. Defaults to {@code 0}.
     */
    public Double ratio;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * The minimal ratio of the element to intersect viewport. If equals to {@code 0}, then element should intersect viewport
     * at any positive ratio. Defaults to {@code 0}.
     */
    public IsInViewportOptions setRatio(double ratio) {
      this.ratio = ratio;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public IsInViewportOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;
    public Boolean visible;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
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
  class ContainsClassOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public ContainsClassOptions setTimeout(double timeout) {
      this.timeout = timeout;
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
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
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
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
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
  class HasAccessibleDescriptionOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public HasAccessibleDescriptionOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasAccessibleDescriptionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasAccessibleErrorMessageOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public HasAccessibleErrorMessageOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasAccessibleErrorMessageOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasAccessibleNameOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public HasAccessibleNameOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasAccessibleNameOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasAttributeOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression flag if specified.
     */
    public HasAttributeOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasAttributeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasClassOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasClassOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasCountOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasCountOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasCSSOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasCSSOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasIdOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasIdOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasJSPropertyOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasJSPropertyOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasRoleOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasRoleOptions setTimeout(double timeout) {
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
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
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
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
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
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasValueOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasValuesOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasValuesOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class MatchesAriaSnapshotOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public MatchesAriaSnapshotOptions setTimeout(double timeout) {
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
   *
   * @since v1.20
   */
  LocatorAssertions not();
  /**
   * Ensures that {@code Locator} points to an element that is <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected">connected</a> to a Document or a ShadowRoot.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByText("Hidden text")).isAttached();
   * }</pre>
   *
   * @since v1.33
   */
  default void isAttached() {
    isAttached(null);
  }
  /**
   * Ensures that {@code Locator} points to an element that is <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected">connected</a> to a Document or a ShadowRoot.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByText("Hidden text")).isAttached();
   * }</pre>
   *
   * @since v1.33
   */
  void isAttached(IsAttachedOptions options);
  /**
   * Ensures the {@code Locator} points to a checked input.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByLabel("Subscribe to newsletter")).isChecked();
   * }</pre>
   *
   * @since v1.20
   */
  default void isChecked() {
    isChecked(null);
  }
  /**
   * Ensures the {@code Locator} points to a checked input.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByLabel("Subscribe to newsletter")).isChecked();
   * }</pre>
   *
   * @since v1.20
   */
  void isChecked(IsCheckedOptions options);
  /**
   * Ensures the {@code Locator} points to a disabled element. Element is disabled if it has "disabled" attribute or is
   * disabled via <a
   * href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-disabled">'aria-disabled'</a>.
   * Note that only native control elements such as HTML {@code button}, {@code input}, {@code select}, {@code textarea},
   * {@code option}, {@code optgroup} can be disabled by setting "disabled" attribute. "disabled" attribute on other elements
   * is ignored by the browser.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isDisabled();
   * }</pre>
   *
   * @since v1.20
   */
  default void isDisabled() {
    isDisabled(null);
  }
  /**
   * Ensures the {@code Locator} points to a disabled element. Element is disabled if it has "disabled" attribute or is
   * disabled via <a
   * href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-disabled">'aria-disabled'</a>.
   * Note that only native control elements such as HTML {@code button}, {@code input}, {@code select}, {@code textarea},
   * {@code option}, {@code optgroup} can be disabled by setting "disabled" attribute. "disabled" attribute on other elements
   * is ignored by the browser.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isDisabled();
   * }</pre>
   *
   * @since v1.20
   */
  void isDisabled(IsDisabledOptions options);
  /**
   * Ensures the {@code Locator} points to an editable element.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).isEditable();
   * }</pre>
   *
   * @since v1.20
   */
  default void isEditable() {
    isEditable(null);
  }
  /**
   * Ensures the {@code Locator} points to an editable element.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).isEditable();
   * }</pre>
   *
   * @since v1.20
   */
  void isEditable(IsEditableOptions options);
  /**
   * Ensures the {@code Locator} points to an empty editable element or to a DOM node that has no text.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("div.warning")).isEmpty();
   * }</pre>
   *
   * @since v1.20
   */
  default void isEmpty() {
    isEmpty(null);
  }
  /**
   * Ensures the {@code Locator} points to an empty editable element or to a DOM node that has no text.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("div.warning")).isEmpty();
   * }</pre>
   *
   * @since v1.20
   */
  void isEmpty(IsEmptyOptions options);
  /**
   * Ensures the {@code Locator} points to an enabled element.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isEnabled();
   * }</pre>
   *
   * @since v1.20
   */
  default void isEnabled() {
    isEnabled(null);
  }
  /**
   * Ensures the {@code Locator} points to an enabled element.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("button.submit")).isEnabled();
   * }</pre>
   *
   * @since v1.20
   */
  void isEnabled(IsEnabledOptions options);
  /**
   * Ensures the {@code Locator} points to a focused DOM node.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).isFocused();
   * }</pre>
   *
   * @since v1.20
   */
  default void isFocused() {
    isFocused(null);
  }
  /**
   * Ensures the {@code Locator} points to a focused DOM node.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).isFocused();
   * }</pre>
   *
   * @since v1.20
   */
  void isFocused(IsFocusedOptions options);
  /**
   * Ensures that {@code Locator} either does not resolve to any DOM node, or resolves to a <a
   * href="https://playwright.dev/java/docs/actionability#visible">non-visible</a> one.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isHidden();
   * }</pre>
   *
   * @since v1.20
   */
  default void isHidden() {
    isHidden(null);
  }
  /**
   * Ensures that {@code Locator} either does not resolve to any DOM node, or resolves to a <a
   * href="https://playwright.dev/java/docs/actionability#visible">non-visible</a> one.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator(".my-element")).isHidden();
   * }</pre>
   *
   * @since v1.20
   */
  void isHidden(IsHiddenOptions options);
  /**
   * Ensures the {@code Locator} points to an element that intersects viewport, according to the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">intersection observer API</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByRole(AriaRole.BUTTON);
   * // Make sure at least some part of element intersects viewport.
   * assertThat(locator).isInViewport();
   * // Make sure element is fully outside of viewport.
   * assertThat(locator).not().isInViewport();
   * // Make sure that at least half of the element intersects viewport.
   * assertThat(locator).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.5));
   * }</pre>
   *
   * @since v1.31
   */
  default void isInViewport() {
    isInViewport(null);
  }
  /**
   * Ensures the {@code Locator} points to an element that intersects viewport, according to the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">intersection observer API</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByRole(AriaRole.BUTTON);
   * // Make sure at least some part of element intersects viewport.
   * assertThat(locator).isInViewport();
   * // Make sure element is fully outside of viewport.
   * assertThat(locator).not().isInViewport();
   * // Make sure that at least half of the element intersects viewport.
   * assertThat(locator).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.5));
   * }</pre>
   *
   * @since v1.31
   */
  void isInViewport(IsInViewportOptions options);
  /**
   * Ensures that {@code Locator} points to an attached and <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a> DOM node.
   *
   * <p> To check that at least one element from the list is visible, use {@link com.microsoft.playwright.Locator#first
   * Locator.first()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * // A specific element is visible.
   * assertThat(page.getByText("Welcome")).isVisible();
   *
   * // At least one item in the list is visible.
   * assertThat(page.getByTestId("todo-item").first()).isVisible();
   *
   * // At least one of the two elements is visible, possibly both.
   * assertThat(
   *   page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"))
   *     .or(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up")))
   *     .first()
   * ).isVisible();
   * }</pre>
   *
   * @since v1.20
   */
  default void isVisible() {
    isVisible(null);
  }
  /**
   * Ensures that {@code Locator} points to an attached and <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a> DOM node.
   *
   * <p> To check that at least one element from the list is visible, use {@link com.microsoft.playwright.Locator#first
   * Locator.first()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * // A specific element is visible.
   * assertThat(page.getByText("Welcome")).isVisible();
   *
   * // At least one item in the list is visible.
   * assertThat(page.getByTestId("todo-item").first()).isVisible();
   *
   * // At least one of the two elements is visible, possibly both.
   * assertThat(
   *   page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"))
   *     .or(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up")))
   *     .first()
   * ).isVisible();
   * }</pre>
   *
   * @since v1.20
   */
  void isVisible(IsVisibleOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. All classes from the asserted value, separated
   * by spaces, must be present in the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/classList">Element.classList</a> in any order.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).containsClass("middle selected row");
   * assertThat(page.locator("#component")).containsClass("selected");
   * assertThat(page.locator("#component")).containsClass("row middle");
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class lists. Each element's class attribute is matched against the corresponding class in the array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).containsClass(new String[] {"inactive", "active", "inactive"});
   * }</pre>
   *
   * @param expected A string containing expected class names, separated by spaces, or a list of such strings to assert multiple elements.
   * @since v1.52
   */
  default void containsClass(String expected) {
    containsClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. All classes from the asserted value, separated
   * by spaces, must be present in the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/classList">Element.classList</a> in any order.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).containsClass("middle selected row");
   * assertThat(page.locator("#component")).containsClass("selected");
   * assertThat(page.locator("#component")).containsClass("row middle");
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class lists. Each element's class attribute is matched against the corresponding class in the array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).containsClass(new String[] {"inactive", "active", "inactive"});
   * }</pre>
   *
   * @param expected A string containing expected class names, separated by spaces, or a list of such strings to assert multiple elements.
   * @since v1.52
   */
  void containsClass(String expected, ContainsClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. All classes from the asserted value, separated
   * by spaces, must be present in the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/classList">Element.classList</a> in any order.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).containsClass("middle selected row");
   * assertThat(page.locator("#component")).containsClass("selected");
   * assertThat(page.locator("#component")).containsClass("row middle");
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class lists. Each element's class attribute is matched against the corresponding class in the array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).containsClass(new String[] {"inactive", "active", "inactive"});
   * }</pre>
   *
   * @param expected A string containing expected class names, separated by spaces, or a list of such strings to assert multiple elements.
   * @since v1.52
   */
  default void containsClass(List<String> expected) {
    containsClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. All classes from the asserted value, separated
   * by spaces, must be present in the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/classList">Element.classList</a> in any order.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).containsClass("middle selected row");
   * assertThat(page.locator("#component")).containsClass("selected");
   * assertThat(page.locator("#component")).containsClass("row middle");
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class lists. Each element's class attribute is matched against the corresponding class in the array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).containsClass(new String[] {"inactive", "active", "inactive"});
   * }</pre>
   *
   * @param expected A string containing expected class names, separated by spaces, or a list of such strings to assert multiple elements.
   * @since v1.52
   */
  void containsClass(List<String> expected, ContainsClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  default void containsText(String expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  void containsText(String expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  default void containsText(Pattern expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  void containsText(Pattern expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  default void containsText(String[] expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  void containsText(String[] expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  default void containsText(Pattern[] expected) {
    containsText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element that contains the given text. All nested elements will be considered
   * when computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @since v1.20
   */
  void containsText(Pattern[] expected, ContainsTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-description">accessible description</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleDescription("Save results to disk");
   * }</pre>
   *
   * @param description Expected accessible description.
   * @since v1.44
   */
  default void hasAccessibleDescription(String description) {
    hasAccessibleDescription(description, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-description">accessible description</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleDescription("Save results to disk");
   * }</pre>
   *
   * @param description Expected accessible description.
   * @since v1.44
   */
  void hasAccessibleDescription(String description, HasAccessibleDescriptionOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-description">accessible description</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleDescription("Save results to disk");
   * }</pre>
   *
   * @param description Expected accessible description.
   * @since v1.44
   */
  default void hasAccessibleDescription(Pattern description) {
    hasAccessibleDescription(description, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-description">accessible description</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleDescription("Save results to disk");
   * }</pre>
   *
   * @param description Expected accessible description.
   * @since v1.44
   */
  void hasAccessibleDescription(Pattern description, HasAccessibleDescriptionOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/aria/#aria-errormessage">aria errormessage</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("username-input");
   * assertThat(locator).hasAccessibleErrorMessage("Username is required.");
   * }</pre>
   *
   * @param errorMessage Expected accessible error message.
   * @since v1.50
   */
  default void hasAccessibleErrorMessage(String errorMessage) {
    hasAccessibleErrorMessage(errorMessage, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/aria/#aria-errormessage">aria errormessage</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("username-input");
   * assertThat(locator).hasAccessibleErrorMessage("Username is required.");
   * }</pre>
   *
   * @param errorMessage Expected accessible error message.
   * @since v1.50
   */
  void hasAccessibleErrorMessage(String errorMessage, HasAccessibleErrorMessageOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/aria/#aria-errormessage">aria errormessage</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("username-input");
   * assertThat(locator).hasAccessibleErrorMessage("Username is required.");
   * }</pre>
   *
   * @param errorMessage Expected accessible error message.
   * @since v1.50
   */
  default void hasAccessibleErrorMessage(Pattern errorMessage) {
    hasAccessibleErrorMessage(errorMessage, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/aria/#aria-errormessage">aria errormessage</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("username-input");
   * assertThat(locator).hasAccessibleErrorMessage("Username is required.");
   * }</pre>
   *
   * @param errorMessage Expected accessible error message.
   * @since v1.50
   */
  void hasAccessibleErrorMessage(Pattern errorMessage, HasAccessibleErrorMessageOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleName("Save to disk");
   * }</pre>
   *
   * @param name Expected accessible name.
   * @since v1.44
   */
  default void hasAccessibleName(String name) {
    hasAccessibleName(name, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleName("Save to disk");
   * }</pre>
   *
   * @param name Expected accessible name.
   * @since v1.44
   */
  void hasAccessibleName(String name, HasAccessibleNameOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleName("Save to disk");
   * }</pre>
   *
   * @param name Expected accessible name.
   * @since v1.44
   */
  default void hasAccessibleName(Pattern name) {
    hasAccessibleName(name, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasAccessibleName("Save to disk");
   * }</pre>
   *
   * @param name Expected accessible name.
   * @since v1.44
   */
  void hasAccessibleName(Pattern name, HasAccessibleNameOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   * @since v1.20
   */
  default void hasAttribute(String name, String value) {
    hasAttribute(name, value, (HasAttributeOptions) null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   * @since v1.20
   */
  void hasAttribute(String name, String value, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   * @since v1.20
   */
  default void hasAttribute(String name, Pattern value) {
    hasAttribute(name, value, (HasAttributeOptions) null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given attribute.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input")).hasAttribute("type", "text");
   * }</pre>
   *
   * @param name Attribute name.
   * @param value Expected attribute value.
   * @since v1.20
   */
  void hasAttribute(String name, Pattern value, HasAttributeOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  default void hasClass(String expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  void hasClass(String expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  default void hasClass(Pattern expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  void hasClass(Pattern expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  default void hasClass(String[] expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  void hasClass(String[] expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  default void hasClass(Pattern[] expected) {
    hasClass(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given CSS classes. When a string is provided, it must fully match
   * the element's {@code class} attribute. To match individual classes use {@link
   * com.microsoft.playwright.assertions.LocatorAssertions#containsClass LocatorAssertions.containsClass()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("#component")).hasClass("middle selected row");
   * assertThat(page.locator("#component")).hasClass(Pattern.compile("(^|\\s)selected(\\s|$)"));
   * }</pre>
   *
   * <p> When an array is passed, the method asserts that the list of elements located matches the corresponding list of expected
   * class values. Each element's class attribute is matched against the corresponding string or regular expression in the
   * array:
   * <pre>{@code
   * assertThat(page.locator(".list > .component")).hasClass(new String[] {"component", "component selected", "component"});
   * }</pre>
   *
   * @param expected Expected class or RegExp or a list of those.
   * @since v1.20
   */
  void hasClass(Pattern[] expected, HasClassOptions options);
  /**
   * Ensures the {@code Locator} resolves to an exact number of DOM nodes.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasCount(3);
   * }</pre>
   *
   * @param count Expected count.
   * @since v1.20
   */
  default void hasCount(int count) {
    hasCount(count, null);
  }
  /**
   * Ensures the {@code Locator} resolves to an exact number of DOM nodes.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("list > .component")).hasCount(3);
   * }</pre>
   *
   * @param count Expected count.
   * @since v1.20
   */
  void hasCount(int count, HasCountOptions options);
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.BUTTON)).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   * @since v1.20
   */
  default void hasCSS(String name, String value) {
    hasCSS(name, value, null);
  }
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.BUTTON)).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   * @since v1.20
   */
  void hasCSS(String name, String value, HasCSSOptions options);
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.BUTTON)).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   * @since v1.20
   */
  default void hasCSS(String name, Pattern value) {
    hasCSS(name, value, null);
  }
  /**
   * Ensures the {@code Locator} resolves to an element with the given computed CSS style.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.BUTTON)).hasCSS("display", "flex");
   * }</pre>
   *
   * @param name CSS property name.
   * @param value CSS property value.
   * @since v1.20
   */
  void hasCSS(String name, Pattern value, HasCSSOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   * @since v1.20
   */
  default void hasId(String id) {
    hasId(id, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   * @since v1.20
   */
  void hasId(String id, HasIdOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   * @since v1.20
   */
  default void hasId(Pattern id) {
    hasId(id, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given DOM Node ID.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.getByRole(AriaRole.TEXTBOX)).hasId("lastname");
   * }</pre>
   *
   * @param id Element id.
   * @since v1.20
   */
  void hasId(Pattern id, HasIdOptions options);
  /**
   * Ensures the {@code Locator} points to an element with given JavaScript property. Note that this property can be of a
   * primitive type as well as a plain serializable JavaScript object.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input")).hasJSProperty("loaded", true);
   * }</pre>
   *
   * @param name Property name.
   * @param value Property value.
   * @since v1.20
   */
  default void hasJSProperty(String name, Object value) {
    hasJSProperty(name, value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with given JavaScript property. Note that this property can be of a
   * primitive type as well as a plain serializable JavaScript object.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input")).hasJSProperty("loaded", true);
   * }</pre>
   *
   * @param name Property name.
   * @param value Property value.
   * @since v1.20
   */
  void hasJSProperty(String name, Object value, HasJSPropertyOptions options);
  /**
   * Ensures the {@code Locator} points to an element with a given <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA
   * role</a>.
   *
   * <p> Note that role is matched as a string, disregarding the ARIA role hierarchy. For example, asserting  a superclass role
   * {@code "checkbox"} on an element with a subclass role {@code "switch"} will fail.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasRole(AriaRole.BUTTON);
   * }</pre>
   *
   * @param role Required aria role.
   * @since v1.44
   */
  default void hasRole(AriaRole role) {
    hasRole(role, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with a given <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA
   * role</a>.
   *
   * <p> Note that role is matched as a string, disregarding the ARIA role hierarchy. For example, asserting  a superclass role
   * {@code "checkbox"} on an element with a subclass role {@code "switch"} will fail.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * Locator locator = page.getByTestId("save-button");
   * assertThat(locator).hasRole(AriaRole.BUTTON);
   * }</pre>
   *
   * @param role Required aria role.
   * @since v1.44
   */
  void hasRole(AriaRole role, HasRoleOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  default void hasText(String expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  void hasText(String expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  default void hasText(Pattern expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  void hasText(Pattern expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  default void hasText(String[] expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  void hasText(String[] expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  default void hasText(Pattern[] expected) {
    hasText(expected, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given text. All nested elements will be considered when
   * computing the text content of the element. You can use regular expressions for the value as well.
   *
   * <p> <strong>Details</strong>
   *
   * <p> When {@code expected} parameter is a string, Playwright will normalize whitespaces and line breaks both in the actual
   * text and in the expected string before matching. When regular expression is used, the actual text is matched as is.
   *
   * <p> <strong>Usage</strong>
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
   * @param expected Expected string or RegExp or a list of those.
   * @since v1.20
   */
  void hasText(Pattern[] expected, HasTextOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the
   * value as well.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   * @since v1.20
   */
  default void hasValue(String value) {
    hasValue(value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the
   * value as well.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   * @since v1.20
   */
  void hasValue(String value, HasValueOptions options);
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the
   * value as well.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   * @since v1.20
   */
  default void hasValue(Pattern value) {
    hasValue(value, null);
  }
  /**
   * Ensures the {@code Locator} points to an element with the given input value. You can use regular expressions for the
   * value as well.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page.locator("input[type=number]")).hasValue(Pattern.compile("[0-9]"));
   * }</pre>
   *
   * @param value Expected value.
   * @since v1.20
   */
  void hasValue(Pattern value, HasValueOptions options);
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute)
   * and the specified values are selected.
   *
   * <p> <strong>Usage</strong>
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(new String[]{"R", "G"});
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   * @since v1.23
   */
  default void hasValues(String[] values) {
    hasValues(values, null);
  }
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute)
   * and the specified values are selected.
   *
   * <p> <strong>Usage</strong>
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(new String[]{"R", "G"});
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   * @since v1.23
   */
  void hasValues(String[] values, HasValuesOptions options);
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute)
   * and the specified values are selected.
   *
   * <p> <strong>Usage</strong>
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(new String[]{"R", "G"});
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   * @since v1.23
   */
  default void hasValues(Pattern[] values) {
    hasValues(values, null);
  }
  /**
   * Ensures the {@code Locator} points to multi-select/combobox (i.e. a {@code select} with the {@code multiple} attribute)
   * and the specified values are selected.
   *
   * <p> <strong>Usage</strong>
   *
   * <p> For example, given the following element:
   * <pre>{@code
   * page.locator("id=favorite-colors").selectOption(new String[]{"R", "G"});
   * assertThat(page.locator("id=favorite-colors")).hasValues(new Pattern[] { Pattern.compile("R"), Pattern.compile("G") });
   * }</pre>
   *
   * @param values Expected options currently selected.
   * @since v1.23
   */
  void hasValues(Pattern[] values, HasValuesOptions options);
  /**
   * Asserts that the target element matches the given <a
   * href="https://playwright.dev/java/docs/aria-snapshots">accessibility snapshot</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.navigate("https://demo.playwright.dev/todomvc/");
   * assertThat(page.locator("body")).matchesAriaSnapshot("""
   *   - heading "todos"
   *   - textbox "What needs to be done?"
   * """);
   * }</pre>
   *
   * @since v1.49
   */
  default void matchesAriaSnapshot(String expected) {
    matchesAriaSnapshot(expected, null);
  }
  /**
   * Asserts that the target element matches the given <a
   * href="https://playwright.dev/java/docs/aria-snapshots">accessibility snapshot</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.navigate("https://demo.playwright.dev/todomvc/");
   * assertThat(page.locator("body")).matchesAriaSnapshot("""
   *   - heading "todos"
   *   - textbox "What needs to be done?"
   * """);
   * }</pre>
   *
   * @since v1.49
   */
  void matchesAriaSnapshot(String expected, MatchesAriaSnapshotOptions options);
}

