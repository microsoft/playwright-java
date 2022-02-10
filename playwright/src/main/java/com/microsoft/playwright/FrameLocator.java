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
 * given selector.
 * <pre>{@code
 * // Throws if there are several frames in DOM:
 * page.frame_locator(".result-frame").locator("button").click();
 *
 * // Works because we explicitly tell locator to pick the first frame:
 * page.frame_locator(".result-frame").first().locator("button").click();
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
  class LocatorOptions {
    /**
     * Matches elements containing an element that matches an inner locator. Inner locator is queried against the outer one.
     * For example, {@code article} that has {@code text=Playwright} matches {@code <article><div>Playwright</div></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public Locator has;
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. For example,
     * {@code "Playwright"} matches {@code <article><div>Playwright</div></article>}.
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
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. For example,
     * {@code "Playwright"} matches {@code <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(String hasText) {
      this.hasText = hasText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. For example,
     * {@code "Playwright"} matches {@code <article><div>Playwright</div></article>}.
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
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors/">working with
   * selectors</a> for more details.
   */
  FrameLocator frameLocator(String selector);
  /**
   * Returns locator to the last matching frame.
   */
  FrameLocator last();
  /**
   * The method finds an element matching the specified selector in the FrameLocator's subtree.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors/">working with
   * selectors</a> for more details.
   */
  default Locator locator(String selector) {
    return locator(selector, null);
  }
  /**
   * The method finds an element matching the specified selector in the FrameLocator's subtree.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors/">working with
   * selectors</a> for more details.
   */
  Locator locator(String selector, LocatorOptions options);
  /**
   * Returns locator to the n-th matching frame.
   */
  FrameLocator nth(int index);
}

