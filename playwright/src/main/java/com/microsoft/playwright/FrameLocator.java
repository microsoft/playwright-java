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

import com.microsoft.playwright.options.*;
import java.util.regex.Pattern;

/**
 * FrameLocator represents a view to the {@code iframe} on the page. It captures the logic sufficient to retrieve the {@code iframe}
 * and locate elements in that iframe. FrameLocator can be created with either {@link Page#frameLocator
 * Page.frameLocator()} or {@link Locator#frameLocator Locator.frameLocator()} method.
 * <pre>{@code
 * Locator locator = page.frameLocator("#my-frame").locator("text=Submit");
 * locator.click();
 * }</pre>
 *
 * <p> **Strictness**
 *
 * <p> Frame locators are strict. This means that all operations on frame locators will throw if more than one element matches
 * a given selector.
 * <pre>{@code
 * // Throws if there are several frames in DOM:
 * page.frame_locator(".result-frame").getByRole("button").click();
 *
 * // Works because we explicitly tell locator to pick the first frame:
 * page.frame_locator(".result-frame").first().getByRole("button").click();
 * }</pre>
 *
 * <p> **Converting Locator to FrameLocator**
 *
 * <p> If you have a {@code Locator} object pointing to an {@code iframe} it can be converted to {@code FrameLocator} using <a
 * href="https://developer.mozilla.org/en-US/docs/Web/CSS/:scope">{@code :scope}</a> CSS selector:
 * <pre>{@code
 * Locator frameLocator = locator.frameLocator(':scope');
 * }</pre>
 */
public interface FrameLocator {
  class GetByAltTextOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public GetByAltTextOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByLabelOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public GetByLabelOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByPlaceholderOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public GetByPlaceholderOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByRoleOptions {
    /**
     * An attribute that is usually set by {@code aria-checked} or native {@code <input type=checkbox>} controls. Available values for
     * checked are {@code true}, {@code false} and {@code "mixed"}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-checked">{@code aria-checked}</a>.
     */
    public Boolean checked;
    /**
     * A boolean attribute that is usually set by {@code aria-disabled} or {@code disabled}.
     *
     * <p> <strong>NOTE:</strong> Unlike most other attributes, {@code disabled} is inherited through the DOM hierarchy. Learn more about <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#aria-disabled">{@code aria-disabled}</a>.
     */
    public Boolean disabled;
    /**
     * A boolean attribute that is usually set by {@code aria-expanded}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-expanded">{@code aria-expanded}</a>.
     */
    public Boolean expanded;
    /**
     * A boolean attribute that controls whether hidden elements are matched. By default, only non-hidden elements, as <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#tree_exclusion">defined by ARIA</a>, are matched by role selector.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-hidden">{@code aria-hidden}</a>.
     */
    public Boolean includeHidden;
    /**
     * A number attribute that is usually present for roles {@code heading}, {@code listitem}, {@code row}, {@code treeitem}, with default values for
     * {@code <h1>-<h6>} elements.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-level">{@code aria-level}</a>.
     */
    public Integer level;
    /**
     * A string attribute that matches <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public Object name;
    /**
     * An attribute that is usually set by {@code aria-pressed}. Available values for pressed are {@code true}, {@code false} and {@code "mixed"}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-pressed">{@code aria-pressed}</a>.
     */
    public Boolean pressed;
    /**
     * A boolean attribute that is usually set by {@code aria-selected}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-selected">{@code aria-selected}</a>.
     */
    public Boolean selected;

