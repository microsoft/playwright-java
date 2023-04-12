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
 * FrameLocator represents a view to the {@code iframe} on the page. It captures the logic sufficient to retrieve the
 * {@code iframe} and locate elements in that iframe. FrameLocator can be created with either {@link Page#frameLocator
 * Page.frameLocator()} or {@link Locator#frameLocator Locator.frameLocator()} method.
 * <pre>{@code
 * Locator locator = page.frameLocator("#my-frame").getByText("Submit");
 * locator.click();
 * }</pre>
 *
 * <p> **Strictness**
 *
 * <p> Frame locators are strict. This means that all operations on frame locators will throw if more than one element matches
 * a given selector.
 * <pre>{@code
 * // Throws if there are several frames in DOM:
 * page.frame_locator(".result-frame").getByRole(AriaRole.BUTTON).click();
 *
 * // Works because we explicitly tell locator to pick the first frame:
 * page.frame_locator(".result-frame").first().getByRole(AriaRole.BUTTON).click();
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
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByAltTextOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByLabelOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByLabelOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByPlaceholderOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByPlaceholderOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByRoleOptions {
    /**
     * An attribute that is usually set by {@code aria-checked} or native {@code <input type=checkbox>} controls.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-checked">{@code aria-checked}</a>.
     */
    public Boolean checked;
    /**
     * An attribute that is usually set by {@code aria-disabled} or {@code disabled}.
     *
     * <p> <strong>NOTE:</strong> Unlike most other attributes, {@code disabled} is inherited through the DOM hierarchy. Learn more about <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#aria-disabled">{@code aria-disabled}</a>.
     */
    public Boolean disabled;
    /**
     * Whether {@code name} is matched exactly: case-sensitive and whole-string. Defaults to false. Ignored when {@code name}
     * is a regular expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;
    /**
     * An attribute that is usually set by {@code aria-expanded}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-expanded">{@code aria-expanded}</a>.
     */
    public Boolean expanded;
    /**
     * Option that controls whether hidden elements are matched. By default, only non-hidden elements, as <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#tree_exclusion">defined by ARIA</a>, are matched by role selector.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-hidden">{@code aria-hidden}</a>.
     */
    public Boolean includeHidden;
    /**
     * A number attribute that is usually present for roles {@code heading}, {@code listitem}, {@code row}, {@code treeitem},
     * with default values for {@code <h1>-<h6>} elements.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-level">{@code aria-level}</a>.
     */
    public Integer level;
    /**
     * Option to match the <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. By default,
     * matching is case-insensitive and searches for a substring, use {@code exact} to control this behavior.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public Object name;
    /**
     * An attribute that is usually set by {@code aria-pressed}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-pressed">{@code aria-pressed}</a>.
     */
    public Boolean pressed;
    /**
     * An attribute that is usually set by {@code aria-selected}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-selected">{@code aria-selected}</a>.
     */
    public Boolean selected;

    /**
     * An attribute that is usually set by {@code aria-checked} or native {@code <input type=checkbox>} controls.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-checked">{@code aria-checked}</a>.
     */
    public GetByRoleOptions setChecked(boolean checked) {
      this.checked = checked;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-disabled} or {@code disabled}.
     *
     * <p> <strong>NOTE:</strong> Unlike most other attributes, {@code disabled} is inherited through the DOM hierarchy. Learn more about <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#aria-disabled">{@code aria-disabled}</a>.
     */
    public GetByRoleOptions setDisabled(boolean disabled) {
      this.disabled = disabled;
      return this;
    }
    /**
     * Whether {@code name} is matched exactly: case-sensitive and whole-string. Defaults to false. Ignored when {@code name}
     * is a regular expression. Note that exact match still trims whitespace.
     */
    public GetByRoleOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-expanded}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-expanded">{@code aria-expanded}</a>.
     */
    public GetByRoleOptions setExpanded(boolean expanded) {
      this.expanded = expanded;
      return this;
    }
    /**
     * Option that controls whether hidden elements are matched. By default, only non-hidden elements, as <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#tree_exclusion">defined by ARIA</a>, are matched by role selector.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-hidden">{@code aria-hidden}</a>.
     */
    public GetByRoleOptions setIncludeHidden(boolean includeHidden) {
      this.includeHidden = includeHidden;
      return this;
    }
    /**
     * A number attribute that is usually present for roles {@code heading}, {@code listitem}, {@code row}, {@code treeitem},
     * with default values for {@code <h1>-<h6>} elements.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-level">{@code aria-level}</a>.
     */
    public GetByRoleOptions setLevel(int level) {
      this.level = level;
      return this;
    }
    /**
     * Option to match the <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. By default,
     * matching is case-insensitive and searches for a substring, use {@code exact} to control this behavior.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public GetByRoleOptions setName(String name) {
      this.name = name;
      return this;
    }
    /**
     * Option to match the <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. By default,
     * matching is case-insensitive and searches for a substring, use {@code exact} to control this behavior.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public GetByRoleOptions setName(Pattern name) {
      this.name = name;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-pressed}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-pressed">{@code aria-pressed}</a>.
     */
    public GetByRoleOptions setPressed(boolean pressed) {
      this.pressed = pressed;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-selected}.
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
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByTextOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByTitleOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
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
     * Matches elements that do not contain an element that matches an inner locator. Inner locator is queried against the
     * outer one. For example, {@code article} that does not have {@code div} matches {@code
     * <article><span>Playwright</span></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public Locator hasNot;
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public Object hasNotText;
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
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
     * Matches elements that do not contain an element that matches an inner locator. Inner locator is queried against the
     * outer one. For example, {@code article} that does not have {@code div} matches {@code
     * <article><span>Playwright</span></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public LocatorOptions setHasNot(Locator hasNot) {
      this.hasNot = hasNot;
      return this;
    }
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public LocatorOptions setHasNotText(String hasNotText) {
      this.hasNotText = hasNotText;
      return this;
    }
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public LocatorOptions setHasNotText(Pattern hasNotText) {
      this.hasNotText = hasNotText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(String hasText) {
      this.hasText = hasText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(Pattern hasText) {
      this.hasText = hasText;
      return this;
    }
  }
  /**
   * Returns locator to the first matching frame.
   *
   * @since v1.17
   */
  FrameLocator first();
  /**
   * When working with iframes, you can create a frame locator that will enter the iframe and allow selecting elements in
   * that iframe.
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.17
   */
  FrameLocator frameLocator(String selector);
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByAltText(String text) {
    return getByAltText(text, null);
  }
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByAltText(String text, GetByAltTextOptions options);
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByAltText(Pattern text) {
    return getByAltText(text, null);
  }
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByAltText(Pattern text, GetByAltTextOptions options);
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByLabel(String text) {
    return getByLabel(text, null);
  }
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByLabel(String text, GetByLabelOptions options);
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByLabel(Pattern text) {
    return getByLabel(text, null);
  }
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByLabel(Pattern text, GetByLabelOptions options);
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByPlaceholder(String text) {
    return getByPlaceholder(text, null);
  }
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByPlaceholder(String text, GetByPlaceholderOptions options);
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByPlaceholder(Pattern text) {
    return getByPlaceholder(text, null);
  }
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options);
  /**
   * Allows locating elements by their <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA role</a>, <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#aria-attributes">ARIA attributes</a> and <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate each element by it's implicit role:
   * <pre>{@code
   * assertThat(page
   *     .getByRole(AriaRole.HEADING,
   *                new Page.GetByRoleOptions().setName("Sign up")))
   *     .isVisible();
   *
   * page.getByRole(AriaRole.CHECKBOX,
   *                new Page.GetByRoleOptions().setName("Subscribe"))
   *     .check();
   *
   * page.getByRole(AriaRole.BUTTON,
   *                new Page.GetByRoleOptions().setName(
   *                    Pattern.compile("submit", Pattern.CASE_INSENSITIVE)))
   *     .click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Role selector **does not replace** accessibility audits and conformance tests, but rather gives early feedback about the
   * ARIA guidelines.
   *
   * <p> Many html elements have an implicitly <a href="https://w3c.github.io/html-aam/#html-element-role-mappings">defined
   * role</a> that is recognized by the role selector. You can find all the <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">supported roles here</a>. ARIA guidelines **do not
   * recommend** duplicating implicit roles and attributes by setting {@code role} and/or {@code aria-*} attributes to
   * default values.
   *
   * @param role Required aria role.
   * @since v1.27
   */
  default Locator getByRole(AriaRole role) {
    return getByRole(role, null);
  }
  /**
   * Allows locating elements by their <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA role</a>, <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#aria-attributes">ARIA attributes</a> and <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate each element by it's implicit role:
   * <pre>{@code
   * assertThat(page
   *     .getByRole(AriaRole.HEADING,
   *                new Page.GetByRoleOptions().setName("Sign up")))
   *     .isVisible();
   *
   * page.getByRole(AriaRole.CHECKBOX,
   *                new Page.GetByRoleOptions().setName("Subscribe"))
   *     .check();
   *
   * page.getByRole(AriaRole.BUTTON,
   *                new Page.GetByRoleOptions().setName(
   *                    Pattern.compile("submit", Pattern.CASE_INSENSITIVE)))
   *     .click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Role selector **does not replace** accessibility audits and conformance tests, but rather gives early feedback about the
   * ARIA guidelines.
   *
   * <p> Many html elements have an implicitly <a href="https://w3c.github.io/html-aam/#html-element-role-mappings">defined
   * role</a> that is recognized by the role selector. You can find all the <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">supported roles here</a>. ARIA guidelines **do not
   * recommend** duplicating implicit roles and attributes by setting {@code role} and/or {@code aria-*} attributes to
   * default values.
   *
   * @param role Required aria role.
   * @since v1.27
   */
  Locator getByRole(AriaRole role, GetByRoleOptions options);
  /**
   * Locate element by the test id.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate the element by it's test id:
   * <pre>{@code
   * page.getByTestId("directions").click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> By default, the {@code data-testid} attribute is used as a test id. Use {@link Selectors#setTestIdAttribute
   * Selectors.setTestIdAttribute()} to configure a different test id attribute if necessary.
   *
   * @param testId Id to locate the element by.
   * @since v1.27
   */
  Locator getByTestId(String testId);
  /**
   * Locate element by the test id.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate the element by it's test id:
   * <pre>{@code
   * page.getByTestId("directions").click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> By default, the {@code data-testid} attribute is used as a test id. Use {@link Selectors#setTestIdAttribute
   * Selectors.setTestIdAttribute()} to configure a different test id attribute if necessary.
   *
   * @param testId Id to locate the element by.
   * @since v1.27
   */
  Locator getByTestId(Pattern testId);
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByText(String text) {
    return getByText(text, null);
  }
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByText(String text, GetByTextOptions options);
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByText(Pattern text) {
    return getByText(text, null);
  }
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByText(Pattern text, GetByTextOptions options);
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByTitle(String text) {
    return getByTitle(text, null);
  }
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByTitle(String text, GetByTitleOptions options);
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByTitle(Pattern text) {
    return getByTitle(text, null);
  }
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByTitle(Pattern text, GetByTitleOptions options);
  /**
   * Returns locator to the last matching frame.
   *
   * @since v1.17
   */
  FrameLocator last();
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selectorOrLocator A selector or locator to use when resolving DOM element.
   * @since v1.17
   */
  default Locator locator(String selectorOrLocator) {
    return locator(selectorOrLocator, null);
  }
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selectorOrLocator A selector or locator to use when resolving DOM element.
   * @since v1.17
   */
  Locator locator(String selectorOrLocator, LocatorOptions options);
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selectorOrLocator A selector or locator to use when resolving DOM element.
   * @since v1.17
   */
  default Locator locator(Locator selectorOrLocator) {
    return locator(selectorOrLocator, null);
  }
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selectorOrLocator A selector or locator to use when resolving DOM element.
   * @since v1.17
   */
  Locator locator(Locator selectorOrLocator, LocatorOptions options);
  /**
   * Returns locator to the n-th matching frame. It's zero based, {@code nth(0)} selects the first frame.
   *
   * @since v1.17
   */
  FrameLocator nth(int index);
}

