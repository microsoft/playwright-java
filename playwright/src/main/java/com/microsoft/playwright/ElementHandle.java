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
import java.nio.file.Path;
import java.util.*;

/**
 * ElementHandle represents an in-page DOM element. ElementHandles can be created with the {@link Page#querySelector
 * Page.querySelector()} method.
 *
 * <p> <strong>NOTE:</strong> The use of ElementHandle is discouraged, use {@code Locator} objects and web-first assertions instead.
 * <pre>{@code
 * ElementHandle hrefElement = page.querySelector("a");
 * hrefElement.click();
 * }</pre>
 *
 * <p> ElementHandle prevents DOM element from garbage collection unless the handle is disposed with {@link JSHandle#dispose
 * JSHandle.dispose()}. ElementHandles are auto-disposed when their origin frame gets navigated.
 *
 * <p> ElementHandle instances can be used as an argument in {@link Page#evalOnSelector Page.evalOnSelector()} and {@link
 * Page#evaluate Page.evaluate()} methods.
 *
 * <p> The difference between the {@code Locator} and ElementHandle is that the ElementHandle points to a particular element,
 * while {@code Locator} captures the logic of how to retrieve an element.
 *
 * <p> In the example below, handle points to a particular DOM element on page. If that element changes text or is used by
 * React to render an entirely different component, handle is still pointing to that very DOM element. This can lead to
 * unexpected behaviors.
 * <pre>{@code
 * ElementHandle handle = page.querySelector("text=Submit");
 * handle.hover();
 * handle.click();
 * }</pre>
 *
 * <p> With the locator, every time the {@code element} is used, up-to-date DOM element is located in the page using the
 * selector. So in the snippet below, underlying DOM element is going to be located twice.
 * <pre>{@code
 * Locator locator = page.getByText("Submit");
 * locator.hover();
 * locator.click();
 * }</pre>
 */
