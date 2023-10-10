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
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * At every point of time, page exposes its current frame tree via the {@link Page#mainFrame Page.mainFrame()} and {@link
 * Frame#childFrames Frame.childFrames()} methods.
 *
 * <p> {@code Frame} object's lifecycle is controlled by three events, dispatched on the page object:
 * <ul>
 * <li> {@link Page#onFrameAttached Page.onFrameAttached()} - fired when the frame gets attached to the page. A Frame can be
 * attached to the page only once.</li>
 * <li> {@link Page#onFrameNavigated Page.onFrameNavigated()} - fired when the frame commits navigation to a different URL.</li>
 * <li> {@link Page#onFrameDetached Page.onFrameDetached()} - fired when the frame gets detached from the page.  A Frame can be
 * detached from the page only once.</li>
 * </ul>
 *
 * <p> An example of dumping frame tree:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 *
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       BrowserType firefox = playwright.firefox();
 *       Browser browser = firefox.launch();
 *       Page page = browser.newPage();
 *       page.navigate("https://www.google.com/chrome/browser/canary.html");
 *       dumpFrameTree(page.mainFrame(), "");
 *       browser.close();
 *     }
 *   }
 *   static void dumpFrameTree(Frame frame, String indent) {
 *     System.out.println(indent + frame.url());
 *     for (Frame child : frame.childFrames()) {
 *       dumpFrameTree(child, indent + "  ");
 *     }
 *   }
 * }
 * }</pre>
 */
public interface Frame {
  class AddScriptTagOptions {
    /**
     * Raw JavaScript content to be injected into frame.
     */
    public String content;
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative
     * to the current working directory.
     */
    public Path path;
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script">script</a> for more details.
     */
    public String type;
    /**
     * URL of a script to be added.
     */
    public String url;

