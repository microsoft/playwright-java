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
 * - extends: {@code JSHandle}
 *
 * <p> ElementHandle represents an in-page DOM element. ElementHandles can be created with the [{@code method: Page.querySelector}]
 * method.
 *
 * <p> ElementHandle prevents DOM element from garbage collection unless the handle is disposed with
 * [{@code method: JSHandle.dispose}]. ElementHandles are auto-disposed when their origin frame gets navigated.
 *
 * <p> ElementHandle instances can be used as an argument in [{@code method: Page.evalOnSelector}] and [{@code method: Page.evaluate}]
 * methods.
 */
public interface ElementHandle extends JSHandle {
  class CheckOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public CheckOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public CheckOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public CheckOptions withTimeout(double timeout) {
      this.timeout = timeout;
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
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public ClickOptions withButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public ClickOptions withClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public ClickOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public ClickOptions withModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public ClickOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public ClickOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
    }
    public ClickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public ClickOptions withTimeout(double timeout) {
      this.timeout = timeout;
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
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public DblclickOptions withButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public DblclickOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public DblclickOptions withModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public DblclickOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public DblclickOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
    }
    public DblclickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public DblclickOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FillOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public FillOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public FillOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HoverOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public HoverOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public HoverOptions withModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public HoverOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
    }
    public HoverOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public HoverOptions withTimeout(double timeout) {
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public PressOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public PressOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public PressOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ScreenshotOptions {
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg} images.
     * Defaults to {@code false}.
     */
    public Boolean omitBackground;
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a relative
     * path, then it is resolved relative to the current working directory. If no path is provided, the image won't be saved to
     * the disk.
     */
    public Path path;
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public Integer quality;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public ScreenshotType type;

    public ScreenshotOptions withOmitBackground(boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    public ScreenshotOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public ScreenshotOptions withQuality(int quality) {
      this.quality = quality;
      return this;
    }
    public ScreenshotOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public ScreenshotOptions withType(ScreenshotType type) {
      this.type = type;
      return this;
    }
  }
  class ScrollIntoViewIfNeededOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public ScrollIntoViewIfNeededOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectOptionOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public SelectOptionOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SelectOptionOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectTextOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public SelectTextOptions withTimeout(double timeout) {
      this.timeout = timeout;
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public SetInputFilesOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetInputFilesOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public TapOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public TapOptions withModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public TapOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TapOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
    }
    public TapOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public TapOptions withTimeout(double timeout) {
      this.timeout = timeout;
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public TypeOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public TypeOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TypeOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public UncheckOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public UncheckOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public UncheckOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForElementStateOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public WaitForElementStateOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForSelectorOptions {
    /**
     * Defaults to {@code 'visible'}. Can be either:
     * - {@code 'attached'} - wait for element to be present in DOM.
     * - {@code 'detached'} - wait for element to not be present in DOM.
     * - {@code 'visible'} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without
     *   any content or with {@code display:none} has an empty bounding box and is not considered visible.
     * - {@code 'hidden'} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}.
     *   This is opposite to the {@code 'visible'} option.
     */
    public WaitForSelectorState state;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public WaitForSelectorOptions withState(WaitForSelectorState state) {
      this.state = state;
      return this;
    }
    public WaitForSelectorOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * This method returns the bounding box of the element, or {@code null} if the element is not visible. The bounding box is
   * calculated relative to the main frame viewport - which is usually the same as the browser window.
   *
   * <p> Scrolling affects the returned bonding box, similarly to
   * [Element.getBoundingClientRect](https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect). That
   * means {@code x} and/or {@code y} may be negative.
   *
   * <p> Elements from child frames return the bounding box relative to the main frame, unlike the
   * [Element.getBoundingClientRect](https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect).
   *
   * <p> Assuming the page is static, it is safe to use bounding box coordinates to perform input. For example, the following
   * snippet should click the center of the element.
   */
  BoundingBox boundingBox();
  /**
   * This method checks the element by performing the following steps:
   * 1. Ensure that element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    checked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now checked. If not, this method rejects.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  default void check() {
    check(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * 1. Ensure that element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    checked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now checked. If not, this method rejects.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  void check(CheckOptions options);
  /**
   * This method clicks the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  default void click() {
    click(null);
  }
  /**
   * This method clicks the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  void click(ClickOptions options);
  /**
   * Returns the content frame for element handles referencing iframe nodes, or {@code null} otherwise
   */
  Frame contentFrame();
  /**
   * This method double clicks the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to double click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   *    first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   */
  default void dblclick() {
    dblclick(null);
  }
  /**
   * This method double clicks the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to double click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   *    first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   */
  void dblclick(DblclickOptions options);
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the elment, {@code click}
   * is dispatched. This is equivalend to calling
   * [element.click()](https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click).
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * - [DragEvent](https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent)
   * - [FocusEvent](https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent)
   * - [KeyboardEvent](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent)
   * - [MouseEvent](https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent)
   * - [PointerEvent](https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent)
   * - [TouchEvent](https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent)
   * - [Event](https://developer.mozilla.org/en-US/docs/Web/API/Event/Event)
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   *
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   */
  default void dispatchEvent(String type) {
    dispatchEvent(type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the elment, {@code click}
   * is dispatched. This is equivalend to calling
   * [element.click()](https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click).
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * - [DragEvent](https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent)
   * - [FocusEvent](https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent)
   * - [KeyboardEvent](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent)
   * - [MouseEvent](https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent)
   * - [PointerEvent](https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent)
   * - [TouchEvent](https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent)
   * - [Event](https://developer.mozilla.org/en-US/docs/Web/API/Event/Event)
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   *
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String type, Object eventInit);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a first
   * argument to {@code expression}. See [Working with selectors](./selectors.md) for more details. If no elements match the
   * selector, the method throws an error.
   *
   * <p> If {@code expression} returns a [Promise], then [{@code method: ElementHandle.evalOnSelector}] would wait for the promise to resolve
   * and return its value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default Object evalOnSelector(String selector, String expression) {
    return evalOnSelector(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a first
   * argument to {@code expression}. See [Working with selectors](./selectors.md) for more details. If no elements match the
   * selector, the method throws an error.
   *
   * <p> If {@code expression} returns a [Promise], then [{@code method: ElementHandle.evalOnSelector}] would wait for the promise to resolve
   * and return its value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evalOnSelector(String selector, String expression, Object arg);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array of
   * matched elements as a first argument to {@code expression}. See [Working with selectors](./selectors.md) for more details.
   *
   * <p> If {@code expression} returns a [Promise], then [{@code method: ElementHandle.evalOnSelectorAll}] would wait for the promise to
   * resolve and return its value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default Object evalOnSelectorAll(String selector, String expression) {
    return evalOnSelectorAll(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array of
   * matched elements as a first argument to {@code expression}. See [Working with selectors](./selectors.md) for more details.
   *
   * <p> If {@code expression} returns a [Promise], then [{@code method: ElementHandle.evalOnSelectorAll}] would wait for the promise to
   * resolve and return its value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evalOnSelectorAll(String selector, String expression, Object arg);
  /**
   * This method waits for [actionability](./actionability.md) checks, focuses the element, fills it and triggers an {@code input}
   * event after filling. If the element is inside the {@code <label>} element that has associated
   * [control](https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control), that control will be filled
   * instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. Note that you can pass an empty string to clear the input field.
   *
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  default void fill(String value) {
    fill(value, null);
  }
  /**
   * This method waits for [actionability](./actionability.md) checks, focuses the element, fills it and triggers an {@code input}
   * event after filling. If the element is inside the {@code <label>} element that has associated
   * [control](https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control), that control will be filled
   * instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. Note that you can pass an empty string to clear the input field.
   *
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String value, FillOptions options);
  /**
   * Calls [focus](https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus) on the element.
   */
  void focus();
  /**
   * Returns element attribute value.
   *
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String name);
  /**
   * This method hovers over the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to hover over the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  default void hover() {
    hover(null);
  }
  /**
   * This method hovers over the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to hover over the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  void hover(HoverOptions options);
  /**
   * Returns the {@code element.innerHTML}.
   */
  String innerHTML();
  /**
   * Returns the {@code element.innerText}.
   */
  String innerText();
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   */
  boolean isChecked();
  /**
   * Returns whether the element is disabled, the opposite of [enabled](./actionability.md#enabled).
   */
  boolean isDisabled();
  /**
   * Returns whether the element is [editable](./actionability.md#editable).
   */
  boolean isEditable();
  /**
   * Returns whether the element is [enabled](./actionability.md#enabled).
   */
  boolean isEnabled();
  /**
   * Returns whether the element is hidden, the opposite of [visible](./actionability.md#visible).
   */
  boolean isHidden();
  /**
   * Returns whether the element is [visible](./actionability.md#visible).
   */
  boolean isVisible();
  /**
   * Returns the frame containing the given element.
   */
  Frame ownerFrame();
  /**
   * Focuses the element, and then uses [{@code method: Keyboard.down}] and [{@code method: Keyboard.up}].
   *
   * <p> {@code key} can specify the intended [keyboardEvent.key](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key)
   * value or a single character to generate the text for. A superset of the {@code key} values can be found
   * [here](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values). Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
   * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
   * texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the
   * modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  default void press(String key) {
    press(key, null);
  }
  /**
   * Focuses the element, and then uses [{@code method: Keyboard.down}] and [{@code method: Keyboard.up}].
   *
   * <p> {@code key} can specify the intended [keyboardEvent.key](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key)
   * value or a single character to generate the text for. A superset of the {@code key} values can be found
   * [here](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values). Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
   * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
   * texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the
   * modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String key, PressOptions options);
  /**
   * The method finds an element matching the specified selector in the {@code ElementHandle}'s subtree. See
   * [Working with selectors](./selectors.md) for more details. If no elements match the selector, returns {@code null}.
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * The method finds all elements matching the specified selector in the {@code ElementHandle}s subtree. See
   * [Working with selectors](./selectors.md) for more details. If no elements match the selector, returns empty array.
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  /**
   * Returns the buffer with the captured screenshot.
   *
   * <p> This method waits for the [actionability](./actionability.md) checks, then scrolls element into view before taking a
   * screenshot. If the element is detached from DOM, the method throws an error.
   */
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * Returns the buffer with the captured screenshot.
   *
   * <p> This method waits for the [actionability](./actionability.md) checks, then scrolls element into view before taking a
   * screenshot. If the element is detached from DOM, the method throws an error.
   */
  byte[] screenshot(ScreenshotOptions options);
  /**
   * This method waits for [actionability](./actionability.md) checks, then tries to scroll element into view, unless it is
   * completely visible as defined by
   * [IntersectionObserver](https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API)'s {@code ratio}.
   *
   * <p> Throws when {@code elementHandle} does not point to an element
   * [connected](https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected) to a Document or a ShadowRoot.
   */
  default void scrollIntoViewIfNeeded() {
    scrollIntoViewIfNeeded(null);
  }
  /**
   * This method waits for [actionability](./actionability.md) checks, then tries to scroll element into view, unless it is
   * completely visible as defined by
   * [IntersectionObserver](https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API)'s {@code ratio}.
   *
   * <p> Throws when {@code elementHandle} does not point to an element
   * [connected](https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected) to a Document or a ShadowRoot.
   */
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String values) {
    return selectOption(values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String values, SelectOptionOptions options);
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(ElementHandle values) {
    return selectOption(values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(ElementHandle values, SelectOptionOptions options);
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String[] values) {
    return selectOption(values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String[] values, SelectOptionOptions options);
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(SelectOption values) {
    return selectOption(values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(SelectOption values, SelectOptionOptions options);
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(ElementHandle[] values) {
    return selectOption(values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(ElementHandle[] values, SelectOptionOptions options);
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(SelectOption[] values) {
    return selectOption(values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
   * element, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(SelectOption[] values, SelectOptionOptions options);
  /**
   * This method waits for [actionability](./actionability.md) checks, then focuses the element and selects all its text
   * content.
   */
  default void selectText() {
    selectText(null);
  }
  /**
   * This method waits for [actionability](./actionability.md) checks, then focuses the element and selects all its text
   * content.
   */
  void selectText(SelectTextOptions options);
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  default void setInputFiles(Path files) {
    setInputFiles(files, null);
  }
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  void setInputFiles(Path files, SetInputFilesOptions options);
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  default void setInputFiles(Path[] files) {
    setInputFiles(files, null);
  }
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  void setInputFiles(Path[] files, SetInputFilesOptions options);
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  default void setInputFiles(FilePayload files) {
    setInputFiles(files, null);
  }
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  void setInputFiles(FilePayload files, SetInputFilesOptions options);
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  default void setInputFiles(FilePayload[] files) {
    setInputFiles(files, null);
  }
  /**
   * This method expects {@code elementHandle} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  void setInputFiles(FilePayload[] files, SetInputFilesOptions options);
  /**
   * This method taps the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.touchscreen}] to tap the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   */
  default void tap() {
    tap(null);
  }
  /**
   * This method taps the element by performing the following steps:
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.touchscreen}] to tap the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   */
  void tap(TapOptions options);
  /**
   * Returns the {@code node.textContent}.
   */
  String textContent();
  /**
   * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use [{@code method: ElementHandle.press}].
   *
   *
   * @param text A text to type into a focused element.
   */
  default void type(String text) {
    type(text, null);
  }
  /**
   * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use [{@code method: ElementHandle.press}].
   *
   *
   * @param text A text to type into a focused element.
   */
  void type(String text, TypeOptions options);
  /**
   * This method checks the element by performing the following steps:
   * 1. Ensure that element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    unchecked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now unchecked. If not, this method rejects.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  default void uncheck() {
    uncheck(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * 1. Ensure that element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    unchecked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the element, unless {@code force} option is set.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now unchecked. If not, this method rejects.
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   */
  void uncheck(UncheckOptions options);
  /**
   * Returns when the element satisfies the {@code state}.
   *
   * <p> Depending on the {@code state} parameter, this method waits for one of the [actionability](./actionability.md) checks to pass.
   * This method throws when the element is detached while waiting, unless waiting for the {@code "hidden"} state.
   * - {@code "visible"} Wait until the element is [visible](./actionability.md#visible).
   * - {@code "hidden"} Wait until the element is [not visible](./actionability.md#visible) or
   *   [not attached](./actionability.md#attached). Note that waiting for hidden does not throw when the element detaches.
   * - {@code "stable"} Wait until the element is both [visible](./actionability.md#visible) and
   *   [stable](./actionability.md#stable).
   * - {@code "enabled"} Wait until the element is [enabled](./actionability.md#enabled).
   * - {@code "disabled"} Wait until the element is [not enabled](./actionability.md#enabled).
   * - {@code "editable"} Wait until the element is [editable](./actionability.md#editable).
   *
   * <p> If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
   *
   * @param state A state to wait for, see below for more details.
   */
  default void waitForElementState(ElementState state) {
    waitForElementState(state, null);
  }
  /**
   * Returns when the element satisfies the {@code state}.
   *
   * <p> Depending on the {@code state} parameter, this method waits for one of the [actionability](./actionability.md) checks to pass.
   * This method throws when the element is detached while waiting, unless waiting for the {@code "hidden"} state.
   * - {@code "visible"} Wait until the element is [visible](./actionability.md#visible).
   * - {@code "hidden"} Wait until the element is [not visible](./actionability.md#visible) or
   *   [not attached](./actionability.md#attached). Note that waiting for hidden does not throw when the element detaches.
   * - {@code "stable"} Wait until the element is both [visible](./actionability.md#visible) and
   *   [stable](./actionability.md#stable).
   * - {@code "enabled"} Wait until the element is [enabled](./actionability.md#enabled).
   * - {@code "disabled"} Wait until the element is [not enabled](./actionability.md#enabled).
   * - {@code "editable"} Wait until the element is [editable](./actionability.md#editable).
   *
   * <p> If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
   *
   * @param state A state to wait for, see below for more details.
   */
  void waitForElementState(ElementState state, WaitForElementStateOptions options);
  /**
   * Returns element specified by selector when it satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
   * {@code detached}.
   *
   * <p> Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear from dom, or
   * become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
   * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will
   * throw.
   *
   * <p> <strong>NOTE:</strong> This method does not work across navigations, use [{@code method: Page.waitForSelector}] instead.
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   */
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns element specified by selector when it satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
   * {@code detached}.
   *
   * <p> Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear from dom, or
   * become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
   * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will
   * throw.
   *
   * <p> <strong>NOTE:</strong> This method does not work across navigations, use [{@code method: Page.waitForSelector}] instead.
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
}