public interface ElementHandle extends JSHandle {
  class CheckOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public CheckOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public CheckOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public CheckOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public CheckOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public CheckOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public CheckOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class ClickOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Defaults to {@code left}.
     */
    public ClickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public ClickOptions setClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public ClickOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public ClickOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public ClickOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public ClickOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public ClickOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public ClickOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ClickOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public ClickOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class DblclickOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Defaults to {@code left}.
     */
    public DblclickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public DblclickOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public DblclickOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public DblclickOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public DblclickOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public DblclickOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public DblclickOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public DblclickOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public DblclickOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class FillOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public FillOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public FillOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public FillOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HoverOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public HoverOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public HoverOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public HoverOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public HoverOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public HoverOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public HoverOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public HoverOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class InputValueOptions {
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public InputValueOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PressOptions {
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public PressOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public PressOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public PressOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ScreenshotOptions {
    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different
     * treatment depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "allow"} that leaves animations untouched.
     */
    public ScreenshotAnimations animations;
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not
     * be changed.  Defaults to {@code "hide"}.
     */
    public ScreenshotCaret caret;
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
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg}
     * images. Defaults to {@code false}.
     */
    public Boolean omitBackground;
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a
     * relative path, then it is resolved relative to the current working directory. If no path is provided, the image won't be
     * saved to the disk.
     */
    public Path path;
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public Integer quality;
    /**
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices,
     * this will keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so
     * screenshots of high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "device"}.
     */
    public ScreenshotScale scale;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public ScreenshotType type;

    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different
     * treatment depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "allow"} that leaves animations untouched.
     */
    public ScreenshotOptions setAnimations(ScreenshotAnimations animations) {
      this.animations = animations;
      return this;
    }
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not
     * be changed.  Defaults to {@code "hide"}.
     */
    public ScreenshotOptions setCaret(ScreenshotCaret caret) {
      this.caret = caret;
      return this;
    }
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} (customized by {@code maskColor}) that completely covers its bounding box.
     */
    public ScreenshotOptions setMask(List<Locator> mask) {
      this.mask = mask;
      return this;
    }
    /**
     * Specify the color of the overlay box for masked elements, in <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/color_value">CSS color format</a>. Default color is pink {@code
     * #FF00FF}.
     */
    public ScreenshotOptions setMaskColor(String maskColor) {
      this.maskColor = maskColor;
      return this;
    }
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg}
     * images. Defaults to {@code false}.
     */
    public ScreenshotOptions setOmitBackground(boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a
     * relative path, then it is resolved relative to the current working directory. If no path is provided, the image won't be
     * saved to the disk.
     */
    public ScreenshotOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public ScreenshotOptions setQuality(int quality) {
      this.quality = quality;
      return this;
    }
    /**
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices,
     * this will keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so
     * screenshots of high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "device"}.
     */
    public ScreenshotOptions setScale(ScreenshotScale scale) {
      this.scale = scale;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ScreenshotOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public ScreenshotOptions setType(ScreenshotType type) {
      this.type = type;
      return this;
    }
  }
  class ScrollIntoViewIfNeededOptions {
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ScrollIntoViewIfNeededOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectOptionOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public SelectOptionOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SelectOptionOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SelectOptionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectTextOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public SelectTextOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SelectTextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetCheckedOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public SetCheckedOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SetCheckedOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public SetCheckedOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public SetCheckedOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public SetCheckedOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class SetInputFilesOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SetInputFilesOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetInputFilesOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public TapOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public TapOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public TapOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public TapOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public TapOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public TapOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public TapOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class TypeOptions {
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public TypeOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public TypeOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public TypeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public UncheckOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public UncheckOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public UncheckOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public UncheckOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public UncheckOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public UncheckOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class WaitForElementStateOptions {
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForElementStateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForSelectorOptions {
    /**
     * Defaults to {@code "visible"}. Can be either:
     * <ul>
     * <li> {@code "attached"} - wait for element to be present in DOM.</li>
     * <li> {@code "detached"} - wait for element to not be present in DOM.</li>
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element
     * without any content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code
     * visibility:hidden}. This is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    public WaitForSelectorState state;
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Defaults to {@code "visible"}. Can be either:
     * <ul>
     * <li> {@code "attached"} - wait for element to be present in DOM.</li>
     * <li> {@code "detached"} - wait for element to not be present in DOM.</li>
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element
     * without any content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code
     * visibility:hidden}. This is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    public WaitForSelectorOptions setState(WaitForSelectorState state) {
      this.state = state;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public WaitForSelectorOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForSelectorOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * This method returns the bounding box of the element, or {@code null} if the element is not visible. The bounding box is
   * calculated relative to the main frame viewport - which is usually the same as the browser window.
   *
   * <p> Scrolling affects the returned bounding box, similarly to <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">Element.getBoundingClientRect</a>.
   * That means {@code x} and/or {@code y} may be negative.
   *
   * <p> Elements from child frames return the bounding box relative to the main frame, unlike the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">Element.getBoundingClientRect</a>.
   *
   * <p> Assuming the page is static, it is safe to use bounding box coordinates to perform input. For example, the following
   * snippet should click the center of the element.
   *
   * <p> **Usage**
   * <pre>{@code
   * BoundingBox box = elementHandle.boundingBox();
   * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
   * }</pre>
   *
   * @since v1.8
   */
  BoundingBox boundingBox();
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already checked, this
   * method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  default void check() {
    check(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already checked, this
   * method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  void check(CheckOptions options);
  /**
   * This method clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  default void click() {
    click(null);
  }
  /**
   * This method clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  void click(ClickOptions options);
  /**
   * Returns the content frame for element handles referencing iframe nodes, or {@code null} otherwise
   *
   * @since v1.8
   */
  Frame contentFrame();
  /**
   * This method double clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   * first click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @since v1.8
   */
  default void dblclick() {
    dblclick(null);
  }
  /**
   * This method double clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   * first click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @since v1.8
   */
  void dblclick(DblclickOptions options);
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * elementHandle.dispatchEvent("click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code
   * eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by
   * default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <ul>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
   * </ul>
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <pre>{@code
   * // Note you can only create DataTransfer in Chromium and Firefox
   * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * elementHandle.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @since v1.8
   */
  default void dispatchEvent(String type) {
    dispatchEvent(type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * elementHandle.dispatchEvent("click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code
   * eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by
   * default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <ul>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
   * </ul>
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <pre>{@code
   * // Note you can only create DataTransfer in Chromium and Firefox
   * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * elementHandle.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.8
   */
  void dispatchEvent(String type, Object eventInit);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a
   * first argument to {@code expression}. If no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * ElementHandle#evalOnSelector ElementHandle.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * ElementHandle tweetHandle = page.querySelector(".tweet");
   * assertEquals("100", tweetHandle.evalOnSelector(".like", "node => node.innerText"));
   * assertEquals("10", tweetHandle.evalOnSelector(".retweets", "node => node.innerText"));
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.9
   */
  default Object evalOnSelector(String selector, String expression) {
    return evalOnSelector(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a
   * first argument to {@code expression}. If no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * ElementHandle#evalOnSelector ElementHandle.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * ElementHandle tweetHandle = page.querySelector(".tweet");
   * assertEquals("100", tweetHandle.evalOnSelector(".like", "node => node.innerText"));
   * assertEquals("10", tweetHandle.evalOnSelector(".retweets", "node => node.innerText"));
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  Object evalOnSelector(String selector, String expression, Object arg);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array
   * of matched elements as a first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * ElementHandle#evalOnSelectorAll ElementHandle.evalOnSelectorAll()} would wait for the promise to resolve and return its
   * value.
   *
   * <p> **Usage**
   * <pre>{@code
   * ElementHandle feedHandle = page.querySelector(".feed");
   * assertEquals(Arrays.asList("Hello!", "Hi!"), feedHandle.evalOnSelectorAll(".tweet", "nodes => nodes.map(n => n.innerText)"));
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.9
   */
  default Object evalOnSelectorAll(String selector, String expression) {
    return evalOnSelectorAll(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array
   * of matched elements as a first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * ElementHandle#evalOnSelectorAll ElementHandle.evalOnSelectorAll()} would wait for the promise to resolve and return its
   * value.
   *
   * <p> **Usage**
   * <pre>{@code
   * ElementHandle feedHandle = page.querySelector(".feed");
   * assertEquals(Arrays.asList("Hello!", "Hi!"), feedHandle.evalOnSelectorAll(".tweet", "nodes => nodes.map(n => n.innerText)"));
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  Object evalOnSelectorAll(String selector, String expression, Object arg);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
   * element, fills it and triggers an {@code input} event after filling. Note that you can pass an empty string to clear the
   * input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#pressSequentially Locator.pressSequentially()}.
   *
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   * @since v1.8
   */
  default void fill(String value) {
    fill(value, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
   * element, fills it and triggers an {@code input} event after filling. Note that you can pass an empty string to clear the
   * input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#pressSequentially Locator.pressSequentially()}.
   *
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   * @since v1.8
   */
  void fill(String value, FillOptions options);
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the element.
   *
   * @since v1.8
   */
  void focus();
  /**
   * Returns element attribute value.
   *
   * @param name Attribute name to get the value for.
   * @since v1.8
   */
  String getAttribute(String name);
  /**
   * This method hovers over the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  default void hover() {
    hover(null);
  }
  /**
   * This method hovers over the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  void hover(HoverOptions options);
  /**
   * Returns the {@code element.innerHTML}.
   *
   * @since v1.8
   */
  String innerHTML();
  /**
   * Returns the {@code element.innerText}.
   *
   * @since v1.8
   */
  String innerText();
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @since v1.13
   */
  default String inputValue() {
    return inputValue(null);
  }
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @since v1.13
   */
  String inputValue(InputValueOptions options);
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @since v1.8
   */
  boolean isChecked();
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @since v1.8
   */
  boolean isDisabled();
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * @since v1.8
   */
  boolean isEditable();
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @since v1.8
   */
  boolean isEnabled();
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   *
   * @since v1.8
   */
  boolean isHidden();
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   *
   * @since v1.8
   */
  boolean isVisible();
  /**
   * Returns the frame containing the given element.
   *
   * @since v1.8
   */
  Frame ownerFrame();
  /**
   * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with
   * the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  default void press(String key) {
    press(key, null);
  }
  /**
   * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with
   * the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  void press(String key, PressOptions options);
  /**
   * The method finds an element matching the specified selector in the {@code ElementHandle}'s subtree. If no elements match
   * the selector, returns {@code null}.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  ElementHandle querySelector(String selector);
  /**
   * The method finds all elements matching the specified selector in the {@code ElementHandle}s subtree. If no elements
   * match the selector, returns empty array.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  List<ElementHandle> querySelectorAll(String selector);
  /**
   * This method captures a screenshot of the page, clipped to the size and position of this particular element. If the
   * element is covered by other elements, it will not be actually visible on the screenshot. If the element is a scrollable
   * container, only the currently scrolled content will be visible on the screenshot.
   *
   * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then
   * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   *
   * <p> Returns the buffer with the captured screenshot.
   *
   * @since v1.8
   */
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * This method captures a screenshot of the page, clipped to the size and position of this particular element. If the
   * element is covered by other elements, it will not be actually visible on the screenshot. If the element is a scrollable
   * container, only the currently scrolled content will be visible on the screenshot.
   *
   * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then
   * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   *
   * <p> Returns the buffer with the captured screenshot.
   *
   * @since v1.8
   */
  byte[] screenshot(ScreenshotOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then tries to
   * scroll element into view, unless it is completely visible as defined by <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code
   * ratio}.
   *
   * <p> Throws when {@code elementHandle} does not point to an element <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected">connected</a> to a Document or a ShadowRoot.
   *
   * @since v1.8
   */
  default void scrollIntoViewIfNeeded() {
    scrollIntoViewIfNeeded(null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then tries to
   * scroll element into view, unless it is completely visible as defined by <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code
   * ratio}.
   *
   * <p> Throws when {@code elementHandle} does not point to an element <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected">connected</a> to a Document or a ShadowRoot.
   *
   * @since v1.8
   */
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(ElementHandle values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(ElementHandle values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String[] values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String[] values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(SelectOption values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(SelectOption values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(ElementHandle[] values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(ElementHandle[] values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(SelectOption[] values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * handle.selectOption("blue");
   * // single selection matching the label
   * handle.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * handle.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(SelectOption[] values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then focuses
   * the element and selects all its text content.
   *
   * <p> If the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, focuses and selects text
   * in the control instead.
   *
   * @since v1.8
   */
  default void selectText() {
    selectText(null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then focuses
   * the element and selects all its text content.
   *
   * <p> If the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, focuses and selects text
   * in the control instead.
   *
   * @since v1.8
   */
  void selectText(SelectTextOptions options);
  /**
   * This method checks or unchecks an element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws.</li>
   * <li> If the element already has the right checked state, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked or unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param checked Whether to check or uncheck the checkbox.
   * @since v1.15
   */
  default void setChecked(boolean checked) {
    setChecked(checked, null);
  }
  /**
   * This method checks or unchecks an element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws.</li>
   * <li> If the element already has the right checked state, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked or unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param checked Whether to check or uncheck the checkbox.
   * @since v1.15
   */
  void setChecked(boolean checked, SetCheckedOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  default void setInputFiles(Path files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  void setInputFiles(Path files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  default void setInputFiles(Path[] files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  void setInputFiles(Path[] files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  default void setInputFiles(FilePayload files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  void setInputFiles(FilePayload files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  default void setInputFiles(FilePayload[] files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code ElementHandle} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.8
   */
  void setInputFiles(FilePayload[] files, SetInputFilesOptions options);
  /**
   * This method taps the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @since v1.8
   */
  default void tap() {
    tap(null);
  }
  /**
   * This method taps the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @since v1.8
   */
  void tap(TapOptions options);
  /**
   * Returns the {@code node.textContent}.
   *
   * @since v1.8
   */
  String textContent();
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  default void type(String text) {
    type(text, null);
  }
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  void type(String text, TypeOptions options);
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already unchecked,
   * this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  default void uncheck() {
    uncheck(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already unchecked,
   * this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless {@code
   * force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @since v1.8
   */
  void uncheck(UncheckOptions options);
  /**
   * Returns when the element satisfies the {@code state}.
   *
   * <p> Depending on the {@code state} parameter, this method waits for one of the <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks to pass. This method throws when the
   * element is detached while waiting, unless waiting for the {@code "hidden"} state.
   * <ul>
   * <li> {@code "visible"} Wait until the element is <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.</li>
   * <li> {@code "hidden"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability#visible">not
   * visible</a> or <a href="https://playwright.dev/java/docs/actionability#attached">not attached</a>. Note that waiting for
   * hidden does not throw when the element detaches.</li>
   * <li> {@code "stable"} Wait until the element is both <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a> and <a
   * href="https://playwright.dev/java/docs/actionability#stable">stable</a>.</li>
   * <li> {@code "enabled"} Wait until the element is <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.</li>
   * <li> {@code "disabled"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability#enabled">not
   * enabled</a>.</li>
   * <li> {@code "editable"} Wait until the element is <a
   * href="https://playwright.dev/java/docs/actionability#editable">editable</a>.</li>
   * </ul>
   *
   * <p> If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
   *
   * @param state A state to wait for, see below for more details.
   * @since v1.8
   */
  default void waitForElementState(ElementState state) {
    waitForElementState(state, null);
  }
  /**
   * Returns when the element satisfies the {@code state}.
   *
   * <p> Depending on the {@code state} parameter, this method waits for one of the <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks to pass. This method throws when the
   * element is detached while waiting, unless waiting for the {@code "hidden"} state.
   * <ul>
   * <li> {@code "visible"} Wait until the element is <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.</li>
   * <li> {@code "hidden"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability#visible">not
   * visible</a> or <a href="https://playwright.dev/java/docs/actionability#attached">not attached</a>. Note that waiting for
   * hidden does not throw when the element detaches.</li>
   * <li> {@code "stable"} Wait until the element is both <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a> and <a
   * href="https://playwright.dev/java/docs/actionability#stable">stable</a>.</li>
   * <li> {@code "enabled"} Wait until the element is <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.</li>
   * <li> {@code "disabled"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability#enabled">not
   * enabled</a>.</li>
   * <li> {@code "editable"} Wait until the element is <a
   * href="https://playwright.dev/java/docs/actionability#editable">editable</a>.</li>
   * </ul>
   *
   * <p> If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
   *
   * @param state A state to wait for, see below for more details.
   * @since v1.8
   */
  void waitForElementState(ElementState state, WaitForElementStateOptions options);
  /**
   * Returns element specified by selector when it satisfies {@code state} option. Returns {@code null} if waiting for {@code
   * hidden} or {@code detached}.
   *
   * <p> Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear
   * from dom, or become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the
   * condition, the method will return immediately. If the selector doesn't satisfy the condition for the {@code timeout}
   * milliseconds, the function will throw.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.setContent("<div><span></span></div>");
   * ElementHandle div = page.querySelector("div");
   * // Waiting for the "span" selector relative to the div.
   * ElementHandle span = div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions()
   *   .setState(WaitForSelectorState.ATTACHED));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> This method does not work across navigations, use {@link Page#waitForSelector Page.waitForSelector()} instead.
   *
   * @param selector A selector to query for.
   * @since v1.8
   */
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns element specified by selector when it satisfies {@code state} option. Returns {@code null} if waiting for {@code
   * hidden} or {@code detached}.
   *
   * <p> Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear
   * from dom, or become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the
   * condition, the method will return immediately. If the selector doesn't satisfy the condition for the {@code timeout}
   * milliseconds, the function will throw.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.setContent("<div><span></span></div>");
   * ElementHandle div = page.querySelector("div");
   * // Waiting for the "span" selector relative to the div.
   * ElementHandle span = div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions()
   *   .setState(WaitForSelectorState.ATTACHED));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> This method does not work across navigations, use {@link Page#waitForSelector Page.waitForSelector()} instead.
   *
   * @param selector A selector to query for.
   * @since v1.8
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
}

