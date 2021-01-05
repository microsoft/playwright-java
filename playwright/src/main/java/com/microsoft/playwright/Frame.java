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

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * At every point of time, page exposes its current frame tree via the {@code page.mainFrame()} and {@code frame.childFrames()} methods.
 * <p>
 * Frame object's lifecycle is controlled by three events, dispatched on the page object:
 * <p>
 * page.on('frameattached') - fired when the frame gets attached to the page. A Frame can be attached to the page only once.
 * <p>
 * page.on('framenavigated') - fired when the frame commits navigation to a different URL.
 * <p>
 * page.on('framedetached') - fired when the frame gets detached from the page.  A Frame can be detached from the page only once.
 * <p>
 */
public interface Frame {
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
  class AddScriptTagParams {
    /**
     * URL of a script to be added.
     */
    public String url;
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the current working directory.
     */
    public Path path;
    /**
     * Raw JavaScript content to be injected into frame.
     */
    public String content;
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See script for more details.
     */
    public String type;

    public AddScriptTagParams withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddScriptTagParams withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddScriptTagParams withContent(String content) {
      this.content = content;
      return this;
    }
    public AddScriptTagParams withType(String type) {
      this.type = type;
      return this;
    }
  }
  class AddStyleTagParams {
    /**
     * URL of the {@code <link>} tag.
     */
    public String url;
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the current working directory.
     */
    public Path path;
    /**
     * Raw CSS content to be injected into frame.
     */
    public String content;