    /**
     * An attribute that is usually set by {@code aria-checked} or native {@code <input type=checkbox>} controls. Available values for
     * checked are {@code true}, {@code false} and {@code "mixed"}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-checked">{@code aria-checked}</a>.
     */
    public GetByRoleOptions setChecked(boolean checked) {
      this.checked = checked;
      return this;
    }
    /**
     * A boolean attribute that is usually set by {@code aria-disabled} or {@code disabled}.
     *
     * <p> <strong>NOTE:</strong> Unlike most other attributes, {@code disabled} is inherited through the DOM hierarchy. Learn more about <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#aria-disabled">{@code aria-disabled}</a>.
     */
    public GetByRoleOptions setDisabled(boolean disabled) {
      this.disabled = disabled;
      return this;
    }
    /**
     * A boolean attribute that is usually set by {@code aria-expanded}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-expanded">{@code aria-expanded}</a>.
     */
    public GetByRoleOptions setExpanded(boolean expanded) {
      this.expanded = expanded;
      return this;
    }
    /**
     * A boolean attribute that controls whether hidden elements are matched. By default, only non-hidden elements, as <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#tree_exclusion">defined by ARIA</a>, are matched by role selector.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-hidden">{@code aria-hidden}</a>.
     */
    public GetByRoleOptions setIncludeHidden(boolean includeHidden) {
      this.includeHidden = includeHidden;
      return this;
    }
    /**
     * A number attribute that is usually present for roles {@code heading}, {@code listitem}, {@code row}, {@code treeitem}, with default values for
     * {@code <h1>-<h6>} elements.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-level">{@code aria-level}</a>.
     */
    public GetByRoleOptions setLevel(int level) {
      this.level = level;
      return this;
    }
    /**
     * A string attribute that matches <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public GetByRoleOptions setName(String name) {
      this.name = name;
      return this;
    }
    /**
     * A string attribute that matches <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public GetByRoleOptions setName(Pattern name) {
      this.name = name;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-pressed}. Available values for pressed are {@code true}, {@code false} and {@code "mixed"}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-pressed">{@code aria-pressed}</a>.
     */
    public GetByRoleOptions setPressed(boolean pressed) {
      this.pressed = pressed;
      return this;
    }
    /**
     * A boolean attribute that is usually set by {@code aria-selected}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-selected">{@code aria-selected}</a>.
     */
    public GetByRoleOptions setSelected(boolean selected) {
      this.selected = selected;
      return this;
    }
  }
  class GetByTextOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public GetByTextOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByTitleOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false.
     */
    public GetByTitleOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class LocatorOptions {
    /**
     * Matches elements containing an element that matches an inner locator. Inner locator is queried against the outer one.
     * For example, {@code article} that has {@code text=Playwright} matches {@code <article><div>Playwright</div></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public Locator has;
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches
     * {@code <article><div>Playwright</div></article>}.
     */
    public Object hasText;

    /**
     * Matches elements containing an element that matches an inner locator. Inner locator is queried against the outer one.
     * For example, {@code article} that has {@code text=Playwright} matches {@code <article><div>Playwright</div></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public LocatorOptions setHas(Locator has) {
      this.has = has;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches
     * {@code <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(String hasText) {
      this.hasText = hasText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches
     * {@code <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(Pattern hasText) {
      this.hasText = hasText;
      return this;
    }
  }
  /**
   * Returns locator to the first matching frame.
   */
  FrameLocator first();
  /**
   * When working with iframes, you can create a frame locator that will enter the iframe and allow selecting elements in
   * that iframe.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors">working with
   * selectors</a> for more details.
   */
  FrameLocator frameLocator(String selector);
  /**
   * Allows locating elements by their alt text. For example, this method will find the image by alt text "Castle":
   *
   * @param text Text to locate the element for.
   */
  default Locator getByAltText(String text) {
    return getByAltText(text, null);
  }
  /**
   * Allows locating elements by their alt text. For example, this method will find the image by alt text "Castle":
   *
   * @param text Text to locate the element for.
   */
  Locator getByAltText(String text, GetByAltTextOptions options);
  /**
   * Allows locating elements by their alt text. For example, this method will find the image by alt text "Castle":
   *
   * @param text Text to locate the element for.
   */
  default Locator getByAltText(Pattern text) {
    return getByAltText(text, null);
  }
  /**
   * Allows locating elements by their alt text. For example, this method will find the image by alt text "Castle":
   *
   * @param text Text to locate the element for.
   */
  Locator getByAltText(Pattern text, GetByAltTextOptions options);
  /**
   * Allows locating input elements by the text of the associated label. For example, this method will find the input by
   * label text Password in the following DOM:
   *
   * @param text Text to locate the element for.
   */
  default Locator getByLabel(String text) {
    return getByLabel(text, null);
  }
  /**
   * Allows locating input elements by the text of the associated label. For example, this method will find the input by
   * label text Password in the following DOM:
   *
   * @param text Text to locate the element for.
   */
  Locator getByLabel(String text, GetByLabelOptions options);
  /**
   * Allows locating input elements by the text of the associated label. For example, this method will find the input by
   * label text Password in the following DOM:
   *
   * @param text Text to locate the element for.
   */
  default Locator getByLabel(Pattern text) {
    return getByLabel(text, null);
  }
  /**
   * Allows locating input elements by the text of the associated label. For example, this method will find the input by
   * label text Password in the following DOM:
   *
   * @param text Text to locate the element for.
   */
  Locator getByLabel(Pattern text, GetByLabelOptions options);
  /**
   * Allows locating input elements by the placeholder text. For example, this method will find the input by placeholder
   * "Country":
   *
   * @param text Text to locate the element for.
   */
  default Locator getByPlaceholder(String text) {
    return getByPlaceholder(text, null);
  }
  /**
   * Allows locating input elements by the placeholder text. For example, this method will find the input by placeholder
   * "Country":
   *
   * @param text Text to locate the element for.
   */
  Locator getByPlaceholder(String text, GetByPlaceholderOptions options);
  /**
   * Allows locating input elements by the placeholder text. For example, this method will find the input by placeholder
   * "Country":
   *
   * @param text Text to locate the element for.
   */
  default Locator getByPlaceholder(Pattern text) {
    return getByPlaceholder(text, null);
  }
  /**
   * Allows locating input elements by the placeholder text. For example, this method will find the input by placeholder
   * "Country":
   *
   * @param text Text to locate the element for.
   */
  Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options);
  /**
   * Allows locating elements by their <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA role</a>, <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#aria-attributes">ARIA attributes</a> and <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. Note that role selector **does not
   * replace** accessibility audits and conformance tests, but rather gives early feedback about the ARIA guidelines.
   *
   * <p> Note that many html elements have an implicitly <a
   * href="https://w3c.github.io/html-aam/#html-element-role-mappings">defined role</a> that is recognized by the role
   * selector. You can find all the <a href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">supported roles here</a>.
   * ARIA guidelines **do not recommend** duplicating implicit roles and attributes by setting {@code role} and/or {@code aria-*}
   * attributes to default values.
   *
   * @param role Required aria role.
   */
  default Locator getByRole(AriaRole role) {
    return getByRole(role, null);
  }
  /**
   * Allows locating elements by their <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA role</a>, <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#aria-attributes">ARIA attributes</a> and <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. Note that role selector **does not
   * replace** accessibility audits and conformance tests, but rather gives early feedback about the ARIA guidelines.
   *
   * <p> Note that many html elements have an implicitly <a
   * href="https://w3c.github.io/html-aam/#html-element-role-mappings">defined role</a> that is recognized by the role
   * selector. You can find all the <a href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">supported roles here</a>.
   * ARIA guidelines **do not recommend** duplicating implicit roles and attributes by setting {@code role} and/or {@code aria-*}
   * attributes to default values.
   *
   * @param role Required aria role.
   */
  Locator getByRole(AriaRole role, GetByRoleOptions options);
  /**
   * Locate element by the test id. By default, the {@code data-testid} attribute is used as a test id. Use {@link
   * Selectors#setTestIdAttribute Selectors.setTestIdAttribute()} to configure a different test id attribute if necessary.
   *
   * @param testId Id to locate the element by.
   */
  Locator getByTestId(String testId);
  /**
   * Allows locating elements that contain given text.
   *
   * @param text Text to locate the element for.
   */
  default Locator getByText(String text) {
    return getByText(text, null);
  }
  /**
   * Allows locating elements that contain given text.
   *
   * @param text Text to locate the element for.
   */
  Locator getByText(String text, GetByTextOptions options);
  /**
   * Allows locating elements that contain given text.
   *
   * @param text Text to locate the element for.
   */
  default Locator getByText(Pattern text) {
    return getByText(text, null);
  }
  /**
   * Allows locating elements that contain given text.
   *
   * @param text Text to locate the element for.
   */
  Locator getByText(Pattern text, GetByTextOptions options);
  /**
   * Allows locating elements by their title. For example, this method will find the button by its title "Submit":
   *
   * @param text Text to locate the element for.
   */
  default Locator getByTitle(String text) {
    return getByTitle(text, null);
  }
  /**
   * Allows locating elements by their title. For example, this method will find the button by its title "Submit":
   *
   * @param text Text to locate the element for.
   */
  Locator getByTitle(String text, GetByTitleOptions options);
  /**
   * Allows locating elements by their title. For example, this method will find the button by its title "Submit":
   *
   * @param text Text to locate the element for.
   */
  default Locator getByTitle(Pattern text) {
    return getByTitle(text, null);
  }
  /**
   * Allows locating elements by their title. For example, this method will find the button by its title "Submit":
   *
   * @param text Text to locate the element for.
   */
  Locator getByTitle(Pattern text, GetByTitleOptions options);
  /**
   * Returns locator to the last matching frame.
   */
  FrameLocator last();
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors">working with
   * selectors</a> for more details.
   */
  default Locator locator(String selector) {
    return locator(selector, null);
  }
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors">working with
   * selectors</a> for more details.
   */
  Locator locator(String selector, LocatorOptions options);
  /**
   * Returns locator to the n-th matching frame. It's zero based, {@code nth(0)} selects the first frame.
   */
  FrameLocator nth(int index);
}