    /**
     * Raw JavaScript content to be injected into frame.
     */
    public AddScriptTagOptions setContent(String content) {
      this.content = content;
      return this;
    }
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative
     * to the current working directory.
     */
    public AddScriptTagOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script">script</a> for more details.
     */
    public AddScriptTagOptions setType(String type) {
      this.type = type;
      return this;
    }
    /**
     * URL of a script to be added.
     */
    public AddScriptTagOptions setUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class AddStyleTagOptions {
    /**
     * Raw CSS content to be injected into frame.
     */
    public String content;
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    public Path path;
    /**
     * URL of the {@code <link>} tag.
     */
    public String url;

    /**
     * Raw CSS content to be injected into frame.
     */
    public AddStyleTagOptions setContent(String content) {
      this.content = content;
      return this;
    }
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    public AddStyleTagOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * URL of the {@code <link>} tag.
     */
    public AddStyleTagOptions setUrl(String url) {
      this.url = url;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public CheckOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public ClickOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public DblclickOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public DispatchEventOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
  class DragAndDropOptions {
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
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
    public DragAndDropOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public DragAndDropOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setSourcePosition(double x, double y) {
      return setSourcePosition(new Position(x, y));
    }
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setSourcePosition(Position sourcePosition) {
      this.sourcePosition = sourcePosition;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public DragAndDropOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setTargetPosition(double x, double y) {
      return setTargetPosition(new Position(x, y));
    }
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setTargetPosition(Position targetPosition) {
      this.targetPosition = targetPosition;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public DragAndDropOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public DragAndDropOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class EvalOnSelectorOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public EvalOnSelectorOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public FillOptions setStrict(boolean strict) {
      this.strict = strict;
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
  class FocusOptions {
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public FocusOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public GetAttributeOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
  class NavigateOptions {
    /**
     * Referer header value. If provided it will take preference over the referer header value set by {@link
     * Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}.
     */
    public String referer;
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Referer header value. If provided it will take preference over the referer header value set by {@link
     * Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}.
     */
    public NavigateOptions setReferer(String referer) {
      this.referer = referer;
      return this;
    }
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public NavigateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public NavigateOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public HoverOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public InnerHTMLOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public InnerTextOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public InputValueOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsCheckedOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsDisabledOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsEditableOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsEnabledOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * @deprecated This option is ignored. {@link Frame#isHidden Frame.isHidden()} does not wait for the element to become hidden and
     * returns immediately.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsHiddenOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * @deprecated This option is ignored. {@link Frame#isHidden Frame.isHidden()} does not wait for the element to become hidden and
     * returns immediately.
     */
    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * @deprecated This option is ignored. {@link Frame#isVisible Frame.isVisible()} does not wait for the element to become visible and
     * returns immediately.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsVisibleOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * @deprecated This option is ignored. {@link Frame#isVisible Frame.isVisible()} does not wait for the element to become visible and
     * returns immediately.
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public PressOptions setStrict(boolean strict) {
      this.strict = strict;
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
  class QuerySelectorOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public QuerySelectorOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public SelectOptionOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public SetCheckedOptions setStrict(boolean strict) {
      this.strict = strict;
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
  class SetContentOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetContentOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public SetContentOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
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
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SetInputFilesOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public SetInputFilesOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public TapOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public TextContentOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public TypeOptions setStrict(boolean strict) {
      this.strict = strict;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public UncheckOptions setStrict(boolean strict) {
      this.strict = strict;
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
  class WaitForFunctionOptions {
    /**
     * If specified, then it is treated as an interval in milliseconds at which the function would be executed. By default if
     * the option is not specified {@code expression} is executed in {@code requestAnimationFrame} callback.
     */
    public Double pollingInterval;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or
     * {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * If specified, then it is treated as an interval in milliseconds at which the function would be executed. By default if
     * the option is not specified {@code expression} is executed in {@code requestAnimationFrame} callback.
     */
    public WaitForFunctionOptions setPollingInterval(double pollingInterval) {
      this.pollingInterval = pollingInterval;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or
     * {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForFunctionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForLoadStateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public Object url;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForNavigationOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public WaitForNavigationOptions setUrl(String url) {
      this.url = url;
      return this;
    }
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public WaitForNavigationOptions setUrl(Pattern url) {
      this.url = url;
      return this;
    }
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public WaitForNavigationOptions setUrl(Predicate<String> url) {
      this.url = url;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitForNavigationOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
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
  class WaitForURLOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForURLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitForURLOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  /**
   * Returns the added tag when the script's onload fires or when the script content was injected into frame.
   *
   * <p> Adds a {@code <script>} tag into the page with the desired url or content.
   *
   * @since v1.8
   */
  default ElementHandle addScriptTag() {
    return addScriptTag(null);
  }
  /**
   * Returns the added tag when the script's onload fires or when the script content was injected into frame.
   *
   * <p> Adds a {@code <script>} tag into the page with the desired url or content.
   *
   * @since v1.8
   */
  ElementHandle addScriptTag(AddScriptTagOptions options);
  /**
   * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   *
   * <p> Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag
   * with the content.
   *
   * @since v1.8
   */
  default ElementHandle addStyleTag() {
    return addStyleTag(null);
  }
  /**
   * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   *
   * <p> Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag
   * with the content.
   *
   * @since v1.8
   */
  ElementHandle addStyleTag(AddStyleTagOptions options);
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * checked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void check(String selector) {
    check(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * checked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void check(String selector, CheckOptions options);
  /**
   *
   *
   * @since v1.8
   */
  List<Frame> childFrames();
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void click(String selector, ClickOptions options);
  /**
   * Gets the full HTML contents of the frame, including the doctype.
   *
   * @since v1.8
   */
  String content();
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   * first click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void dblclick(String selector) {
    dblclick(selector, null);
  }
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   * first click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void dblclick(String selector, DblclickOptions options);
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.dispatchEvent("button#submit", "click");
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
   * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * frame.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.8
   */
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.dispatchEvent("button#submit", "click");
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
   * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * frame.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @since v1.8
   */
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.dispatchEvent("button#submit", "click");
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
   * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * frame.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.8
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  /**
   *
   *
   * @param source A selector to search for an element to drag. If there are multiple elements satisfying the selector, the first will be
   * used.
   * @param target A selector to search for an element to drop onto. If there are multiple elements satisfying the selector, the first will
   * be used.
   * @since v1.13
   */
  default void dragAndDrop(String source, String target) {
    dragAndDrop(source, target, null);
  }
  /**
   *
   *
   * @param source A selector to search for an element to drag. If there are multiple elements satisfying the selector, the first will be
   * used.
   * @param target A selector to search for an element to drop onto. If there are multiple elements satisfying the selector, the first will
   * be used.
   * @since v1.13
   */
  void dragAndDrop(String source, String target, DragAndDropOptions options);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to {@code
   * expression}. If no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  default Object evalOnSelector(String selector, String expression, Object arg) {
    return evalOnSelector(selector, expression, arg, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to {@code
   * expression}. If no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
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
   * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to {@code
   * expression}. If no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  Object evalOnSelector(String selector, String expression, Object arg, EvalOnSelectorOptions options);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
   * as a first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelectorAll Frame.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean divsCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
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
   * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
   * as a first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelectorAll Frame.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean divsCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
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
   * Returns the return value of {@code expression}.
   *
   * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evaluate Frame.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a non-[Serializable] value, then {@link
   * Frame#evaluate Frame.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values
   * that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * <p> **Usage**
   * <pre>{@code
   * Object result = frame.evaluate("([x, y]) => {\n" +
   *   "  return Promise.resolve(x * y);\n" +
   *   "}", Arrays.asList(7, 8));
   * System.out.println(result); // prints "56"
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function.
   * <pre>{@code
   * System.out.println(frame.evaluate("1 + 2")); // prints "3"
   * }</pre>
   *
   * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Frame#evaluate Frame.evaluate()}:
   * <pre>{@code
   * ElementHandle bodyHandle = frame.evaluate("document.body");
   * String html = (String) frame.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
   * bodyHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evaluate Frame.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a non-[Serializable] value, then {@link
   * Frame#evaluate Frame.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values
   * that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * <p> **Usage**
   * <pre>{@code
   * Object result = frame.evaluate("([x, y]) => {\n" +
   *   "  return Promise.resolve(x * y);\n" +
   *   "}", Arrays.asList(7, 8));
   * System.out.println(result); // prints "56"
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function.
   * <pre>{@code
   * System.out.println(frame.evaluate("1 + 2")); // prints "3"
   * }</pre>
   *
   * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Frame#evaluate Frame.evaluate()}:
   * <pre>{@code
   * ElementHandle bodyHandle = frame.evaluate("document.body");
   * String html = (String) frame.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
   * bodyHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  Object evaluate(String expression, Object arg);
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> The only difference between {@link Frame#evaluate Frame.evaluate()} and {@link Frame#evaluateHandle
   * Frame.evaluateHandle()} is that {@link Frame#evaluateHandle Frame.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function, passed to the {@link Frame#evaluateHandle Frame.evaluateHandle()}, returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evaluateHandle Frame.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Handle for the window object.
   * JSHandle aWindowHandle = frame.evaluateHandle("() => Promise.resolve(window)");
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function.
   * <pre>{@code
   * JSHandle aHandle = frame.evaluateHandle("document"); // Handle for the "document".
   * }</pre>
   *
   * <p> {@code JSHandle} instances can be passed as an argument to the {@link Frame#evaluateHandle Frame.evaluateHandle()}:
   * <pre>{@code
   * JSHandle aHandle = frame.evaluateHandle("() => document.body");
   * JSHandle resultHandle = frame.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
   * System.out.println(resultHandle.jsonValue());
   * resultHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> The only difference between {@link Frame#evaluate Frame.evaluate()} and {@link Frame#evaluateHandle
   * Frame.evaluateHandle()} is that {@link Frame#evaluateHandle Frame.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function, passed to the {@link Frame#evaluateHandle Frame.evaluateHandle()}, returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evaluateHandle Frame.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Handle for the window object.
   * JSHandle aWindowHandle = frame.evaluateHandle("() => Promise.resolve(window)");
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function.
   * <pre>{@code
   * JSHandle aHandle = frame.evaluateHandle("document"); // Handle for the "document".
   * }</pre>
   *
   * <p> {@code JSHandle} instances can be passed as an argument to the {@link Frame#evaluateHandle Frame.evaluateHandle()}:
   * <pre>{@code
   * JSHandle aHandle = frame.evaluateHandle("() => document.body");
   * JSHandle resultHandle = frame.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
   * System.out.println(resultHandle.jsonValue());
   * resultHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  JSHandle evaluateHandle(String expression, Object arg);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the element, fills it and
   * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#pressSequentially Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   * @since v1.8
   */
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the element, fills it and
   * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#pressSequentially Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   * @since v1.8
   */
  void fill(String selector, String value, FillOptions options);
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector},
   * the method waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector},
   * the method waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void focus(String selector, FocusOptions options);
  /**
   * Returns the {@code frame} or {@code iframe} element handle which corresponds to this frame.
   *
   * <p> This is an inverse of {@link ElementHandle#contentFrame ElementHandle.contentFrame()}. Note that returned handle
   * actually belongs to the parent frame.
   *
   * <p> This method throws an error if the frame has been detached before {@code frameElement()} returns.
   *
   * <p> **Usage**
   * <pre>{@code
   * ElementHandle frameElement = frame.frameElement();
   * Frame contentFrame = frameElement.contentFrame();
   * System.out.println(frame == contentFrame);  // -> true
   * }</pre>
   *
   * @since v1.8
   */
  ElementHandle frameElement();
  /**
   * When working with iframes, you can create a frame locator that will enter the iframe and allow selecting elements in
   * that iframe.
   *
   * <p> **Usage**
   *
   * <p> Following snippet locates element with text "Submit" in the iframe with id {@code my-frame}, like {@code <iframe
   * id="my-frame">}:
   * <pre>{@code
   * Locator locator = frame.frameLocator("#my-iframe").getByText("Submit");
   * locator.click();
   * }</pre>
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.17
   */
  FrameLocator frameLocator(String selector);
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param name Attribute name to get the value for.
   * @since v1.8
   */
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param name Attribute name to get the value for.
   * @since v1.8
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
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
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   *
   * <p> The method will throw an error if:
   * <ul>
   * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
   * <li> target URL is invalid.</li>
   * <li> the {@code timeout} is exceeded during navigation.</li>
   * <li> the remote server does not respond or is unreachable.</li>
   * <li> the main resource failed to load.</li>
   * </ul>
   *
   * <p> The method will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
   * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
   * Response#status Response.status()}.
   *
   * <p> <strong>NOTE:</strong> The method either throws an error or returns a main resource response. The only exceptions are navigation to {@code
   * about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   *
   * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
   * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
   *
   * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
   * @since v1.8
   */
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   *
   * <p> The method will throw an error if:
   * <ul>
   * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
   * <li> target URL is invalid.</li>
   * <li> the {@code timeout} is exceeded during navigation.</li>
   * <li> the remote server does not respond or is unreachable.</li>
   * <li> the main resource failed to load.</li>
   * </ul>
   *
   * <p> The method will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
   * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
   * Response#status Response.status()}.
   *
   * <p> <strong>NOTE:</strong> The method either throws an error or returns a main resource response. The only exceptions are navigation to {@code
   * about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   *
   * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
   * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
   *
   * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
   * @since v1.8
   */
  Response navigate(String url, NavigateOptions options);
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void hover(String selector, HoverOptions options);
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  String innerText(String selector, InnerTextOptions options);
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.13
   */
  default String inputValue(String selector) {
    return inputValue(selector, null);
  }
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.13
   */
  String inputValue(String selector, InputValueOptions options);
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isChecked(String selector) {
    return isChecked(selector, null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isChecked(String selector, IsCheckedOptions options);
  /**
   * Returns {@code true} if the frame has been detached, or {@code false} otherwise.
   *
   * @since v1.8
   */
  boolean isDetached();
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isDisabled(String selector) {
    return isDisabled(selector, null);
  }
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isDisabled(String selector, IsDisabledOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isEditable(String selector) {
    return isEditable(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isEditable(String selector, IsEditableOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isEnabled(String selector) {
    return isEnabled(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isEnabled(String selector, IsEnabledOptions options);
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.  {@code selector} that does not match any
   * elements is considered hidden.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isHidden(String selector) {
    return isHidden(selector, null);
  }
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.  {@code selector} that does not match any
   * elements is considered hidden.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isHidden(String selector, IsHiddenOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>. {@code
   * selector} that does not match any elements is considered not visible.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isVisible(String selector) {
    return isVisible(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>. {@code
   * selector} that does not match any elements is considered not visible.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isVisible(String selector, IsVisibleOptions options);
  /**
   * The method returns an element locator that can be used to perform actions on this page / frame. Locator is resolved to
   * the element immediately before performing an action, so a series of actions on the same locator can in fact be performed
   * on different DOM elements. That would happen if the DOM structure between those actions has changed.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.14
   */
  default Locator locator(String selector) {
    return locator(selector, null);
  }
  /**
   * The method returns an element locator that can be used to perform actions on this page / frame. Locator is resolved to
   * the element immediately before performing an action, so a series of actions on the same locator can in fact be performed
   * on different DOM elements. That would happen if the DOM structure between those actions has changed.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.14
   */
  Locator locator(String selector, LocatorOptions options);
  /**
   * Returns frame's name attribute as specified in the tag.
   *
   * <p> If the name is empty, returns the id attribute instead.
   *
   * <p> <strong>NOTE:</strong> This value is calculated once when the frame is created, and will not update if the attribute is changed later.
   *
   * @since v1.8
   */
  String name();
  /**
   * Returns the page containing this frame.
   *
   * @since v1.8
   */
  Page page();
  /**
   * Parent frame, if any. Detached frames and main frames return {@code null}.
   *
   * @since v1.8
   */
  Frame parentFrame();
  /**
   * {@code key} can specify the intended <a
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
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  /**
   * {@code key} can specify the intended <a
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
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  void press(String selector, String key, PressOptions options);
  /**
   * Returns the ElementHandle pointing to the frame element.
   *
   * <p> <strong>NOTE:</strong> The use of {@code ElementHandle} is discouraged, use {@code Locator} objects and web-first assertions instead.
   *
   * <p> The method finds an element matching the specified selector within the frame. If no elements match the selector, returns
   * {@code null}.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  default ElementHandle querySelector(String selector) {
    return querySelector(selector, null);
  }
  /**
   * Returns the ElementHandle pointing to the frame element.
   *
   * <p> <strong>NOTE:</strong> The use of {@code ElementHandle} is discouraged, use {@code Locator} objects and web-first assertions instead.
   *
   * <p> The method finds an element matching the specified selector within the frame. If no elements match the selector, returns
   * {@code null}.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  ElementHandle querySelector(String selector, QuerySelectorOptions options);
  /**
   * Returns the ElementHandles pointing to the frame elements.
   *
   * <p> <strong>NOTE:</strong> The use of {@code ElementHandle} is discouraged, use {@code Locator} objects instead.
   *
   * <p> The method finds all elements matching the specified selector within the frame. If no elements match the selector,
   * returns empty array.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  List<ElementHandle> querySelectorAll(String selector);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, String values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, String values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, ElementHandle values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, ElementHandle values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, String[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, String[] values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, SelectOption values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, SelectOption values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, ElementHandle[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, SelectOption[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options);
  /**
   * This method checks or unchecks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
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
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param checked Whether to check or uncheck the checkbox.
   * @since v1.15
   */
  default void setChecked(String selector, boolean checked) {
    setChecked(selector, checked, null);
  }
  /**
   * This method checks or unchecks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
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
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param checked Whether to check or uncheck the checkbox.
   * @since v1.15
   */
  void setChecked(String selector, boolean checked, SetCheckedOptions options);
  /**
   * This method internally calls <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Document/write">document.write()</a>, inheriting all its specific
   * characteristics and behaviors.
   *
   * @param html HTML markup to assign to the page.
   * @since v1.8
   */
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   * This method internally calls <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Document/write">document.write()</a>, inheriting all its specific
   * characteristics and behaviors.
   *
   * @param html HTML markup to assign to the page.
   * @since v1.8
   */
  void setContent(String html, SetContentOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, Path files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, Path files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, Path[] files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, FilePayload files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, FilePayload[] files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options);
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void tap(String selector, TapOptions options);
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Returns the page title.
   *
   * @since v1.8
   */
  String title();
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  void type(String selector, String text, TypeOptions options);
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * unchecked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * unchecked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void uncheck(String selector, UncheckOptions options);
  /**
   * Returns frame's url.
   *
   * @since v1.8
   */
  String url();
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
   *
   * <p> **Usage**
   *
   * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType firefox = playwright.firefox();
   *       Browser browser = firefox.launch();
   *       Page page = browser.newPage();
   *       page.setViewportSize(50, 50);
   *       page.mainFrame().waitForFunction("window.innerWidth < 100");
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
   * <pre>{@code
   * String selector = ".foo";
   * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  default JSHandle waitForFunction(String expression, Object arg) {
    return waitForFunction(expression, arg, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
   *
   * <p> **Usage**
   *
   * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType firefox = playwright.firefox();
   *       Browser browser = firefox.launch();
   *       Page page = browser.newPage();
   *       page.setViewportSize(50, 50);
   *       page.mainFrame().waitForFunction("window.innerWidth < 100");
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
   * <pre>{@code
   * String selector = ".foo";
   * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default JSHandle waitForFunction(String expression) {
    return waitForFunction(expression, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
   *
   * <p> **Usage**
   *
   * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType firefox = playwright.firefox();
   *       Browser browser = firefox.launch();
   *       Page page = browser.newPage();
   *       page.setViewportSize(50, 50);
   *       page.mainFrame().waitForFunction("window.innerWidth < 100");
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
   * <pre>{@code
   * String selector = ".foo";
   * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options);
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been
   * committed when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("button"); // Click triggers navigation.
   * frame.waitForLoadState(); // Waits for "load" state by default.
   * }</pre>
   *
   * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
   * document, the method resolves immediately. Can be one of:
   * <ul>
   * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
   * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
   * <li> {@code "networkidle"} - **DISCOURAGED** wait until there are no network connections for at least {@code 500} ms. Don't
   * use this method for testing, rely on web assertions to assess readiness instead.</li>
   * </ul>
   * @since v1.8
   */
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been
   * committed when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("button"); // Click triggers navigation.
   * frame.waitForLoadState(); // Waits for "load" state by default.
   * }</pre>
   *
   * @since v1.8
   */
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been
   * committed when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("button"); // Click triggers navigation.
   * frame.waitForLoadState(); // Waits for "load" state by default.
   * }</pre>
   *
   * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
   * document, the method resolves immediately. Can be one of:
   * <ul>
   * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
   * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
   * <li> {@code "networkidle"} - **DISCOURAGED** wait until there are no network connections for at least {@code 500} ms. Don't
   * use this method for testing, rely on web assertions to assess readiness instead.</li>
   * </ul>
   * @since v1.8
   */
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  /**
   * @deprecated This method is inherently racy, please use {@link Frame#waitForURL Frame.waitForURL()} instead.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Response waitForNavigation(Runnable callback) {
    return waitForNavigation(null, callback);
  }
  /**
   * @deprecated This method is inherently racy, please use {@link Frame#waitForURL Frame.waitForURL()} instead.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Response waitForNavigation(WaitForNavigationOptions options, Runnable callback);
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code
   * hidden} or {@code detached}.
   *
   * <p> <strong>NOTE:</strong> Playwright automatically waits for element to be ready before performing an action. Using {@code Locator} objects and
   * web-first assertions make the code wait-for-selector-free.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become
   * visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
   * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the
   * function will throw.
   *
   * <p> **Usage**
   *
   * <p> This method works across navigations:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType chromium = playwright.chromium();
   *       Browser browser = chromium.launch();
   *       Page page = browser.newPage();
   *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
   *         page.navigate(currentURL);
   *         ElementHandle element = page.mainFrame().waitForSelector("img");
   *         System.out.println("Loaded image: " + element.getAttribute("src"));
   *       }
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param selector A selector to query for.
   * @since v1.8
   */
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code
   * hidden} or {@code detached}.
   *
   * <p> <strong>NOTE:</strong> Playwright automatically waits for element to be ready before performing an action. Using {@code Locator} objects and
   * web-first assertions make the code wait-for-selector-free.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become
   * visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
   * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the
   * function will throw.
   *
   * <p> **Usage**
   *
   * <p> This method works across navigations:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType chromium = playwright.chromium();
   *       Browser browser = chromium.launch();
   *       Page page = browser.newPage();
   *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
   *         page.navigate(currentURL);
   *         ElementHandle element = page.mainFrame().waitForSelector("img");
   *         System.out.println("Loaded image: " + element.getAttribute("src"));
   *       }
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param selector A selector to query for.
   * @since v1.8
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * Waits for the given {@code timeout} in milliseconds.
   *
   * <p> Note that {@code frame.waitForTimeout()} should only be used for debugging. Tests using the timer in production are
   * going to be flaky. Use signals such as network events, selectors becoming visible and others instead.
   *
   * @param timeout A timeout to wait for
   * @since v1.8
   */
  void waitForTimeout(double timeout);
  /**
   * Waits for the frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  default void waitForURL(String url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  void waitForURL(String url, WaitForURLOptions options);
  /**
   * Waits for the frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  default void waitForURL(Pattern url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  void waitForURL(Pattern url, WaitForURLOptions options);
  /**
   * Waits for the frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  default void waitForURL(Predicate<String> url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  void waitForURL(Predicate<String> url, WaitForURLOptions options);
}