    public AddStyleTagParams withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddStyleTagParams withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddStyleTagParams withContent(String content) {
      this.content = content;
      return this;
    }
  }
  class CheckOptions {
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public CheckOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public CheckOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public CheckOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ClickOptions {
    /**
     * Defaults to {@code left}.
     */
    public Mouse.Button button;
    /**
     * defaults to 1. See UIEvent.detail.
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public ClickOptions withButton(Mouse.Button button) {
      this.button = button;
      return this;
    }
    public ClickOptions withClickCount(Integer clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public ClickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public ClickOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public ClickOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public ClickOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public ClickOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public ClickOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DblclickOptions {
    /**
     * Defaults to {@code left}.
     */
    public Mouse.Button button;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public DblclickOptions withButton(Mouse.Button button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public DblclickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public DblclickOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public DblclickOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public DblclickOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public DblclickOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public DblclickOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DispatchEventOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public DispatchEventOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FillOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public FillOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public FillOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FocusOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public FocusOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public GetAttributeOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class NavigateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultNavigationTimeout(timeout)}, {@code browserContext.setDefaultTimeout(timeout)}, {@code page.setDefaultNavigationTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public LoadState waitUntil;
    /**
     * Referer header value. If provided it will take preference over the referer header value set by {@code page.setExtraHTTPHeaders(headers)}.
     */
    public String referer;

    public NavigateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions withWaitUntil(LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
    public NavigateOptions withReferer(String referer) {
      this.referer = referer;
      return this;
    }
  }
  class HoverOptions {
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public HoverOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public HoverOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public HoverOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public HoverOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public HoverOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerHTMLOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public InnerHTMLOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public InnerTextOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PressOptions {
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public PressOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public PressOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public PressOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectOptionOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public SelectOptionOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SelectOptionOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetContentOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultNavigationTimeout(timeout)}, {@code browserContext.setDefaultTimeout(timeout)}, {@code page.setDefaultNavigationTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public LoadState waitUntil;

    public SetContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public SetContentOptions withWaitUntil(LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class SetInputFilesOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public SetInputFilesOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetInputFilesOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    public class Position {
      public int x;
      public int y;

      Position() {
      }
      public TapOptions done() {
        return TapOptions.this;
      }

      public Position withX(int x) {
        this.x = x;
        return this;
      }
      public Position withY(int y) {
        this.y = y;
        return this;
      }
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public TapOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public TapOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TapOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public TapOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TextContentOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public TextContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TypeOptions {
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public TypeOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public TypeOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TypeOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public UncheckOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public UncheckOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public UncheckOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForFunctionOptions {
    /**
     * If {@code polling} is {@code 'raf'}, then {@code pageFunction} is constantly executed in {@code requestAnimationFrame} callback. If {@code polling} is a number, then it is treated as an interval in milliseconds at which the function would be executed. Defaults to {@code raf}.
     */
    public Integer pollingInterval;
    /**
     * maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)}.
     */
    public Integer timeout;

    public WaitForFunctionOptions withRequestAnimationFrame() {
      this.pollingInterval = null;
      return this;
    }
    public WaitForFunctionOptions withPollingInterval(int millis) {
      this.pollingInterval = millis;
      return this;
    }
    public WaitForFunctionOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultNavigationTimeout(timeout)}, {@code browserContext.setDefaultTimeout(timeout)}, {@code page.setDefaultNavigationTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public WaitForLoadStateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultNavigationTimeout(timeout)}, {@code browserContext.setDefaultTimeout(timeout)}, {@code page.setDefaultNavigationTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;
    /**
     * URL string, URL regex pattern or predicate receiving URL to match while waiting for the navigation.
     */
    public String glob;
    public Pattern pattern;
    public Predicate<String> predicate;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public LoadState waitUntil;

    public WaitForNavigationOptions withTimeout(Integer timeout) {
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
    public WaitForNavigationOptions withWaitUntil(LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class WaitForSelectorOptions {
    public enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE }
    /**
     * Defaults to {@code 'visible'}. Can be either:
     *  - {@code 'attached'} - wait for element to be present in DOM.
     *  - {@code 'detached'} - wait for element to not be present in DOM.
     *  - {@code 'visible'} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without any content or with {@code display:none} has an empty bounding box and is not considered visible.
     *  - {@code 'hidden'} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}. This is opposite to the {@code 'visible'} option.
     */
    public State state;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public WaitForSelectorOptions withState(State state) {
      this.state = state;
      return this;
    }
    public WaitForSelectorOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Returns the ElementHandle pointing to the frame element.
   * <p>
   * The method finds an element matching the specified selector within the frame. See Working with selectors for more details. If no elements match the selector, returns {@code null}.
   * @param selector A selector to query for. See working with selectors for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * Returns the ElementHandles pointing to the frame elements.
   * <p>
   * The method finds all elements matching the specified selector within the frame. See Working with selectors for more details. If no elements match the selector, returns empty array.
   * @param selector A selector to query for. See working with selectors for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  default Object evalOnSelector(String selector, String pageFunction) {
    return evalOnSelector(selector, pageFunction, null);
  }
  /**
   * Returns the return value of {@code pageFunction}
   * <p>
   * The method finds an element matching the specified selector within the frame and passes it as a first argument to {@code pageFunction}. See Working with selectors for more details. If no elements match the selector, the method throws an error.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code frame.$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * 
   * @param selector A selector to query for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  default Object evalOnSelectorAll(String selector, String pageFunction) {
    return evalOnSelectorAll(selector, pageFunction, null);
  }
  /**
   * Returns the return value of {@code pageFunction}
   * <p>
   * The method finds all elements matching the specified selector within the frame and passes an array of matched elements as a first argument to {@code pageFunction}. See Working with selectors for more details.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code frame.$$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * 
   * @param selector A selector to query for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  /**
   * Returns the added tag when the script's onload fires or when the script content was injected into frame.
   * <p>
   * Adds a {@code <script>} tag into the page with the desired url or content.
   */
  ElementHandle addScriptTag(AddScriptTagParams params);
  /**
   * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   * <p>
   * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the content.
   */
  ElementHandle addStyleTag(AddStyleTagParams params);
  default void check(String selector) {
    check(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already checked, this method returns immediately.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * Ensure that the element is now checked. If not, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void check(String selector, CheckOptions options);
  List<Frame> childFrames();
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
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
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to double click in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void dblclick(String selector, DblclickOptions options);
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the elment, {@code click} is dispatched. This is equivalend to calling element.click().
   * <p>
   * Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
   * <p>
   * Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <p>
   * DragEvent
   * <p>
   * FocusEvent
   * <p>
   * KeyboardEvent
   * <p>
   * MouseEvent
   * <p>
   * PointerEvent
   * <p>
   * TouchEvent
   * <p>
   * Event
   * <p>
   * You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <p>
   * 
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  /**
   * Returns the return value of {@code pageFunction}
   * <p>
   * If the function passed to the {@code frame.evaluate} returns a Promise, then {@code frame.evaluate} would wait for the promise to resolve and return its value.
   * <p>
   * If the function passed to the {@code frame.evaluate} returns a non-Serializable value, then {@code frame.evaluate} returns {@code undefined}. DevTools Protocol also supports transferring some additional values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}, and bigint literals.
   * <p>
   * A string can also be passed in instead of a function.
   * <p>
   * ElementHandle instances can be passed as an argument to the {@code frame.evaluate}:
   * <p>
   * 
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  /**
   * Returns the return value of {@code pageFunction} as in-page object (JSHandle).
   * <p>
   * The only difference between {@code frame.evaluate} and {@code frame.evaluateHandle} is that {@code frame.evaluateHandle} returns in-page object (JSHandle).
   * <p>
   * If the function, passed to the {@code frame.evaluateHandle}, returns a Promise, then {@code frame.evaluateHandle} would wait for the promise to resolve and return its value.
   * <p>
   * A string can also be passed in instead of a function.
   * <p>
   * JSHandle instances can be passed as an argument to the {@code frame.evaluateHandle}:
   * <p>
   * 
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  JSHandle evaluateHandle(String pageFunction, Object arg);
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for actionability checks, focuses the element, fills it and triggers an {@code input} event after filling. If the element matching {@code selector} is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error. Note that you can pass an empty string to clear the input field.
   * <p>
   * To send fine-grained keyboard events, use {@code frame.type(selector, text[, options])}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method waits until a matching element appears in the DOM.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void focus(String selector, FocusOptions options);
  /**
   * Returns the {@code frame} or {@code iframe} element handle which corresponds to this frame.
   * <p>
   * This is an inverse of {@code elementHandle.contentFrame()}. Note that returned handle actually belongs to the parent frame.
   * <p>
   * This method throws an error if the frame has been detached before {@code frameElement()} returns.
   * <p>
   */
  ElementHandle frameElement();
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect.
   * <p>
   * {@code frame.goto} will throw an error if:
   * <p>
   * there's an SSL error (e.g. in case of self-signed certificates).
   * <p>
   * target URL is invalid.
   * <p>
   * the {@code timeout} is exceeded during navigation.
   * <p>
   * the remote server does not respond or is unreachable.
   * <p>
   * the main resource failed to load.
   * <p>
   * {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@code response.status()}.
   * <p>
   * <strong>NOTE</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   * <p>
   * <strong>NOTE</strong> Headless mode doesn't support navigation to a PDF document. See the upstream issue.
   * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
   */
  Response navigate(String url, NavigateOptions options);
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to hover over the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void hover(String selector, HoverOptions options);
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  String innerText(String selector, InnerTextOptions options);
  /**
   * Returns {@code true} if the frame has been detached, or {@code false} otherwise.
   */
  boolean isDetached();
  /**
   * Returns frame's name attribute as specified in the tag.
   * <p>
   * If the name is empty, returns the id attribute instead.
   * <p>
   * <strong>NOTE</strong> This value is calculated once when the frame is created, and will not update if the attribute is changed later.
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
   * {@code key} can specify the intended keyboardEvent.key value or a single character to generate the text for. A superset of the {@code key} values can be found here. Examples of the keys are:
   * <p>
   * {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   * <p>
   * Following modification shortcuts are also suported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   * <p>
   * Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   * <p>
   * If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective texts.
   * <p>
   * Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String selector, String key, PressOptions options);
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
   * <p>
   * Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element matching {@code selector}, the method throws an error.
   * <p>
   * 
   * @param selector A selector to query for. See working with selectors for more details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
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
   * This method expects {@code selector} to point to an input element.
   * <p>
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they are resolved relative to the the current working directory. For empty array, clears the selected files.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.touchscreen to tap the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void tap(String selector, TapOptions options);
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
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
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to send fine-grained keyboard events. To fill values in form fields, use {@code frame.fill(selector, value[, options])}.
   * <p>
   * To press a special key, like {@code Control} or {@code ArrowDown}, use {@code keyboard.press(key[, options])}.
   * <p>
   * 
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param text A text to type into a focused element.
   */
  void type(String selector, String text, TypeOptions options);
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already unchecked, this method returns immediately.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * Ensure that the element is now unchecked. If not, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void uncheck(String selector, UncheckOptions options);
  /**
   * Returns frame's url.
   */
  String url();
  default JSHandle waitForFunction(String pageFunction, Object arg) {
    return waitForFunction(pageFunction, arg, null);
  }
  default JSHandle waitForFunction(String pageFunction) {
    return waitForFunction(pageFunction, null);
  }
  /**
   * Returns when the {@code pageFunction} returns a truthy value, returns that value.
   * <p>
   * The {@code waitForFunction} can be used to observe viewport size change:
   * <p>
   * To pass an argument to the predicate of {@code frame.waitForFunction} function:
   * <p>
   * 
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  /**
   * Waits for the required load state to be reached.
   * <p>
   * This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed when this method is called. If current document has already reached the required state, resolves immediately.
   * <p>
   * 
   * @param state Load state to wait for, defaults to {@code load}. If the state has been already reached while loading current document, the method returns immediately. Optional.
   *  - {@code 'load'} - wait for the {@code load} event to be fired.
   *  - {@code 'domcontentloaded'} - wait for the {@code DOMContentLoaded} event to be fired.
   *  - {@code 'networkidle'} - wait until there are no network connections for at least {@code 500} ms.
   */
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Deferred<Response> waitForNavigation() {
    return waitForNavigation(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to History API usage, the navigation will resolve with {@code null}.
   * <p>
   * This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause the frame to navigate. Consider this example:
   * <p>
   * <strong>NOTE</strong> Usage of the History API to change the URL is considered a navigation.
   */
  Deferred<Response> waitForNavigation(WaitForNavigationOptions options);
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or {@code detached}.
   * <p>
   * Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
   * <p>
   * This method works across navigations:
   * <p>
   * 
   * @param selector A selector to query for. See working with selectors for more details.
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * Waits for the given {@code timeout} in milliseconds.
   * <p>
   * Note that {@code frame.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to be flaky. Use signals such as network events, selectors becoming visible and others instead.
   * @param timeout A timeout to wait for
   */
  void waitForTimeout(int timeout);
}

