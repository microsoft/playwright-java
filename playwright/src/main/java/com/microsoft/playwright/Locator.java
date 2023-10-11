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
import java.util.regex.Pattern;

/**
 * Locators are the central piece of Playwright's auto-waiting and retry-ability. In a nutshell, locators represent a way
 * to find element(s) on the page at any moment. A locator can be created with the {@link Page#locator Page.locator()}
 * method.
 *
 * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
 */
public interface Locator {
  class BlurOptions {
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
    public BlurOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class BoundingBoxOptions {
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
    public BoundingBoxOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
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
  class ClearOptions {
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
    public ClearOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public ClearOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ClearOptions setTimeout(double timeout) {
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
  class DispatchEventOptions {
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
    public DispatchEventOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DragToOptions {
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
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public Position sourcePosition;
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public Position targetPosition;
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
    public DragToOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public DragToOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragToOptions setSourcePosition(double x, double y) {
      return setSourcePosition(new Position(x, y));
    }
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragToOptions setSourcePosition(Position sourcePosition) {
      this.sourcePosition = sourcePosition;
      return this;
    }
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragToOptions setTargetPosition(double x, double y) {
      return setTargetPosition(new Position(x, y));
    }
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragToOptions setTargetPosition(Position targetPosition) {
      this.targetPosition = targetPosition;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public DragToOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public DragToOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class ElementHandleOptions {
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
    public ElementHandleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class EvaluateOptions {
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
    public EvaluateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class EvaluateHandleOptions {
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
    public EvaluateHandleOptions setTimeout(double timeout) {
      this.timeout = timeout;
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
  class FilterOptions {
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
    public FilterOptions setHas(Locator has) {
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
    public FilterOptions setHasNot(Locator hasNot) {
      this.hasNot = hasNot;
      return this;
    }
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public FilterOptions setHasNotText(String hasNotText) {
      this.hasNotText = hasNotText;
      return this;
    }
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public FilterOptions setHasNotText(Pattern hasNotText) {
      this.hasNotText = hasNotText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public FilterOptions setHasText(String hasText) {
      this.hasText = hasText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public FilterOptions setHasText(Pattern hasText) {
      this.hasText = hasText;
      return this;
    }
  }
  class FocusOptions {
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
    public FocusOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
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
    public GetAttributeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
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
  class InnerHTMLOptions {
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
    public InnerHTMLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
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
    public InnerTextOptions setTimeout(double timeout) {
      this.timeout = timeout;
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
  class IsCheckedOptions {
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
    public IsCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
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
    public IsDisabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
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
    public IsEditableOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
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
    public IsEnabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * @deprecated This option is ignored. {@link Locator#isHidden Locator.isHidden()} does not wait for the element to become hidden and
     * returns immediately.
     */
    public Double timeout;

    /**
     * @deprecated This option is ignored. {@link Locator#isHidden Locator.isHidden()} does not wait for the element to become hidden and
     * returns immediately.
     */
    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * @deprecated This option is ignored. {@link Locator#isVisible Locator.isVisible()} does not wait for the element to become visible
     * and returns immediately.
     */
    public Double timeout;

    /**
     * @deprecated This option is ignored. {@link Locator#isVisible Locator.isVisible()} does not wait for the element to become visible
     * and returns immediately.
     */
    public IsVisibleOptions setTimeout(double timeout) {
      this.timeout = timeout;
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
  class PressSequentiallyOptions {
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
    public PressSequentiallyOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public PressSequentiallyOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public PressSequentiallyOptions setTimeout(double timeout) {
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
  class TextContentOptions {
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
    public TextContentOptions setTimeout(double timeout) {
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
  class WaitForOptions {
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
    public WaitForOptions setState(WaitForSelectorState state) {
      this.state = state;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * When the locator points to a list of elements, this returns an array of locators, pointing to their respective elements.
   *
   * <p> <strong>NOTE:</strong> {@link Locator#all Locator.all()} does not wait for elements to match the locator, and instead immediately returns
   * whatever is present in the page.  When the list of elements changes dynamically, {@link Locator#all Locator.all()} will
   * produce unpredictable and flaky results.  When the list of elements is stable, but loaded dynamically, wait for the full
   * list to finish loading before calling {@link Locator#all Locator.all()}.
   *
   * <p> **Usage**
   * <pre>{@code
   * for (Locator li : page.getByRole('listitem').all())
   *   li.click();
   * }</pre>
   *
   * @since v1.29
   */
  List<Locator> all();
  /**
   * Returns an array of {@code node.innerText} values for all matching nodes.
   *
   * <p> <strong>NOTE:</strong> If you need to assert text on the page, prefer {@link LocatorAssertions#hasText LocatorAssertions.hasText()} with {@code
   * useInnerText} option to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions
   * guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * String[] texts = page.getByRole(AriaRole.LINK).allInnerTexts();
   * }</pre>
   *
   * @since v1.14
   */
  List<String> allInnerTexts();
  /**
   * Returns an array of {@code node.textContent} values for all matching nodes.
   *
   * <p> <strong>NOTE:</strong> If you need to assert text on the page, prefer {@link LocatorAssertions#hasText LocatorAssertions.hasText()} to avoid
   * flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * String[] texts = page.getByRole(AriaRole.LINK).allTextContents();
   * }</pre>
   *
   * @since v1.14
   */
  List<String> allTextContents();
  /**
   * Creates a locator that matches both this locator and the argument locator.
   *
   * <p> **Usage**
   *
   * <p> The following example finds a button with a specific title.
   * <pre>{@code
   * Locator button = page.getByRole(AriaRole.BUTTON).and(page.getByTitle("Subscribe"));
   * }</pre>
   *
   * @param locator Additional locator to match.
   * @since v1.34
   */
  Locator and(Locator locator);
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/blur">blur</a> on the element.
   *
   * @since v1.28
   */
  default void blur() {
    blur(null);
  }
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/blur">blur</a> on the element.
   *
   * @since v1.28
   */
  void blur(BlurOptions options);
  /**
   * This method returns the bounding box of the element matching the locator, or {@code null} if the element is not visible.
   * The bounding box is calculated relative to the main frame viewport - which is usually the same as the browser window.
   *
   * <p> **Details**
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
   * BoundingBox box = page.getByRole(AriaRole.BUTTON).boundingBox();
   * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
   * }</pre>
   *
   * @since v1.14
   */
  default BoundingBox boundingBox() {
    return boundingBox(null);
  }
  /**
   * This method returns the bounding box of the element matching the locator, or {@code null} if the element is not visible.
   * The bounding box is calculated relative to the main frame viewport - which is usually the same as the browser window.
   *
   * <p> **Details**
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
   * BoundingBox box = page.getByRole(AriaRole.BUTTON).boundingBox();
   * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
   * }</pre>
   *
   * @since v1.14
   */
  BoundingBox boundingBox(BoundingBoxOptions options);
  /**
   * Ensure that checkbox or radio element is checked.
   *
   * <p> **Details**
   *
   * <p> Performs the following steps:
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
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.CHECKBOX).check();
   * }</pre>
   *
   * @since v1.14
   */
  default void check() {
    check(null);
  }
  /**
   * Ensure that checkbox or radio element is checked.
   *
   * <p> **Details**
   *
   * <p> Performs the following steps:
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
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.CHECKBOX).check();
   * }</pre>
   *
   * @since v1.14
   */
  void check(CheckOptions options);
  /**
   * Clear the input field.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
   * element, clears it and triggers an {@code input} event after clearing.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be
   * cleared instead.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.TEXTBOX).clear();
   * }</pre>
   *
   * @since v1.28
   */
  default void clear() {
    clear(null);
  }
  /**
   * Clear the input field.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
   * element, clears it and triggers an {@code input} event after clearing.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be
   * cleared instead.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.TEXTBOX).clear();
   * }</pre>
   *
   * @since v1.28
   */
  void clear(ClearOptions options);
  /**
   * Click an element.
   *
   * <p> **Details**
   *
   * <p> This method clicks the element by performing the following steps:
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
   * <p> **Usage**
   *
   * <p> Click a button:
   * <pre>{@code
   * page.getByRole(AriaRole.BUTTON).click();
   * }</pre>
   *
   * <p> Shift-right-click at a specific position on a canvas:
   * <pre>{@code
   * page.locator("canvas").click(new Locator.ClickOptions()
   *   .setButton(MouseButton.RIGHT)
   *   .setModifiers(Arrays.asList(KeyboardModifier.SHIFT))
   *   .setPosition(23, 32));
   * }</pre>
   *
   * @since v1.14
   */
  default void click() {
    click(null);
  }
  /**
   * Click an element.
   *
   * <p> **Details**
   *
   * <p> This method clicks the element by performing the following steps:
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
   * <p> **Usage**
   *
   * <p> Click a button:
   * <pre>{@code
   * page.getByRole(AriaRole.BUTTON).click();
   * }</pre>
   *
   * <p> Shift-right-click at a specific position on a canvas:
   * <pre>{@code
   * page.locator("canvas").click(new Locator.ClickOptions()
   *   .setButton(MouseButton.RIGHT)
   *   .setModifiers(Arrays.asList(KeyboardModifier.SHIFT))
   *   .setPosition(23, 32));
   * }</pre>
   *
   * @since v1.14
   */
  void click(ClickOptions options);
  /**
   * Returns the number of elements matching the locator.
   *
   * <p> <strong>NOTE:</strong> If you need to assert the number of elements on the page, prefer {@link LocatorAssertions#hasCount
   * LocatorAssertions.hasCount()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * int count = page.getByRole(AriaRole.LISTITEM).count();
   * }</pre>
   *
   * @since v1.14
   */
  int count();
  /**
   * Double-click an element.
   *
   * <p> **Details**
   *
   * <p> This method double clicks the element by performing the following steps:
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
   * <p> <strong>NOTE:</strong> {@code element.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @since v1.14
   */
  default void dblclick() {
    dblclick(null);
  }
  /**
   * Double-click an element.
   *
   * <p> **Details**
   *
   * <p> This method double clicks the element by performing the following steps:
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
   * <p> <strong>NOTE:</strong> {@code element.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @since v1.14
   */
  void dblclick(DblclickOptions options);
  /**
   * Programmatically dispatch an event on the matching element.
   *
   * <p> **Usage**
   * <pre>{@code
   * locator.dispatchEvent("click");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> The snippet above dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
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
   * locator.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.14
   */
  default void dispatchEvent(String type, Object eventInit) {
    dispatchEvent(type, eventInit, null);
  }
  /**
   * Programmatically dispatch an event on the matching element.
   *
   * <p> **Usage**
   * <pre>{@code
   * locator.dispatchEvent("click");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> The snippet above dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
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
   * locator.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @since v1.14
   */
  default void dispatchEvent(String type) {
    dispatchEvent(type, null);
  }
  /**
   * Programmatically dispatch an event on the matching element.
   *
   * <p> **Usage**
   * <pre>{@code
   * locator.dispatchEvent("click");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> The snippet above dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
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
   * locator.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.14
   */
  void dispatchEvent(String type, Object eventInit, DispatchEventOptions options);
  /**
   * Drag the source element towards the target element and drop it.
   *
   * <p> **Details**
   *
   * <p> This method drags the locator to another target locator or target position. It will first move to the source element,
   * perform a {@code mousedown}, then move to the target element or position and perform a {@code mouseup}.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator source = page.locator("#source");
   * Locator target = page.locator("#target");
   *
   * source.dragTo(target);
   * // or specify exact positions relative to the top-left corners of the elements:
   * source.dragTo(target, new Locator.DragToOptions()
   *   .setSourcePosition(34, 7).setTargetPosition(10, 20));
   * }</pre>
   *
   * @param target Locator of the element to drag to.
   * @since v1.18
   */
  default void dragTo(Locator target) {
    dragTo(target, null);
  }
  /**
   * Drag the source element towards the target element and drop it.
   *
   * <p> **Details**
   *
   * <p> This method drags the locator to another target locator or target position. It will first move to the source element,
   * perform a {@code mousedown}, then move to the target element or position and perform a {@code mouseup}.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator source = page.locator("#source");
   * Locator target = page.locator("#target");
   *
   * source.dragTo(target);
   * // or specify exact positions relative to the top-left corners of the elements:
   * source.dragTo(target, new Locator.DragToOptions()
   *   .setSourcePosition(34, 7).setTargetPosition(10, 20));
   * }</pre>
   *
   * @param target Locator of the element to drag to.
   * @since v1.18
   */
  void dragTo(Locator target, DragToOptions options);
  /**
   * Resolves given locator to the first matching DOM element. If there are no matching elements, waits for one. If multiple
   * elements match the locator, throws.
   *
   * @since v1.14
   */
  default ElementHandle elementHandle() {
    return elementHandle(null);
  }
  /**
   * Resolves given locator to the first matching DOM element. If there are no matching elements, waits for one. If multiple
   * elements match the locator, throws.
   *
   * @since v1.14
   */
  ElementHandle elementHandle(ElementHandleOptions options);
  /**
   * Resolves given locator to all matching DOM elements. If there are no matching elements, returns an empty list.
   *
   * @since v1.14
   */
  List<ElementHandle> elementHandles();
  /**
   * Execute JavaScript code in the page, taking the matching element as an argument.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression}, called with the matching element as a first argument, and {@code arg} as
   * a second argument.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator tweets = page.locator(".tweet .retweets");
   * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.14
   */
  default Object evaluate(String expression, Object arg) {
    return evaluate(expression, arg, null);
  }
  /**
   * Execute JavaScript code in the page, taking the matching element as an argument.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression}, called with the matching element as a first argument, and {@code arg} as
   * a second argument.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator tweets = page.locator(".tweet .retweets");
   * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.14
   */
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Execute JavaScript code in the page, taking the matching element as an argument.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression}, called with the matching element as a first argument, and {@code arg} as
   * a second argument.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator tweets = page.locator(".tweet .retweets");
   * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.14
   */
  Object evaluate(String expression, Object arg, EvaluateOptions options);
  /**
   * Execute JavaScript code in the page, taking all matching elements as an argument.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression}, called with an array of all matching elements as a first argument, and
   * {@code arg} as a second argument.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator locator = page.locator("div");
   * boolean moreThanTen = (boolean) locator.evaluateAll("(divs, min) => divs.length > min", 10);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.14
   */
  default Object evaluateAll(String expression) {
    return evaluateAll(expression, null);
  }
  /**
   * Execute JavaScript code in the page, taking all matching elements as an argument.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression}, called with an array of all matching elements as a first argument, and
   * {@code arg} as a second argument.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator locator = page.locator("div");
   * boolean moreThanTen = (boolean) locator.evaluateAll("(divs, min) => divs.length > min", 10);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.14
   */
  Object evaluateAll(String expression, Object arg);
  /**
   * Execute JavaScript code in the page, taking the matching element as an argument, and return a {@code JSHandle} with the
   * result.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression} as a{@code JSHandle}, called with the matching element as a first
   * argument, and {@code arg} as a second argument.
   *
   * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
   * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.14
   */
  default JSHandle evaluateHandle(String expression, Object arg) {
    return evaluateHandle(expression, arg, null);
  }
  /**
   * Execute JavaScript code in the page, taking the matching element as an argument, and return a {@code JSHandle} with the
   * result.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression} as a{@code JSHandle}, called with the matching element as a first
   * argument, and {@code arg} as a second argument.
   *
   * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
   * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.14
   */
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Execute JavaScript code in the page, taking the matching element as an argument, and return a {@code JSHandle} with the
   * result.
   *
   * <p> **Details**
   *
   * <p> Returns the return value of {@code expression} as a{@code JSHandle}, called with the matching element as a first
   * argument, and {@code arg} as a second argument.
   *
   * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
   * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, this method
   * will wait for the promise to resolve and return its value.
   *
   * <p> If {@code expression} throws or rejects, this method throws.
   *
   * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.14
   */
  JSHandle evaluateHandle(String expression, Object arg, EvaluateHandleOptions options);
  /**
   * Set a value to the input field.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.TEXTBOX).fill("example value");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
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
   * @since v1.14
   */
  default void fill(String value) {
    fill(value, null);
  }
  /**
   * Set a value to the input field.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.TEXTBOX).fill("example value");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
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
   * @since v1.14
   */
  void fill(String value, FillOptions options);
  /**
   * This method narrows existing locator according to the options, for example filters by text. It can be chained to filter
   * multiple times.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator rowLocator = page.locator("tr");
   * // ...
   * rowLocator
   *     .filter(new Locator.FilterOptions().setHasText("text in column 1"))
   *     .filter(new Locator.FilterOptions().setHas(
   *         page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("column 2 button"))
   *     ))
   *     .screenshot();
   * }</pre>
   *
   * @since v1.22
   */
  default Locator filter() {
    return filter(null);
  }
  /**
   * This method narrows existing locator according to the options, for example filters by text. It can be chained to filter
   * multiple times.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator rowLocator = page.locator("tr");
   * // ...
   * rowLocator
   *     .filter(new Locator.FilterOptions().setHasText("text in column 1"))
   *     .filter(new Locator.FilterOptions().setHas(
   *         page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("column 2 button"))
   *     ))
   *     .screenshot();
   * }</pre>
   *
   * @since v1.22
   */
  Locator filter(FilterOptions options);
  /**
   * Returns locator to the first matching element.
   *
   * @since v1.14
   */
  Locator first();
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the matching element.
   *
   * @since v1.14
   */
  default void focus() {
    focus(null);
  }
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the matching element.
   *
   * @since v1.14
   */
  void focus(FocusOptions options);
  /**
   * When working with iframes, you can create a frame locator that will enter the iframe and allow locating elements in that
   * iframe:
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator locator = page.frameLocator("iframe").getByText("Submit");
   * locator.click();
   * }</pre>
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.17
   */
  FrameLocator frameLocator(String selector);
  /**
   * Returns the matching element's attribute value.
   *
   * <p> <strong>NOTE:</strong> If you need to assert an element's attribute, prefer {@link LocatorAssertions#hasAttribute
   * LocatorAssertions.hasAttribute()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * @param name Attribute name to get the value for.
   * @since v1.14
   */
  default String getAttribute(String name) {
    return getAttribute(name, null);
  }
  /**
   * Returns the matching element's attribute value.
   *
   * <p> <strong>NOTE:</strong> If you need to assert an element's attribute, prefer {@link LocatorAssertions#hasAttribute
   * LocatorAssertions.hasAttribute()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * @param name Attribute name to get the value for.
   * @since v1.14
   */
  String getAttribute(String name, GetAttributeOptions options);
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
   * Highlight the corresponding element(s) on the screen. Useful for debugging, don't commit the code that uses {@link
   * Locator#highlight Locator.highlight()}.
   *
   * @since v1.20
   */
  void highlight();
  /**
   * Hover over the matching element.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.LINK).hover();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method hovers over the element by performing the following steps:
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
   * @since v1.14
   */
  default void hover() {
    hover(null);
  }
  /**
   * Hover over the matching element.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.LINK).hover();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method hovers over the element by performing the following steps:
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
   * @since v1.14
   */
  void hover(HoverOptions options);
  /**
   * Returns the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/innerHTML">{@code element.innerHTML}</a>.
   *
   * @since v1.14
   */
  default String innerHTML() {
    return innerHTML(null);
  }
  /**
   * Returns the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/innerHTML">{@code element.innerHTML}</a>.
   *
   * @since v1.14
   */
  String innerHTML(InnerHTMLOptions options);
  /**
   * Returns the <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/innerText">{@code
   * element.innerText}</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert text on the page, prefer {@link LocatorAssertions#hasText LocatorAssertions.hasText()} with {@code
   * useInnerText} option to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions
   * guide</a> for more details.
   *
   * @since v1.14
   */
  default String innerText() {
    return innerText(null);
  }
  /**
   * Returns the <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/innerText">{@code
   * element.innerText}</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert text on the page, prefer {@link LocatorAssertions#hasText LocatorAssertions.hasText()} with {@code
   * useInnerText} option to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions
   * guide</a> for more details.
   *
   * @since v1.14
   */
  String innerText(InnerTextOptions options);
  /**
   * Returns the value for the matching {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> <strong>NOTE:</strong> If you need to assert input value, prefer {@link LocatorAssertions#hasValue LocatorAssertions.hasValue()} to avoid
   * flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * String value = page.getByRole(AriaRole.TEXTBOX).inputValue();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Throws elements that are not an input, textarea or a select. However, if the element is inside the {@code <label>}
   * element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @since v1.14
   */
  default String inputValue() {
    return inputValue(null);
  }
  /**
   * Returns the value for the matching {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> <strong>NOTE:</strong> If you need to assert input value, prefer {@link LocatorAssertions#hasValue LocatorAssertions.hasValue()} to avoid
   * flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * String value = page.getByRole(AriaRole.TEXTBOX).inputValue();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Throws elements that are not an input, textarea or a select. However, if the element is inside the {@code <label>}
   * element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @since v1.14
   */
  String inputValue(InputValueOptions options);
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that checkbox is checked, prefer {@link LocatorAssertions#isChecked LocatorAssertions.isChecked()}
   * to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more
   * details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean checked = page.getByRole(AriaRole.CHECKBOX).isChecked();
   * }</pre>
   *
   * @since v1.14
   */
  default boolean isChecked() {
    return isChecked(null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that checkbox is checked, prefer {@link LocatorAssertions#isChecked LocatorAssertions.isChecked()}
   * to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more
   * details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean checked = page.getByRole(AriaRole.CHECKBOX).isChecked();
   * }</pre>
   *
   * @since v1.14
   */
  boolean isChecked(IsCheckedOptions options);
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that an element is disabled, prefer {@link LocatorAssertions#isDisabled
   * LocatorAssertions.isDisabled()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean disabled = page.getByRole(AriaRole.BUTTON).isDisabled();
   * }</pre>
   *
   * @since v1.14
   */
  default boolean isDisabled() {
    return isDisabled(null);
  }
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that an element is disabled, prefer {@link LocatorAssertions#isDisabled
   * LocatorAssertions.isDisabled()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean disabled = page.getByRole(AriaRole.BUTTON).isDisabled();
   * }</pre>
   *
   * @since v1.14
   */
  boolean isDisabled(IsDisabledOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that an element is editable, prefer {@link LocatorAssertions#isEditable
   * LocatorAssertions.isEditable()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean editable = page.getByRole(AriaRole.TEXTBOX).isEditable();
   * }</pre>
   *
   * @since v1.14
   */
  default boolean isEditable() {
    return isEditable(null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that an element is editable, prefer {@link LocatorAssertions#isEditable
   * LocatorAssertions.isEditable()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean editable = page.getByRole(AriaRole.TEXTBOX).isEditable();
   * }</pre>
   *
   * @since v1.14
   */
  boolean isEditable(IsEditableOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that an element is enabled, prefer {@link LocatorAssertions#isEnabled
   * LocatorAssertions.isEnabled()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean enabled = page.getByRole(AriaRole.BUTTON).isEnabled();
   * }</pre>
   *
   * @since v1.14
   */
  default boolean isEnabled() {
    return isEnabled(null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that an element is enabled, prefer {@link LocatorAssertions#isEnabled
   * LocatorAssertions.isEnabled()} to avoid flakiness. See <a
   * href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean enabled = page.getByRole(AriaRole.BUTTON).isEnabled();
   * }</pre>
   *
   * @since v1.14
   */
  boolean isEnabled(IsEnabledOptions options);
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that element is hidden, prefer {@link LocatorAssertions#isHidden LocatorAssertions.isHidden()} to
   * avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean hidden = page.getByRole(AriaRole.BUTTON).isHidden();
   * }</pre>
   *
   * @since v1.14
   */
  default boolean isHidden() {
    return isHidden(null);
  }
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that element is hidden, prefer {@link LocatorAssertions#isHidden LocatorAssertions.isHidden()} to
   * avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean hidden = page.getByRole(AriaRole.BUTTON).isHidden();
   * }</pre>
   *
   * @since v1.14
   */
  boolean isHidden(IsHiddenOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that element is visible, prefer {@link LocatorAssertions#isVisible LocatorAssertions.isVisible()}
   * to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more
   * details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean visible = page.getByRole(AriaRole.BUTTON).isVisible();
   * }</pre>
   *
   * @since v1.14
   */
  default boolean isVisible() {
    return isVisible(null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert that element is visible, prefer {@link LocatorAssertions#isVisible LocatorAssertions.isVisible()}
   * to avoid flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more
   * details.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean visible = page.getByRole(AriaRole.BUTTON).isVisible();
   * }</pre>
   *
   * @since v1.14
   */
  boolean isVisible(IsVisibleOptions options);
  /**
   * Returns locator to the last matching element.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator banana = page.getByRole(AriaRole.LISTITEM).last();
   * }</pre>
   *
   * @since v1.14
   */
  Locator last();
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selectorOrLocator A selector or locator to use when resolving DOM element.
   * @since v1.14
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
   * @since v1.14
   */
  Locator locator(String selectorOrLocator, LocatorOptions options);
  /**
   * The method finds an element matching the specified selector in the locator's subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selectorOrLocator A selector or locator to use when resolving DOM element.
   * @since v1.14
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
   * @since v1.14
   */
  Locator locator(Locator selectorOrLocator, LocatorOptions options);
  /**
   * Returns locator to the n-th matching element. It's zero based, {@code nth(0)} selects the first element.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator banana = page.getByRole(AriaRole.LISTITEM).nth(2);
   * }</pre>
   *
   * @since v1.14
   */
  Locator nth(int index);
  /**
   * Creates a locator that matches either of the two locators.
   *
   * <p> **Usage**
   *
   * <p> Consider a scenario where you'd like to click on a "New email" button, but sometimes a security settings dialog shows up
   * instead. In this case, you can wait for either a "New email" button, or a dialog and act accordingly.
   * <pre>{@code
   * Locator newEmail = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New"));
   * Locator dialog = page.getByText("Confirm security settings");
   * assertThat(newEmail.or(dialog)).isVisible();
   * if (dialog.isVisible())
   *   page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dismiss")).click();
   * newEmail.click();
   * }</pre>
   *
   * @param locator Alternative locator to match.
   * @since v1.33
   */
  Locator or(Locator locator);
  /**
   * A page this locator belongs to.
   *
   * @since v1.19
   */
  Page page();
  /**
   * Focuses the matching element and presses a combination of the keys.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.TEXTBOX).press("Backspace");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
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
   * @since v1.14
   */
  default void press(String key) {
    press(key, null);
  }
  /**
   * Focuses the matching element and presses a combination of the keys.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.TEXTBOX).press("Backspace");
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
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
   * @since v1.14
   */
  void press(String key, PressOptions options);
  /**
   * <strong>NOTE:</strong> In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page.
   *
   * <p> Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each
   * character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Locator#press Locator.press()}.
   *
   * <p> **Usage**
   * <pre>{@code
   * locator.pressSequentially("Hello"); // Types instantly
   * locator.pressSequentially("World", new Locator.pressSequentiallyOptions().setDelay(100)); // Types slower, like a user
   * }</pre>
   *
   * <p> An example of typing into a text field and then submitting the form:
   * <pre>{@code
   * Locator locator = page.getByLabel("Password");
   * locator.pressSequentially("my password");
   * locator.press("Enter");
   * }</pre>
   *
   * @param text String of characters to sequentially press into a focused element.
   * @since v1.38
   */
  default void pressSequentially(String text) {
    pressSequentially(text, null);
  }
  /**
   * <strong>NOTE:</strong> In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page.
   *
   * <p> Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each
   * character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Locator#press Locator.press()}.
   *
   * <p> **Usage**
   * <pre>{@code
   * locator.pressSequentially("Hello"); // Types instantly
   * locator.pressSequentially("World", new Locator.pressSequentiallyOptions().setDelay(100)); // Types slower, like a user
   * }</pre>
   *
   * <p> An example of typing into a text field and then submitting the form:
   * <pre>{@code
   * Locator locator = page.getByLabel("Password");
   * locator.pressSequentially("my password");
   * locator.press("Enter");
   * }</pre>
   *
   * @param text String of characters to sequentially press into a focused element.
   * @since v1.38
   */
  void pressSequentially(String text, PressSequentiallyOptions options);
  /**
   * Take a screenshot of the element matching the locator.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.LINK).screenshot();
   * }</pre>
   *
   * <p> Disable animations and save screenshot to a file:
   * <pre>{@code
   * page.getByRole(AriaRole.LINK).screenshot(new Locator.ScreenshotOptions()
   *     .setAnimations(ScreenshotAnimations.DISABLED)
   *     .setPath(Paths.get("example.png")));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method captures a screenshot of the page, clipped to the size and position of a particular element matching the
   * locator. If the element is covered by other elements, it will not be actually visible on the screenshot. If the element
   * is a scrollable container, only the currently scrolled content will be visible on the screenshot.
   *
   * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then
   * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   *
   * <p> Returns the buffer with the captured screenshot.
   *
   * @since v1.14
   */
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * Take a screenshot of the element matching the locator.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.LINK).screenshot();
   * }</pre>
   *
   * <p> Disable animations and save screenshot to a file:
   * <pre>{@code
   * page.getByRole(AriaRole.LINK).screenshot(new Locator.ScreenshotOptions()
   *     .setAnimations(ScreenshotAnimations.DISABLED)
   *     .setPath(Paths.get("example.png")));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method captures a screenshot of the page, clipped to the size and position of a particular element matching the
   * locator. If the element is covered by other elements, it will not be actually visible on the screenshot. If the element
   * is a scrollable container, only the currently scrolled content will be visible on the screenshot.
   *
   * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then
   * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   *
   * <p> Returns the buffer with the captured screenshot.
   *
   * @since v1.14
   */
  byte[] screenshot(ScreenshotOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then tries to
   * scroll element into view, unless it is completely visible as defined by <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code
   * ratio}.
   *
   * @since v1.14
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
   * @since v1.14
   */
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  default List<String> selectOption(String values) {
    return selectOption(values, null);
  }
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  List<String> selectOption(String values, SelectOptionOptions options);
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  default List<String> selectOption(ElementHandle values) {
    return selectOption(values, null);
  }
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  List<String> selectOption(ElementHandle values, SelectOptionOptions options);
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  default List<String> selectOption(String[] values) {
    return selectOption(values, null);
  }
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  List<String> selectOption(String[] values, SelectOptionOptions options);
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  default List<String> selectOption(SelectOption values) {
    return selectOption(values, null);
  }
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  List<String> selectOption(SelectOption values, SelectOptionOptions options);
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  default List<String> selectOption(ElementHandle[] values) {
    return selectOption(values, null);
  }
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  List<String> selectOption(ElementHandle[] values, SelectOptionOptions options);
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
   */
  default List<String> selectOption(SelectOption[] values) {
    return selectOption(values, null);
  }
  /**
   * Selects option or options in {@code <select>}.
   *
   * <p> **Details**
   *
   * <p> This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
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
   * // single selection matching the value or label
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection for blue, red and second option
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.14
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
   * @since v1.14
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
   * @since v1.14
   */
  void selectText(SelectTextOptions options);
  /**
   * Set the state of a checkbox or a radio element.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.CHECKBOX).setChecked(true);
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method checks or unchecks an element by performing the following steps:
   * <ol>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws.</li>
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
   * Set the state of a checkbox or a radio element.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.CHECKBOX).setChecked(true);
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method checks or unchecks an element by performing the following steps:
   * <ol>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws.</li>
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
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  default void setInputFiles(Path files) {
    setInputFiles(files, null);
  }
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  void setInputFiles(Path files, SetInputFilesOptions options);
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  default void setInputFiles(Path[] files) {
    setInputFiles(files, null);
  }
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  void setInputFiles(Path[] files, SetInputFilesOptions options);
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  default void setInputFiles(FilePayload files) {
    setInputFiles(files, null);
  }
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  void setInputFiles(FilePayload files, SetInputFilesOptions options);
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  default void setInputFiles(FilePayload[] files) {
    setInputFiles(files, null);
  }
  /**
   * Upload file or multiple files into {@code <input type=file>}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Select one file
   * page.getByLabel("Upload file").setInputFiles(Paths.get("myfile.pdf"));
   *
   * // Select multiple files
   * page.getByLabel("Upload files").setInputFiles(new Path[] {Paths.get("file1.txt"), Paths.get("file2.txt")});
   *
   * // Remove all the selected files
   * page.getByLabel("Upload file").setInputFiles(new Path[0]);
   *
   * // Upload buffer from memory
   * page.getByLabel("Upload file").setInputFiles(new FilePayload(
   *   "file.txt", "text/plain", "this is test".getBytes(StandardCharsets.UTF_8)));
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @since v1.14
   */
  void setInputFiles(FilePayload[] files, SetInputFilesOptions options);
  /**
   * Perform a tap gesture on the element matching the locator.
   *
   * <p> **Details**
   *
   * <p> This method taps the element by performing the following steps:
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
   * <p> <strong>NOTE:</strong> {@code element.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @since v1.14
   */
  default void tap() {
    tap(null);
  }
  /**
   * Perform a tap gesture on the element matching the locator.
   *
   * <p> **Details**
   *
   * <p> This method taps the element by performing the following steps:
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
   * <p> <strong>NOTE:</strong> {@code element.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @since v1.14
   */
  void tap(TapOptions options);
  /**
   * Returns the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Node/textContent">{@code node.textContent}</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert text on the page, prefer {@link LocatorAssertions#hasText LocatorAssertions.hasText()} to avoid
   * flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * @since v1.14
   */
  default String textContent() {
    return textContent(null);
  }
  /**
   * Returns the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Node/textContent">{@code node.textContent}</a>.
   *
   * <p> <strong>NOTE:</strong> If you need to assert text on the page, prefer {@link LocatorAssertions#hasText LocatorAssertions.hasText()} to avoid
   * flakiness. See <a href="https://playwright.dev/java/docs/test-assertions">assertions guide</a> for more details.
   *
   * @since v1.14
   */
  String textContent(TextContentOptions options);
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param text A text to type into a focused element.
   * @since v1.14
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
   * @since v1.14
   */
  void type(String text, TypeOptions options);
  /**
   * Ensure that checkbox or radio element is unchecked.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.CHECKBOX).uncheck();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method unchecks the element by performing the following steps:
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
   * @since v1.14
   */
  default void uncheck() {
    uncheck(null);
  }
  /**
   * Ensure that checkbox or radio element is unchecked.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.CHECKBOX).uncheck();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> This method unchecks the element by performing the following steps:
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
   * @since v1.14
   */
  void uncheck(UncheckOptions options);
  /**
   * Returns when element specified by locator satisfies the {@code state} option.
   *
   * <p> If target element already satisfies the condition, the method returns immediately. Otherwise, waits for up to {@code
   * timeout} milliseconds until the condition is met.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator orderSent = page.locator("#order-sent");
   * orderSent.waitFor();
   * }</pre>
   *
   * @since v1.16
   */
  default void waitFor() {
    waitFor(null);
  }
  /**
   * Returns when element specified by locator satisfies the {@code state} option.
   *
   * <p> If target element already satisfies the condition, the method returns immediately. Otherwise, waits for up to {@code
   * timeout} milliseconds until the condition is met.
   *
   * <p> **Usage**
   * <pre>{@code
   * Locator orderSent = page.locator("#order-sent");
   * orderSent.waitFor();
   * }</pre>
   *
   * @since v1.16
   */
  void waitFor(WaitForOptions options);
}

