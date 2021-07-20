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
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
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

    public AddScriptTagOptions setContent(String content) {
      this.content = content;
      return this;
    }
    public AddScriptTagOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    public AddScriptTagOptions setType(String type) {
      this.type = type;
      return this;
    }
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

    public AddStyleTagOptions setContent(String content) {
      this.content = content;
      return this;
    }
    public AddStyleTagOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    public AddStyleTagOptions setUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class CheckOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public CheckOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public CheckOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public CheckOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    public CheckOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    public CheckOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public ClickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public ClickOptions setClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public ClickOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public ClickOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public ClickOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public ClickOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    public ClickOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    public ClickOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public DblclickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public DblclickOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public DblclickOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public DblclickOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public DblclickOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public DblclickOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    public DblclickOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    public DblclickOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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

    public DispatchEventOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DragAndDropOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public DragAndDropOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public DragAndDropOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public DragAndDropOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public DragAndDropOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class FillOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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

    public FillOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public FillOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public FillOptions setTimeout(double timeout) {
      this.timeout = timeout;
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

    public GetAttributeOptions setTimeout(double timeout) {
      this.timeout = timeout;
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
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - consider operation to be finished when there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    public NavigateOptions setReferer(String referer) {
      this.referer = referer;
      return this;
    }
    public NavigateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class HoverOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public HoverOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public HoverOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public HoverOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    public HoverOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    public HoverOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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

    public IsEnabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    public IsVisibleOptions setTimeout(double timeout) {
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
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    public PressOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public PressOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public PressOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectOptionOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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

    public SelectOptionOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public SelectOptionOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SelectOptionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetContentOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - consider operation to be finished when there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    public SetContentOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    public SetInputFilesOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetInputFilesOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public TapOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public TapOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    public TapOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TapOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    public TapOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    public TapOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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

    public TypeOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public TypeOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TypeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
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
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    public UncheckOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    public UncheckOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public UncheckOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    public UncheckOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    public UncheckOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
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
     * maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    public WaitForFunctionOptions setPollingInterval(double pollingInterval) {
      this.pollingInterval = pollingInterval;
      return this;
    }
    public WaitForFunctionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    public WaitForLoadStateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    public Object url;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - consider operation to be finished when there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    public WaitForNavigationOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public WaitForNavigationOptions setUrl(String url) {
      this.url = url;
      return this;
    }
    public WaitForNavigationOptions setUrl(Pattern url) {
      this.url = url;
      return this;
    }
    public WaitForNavigationOptions setUrl(Predicate<String> url) {
      this.url = url;
      return this;
    }
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

    public WaitForSelectorOptions setState(WaitForSelectorState state) {
      this.state = state;
      return this;
    }
    public WaitForSelectorOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForURLOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - consider operation to be finished when there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    public WaitForURLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public WaitForURLOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  /**
   * Returns the added tag when the script's onload fires or when the script content was injected into frame.
   *
   * <p> Adds a {@code <script>} tag into the page with the desired url or content.
   */
  default ElementHandle addScriptTag() {
    return addScriptTag(null);
  }
  /**
   * Returns the added tag when the script's onload fires or when the script content was injected into frame.
   *
   * <p> Adds a {@code <script>} tag into the page with the desired url or content.
   */
  ElementHandle addScriptTag(AddScriptTagOptions options);
  /**
   * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   *
   * <p> Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
   * content.
   */
  default ElementHandle addStyleTag() {
    return addStyleTag(null);
  }
  /**
   * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   *
   * <p> Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
   * content.
   */
  ElementHandle addStyleTag(AddStyleTagOptions options);
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * checked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
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
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void check(String selector, CheckOptions options);
  List<Frame> childFrames();
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void click(String selector, ClickOptions options);
  /**
   * Gets the full HTML contents of the frame, including the doctype.
   */
  String content();
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
   * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void dblclick(String selector) {
    dblclick(selector, null);
  }
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
   * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void dblclick(String selector, DblclickOptions options);
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   * <pre>{@code
   * frame.dispatchEvent("button#submit", "click");
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
   * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * frame.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   * <pre>{@code
   * frame.dispatchEvent("button#submit", "click");
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
   * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * frame.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   */
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   * <pre>{@code
   * frame.dispatchEvent("button#submit", "click");
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
   * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * frame.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  default void dragAndDrop(String source, String target) {
    dragAndDrop(source, target, null);
  }
  void dragAndDrop(String source, String target, DragAndDropOptions options);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to
   * {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If
   * no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default Object evalOnSelector(String selector, String expression) {
    return evalOnSelector(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to
   * {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If
   * no elements match the selector, the method throws an error.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evalOnSelector(String selector, String expression, Object arg);
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
   * as a first argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with
   * selectors</a> for more details.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelectorAll Frame.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * boolean divsCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default Object evalOnSelectorAll(String selector, String expression) {
    return evalOnSelectorAll(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
   * as a first argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with
   * selectors</a> for more details.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evalOnSelectorAll Frame.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
   *
   * <p> Examples:
   * <pre>{@code
   * boolean divsCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
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
   * Frame#evaluate Frame.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values that
   * are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
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
   * ElementHandle bodyHandle = frame.querySelector("body");
   * String html = (String) frame.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
   * bodyHandle.dispose();
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
   * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Frame#evaluate Frame.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a non-[Serializable] value, then {@link
   * Frame#evaluate Frame.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values that
   * are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
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
   * ElementHandle bodyHandle = frame.querySelector("body");
   * String html = (String) frame.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
   * bodyHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
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
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
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
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  JSHandle evaluateHandle(String expression, Object arg);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the element, fills it and
   * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
   * However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Frame#type Frame.type()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the element, fills it and
   * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
   * However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Frame#type Frame.type()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String selector, String value, FillOptions options);
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
   * waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
   * waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void focus(String selector, FocusOptions options);
  /**
   * Returns the {@code frame} or {@code iframe} element handle which corresponds to this frame.
   *
   * <p> This is an inverse of {@link ElementHandle#contentFrame ElementHandle.contentFrame()}. Note that returned handle
   * actually belongs to the parent frame.
   *
   * <p> This method throws an error if the frame has been detached before {@code frameElement()} returns.
   * <pre>{@code
   * ElementHandle frameElement = frame.frameElement();
   * Frame contentFrame = frameElement.contentFrame();
   * System.out.println(frame == contentFrame);  // -> true
   * }</pre>
   */
  ElementHandle frameElement();
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param name Attribute name to get the value for.
   */
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   *
   * <p> {@code frame.goto} will throw an error if:
   * <ul>
   * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
   * <li> target URL is invalid.</li>
   * <li> the {@code timeout} is exceeded during navigation.</li>
   * <li> the remote server does not respond or is unreachable.</li>
   * <li> the main resource failed to load.</li>
   * </ul>
   *
   * <p> {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404
   * "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
   * Response#status Response.status()}.
   *
   * <p> <strong>NOTE:</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
   * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   *
   * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
   * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
   *
   * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
   */
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   *
   * <p> {@code frame.goto} will throw an error if:
   * <ul>
   * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
   * <li> target URL is invalid.</li>
   * <li> the {@code timeout} is exceeded during navigation.</li>
   * <li> the remote server does not respond or is unreachable.</li>
   * <li> the main resource failed to load.</li>
   * </ul>
   *
   * <p> {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404
   * "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
   * Response#status Response.status()}.
   *
   * <p> <strong>NOTE:</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
   * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   *
   * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
   * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
   *
   * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
   */
  Response navigate(String url, NavigateOptions options);
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void hover(String selector, HoverOptions options);
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  String innerText(String selector, InnerTextOptions options);
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} element. Throws for non-input elements.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default String inputValue(String selector) {
    return inputValue(selector, null);
  }
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} element. Throws for non-input elements.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  String inputValue(String selector, InputValueOptions options);
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default boolean isChecked(String selector) {
    return isChecked(selector, null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  boolean isChecked(String selector, IsCheckedOptions options);
  /**
   * Returns {@code true} if the frame has been detached, or {@code false} otherwise.
   */
  boolean isDetached();
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default boolean isDisabled(String selector) {
    return isDisabled(selector, null);
  }
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  boolean isDisabled(String selector, IsDisabledOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default boolean isEditable(String selector) {
    return isEditable(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  boolean isEditable(String selector, IsEditableOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default boolean isEnabled(String selector) {
    return isEnabled(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  boolean isEnabled(String selector, IsEnabledOptions options);
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.  {@code selector} that does not match any elements
   * is considered hidden.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default boolean isHidden(String selector) {
    return isHidden(selector, null);
  }
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.  {@code selector} that does not match any elements
   * is considered hidden.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  boolean isHidden(String selector, IsHiddenOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>. {@code selector}
   * that does not match any elements is considered not visible.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default boolean isVisible(String selector) {
    return isVisible(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>. {@code selector}
   * that does not match any elements is considered not visible.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  boolean isVisible(String selector, IsVisibleOptions options);
  /**
   * Returns frame's name attribute as specified in the tag.
   *
   * <p> If the name is empty, returns the id attribute instead.
   *
   * <p> <strong>NOTE:</strong> This value is calculated once when the frame is created, and will not update if the attribute is changed later.
   */
  String name();
  /**
   * Returns the page containing this frame.
   */
  Page page();
  /**
   * Parent frame, if any. Detached frames and main frames return {@code null}.
   */
  Frame parentFrame();
  /**
   * {@code key} can specify the intended <a
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
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
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
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String selector, String key, PressOptions options);
  /**
   * Returns the ElementHandle pointing to the frame element.
   *
   * <p> The method finds an element matching the specified selector within the frame. See <a
   * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
   * selector, returns {@code null}.
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   */
  ElementHandle querySelector(String selector);
  /**
   * Returns the ElementHandles pointing to the frame elements.
   *
   * <p> The method finds all elements matching the specified selector within the frame. See <a
   * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
   * selector, returns empty array.
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String selector, String values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, String values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String selector, ElementHandle values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, ElementHandle values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String selector, String[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, String[] values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String selector, SelectOption values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, SelectOption values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String selector, ElementHandle[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  default List<String> selectOption(String selector, SelectOption[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
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
   * frame.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options);
  /**
   *
   *
   * @param html HTML markup to assign to the page.
   */
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   *
   *
   * @param html HTML markup to assign to the page.
   */
  void setContent(String html, SetContentOptions options);
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void setInputFiles(String selector, Path files) {
    setInputFiles(selector, files, null);
  }
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void setInputFiles(String selector, Path files, SetInputFilesOptions options);
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void setInputFiles(String selector, Path[] files) {
    setInputFiles(selector, files, null);
  }
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void setInputFiles(String selector, FilePayload files) {
    setInputFiles(selector, files, null);
  }
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options);
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void setInputFiles(String selector, FilePayload[] files) {
    setInputFiles(selector, files, null);
  }
  /**
   * This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options);
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void tap(String selector, TapOptions options);
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Returns the page title.
   */
  String title();
  /**
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
   * send fine-grained keyboard events. To fill values in form fields, use {@link Frame#fill Frame.fill()}.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
   * <pre>{@code
   * // Types instantly
   * frame.type("#mytextarea", "Hello");
   * // Types slower, like a user
   * frame.type("#mytextarea", "World", new Frame.TypeOptions().setDelay(100));
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param text A text to type into a focused element.
   */
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
   * send fine-grained keyboard events. To fill values in form fields, use {@link Frame#fill Frame.fill()}.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
   * <pre>{@code
   * // Types instantly
   * frame.type("#mytextarea", "Hello");
   * // Types slower, like a user
   * frame.type("#mytextarea", "World", new Frame.TypeOptions().setDelay(100));
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   * @param text A text to type into a focused element.
   */
  void type(String selector, String text, TypeOptions options);
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * unchecked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
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
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
   * zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
   * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
   */
  void uncheck(String selector, UncheckOptions options);
  /**
   * Returns frame's url.
   */
  String url();
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
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
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  default JSHandle waitForFunction(String expression, Object arg) {
    return waitForFunction(expression, arg, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
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
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   */
  default JSHandle waitForFunction(String expression) {
    return waitForFunction(expression, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
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
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options);
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
   * when this method is called. If current document has already reached the required state, resolves immediately.
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
   * <li> {@code "networkidle"} - wait until there are no network connections for at least {@code 500} ms.</li>
   * </ul>
   */
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
   * when this method is called. If current document has already reached the required state, resolves immediately.
   * <pre>{@code
   * frame.click("button"); // Click triggers navigation.
   * frame.waitForLoadState(); // Waits for "load" state by default.
   * }</pre>
   */
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
   * when this method is called. If current document has already reached the required state, resolves immediately.
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
   * <li> {@code "networkidle"} - wait until there are no network connections for at least {@code 500} ms.</li>
   * </ul>
   */
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  /**
   * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
   * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
   * History API usage, the navigation will resolve with {@code null}.
   *
   * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
   * the frame to navigate. Consider this example:
   * <pre>{@code
   * // The method returns after navigation has finished
   * Response response = frame.waitForNavigation(() -> {
   *   // Clicking the link will indirectly cause a navigation
   *   frame.click("a.delayed-navigation");
   * });
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
   * considered a navigation.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  default Response waitForNavigation(Runnable callback) {
    return waitForNavigation(null, callback);
  }
  /**
   * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
   * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
   * History API usage, the navigation will resolve with {@code null}.
   *
   * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
   * the frame to navigate. Consider this example:
   * <pre>{@code
   * // The method returns after navigation has finished
   * Response response = frame.waitForNavigation(() -> {
   *   // Clicking the link will indirectly cause a navigation
   *   frame.click("a.delayed-navigation");
   * });
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
   * considered a navigation.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  Response waitForNavigation(WaitForNavigationOptions options, Runnable callback);
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
   * {@code detached}.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
   * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
   * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
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
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   */
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
   * {@code detached}.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
   * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
   * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
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
   * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
   * details.
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * Waits for the given {@code timeout} in milliseconds.
   *
   * <p> Note that {@code frame.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to
   * be flaky. Use signals such as network events, selectors becoming visible and others instead.
   *
   * @param timeout A timeout to wait for
   */
  void waitForTimeout(double timeout);
  /**
   * Waits for the frame to navigate to the given URL.
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
   */
  default void waitForURL(String url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the frame to navigate to the given URL.
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
   */
  void waitForURL(String url, WaitForURLOptions options);
  /**
   * Waits for the frame to navigate to the given URL.
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
   */
  default void waitForURL(Pattern url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the frame to navigate to the given URL.
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
   */
  void waitForURL(Pattern url, WaitForURLOptions options);
  /**
   * Waits for the frame to navigate to the given URL.
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
   */
  default void waitForURL(Predicate<String> url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the frame to navigate to the given URL.
   * <pre>{@code
   * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * frame.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
   */
  void waitForURL(Predicate<String> url, WaitForURLOptions options);
}

