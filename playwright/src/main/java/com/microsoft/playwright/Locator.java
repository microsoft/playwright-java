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
 * to find element(s) on the page at any moment. Locator can be created with the {@link Page#locator Page.locator()}
 * method.
 *
 * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
 */
public interface Locator {
  class BoundingBoxOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public ElementHandleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class EvaluateOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public EvaluateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class EvaluateHandleOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
    public FilterOptions setHas(Locator has) {
      this.has = has;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches
     * {@code <article><div>Playwright</div></article>}.
     */
    public FilterOptions setHasText(String hasText) {
      this.hasText = hasText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches
     * {@code <article><div>Playwright</div></article>}.
     */
    public FilterOptions setHasText(Pattern hasText) {
      this.hasText = hasText;
      return this;
    }
  }
  class FocusOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public FocusOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public GetAttributeOptions setTimeout(double timeout) {
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
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public InnerHTMLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public InnerTextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InputValueOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public InputValueOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsCheckedOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public IsCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public IsDisabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public IsEditableOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public IsEnabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * **DEPRECATED** This option is ignored. {@link Locator#isHidden Locator.isHidden()} does not wait for the element to
     * become hidden and returns immediately.
     */
    public Double timeout;

    /**
     * **DEPRECATED** This option is ignored. {@link Locator#isHidden Locator.isHidden()} does not wait for the element to
     * become hidden and returns immediately.
     */
    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * **DEPRECATED** This option is ignored. {@link Locator#isVisible Locator.isVisible()} does not wait for the element to
     * become visible and returns immediately.
     */
    public Double timeout;

    /**
     * **DEPRECATED** This option is ignored. {@link Locator#isVisible Locator.isVisible()} does not wait for the element to
     * become visible and returns immediately.
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
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public PressOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ScreenshotOptions {
    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different treatment
     * depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "allow"} that leaves animations untouched.
     */
    public ScreenshotAnimations animations;
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not be changed.
     * Defaults to {@code "hide"}.
     */
    public ScreenshotCaret caret;
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} that completely covers its bounding box.
     */
    public List<Locator> mask;
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
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices, this will
     * keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so screenhots of
     * high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "device"}.
     */
    public ScreenshotScale scale;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public ScreenshotType type;

    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different treatment
     * depending on their duration:
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
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not be changed.
     * Defaults to {@code "hide"}.
     */
    public ScreenshotOptions setCaret(ScreenshotCaret caret) {
      this.caret = caret;
      return this;
    }
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} that completely covers its bounding box.
     */
    public ScreenshotOptions setMask(List<Locator> mask) {
      this.mask = mask;
      return this;
    }
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg} images.
     * Defaults to {@code false}.
     */
    public ScreenshotOptions setOmitBackground(boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a relative
     * path, then it is resolved relative to the current working directory. If no path is provided, the image won't be saved to
     * the disk.
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
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices, this will
     * keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so screenhots of
     * high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "device"}.
     */
    public ScreenshotOptions setScale(ScreenshotScale scale) {
      this.scale = scale;
      return this;
    }
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
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
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without any
     * content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}. This
     * is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    public WaitForSelectorState state;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Defaults to {@code "visible"}. Can be either:
     * <ul>
     * <li> {@code "attached"} - wait for element to be present in DOM.</li>
     * <li> {@code "detached"} - wait for element to not be present in DOM.</li>
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without any
     * content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}. This
     * is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    public WaitForOptions setState(WaitForSelectorState state) {
      this.state = state;
      return this;
    }
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public WaitForOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Returns an array of {@code node.innerText} values for all matching nodes.
   */
  List<String> allInnerTexts();
  /**
   * Returns an array of {@code node.textContent} values for all matching nodes.
   */
  List<String> allTextContents();
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
   * <pre>{@code
   * BoundingBox box = element.boundingBox();
   * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
   * }</pre>
   */
  default BoundingBox boundingBox() {
    return boundingBox(null);
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
   * <pre>{@code
   * BoundingBox box = element.boundingBox();
   * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
   * }</pre>
   */
  BoundingBox boundingBox(BoundingBoxOptions options);
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already checked, this
   * method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  default void check() {
    check(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already checked, this
   * method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  void check(CheckOptions options);
  /**
   * This method clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  default void click() {
    click(null);
  }
  /**
   * This method clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  void click(ClickOptions options);
  /**
   * Returns the number of elements matching given selector.
   */
  int count();
  /**
   * This method double clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
   * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code element.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   */
  default void dblclick() {
    dblclick(null);
  }
  /**
   * This method double clicks the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
   * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code element.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   */
  void dblclick(DblclickOptions options);
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   * <pre>{@code
   * element.dispatchEvent("click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
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
   * element.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  default void dispatchEvent(String type, Object eventInit) {
    dispatchEvent(type, eventInit, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   * <pre>{@code
   * element.dispatchEvent("click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
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
   * element.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   */
  default void dispatchEvent(String type) {
    dispatchEvent(type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   * <pre>{@code
   * element.dispatchEvent("click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
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
   * element.dispatchEvent("dragstart", arg);
   * }</pre>
   *
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String type, Object eventInit, DispatchEventOptions options);
  /**
   *
   *
   * @param target Locator of the element to drag to.
   */
  default void dragTo(Locator target) {
    dragTo(target, null);
  }
  /**
   *
   *
   * @param target Locator of the element to drag to.
   */
  void dragTo(Locator target, DragToOptions options);
  /**
   * Resolves given locator to the first matching DOM element. If no elements matching the query are visible, waits for them
   * up to a given timeout. If multiple elements match the selector, throws.
   */
  default ElementHandle elementHandle() {
    return elementHandle(null);
  }
  /**
   * Resolves given locator to the first matching DOM element. If no elements matching the query are visible, waits for them
   * up to a given timeout. If multiple elements match the selector, throws.
   */
  ElementHandle elementHandle(ElementHandleOptions options);
  /**
   * Resolves given locator to all matching DOM elements.
   */
  List<ElementHandle> elementHandles();
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
   * {@code handle.evaluate} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * Locator tweets = page.locator(".tweet .retweets");
   * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  default Object evaluate(String expression, Object arg) {
    return evaluate(expression, arg, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
   * {@code handle.evaluate} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * Locator tweets = page.locator(".tweet .retweets");
   * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
   * {@code handle.evaluate} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * Locator tweets = page.locator(".tweet .retweets");
   * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evaluate(String expression, Object arg, EvaluateOptions options);
  /**
   * The method finds all elements matching the specified locator and passes an array of matched elements as a first argument
   * to {@code expression}. Returns the result of {@code expression} invocation.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Locator#evaluateAll Locator.evaluateAll()} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * Locator elements = page.locator("div");
   * boolean divCounts = (boolean) elements.evaluateAll("(divs, min) => divs.length >= min", 10);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default Object evaluateAll(String expression) {
    return evaluateAll(expression, null);
  }
  /**
   * The method finds all elements matching the specified locator and passes an array of matched elements as a first argument
   * to {@code expression}. Returns the result of {@code expression} invocation.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Locator#evaluateAll Locator.evaluateAll()} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * Locator elements = page.locator("div");
   * boolean divCounts = (boolean) elements.evaluateAll("(divs, min) => divs.length >= min", 10);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evaluateAll(String expression, Object arg);
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
   * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Locator#evaluateHandle Locator.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Locator#evaluateHandle Locator.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  default JSHandle evaluateHandle(String expression, Object arg) {
    return evaluateHandle(expression, arg, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
   * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Locator#evaluateHandle Locator.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Locator#evaluateHandle Locator.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
   * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Locator#evaluateHandle Locator.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Locator#evaluateHandle Locator.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  JSHandle evaluateHandle(String expression, Object arg, EvaluateHandleOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
   * element, fills it and triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input
   * field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
   * However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#type Locator.type()}.
   *
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  default void fill(String value) {
    fill(value, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the
   * element, fills it and triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input
   * field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
   * However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#type Locator.type()}.
   *
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String value, FillOptions options);
  /**
   * This method narrows existing locator according to the options, for example filters by text. It can be chained to filter
   * multiple times.
   * <pre>{@code
   * Locator rowLocator = page.locator("tr");
   * // ...
   * rowLocator
   *     .filter(new Locator.FilterOptions().setHasText("text in column 1"))
   *     .filter(new Locator.FilterOptions().setHas(
   *         page.locator("button", new Page.LocatorOptions().setHasText("column 2 button"))
   *     ))
   *     .screenshot();
   * }</pre>
   */
  default Locator filter() {
    return filter(null);
  }
  /**
   * This method narrows existing locator according to the options, for example filters by text. It can be chained to filter
   * multiple times.
   * <pre>{@code
   * Locator rowLocator = page.locator("tr");
   * // ...
   * rowLocator
   *     .filter(new Locator.FilterOptions().setHasText("text in column 1"))
   *     .filter(new Locator.FilterOptions().setHas(
   *         page.locator("button", new Page.LocatorOptions().setHasText("column 2 button"))
   *     ))
   *     .screenshot();
   * }</pre>
   */
  Locator filter(FilterOptions options);
  /**
   * Returns locator to the first matching element.
   */
  Locator first();
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the element.
   */
  default void focus() {
    focus(null);
  }
  /**
   * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the element.
   */
  void focus(FocusOptions options);
  /**
   * When working with iframes, you can create a frame locator that will enter the iframe and allow selecting elements in
   * that iframe:
   * <pre>{@code
   * Locator locator = page.frameLocator("iframe").locator("text=Submit");
   * locator.click();
   * }</pre>
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors">working with
   * selectors</a> for more details.
   */
  FrameLocator frameLocator(String selector);
  /**
   * Returns element attribute value.
   *
   * @param name Attribute name to get the value for.
   */
  default String getAttribute(String name) {
    return getAttribute(name, null);
  }
  /**
   * Returns element attribute value.
   *
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String name, GetAttributeOptions options);
  /**
   * Highlight the corresponding element(s) on the screen. Useful for debugging, don't commit the code that uses {@link
   * Locator#highlight Locator.highlight()}.
   */
  void highlight();
  /**
   * This method hovers over the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  default void hover() {
    hover(null);
  }
  /**
   * This method hovers over the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  void hover(HoverOptions options);
  /**
   * Returns the {@code element.innerHTML}.
   */
  default String innerHTML() {
    return innerHTML(null);
  }
  /**
   * Returns the {@code element.innerHTML}.
   */
  String innerHTML(InnerHTMLOptions options);
  /**
   * Returns the {@code element.innerText}.
   */
  default String innerText() {
    return innerText(null);
  }
  /**
   * Returns the {@code element.innerText}.
   */
  String innerText(InnerTextOptions options);
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
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
   */
  String inputValue(InputValueOptions options);
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   */
  default boolean isChecked() {
    return isChecked(null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   */
  boolean isChecked(IsCheckedOptions options);
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   */
  default boolean isDisabled() {
    return isDisabled(null);
  }
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   */
  boolean isDisabled(IsDisabledOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   */
  default boolean isEditable() {
    return isEditable(null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   */
  boolean isEditable(IsEditableOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   */
  default boolean isEnabled() {
    return isEnabled(null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   */
  boolean isEnabled(IsEnabledOptions options);
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   */
  default boolean isHidden() {
    return isHidden(null);
  }
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   */
  boolean isHidden(IsHiddenOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   */
  default boolean isVisible() {
    return isVisible(null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>.
   */
  boolean isVisible(IsVisibleOptions options);
  /**
   * Returns locator to the last matching element.
   */
  Locator last();
  /**
   * The method finds an element matching the specified selector in the {@code Locator}'s subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors">working with
   * selectors</a> for more details.
   */
  default Locator locator(String selector) {
    return locator(selector, null);
  }
  /**
   * The method finds an element matching the specified selector in the {@code Locator}'s subtree. It also accepts filter options,
   * similar to {@link Locator#filter Locator.filter()} method.
   *
   * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors">working with
   * selectors</a> for more details.
   */
  Locator locator(String selector, LocatorOptions options);
  /**
   * Returns locator to the n-th matching element. It's zero based, {@code nth(0)} selects the first element.
   */
  Locator nth(int index);
  /**
   * A page this locator belongs to.
   */
  Page page();
  /**
   * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
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
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with the
   * modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
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
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with the
   * modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String key, PressOptions options);
  /**
   * This method captures a screenshot of the page, clipped to the size and position of a particular element matching the
   * locator. If the element is covered by other elements, it will not be actually visible on the screenshot. If the element
   * is a scrollable container, only the currently scrolled content will be visible on the screenshot.
   *
   * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then
   * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   *
   * <p> Returns the buffer with the captured screenshot.
   */
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * This method captures a screenshot of the page, clipped to the size and position of a particular element matching the
   * locator. If the element is covered by other elements, it will not be actually visible on the screenshot. If the element
   * is a scrollable container, only the currently scrolled content will be visible on the screenshot.
   *
   * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then
   * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   *
   * <p> Returns the buffer with the captured screenshot.
   */
  byte[] screenshot(ScreenshotOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then tries to
   * scroll element into view, unless it is completely visible as defined by <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code ratio}.
   */
  default void scrollIntoViewIfNeeded() {
    scrollIntoViewIfNeeded(null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then tries to
   * scroll element into view, unless it is completely visible as defined by <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code ratio}.
   */
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(ElementHandle values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(ElementHandle values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String[] values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String[] values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(SelectOption values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(SelectOption values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(ElementHandle[] values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(ElementHandle[] values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(SelectOption[] values) {
    return selectOption(values, null);
  }
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all
   * specified options are present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
   * {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <pre>{@code
   * // single selection matching the value
   * element.selectOption("blue");
   * // single selection matching the label
   * element.selectOption(new SelectOption().setLabel("Blue"));
   * // multiple selection
   * element.selectOption(new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(SelectOption[] values, SelectOptionOptions options);
  /**
   * This method waits for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks, then focuses
   * the element and selects all its text content.
   *
   * <p> If the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, focuses and selects text
   * in the control instead.
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
   */
  void selectText(SelectTextOptions options);
  /**
   * This method checks or unchecks an element by performing the following steps:
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
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param checked Whether to check or uncheck the checkbox.
   */
  default void setChecked(boolean checked) {
    setChecked(checked, null);
  }
  /**
   * This method checks or unchecks an element by performing the following steps:
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
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param checked Whether to check or uncheck the checkbox.
   */
  void setChecked(boolean checked, SetCheckedOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  default void setInputFiles(Path files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  void setInputFiles(Path files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  default void setInputFiles(Path[] files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  void setInputFiles(Path[] files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  default void setInputFiles(FilePayload files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  void setInputFiles(FilePayload files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  default void setInputFiles(FilePayload[] files) {
    setInputFiles(files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code Locator} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   */
  void setInputFiles(FilePayload[] files, SetInputFilesOptions options);
  /**
   * This method taps the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code element.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   */
  default void tap() {
    tap(null);
  }
  /**
   * This method taps the element by performing the following steps:
   * <ol>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code element.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   */
  void tap(TapOptions options);
  /**
   * Returns the {@code node.textContent}.
   */
  default String textContent() {
    return textContent(null);
  }
  /**
   * Returns the {@code node.textContent}.
   */
  String textContent(TextContentOptions options);
  /**
   * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Locator#press Locator.press()}.
   * <pre>{@code
   * element.type("Hello"); // Types instantly
   * element.type("World", new Locator.TypeOptions().setDelay(100)); // Types slower, like a user
   * }</pre>
   *
   * <p> An example of typing into a text field and then submitting the form:
   * <pre>{@code
   * Locator element = page.locator("input");
   * element.type("some text");
   * element.press("Enter");
   * }</pre>
   *
   * @param text A text to type into a focused element.
   */
  default void type(String text) {
    type(text, null);
  }
  /**
   * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Locator#press Locator.press()}.
   * <pre>{@code
   * element.type("Hello"); // Types instantly
   * element.type("World", new Locator.TypeOptions().setDelay(100)); // Types slower, like a user
   * }</pre>
   *
   * <p> An example of typing into a text field and then submitting the form:
   * <pre>{@code
   * Locator element = page.locator("input");
   * element.type("some text");
   * element.press("Enter");
   * }</pre>
   *
   * @param text A text to type into a focused element.
   */
  void type(String text, TypeOptions options);
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already unchecked,
   * this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  default void uncheck() {
    uncheck(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * <ol>
   * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already unchecked,
   * this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the element, unless
   * {@code force} option is set.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> If the element is detached from the DOM at any moment during the action, this method throws.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   */
  void uncheck(UncheckOptions options);
  /**
   * Returns when element specified by locator satisfies the {@code state} option.
   *
   * <p> If target element already satisfies the condition, the method returns immediately. Otherwise, waits for up to {@code timeout}
   * milliseconds until the condition is met.
   * <pre>{@code
   * Locator orderSent = page.locator("#order-sent");
   * orderSent.waitFor();
   * }</pre>
   */
  default void waitFor() {
    waitFor(null);
  }
  /**
   * Returns when element specified by locator satisfies the {@code state} option.
   *
   * <p> If target element already satisfies the condition, the method returns immediately. Otherwise, waits for up to {@code timeout}
   * milliseconds until the condition is met.
   * <pre>{@code
   * Locator orderSent = page.locator("#order-sent");
   * orderSent.waitFor();
   * }</pre>
   */
  void waitFor(WaitForOptions options);
}

