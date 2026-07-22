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

import java.util.List;
import java.util.regex.Pattern;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;

/**
 * The {@code PageAssertions} class provides assertion methods that can be used to make assertions about the {@code Page}
 * state in the tests.
 * <pre>{@code
 * // ...
 * import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
 *
 * public class TestPage {
 *   // ...
 *   @Test
 *   void navigatesToLoginPage() {
 *     // ...
 *     page.getByText("Sign in").click();
 *     assertThat(page).hasURL(Pattern.compile(".*\/login"));
 *   }
 * }
 * }</pre>
 */
public interface PageAssertions {
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
  class HasScreenshotOptions {
    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different
     * treatment depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "disabled"}.
     */
    public ScreenshotAnimations animations;
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not
     * be changed.  Defaults to {@code "hide"}.
     */
    public ScreenshotCaret caret;
    /**
     * An object which specifies clipping of the resulting image.
     */
    public Clip clip;
    /**
     * When true, takes a screenshot of the full scrollable page, instead of the currently visible viewport. Defaults to
     * {@code false}.
     */
    public Boolean fullPage;
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} (customized by {@code maskColor}) that completely covers its bounding box.
     */
    public List<Locator> mask;
    /**
     * Specify the color of the overlay box for masked elements, in <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/color_value">CSS color format</a>. Default color is pink {@code
     * #FF00FF}.
     */
    public String maskColor;
    /**
     * An acceptable amount of pixels that could be different. Unset by default.
     */
    public Integer maxDiffPixels;
    /**
     * An acceptable ratio of pixels that are different to the total amount of pixels, between {@code 0} and {@code 1}. Unset
     * by default.
     */
    public Double maxDiffPixelRatio;
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg}
     * images. Defaults to {@code false}.
     */
    public Boolean omitBackground;
    /**
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices,
     * this will keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so
     * screenshots of high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "css"}.
     */
    public ScreenshotScale scale;
    /**
     * Text of the stylesheet to apply while making the screenshot. This is where you can hide dynamic elements, make elements
     * invisible or change their properties to help you creating repeatable screenshots.
     */
    public String style;
    /**
     * An acceptable perceived color difference between the same pixel in compared images, between zero (strict) and one
     * (lax), default is {@code 0.2}.
     */
    public Double threshold;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different
     * treatment depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "disabled"}.
     */
    public HasScreenshotOptions setAnimations(ScreenshotAnimations animations) {
      this.animations = animations;
      return this;
    }
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not
     * be changed.  Defaults to {@code "hide"}.
     */
    public HasScreenshotOptions setCaret(ScreenshotCaret caret) {
      this.caret = caret;
      return this;
    }
    /**
     * An object which specifies clipping of the resulting image.
     */
    public HasScreenshotOptions setClip(Clip clip) {
      this.clip = clip;
      return this;
    }
    /**
     * When true, takes a screenshot of the full scrollable page, instead of the currently visible viewport. Defaults to
     * {@code false}.
     */
    public HasScreenshotOptions setFullPage(boolean fullPage) {
      this.fullPage = fullPage;
      return this;
    }
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} (customized by {@code maskColor}) that completely covers its bounding box.
     */
    public HasScreenshotOptions setMask(List<Locator> mask) {
      this.mask = mask;
      return this;
    }
    /**
     * Specify the color of the overlay box for masked elements, in <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/color_value">CSS color format</a>. Default color is pink {@code
     * #FF00FF}.
     */
    public HasScreenshotOptions setMaskColor(String maskColor) {
      this.maskColor = maskColor;
      return this;
    }
    /**
     * An acceptable amount of pixels that could be different. Unset by default.
     */
    public HasScreenshotOptions setMaxDiffPixels(int maxDiffPixels) {
      this.maxDiffPixels = maxDiffPixels;
      return this;
    }
    /**
     * An acceptable ratio of pixels that are different to the total amount of pixels, between {@code 0} and {@code 1}. Unset
     * by default.
     */
    public HasScreenshotOptions setMaxDiffPixelRatio(double maxDiffPixelRatio) {
      this.maxDiffPixelRatio = maxDiffPixelRatio;
      return this;
    }
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg}
     * images. Defaults to {@code false}.
     */
    public HasScreenshotOptions setOmitBackground(boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    /**
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices,
     * this will keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so
     * screenshots of high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "css"}.
     */
    public HasScreenshotOptions setScale(ScreenshotScale scale) {
      this.scale = scale;
      return this;
    }
    /**
     * Text of the stylesheet to apply while making the screenshot. This is where you can hide dynamic elements, make elements
     * invisible or change their properties to help you creating repeatable screenshots.
     */
    public HasScreenshotOptions setStyle(String style) {
      this.style = style;
      return this;
    }
    /**
     * An acceptable perceived color difference between the same pixel in compared images, between zero (strict) and one
     * (lax), default is {@code 0.2}.
     */
    public HasScreenshotOptions setThreshold(double threshold) {
      this.threshold = threshold;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasScreenshotOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasTitleOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasTitleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasURLOptions {
    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression parameter if specified. A provided predicate ignores this flag.
     */
    public Boolean ignoreCase;
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Whether to perform case-insensitive match. {@code ignoreCase} option takes precedence over the corresponding regular
     * expression parameter if specified. A provided predicate ignores this flag.
     */
    public HasURLOptions setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
      return this;
    }
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasURLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Makes the assertion check for the opposite condition.
   *
   * <p> <strong>Usage</strong>
   *
   * <p> For example, this code tests that the page URL doesn't contain {@code "error"}:
   * <pre>{@code
   * assertThat(page).not().hasURL("error");
   * }</pre>
   *
   * @since v1.20
   */
  PageAssertions not();
  /**
   * Asserts that the page body matches the given <a href="https://playwright.dev/java/docs/aria-snapshots">accessibility
   * snapshot</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.navigate("https://demo.playwright.dev/todomvc/");
   * assertThat(page).matchesAriaSnapshot("""
   *   - heading "todos"
   *   - textbox "What needs to be done?"
   * """);
   * }</pre>
   *
   * @since v1.60
   */
  default void matchesAriaSnapshot(String expected) {
    matchesAriaSnapshot(expected, null);
  }
  /**
   * Asserts that the page body matches the given <a href="https://playwright.dev/java/docs/aria-snapshots">accessibility
   * snapshot</a>.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.navigate("https://demo.playwright.dev/todomvc/");
   * assertThat(page).matchesAriaSnapshot("""
   *   - heading "todos"
   *   - textbox "What needs to be done?"
   * """);
   * }</pre>
   *
   * @since v1.60
   */
  void matchesAriaSnapshot(String expected, MatchesAriaSnapshotOptions options);
  /**
   * This function will wait until two consecutive page screenshots yield the same result, and then compare the last
   * screenshot with the expectation.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasScreenshot("image.png");
   * }</pre>
   *
   * <p> Note that screenshot assertions only work with the Playwright driver's screenshot comparison support; there is no
   * concept of a test-runner-managed snapshot directory as in {@code @playwright/test}. By default, baseline images are
   * stored under {@code src/test/resources/__screenshots__/<TestClassName>/<name>}, overridable via the {@code
   * playwright.snapshotDir} system property. Pass {@code -Dplaywright.updateSnapshots=true} to (re-)generate baselines.
   *
   * @param name Snapshot name. Must have a {@code .png} or {@code .webp} extension, the screenshot is captured in the corresponding format. Both formats are lossless.
   * @since v1.23
   */
  default void hasScreenshot(String name) {
    hasScreenshot(name, null);
  }
  /**
   * This function will wait until two consecutive page screenshots yield the same result, and then compare the last
   * screenshot with the expectation.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasScreenshot("image.png");
   * }</pre>
   *
   * @param name Snapshot name. Must have a {@code .png} or {@code .webp} extension, the screenshot is captured in the corresponding format. Both formats are lossless.
   * @since v1.23
   */
  void hasScreenshot(String name, HasScreenshotOptions options);
  /**
   * This function will wait until two consecutive page screenshots yield the same result, and then compare the last
   * screenshot with the expectation.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasScreenshot(new String[] {"folder", "image.png"});
   * }</pre>
   *
   * @param nameSegments Snapshot name segments that will be joined to form the file name. Must have a {@code .png} or {@code .webp} extension on the last segment.
   * @since v1.23
   */
  default void hasScreenshot(String[] nameSegments) {
    hasScreenshot(nameSegments, null);
  }
  /**
   * This function will wait until two consecutive page screenshots yield the same result, and then compare the last
   * screenshot with the expectation.
   *
   * @param nameSegments Snapshot name segments that will be joined to form the file name. Must have a {@code .png} or {@code .webp} extension on the last segment.
   * @since v1.23
   */
  void hasScreenshot(String[] nameSegments, HasScreenshotOptions options);
  /**
   * This function will wait until two consecutive page screenshots yield the same result, and then compare the last
   * screenshot with the expectation.
   *
   * <p> The snapshot is stored in the PNG format. To store it in the WebP format instead, pass a snapshot name with the
   * {@code .webp} extension via {@link com.microsoft.playwright.assertions.PageAssertions#hasScreenshot
   * PageAssertions.hasScreenshot()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasScreenshot();
   * }</pre>
   *
   * @since v1.23
   */
  default void hasScreenshot() {
    hasScreenshot((HasScreenshotOptions) null);
  }
  /**
   * This function will wait until two consecutive page screenshots yield the same result, and then compare the last
   * screenshot with the expectation.
   *
   * @since v1.23
   */
  void hasScreenshot(HasScreenshotOptions options);
  /**
   * Ensures the page has the given title.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  default void hasTitle(String titleOrRegExp) {
    hasTitle(titleOrRegExp, null);
  }
  /**
   * Ensures the page has the given title.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  void hasTitle(String titleOrRegExp, HasTitleOptions options);
  /**
   * Ensures the page has the given title.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  default void hasTitle(Pattern titleOrRegExp) {
    hasTitle(titleOrRegExp, null);
  }
  /**
   * Ensures the page has the given title.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  void hasTitle(Pattern titleOrRegExp, HasTitleOptions options);
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  default void hasURL(String urlOrRegExp) {
    hasURL(urlOrRegExp, null);
  }
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  void hasURL(String urlOrRegExp, HasURLOptions options);
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  default void hasURL(Pattern urlOrRegExp) {
    hasURL(urlOrRegExp, null);
  }
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  void hasURL(Pattern urlOrRegExp, HasURLOptions options);
}

