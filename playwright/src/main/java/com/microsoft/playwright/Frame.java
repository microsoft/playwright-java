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
 * At every point of time, page exposes its current frame tree via the [{@code method: Page.mainFrame}] and
 * [{@code method: Frame.childFrames}] methods.
 *
 * <p> {@code Frame} object's lifecycle is controlled by three events, dispatched on the page object:
 * - [{@code event: Page.frameAttached}] - fired when the frame gets attached to the page. A Frame can be attached to the page
 *   only once.
 * - [{@code event: Page.frameNavigated}] - fired when the frame commits navigation to a different URL.
 * - [{@code event: Page.frameDetached}] - fired when the frame gets detached from the page.  A Frame can be detached from the
 *   page only once.
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
     * Script type. Use 'module' in order to load a Javascript ES6 module. See
     * [script](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script) for more details.
     */
    public String type;
    /**
     * URL of a script to be added.
     */
    public String url;

    public AddScriptTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
    public AddScriptTagOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddScriptTagOptions withType(String type) {
      this.type = type;
      return this;
    }
    public AddScriptTagOptions withUrl(String url) {
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

    public AddStyleTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
    public AddStyleTagOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddStyleTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
  }
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
    public ClickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public ClickOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
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
    public DblclickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public DblclickOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
    }
    public DblclickOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DispatchEventOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public DispatchEventOptions withTimeout(double timeout) {
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
  class FocusOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public FocusOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public GetAttributeOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class NavigateOptions {
    /**
     * Referer header value. If provided it will take preference over the referer header value set by
     * [{@code method: Page.setExtraHTTPHeaders}].
     */
    public String referer;
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public WaitUntilState waitUntil;

    public NavigateOptions withReferer(String referer) {
      this.referer = referer;
      return this;
    }
    public NavigateOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions withWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
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
    public HoverOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public HoverOptions withPosition(double x, double y) {
      return withPosition(new Position(x, y));
    }
    public HoverOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerHTMLOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public InnerHTMLOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public InnerTextOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsCheckedOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsCheckedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsDisabledOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsEditableOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsEnabledOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsHiddenOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsVisibleOptions withTimeout(double timeout) {
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
  class SetContentOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public WaitUntilState waitUntil;

    public SetContentOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public SetContentOptions withWaitUntil(WaitUntilState waitUntil) {
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
    public TapOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public TapOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TextContentOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public TextContentOptions withTimeout(double timeout) {
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
  class WaitForFunctionOptions {
    /**
     * If {@code polling} is {@code 'raf'}, then {@code expression} is constantly executed in {@code requestAnimationFrame} callback. If {@code polling} is a
     * number, then it is treated as an interval in milliseconds at which the function would be executed. Defaults to {@code raf}.
     */
    public Integer pollingInterval;
    /**
     * maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the [{@code method: BrowserContext.setDefaultTimeout}].
     */
    public Double timeout;

    public WaitForFunctionOptions withRequestAnimationFrame() {
      this.pollingInterval = null;
      return this;
    }
    public WaitForFunctionOptions withPollingInterval(int millis) {
      this.pollingInterval = millis;
      return this;
    }
    public WaitForFunctionOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public WaitForLoadStateOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * URL string, URL regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    public String glob;
    public Pattern pattern;
    public Predicate<String> predicate;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public WaitUntilState waitUntil;

    public WaitForNavigationOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public WaitForNavigationOptions withUrl(String glob) {
      this.glob = glob;
      return this;
    }
    public WaitForNavigationOptions withUrl(Pattern pattern) {
      this.pattern = pattern;
      return this;
    }
    public WaitForNavigationOptions withUrl(Predicate<String> predicate) {
      this.predicate = predicate;
      return this;
    }
    public WaitForNavigationOptions withWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
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
  default ElementHandle addScriptTag() {
    return addScriptTag(null);
  }
  /**
   * Returns the added tag when the script's onload fires or when the script content was injected into frame.
   *
   * <p> Adds a {@code <script>} tag into the page with the desired url or content.
   */
  ElementHandle addScriptTag(AddScriptTagOptions options);
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
  default void check(String selector) {
    check(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    checked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now checked. If not, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void check(String selector, CheckOptions options);
  List<Frame> childFrames();
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void click(String selector, ClickOptions options);
  /**
   * Gets the full HTML contents of the frame, including the doctype.
   */
  String content();
  default void dblclick(String selector) {
    dblclick(selector, null);
  }
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to double click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   *    first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void dblclick(String selector, DblclickOptions options);
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
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
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  default Object evalOnSelector(String selector, String expression) {
    return evalOnSelector(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to
   * {@code expression}. See [Working with selectors](./selectors.md) for more details. If no elements match the selector, the
   * method throws an error.
   *
   * <p> If {@code expression} returns a [Promise], then [{@code method: Frame.evalOnSelector}] would wait for the promise to resolve and
   * return its value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evalOnSelector(String selector, String expression, Object arg);
  default Object evalOnSelectorAll(String selector, String expression) {
    return evalOnSelectorAll(selector, expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
   * as a first argument to {@code expression}. See [Working with selectors](./selectors.md) for more details.
   *
   * <p> If {@code expression} returns a [Promise], then [{@code method: Frame.evalOnSelectorAll}] would wait for the promise to resolve and
   * return its value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evalOnSelectorAll(String selector, String expression, Object arg);
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> If the function passed to the [{@code method: Frame.evaluate}] returns a [Promise], then [{@code method: Frame.evaluate}] would wait
   * for the promise to resolve and return its value.
   *
   * <p> If the function passed to the [{@code method: Frame.evaluate}] returns a non-[Serializable] value, then
   * [{@code method: Frame.evaluate}] returns {@code undefined}. Playwright also supports transferring some additional values that are
   * not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * <p> A string can also be passed in instead of a function.
   *
   * <p> {@code ElementHandle} instances can be passed as an argument to the [{@code method: Frame.evaluate}]:
   *
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evaluate(String expression, Object arg);
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> The only difference between [{@code method: Frame.evaluate}] and [{@code method: Frame.evaluateHandle}] is that
   * [method: Frame.evaluateHandle{@code ] returns }JSHandle`.
   *
   * <p> If the function, passed to the [{@code method: Frame.evaluateHandle}], returns a [Promise], then
   * [{@code method: Frame.evaluateHandle}] would wait for the promise to resolve and return its value.
   *
   * <p> A string can also be passed in instead of a function.
   *
   * <p> {@code JSHandle} instances can be passed as an argument to the [{@code method: Frame.evaluateHandle}]:
   *
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  JSHandle evaluateHandle(String expression, Object arg);
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for [actionability](./actionability.md) checks, focuses the
   * element, fills it and triggers an {@code input} event after filling. If the element is inside the {@code <label>} element that has
   * associated [control](https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control), that control will be
   * filled instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this
   * method throws an error. Note that you can pass an empty string to clear the input field.
   *
   * <p> To send fine-grained keyboard events, use [{@code method: Frame.type}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
   * waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void focus(String selector, FocusOptions options);
  /**
   * Returns the {@code frame} or {@code iframe} element handle which corresponds to this frame.
   *
   * <p> This is an inverse of [{@code method: ElementHandle.contentFrame}]. Note that returned handle actually belongs to the parent
   * frame.
   *
   * <p> This method throws an error if the frame has been detached before {@code frameElement()} returns.
   */
  ElementHandle frameElement();
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   *
   * <p> {@code frame.goto} will throw an error if:
   * - there's an SSL error (e.g. in case of self-signed certificates).
   * - target URL is invalid.
   * - the {@code timeout} is exceeded during navigation.
   * - the remote server does not respond or is unreachable.
   * - the main resource failed to load.
   *
   * <p> {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404
   * "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling
   * [{@code method: Response.status}].
   *
   * <p> <strong>NOTE:</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
   * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   * <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the
   * [upstream issue](https://bugs.chromium.org/p/chromium/issues/detail?id=761295).
   *
   * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
   */
  Response navigate(String url, NavigateOptions options);
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to hover over the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void hover(String selector, HoverOptions options);
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  String innerText(String selector, InnerTextOptions options);
  default boolean isChecked(String selector) {
    return isChecked(selector, null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  boolean isChecked(String selector, IsCheckedOptions options);
  /**
   * Returns {@code true} if the frame has been detached, or {@code false} otherwise.
   */
  boolean isDetached();
  default boolean isDisabled(String selector) {
    return isDisabled(selector, null);
  }
  /**
   * Returns whether the element is disabled, the opposite of [enabled](./actionability.md#enabled).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  boolean isDisabled(String selector, IsDisabledOptions options);
  default boolean isEditable(String selector) {
    return isEditable(selector, null);
  }
  /**
   * Returns whether the element is [editable](./actionability.md#editable).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  boolean isEditable(String selector, IsEditableOptions options);
  default boolean isEnabled(String selector) {
    return isEnabled(selector, null);
  }
  /**
   * Returns whether the element is [enabled](./actionability.md#enabled).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  boolean isEnabled(String selector, IsEnabledOptions options);
  default boolean isHidden(String selector) {
    return isHidden(selector, null);
  }
  /**
   * Returns whether the element is hidden, the opposite of [visible](./actionability.md#visible).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  boolean isHidden(String selector, IsHiddenOptions options);
  default boolean isVisible(String selector) {
    return isVisible(selector, null);
  }
  /**
   * Returns whether the element is [visible](./actionability.md#visible).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
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
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  /**
   * {@code key} can specify the intended [keyboardEvent.key](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key)
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
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String selector, String key, PressOptions options);
  /**
   * Returns the ElementHandle pointing to the frame element.
   *
   * <p> The method finds an element matching the specified selector within the frame. See
   * [Working with selectors](./selectors.md) for more details. If no elements match the selector, returns {@code null}.
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * Returns the ElementHandles pointing to the frame elements.
   *
   * <p> The method finds all elements matching the specified selector within the frame. See
   * [Working with selectors](./selectors.md) for more details. If no elements match the selector, returns empty array.
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  default List<String> selectOption(String selector, String value) {
    return selectOption(selector, value, null);
  }
  default List<String> selectOption(String selector, String value, SelectOptionOptions options) {
    String[] values = value == null ? null : new String[]{ value };
    return selectOption(selector, values, options);
  }
  default List<String> selectOption(String selector, String[] values) {
    return selectOption(selector, values, null);
  }
  default List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    if (values == null) {
      return selectOption(selector, new ElementHandle.SelectOption[0], options);
    }
    return selectOption(selector, Arrays.asList(values).stream().map(
      v -> new ElementHandle.SelectOption().withValue(v)).toArray(ElementHandle.SelectOption[]::new), options);
  }
  default List<String> selectOption(String selector, ElementHandle.SelectOption value) {
    return selectOption(selector, value, null);
  }
  default List<String> selectOption(String selector, ElementHandle.SelectOption value, SelectOptionOptions options) {
    ElementHandle.SelectOption[] values = value == null ? null : new ElementHandle.SelectOption[]{value};
    return selectOption(selector, values, options);
  }
  default List<String> selectOption(String selector, ElementHandle.SelectOption[] values) {
    return selectOption(selector, values, null);
  }
  List<String> selectOption(String selector, ElementHandle.SelectOption[] values, SelectOptionOptions options);
  default List<String> selectOption(String selector, ElementHandle value) {
    return selectOption(selector, value, null);
  }
  default List<String> selectOption(String selector, ElementHandle value, SelectOptionOptions options) {
    ElementHandle[] values = value == null ? null : new ElementHandle[]{value};
    return selectOption(selector, values, options);
  }
  default List<String> selectOption(String selector, ElementHandle[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
   * matching {@code selector}, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   *
   *
   * @param html HTML markup to assign to the page.
   */
  void setContent(String html, SetContentOptions options);
  default void setInputFiles(String selector, Path file) { setInputFiles(selector, file, null); }
  default void setInputFiles(String selector, Path file, SetInputFilesOptions options) { setInputFiles(selector, new Path[]{ file }, options); }
  default void setInputFiles(String selector, Path[] files) { setInputFiles(selector, files, null); }
  void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);
  default void setInputFiles(String selector, FileChooser.FilePayload file) { setInputFiles(selector, file, null); }
  default void setInputFiles(String selector, FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(selector, new FileChooser.FilePayload[]{ file }, options); }
  default void setInputFiles(String selector, FileChooser.FilePayload[] files) { setInputFiles(selector, files, null); }
  /**
   * This method expects {@code selector} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.touchscreen}] to tap the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void tap(String selector, TapOptions options);
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Returns the page title.
   */
  String title();
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
   * send fine-grained keyboard events. To fill values in form fields, use [{@code method: Frame.fill}].
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use [{@code method: Keyboard.press}].
   *
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   * @param text A text to type into a focused element.
   */
  void type(String selector, String text, TypeOptions options);
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    unchecked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now unchecked. If not, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md) for more details.
   */
  void uncheck(String selector, UncheckOptions options);
  /**
   * Returns frame's url.
   */
  String url();
  default JSHandle waitForFunction(String expression, Object arg) {
    return waitForFunction(expression, arg, null);
  }
  default JSHandle waitForFunction(String expression) {
    return waitForFunction(expression, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value, returns that value.
   *
   * <p> The [{@code method: Frame.waitForFunction}] can be used to observe viewport size change:
   *
   * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
   *
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options);
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  /**
   * Waits for the required load state to be reached.
   *
   * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
   * when this method is called. If current document has already reached the required state, resolves immediately.
   *
   *
   * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
   * document, the method resolves immediately. Can be one of:
   * - {@code 'load'} - wait for the {@code load} event to be fired.
   * - {@code 'domcontentloaded'} - wait for the {@code DOMContentLoaded} event to be fired.
   * - {@code 'networkidle'} - wait until there are no network connections for at least {@code 500} ms.
   */
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Response waitForNavigation(Runnable callback) { return waitForNavigation(null, callback); }
  /**
   * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
   * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
   * History API usage, the navigation will resolve with {@code null}.
   *
   * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
   * the frame to navigate. Consider this example:
   *
   * <p> <strong>NOTE:</strong> Usage of the [History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API) to change the URL is
   * considered a navigation.
   */
  Response waitForNavigation(WaitForNavigationOptions options, Runnable callback);
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
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md) for more details.
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
}

