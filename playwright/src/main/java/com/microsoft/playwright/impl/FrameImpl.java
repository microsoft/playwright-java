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

// Port of frame.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.isomorphic.LocatorUtils.*;
import static com.microsoft.playwright.impl.Serialization.deserialize;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.Utils.*;

public class FrameImpl extends ChannelOwner implements Frame {
  final EventEmitter eventEmitter;
  Set<String> loadStates;
  FrameImpl parentFrame;
  String url = "";
  String name = "";
  boolean detached;
  Set<FrameImpl> childFrames = new LinkedHashSet<>();
  PageImpl page;
  final WebMCPImpl webmcp;

  final FrameChannel channel = new FrameChannel(this);

  FrameImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    FrameInitializer init = connection.protocolGson().fromJson(initializer, FrameInitializer.class);
    eventEmitter = new EventEmitter();
    webmcp = new WebMCPImpl(this);
    parentFrame = init.parentFrame;
    if (parentFrame != null) {
      parentFrame.childFrames.add(this);
    }
    name = init.name;
    url = init.url;
    loadStates = new LinkedHashSet<>(init.loadStates);
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  void handleEvent(String event, JsonObject params) {
    if ("loadstate".equals(event)) {
      FrameLoadstateEvent loadstateEvent = connection.protocolGson().fromJson(params, FrameLoadstateEvent.class);
      if (loadstateEvent.add != null) {
        loadStates.add(loadstateEvent.add);
        eventEmitter.emit(Events.Frame.LOAD_STATE, loadstateEvent.add);
      }
      if (loadstateEvent.remove != null) {
        loadStates.remove(loadstateEvent.remove);
      }
      if (parentFrame == null && "load".equals(loadstateEvent.add) && page != null) {
        page.emit(Events.Page.LOAD, page);
        page.context().emit(Events.BrowserContext.PAGE_LOAD, page);
      }
      if (parentFrame == null && "domcontentloaded".equals(loadstateEvent.add) && page != null) {
        page.emit(Events.Page.DOM_CONTENT_LOADED, page);
      }
    } else if ("navigated".equals(event)) {
      FrameNavigatedEvent navigatedEvent = connection.protocolGson().fromJson(params, FrameNavigatedEvent.class);
      url = navigatedEvent.url;
      name = navigatedEvent.name;
      // java: no exposeFunctions option of evaluate(), so no _eraseEvaluateCallbacks().
      eventEmitter.emit(Events.Frame.NAVIGATED, navigatedEvent);
      if (navigatedEvent.error == null && page != null) {
        page.emit(Events.Page.FRAME_NAVIGATED, this);
        page.context().emit(Events.BrowserContext.FRAME_NAVIGATED, this);
      }
    }
  }

  @Override
  public PageImpl page() {
    return page;
  }

  double timeout(Double timeout) {
    TimeoutSettings timeoutSettings = page != null ? page.timeoutSettings : new TimeoutSettings();
    return timeoutSettings.timeout(timeout);
  }

  double navigationTimeout(Double timeout) {
    TimeoutSettings timeoutSettings = page != null ? page.timeoutSettings : new TimeoutSettings();
    return timeoutSettings.navigationTimeout(timeout);
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    if (options == null) {
      options = new NavigateOptions();
    }
    FrameGotoParams params = toProtocol(options, FrameGotoParams.class);
    params.url = url;
    params.waitUntil = verifyLoadState("waitUntil", params.waitUntil == null ? "load" : params.waitUntil);
    return channel.goto_(params, navigationTimeout(options.timeout)).response;
  }

  private Waiter setupNavigationWaiter(Double timeout) {
    Waiter waiter = new Waiter(page, "");
    if (page.isClosed()) {
      waiter.rejectImmediately(page.closeErrorWithReason());
    }
    waiter.rejectOnEvent(page, Events.Page.CLOSE, () -> page.closeErrorWithReason());
    waiter.rejectOnEvent(page, Events.Page.CRASH, () -> new PlaywrightException("Navigation failed because page crashed!"));
    waiter.<Frame>rejectOnEvent(page, Events.Page.FRAME_DETACHED, () -> new PlaywrightException("Navigating frame was detached!"), frame -> frame == this);
    double timeoutMs = page.timeoutSettings.navigationTimeout(timeout);
    waiter.rejectOnTimeout(timeoutMs, "Timeout " + Waiter.formatTimeout(timeoutMs) + "ms exceeded.");
    return waiter;
  }

  @Override
  public ResponseImpl waitForNavigation(WaitForNavigationOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForNavigationOptions();
    }
    // java: the matcher is built once and before the waiter subscribes: a glob is converted by a call to the driver,
    // which dispatches the events that arrive meanwhile.
    UrlMatcher matcher = UrlMatcher.forOneOf(page != null ? page.context().baseUrl() : null, options.url, connection.localUtils(), false);
    return waitForNavigation(matcher, options, callback);
  }

  private ResponseImpl waitForNavigation(UrlMatcher matcher, WaitForNavigationOptions options, Runnable callback) {
    String waitUntil = verifyLoadState("waitUntil", options.waitUntil == null ? "load" : lifecycleEvent(options.waitUntil));
    Double timeout = options.timeout;
    // java: only the setup runs in the "Wait for navigation" zone, so that the calls of the callback are reported on their own.
    Waiter waiter = withTitle("Wait for navigation", () -> setupNavigationWaiter(timeout));

    String toUrl = options.url instanceof String ? " to \"" + options.url + "\"" : "";
    waiter.log("waiting for navigation" + toUrl + " until \"" + waitUntil + "\"");

    FrameNavigatedEvent navigatedEvent = waiter.waitForEvent(eventEmitter, Events.Frame.NAVIGATED, (FrameNavigatedEvent event) -> {
      // Any failed navigation results in a rejection.
      if (event.error != null) {
        return true;
      }
      waiter.log("  navigated to \"" + event.url + "\"");
      return matcher.test(event.url);
    }, callback);
    if (navigatedEvent.error != null) {
      WaitableResult<Object> failure = new WaitableResult<>();
      failure.completeExceptionally(new PlaywrightException(navigatedEvent.error));
      waiter.waitForPromise(failure);
    }

    if (!loadStates.contains(waitUntil)) {
      waiter.waitForPromise(waiter.<String>waitForEvent(eventEmitter, Events.Frame.LOAD_STATE, s -> {
        waiter.log("  \"" + s + "\" event fired");
        return s.equals(waitUntil);
      }));
    }

    RequestImpl request = navigatedEvent.newDocument != null ? navigatedEvent.newDocument.request : null;
    ResponseImpl response = null;
    if (request != null) {
      // java: the call pumps the connection until its reply; its outcome goes through the waiter as upstream's promise does.
      WaitableResult<ResponseImpl> result = new WaitableResult<>();
      try {
        result.complete(request.finalRequest().internalResponse());
      } catch (RuntimeException e) {
        result.completeExceptionally(e);
      }
      response = waiter.waitForPromise(result);
    }
    waiter.dispose();
    return response;
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    waitForLoadState(state == null ? "load" : state.name().toLowerCase(Locale.ROOT), options == null ? null : options.timeout);
  }

  // java: the core of waitForLoadState, taking any lifecycle event: LoadState has no COMMIT, which waitForURL may pass.
  private void waitForLoadState(String state, Double timeout) {
    String verifiedState = verifyLoadState("state", state);
    // java: the title carries the state, tests pin it.
    withTitle("Wait for load state \"" + verifiedState + "\"", () -> {
      Waiter waiter = setupNavigationWaiter(timeout);
      try {
        if (loadStates.contains(verifiedState)) {
          waiter.log("  not waiting, \"" + verifiedState + "\" event already fired");
          waiter.throwIfImmediatelyRejected();
        } else {
          waiter.waitForPromise(waiter.<String>waitForEvent(eventEmitter, Events.Frame.LOAD_STATE, s -> {
            waiter.log("  \"" + s + "\" event fired");
            return s.equals(verifiedState);
          }));
        }
      } finally {
        waiter.dispose();
      }
    });
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    waitForURL((Object) url, options);
  }

  @Override
  public void waitForURL(Pattern url, WaitForURLOptions options) {
    waitForURL((Object) url, options);
  }

  @Override
  public void waitForURL(Predicate<String> url, WaitForURLOptions options) {
    waitForURL((Object) url, options);
  }

  private void waitForURL(Object url, WaitForURLOptions options) {
    WaitForURLOptions waitOptions = options == null ? new WaitForURLOptions() : options;
    // java: one matcher for the check and the wait, nothing pumps the connection between them.
    UrlMatcher matcher = UrlMatcher.forOneOf(page != null ? page.context().baseUrl() : null, url, connection.localUtils(), false);
    if (matcher.test(url())) {
      waitForLoadState(waitOptions.waitUntil == null ? "load" : lifecycleEvent(waitOptions.waitUntil), waitOptions.timeout);
      return;
    }

    WaitForNavigationOptions navigationOptions = new WaitForNavigationOptions();
    navigationOptions.url = url;
    navigationOptions.waitUntil = waitOptions.waitUntil;
    navigationOptions.timeout = waitOptions.timeout;
    waitForNavigation(matcher, navigationOptions, null);
  }

  @Override
  public ElementHandleImpl frameElement() {
    return channel.frameElement(new FrameFrameElementParams(), NO_TIMEOUT).element;
  }

  @Override
  public JSHandle evaluateHandle(String expression, Object arg) {
    FrameEvaluateExpressionHandleParams params = new FrameEvaluateExpressionHandleParams();
    params.expression = expression;
    params.arg = serializeArgument(arg);
    return channel.evaluateExpressionHandle(params, NO_TIMEOUT).handle;
  }

  @Override
  public Object evaluate(String expression, Object arg) {
    FrameEvaluateExpressionParams params = new FrameEvaluateExpressionParams();
    params.expression = expression;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evaluateExpression(params, NO_TIMEOUT).value);
  }

  @Override
  public ElementHandleImpl querySelector(String selector, QuerySelectorOptions options) {
    FrameQuerySelectorParams params = toProtocol(options, FrameQuerySelectorParams.class);
    params.selector = selector;
    return channel.querySelector(params, NO_TIMEOUT).element;
  }

  @Override
  public ElementHandleImpl waitForSelector(String selector, WaitForSelectorOptions options) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    FrameWaitForSelectorParams params = toProtocol(options, FrameWaitForSelectorParams.class);
    params.selector = selector;
    return channel.waitForSelector(params, timeout(options.timeout)).element;
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    if (options == null) {
      options = new DispatchEventOptions();
    }
    FrameDispatchEventParams params = toProtocol(options, FrameDispatchEventParams.class);
    params.selector = selector;
    params.type = type;
    params.eventInit = serializeArgument(eventInit);
    channel.dispatchEvent(params, timeout(options.timeout));
  }

  @Override
  public Object evalOnSelector(String selector, String expression, Object arg, EvalOnSelectorOptions options) {
    FrameEvalOnSelectorParams params = toProtocol(options, FrameEvalOnSelectorParams.class);
    params.selector = selector;
    params.expression = expression;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evalOnSelector(params, NO_TIMEOUT).value);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String expression, Object arg) {
    FrameEvalOnSelectorAllParams params = new FrameEvalOnSelectorAllParams();
    params.selector = selector;
    params.expression = expression;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evalOnSelectorAll(params, NO_TIMEOUT).value);
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    FrameQuerySelectorAllParams params = new FrameQuerySelectorAllParams();
    params.selector = selector;
    return new ArrayList<>(channel.querySelectorAll(params, NO_TIMEOUT).elements);
  }

  int queryCount(String selector) {
    FrameQueryCountParams params = new FrameQueryCountParams();
    params.selector = selector;
    return channel.queryCount(params, NO_TIMEOUT).value;
  }

  @Override
  public String content(ContentOptions options) {
    FrameContentParams params = toProtocol(options, FrameContentParams.class);
    return channel.content(params, NO_TIMEOUT).value;
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    if (options == null) {
      options = new SetContentOptions();
    }
    FrameSetContentParams params = toProtocol(options, FrameSetContentParams.class);
    params.html = html;
    params.waitUntil = verifyLoadState("waitUntil", params.waitUntil == null ? "load" : params.waitUntil);
    channel.setContent(params, navigationTimeout(options.timeout));
  }

  @Override
  public String name() {
    return name == null ? "" : name;
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public FrameImpl parentFrame() {
    return parentFrame;
  }

  @Override
  public List<Frame> childFrames() {
    return new ArrayList<>(childFrames);
  }

  @Override
  public boolean isDetached() {
    return detached;
  }

  @Override
  public ElementHandleImpl addScriptTag(AddScriptTagOptions options) {
    if (options == null) {
      options = new AddScriptTagOptions();
    }
    FrameAddScriptTagParams params = toProtocol(options, FrameAddScriptTagParams.class);
    if (options.path != null) {
      params.content = readFile(options.path);
      params.content = ClientHelper.addSourceUrlToScript(params.content, options.path);
    }
    return channel.addScriptTag(params, NO_TIMEOUT).element;
  }

  @Override
  public ElementHandleImpl addStyleTag(AddStyleTagOptions options) {
    if (options == null) {
      options = new AddStyleTagOptions();
    }
    FrameAddStyleTagParams params = toProtocol(options, FrameAddStyleTagParams.class);
    if (options.path != null) {
      params.content = readFile(options.path);
      params.content += "/*# sourceURL=" + options.path.toString().replace("\n", "") + "*/";
    }
    return channel.addStyleTag(params, NO_TIMEOUT).element;
  }

  @Override
  public void click(String selector, ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    FrameClickParams params = toProtocol(options, FrameClickParams.class);
    params.selector = selector;
    channel.click(params, timeout(options.timeout));
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    FrameDblclickParams params = toProtocol(options, FrameDblclickParams.class);
    params.selector = selector;
    channel.dblclick(params, timeout(options.timeout));
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    if (options == null) {
      options = new DragAndDropOptions();
    }
    FrameDragAndDropParams params = toProtocol(options, FrameDragAndDropParams.class);
    params.source = source;
    params.target = target;
    channel.dragAndDrop(params, timeout(options.timeout));
  }

  // java: the only caller, Locator.drop(), passes strict: true upstream; Locator.DropOptions has no strict field.
  void drop(String selector, DropPayload payload, Locator.DropOptions options) {
    if (options == null) {
      options = new Locator.DropOptions();
    }
    FrameDropParams params = toProtocol(options, FrameDropParams.class);
    params.selector = selector;
    params.strict = true;
    if (payload.files != null) {
      ElementHandleSetInputFilesParams converted = ElementHandleImpl.convertInputFiles(payload.files, page().context());
      if (converted.localDirectory != null || converted.directoryStream != null) {
        throw new PlaywrightException("Dropping a directory is not supported — pass individual files.");
      }
      if (converted.payloads != null) {
        params.payloads = new ArrayList<>();
        for (ElementHandleSetInputFilesParams.ElementHandleSetInputFilesParamsPayloads file : converted.payloads) {
          FrameDropParams.FrameDropParamsPayloads item = new FrameDropParams.FrameDropParamsPayloads();
          item.name = file.name;
          item.mimeType = file.mimeType;
          item.buffer = file.buffer;
          params.payloads.add(item);
        }
      }
      params.localPaths = converted.localPaths;
      params.streams = converted.streams;
    }
    if (payload.data != null) {
      params.data = new ArrayList<>();
      for (Map.Entry<String, String> entry : payload.data.entrySet()) {
        FrameDropParams.FrameDropParamsData item = new FrameDropParams.FrameDropParamsData();
        item.mimeType = entry.getKey();
        item.value = entry.getValue();
        params.data.add(item);
      }
    }
    channel.drop(params, timeout(options.timeout));
  }

  @Override
  public void tap(String selector, TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    FrameTapParams params = toProtocol(options, FrameTapParams.class);
    params.selector = selector;
    channel.tap(params, timeout(options.timeout));
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    FrameFillParams params = toProtocol(options, FrameFillParams.class);
    params.selector = selector;
    params.value = value;
    channel.fill(params, timeout(options.timeout));
  }

  void highlight(String selector, String style) {
    FrameHighlightParams params = new FrameHighlightParams();
    params.selector = selector;
    params.style = style;
    channel.highlight(params, NO_TIMEOUT);
  }

  void hideHighlight(String selector) {
    FrameHideHighlightParams params = new FrameHideHighlightParams();
    params.selector = selector;
    channel.hideHighlight(params, NO_TIMEOUT);
  }

  @Override
  public LocatorImpl locator(String selector, LocatorOptions options) {
    return new LocatorImpl(this, selector, convertType(options, Locator.LocatorOptions.class));
  }

  // java: testIdAttributeName() of locator.ts is a field of SelectorsImpl of the Playwright instance.
  @Override
  public Locator getByTestId(String testId) {
    return locator(getByTestIdSelector(connection.playwright.selectors.testIdAttributeName, testId));
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return locator(getByTestIdSelector(connection.playwright.selectors.testIdAttributeName, testId));
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return locator(getByAltTextSelector(text, convertType(options, Locator.GetByAltTextOptions.class)));
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return locator(getByAltTextSelector(text, convertType(options, Locator.GetByAltTextOptions.class)));
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return locator(getByLabelSelector(text, convertType(options, Locator.GetByLabelOptions.class)));
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return locator(getByLabelSelector(text, convertType(options, Locator.GetByLabelOptions.class)));
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return locator(getByPlaceholderSelector(text, convertType(options, Locator.GetByPlaceholderOptions.class)));
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return locator(getByPlaceholderSelector(text, convertType(options, Locator.GetByPlaceholderOptions.class)));
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return locator(getByTextSelector(text, convertType(options, Locator.GetByTextOptions.class)));
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return locator(getByTextSelector(text, convertType(options, Locator.GetByTextOptions.class)));
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return locator(getByTitleSelector(text, convertType(options, Locator.GetByTitleOptions.class)));
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return locator(getByTitleSelector(text, convertType(options, Locator.GetByTitleOptions.class)));
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return locator(getByRoleSelector(role, convertType(options, Locator.GetByRoleOptions.class)));
  }

  @Override
  public FrameLocatorImpl frameLocator(String selector) {
    return new FrameLocatorImpl(this, selector == null ? ANY_FRAME_SELECTOR : selector);
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    if (options == null) {
      options = new FocusOptions();
    }
    FrameFocusParams params = toProtocol(options, FrameFocusParams.class);
    params.selector = selector;
    channel.focus(params, timeout(options.timeout));
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    if (options == null) {
      options = new TextContentOptions();
    }
    FrameTextContentParams params = toProtocol(options, FrameTextContentParams.class);
    params.selector = selector;
    return channel.textContent(params, timeout(options.timeout)).value;
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    if (options == null) {
      options = new InnerTextOptions();
    }
    FrameInnerTextParams params = toProtocol(options, FrameInnerTextParams.class);
    params.selector = selector;
    return channel.innerText(params, timeout(options.timeout)).value;
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    if (options == null) {
      options = new InnerHTMLOptions();
    }
    FrameInnerHTMLParams params = toProtocol(options, FrameInnerHTMLParams.class);
    params.selector = selector;
    return channel.innerHTML(params, timeout(options.timeout)).value;
  }

  @Override
  public String getAttribute(String selector, String name, GetAttributeOptions options) {
    if (options == null) {
      options = new GetAttributeOptions();
    }
    FrameGetAttributeParams params = toProtocol(options, FrameGetAttributeParams.class);
    params.selector = selector;
    params.name = name;
    return channel.getAttribute(params, timeout(options.timeout)).value;
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    FrameInputValueParams params = toProtocol(options, FrameInputValueParams.class);
    params.selector = selector;
    return channel.inputValue(params, timeout(options.timeout)).value;
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    if (options == null) {
      options = new IsCheckedOptions();
    }
    FrameIsCheckedParams params = toProtocol(options, FrameIsCheckedParams.class);
    params.selector = selector;
    return channel.isChecked(params, timeout(options.timeout)).value;
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    if (options == null) {
      options = new IsDisabledOptions();
    }
    FrameIsDisabledParams params = toProtocol(options, FrameIsDisabledParams.class);
    params.selector = selector;
    return channel.isDisabled(params, timeout(options.timeout)).value;
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    if (options == null) {
      options = new IsEditableOptions();
    }
    FrameIsEditableParams params = toProtocol(options, FrameIsEditableParams.class);
    params.selector = selector;
    return channel.isEditable(params, timeout(options.timeout)).value;
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    if (options == null) {
      options = new IsEnabledOptions();
    }
    FrameIsEnabledParams params = toProtocol(options, FrameIsEnabledParams.class);
    params.selector = selector;
    return channel.isEnabled(params, timeout(options.timeout)).value;
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    FrameIsHiddenParams params = toProtocol(options, FrameIsHiddenParams.class);
    params.selector = selector;
    return channel.isHidden(params, NO_TIMEOUT).value;
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    FrameIsVisibleParams params = toProtocol(options, FrameIsVisibleParams.class);
    params.selector = selector;
    return channel.isVisible(params, NO_TIMEOUT).value;
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    FrameHoverParams params = toProtocol(options, FrameHoverParams.class);
    params.selector = selector;
    channel.hover(params, timeout(options.timeout));
  }

  @Override
  public List<String> selectOption(String selector, String values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, SelectOption values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  private List<String> selectOptionImpl(String selector, Object values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    ElementHandleSelectOptionParams converted = ElementHandleImpl.convertSelectOptionValues(values);
    FrameSelectOptionParams params = toProtocol(options, FrameSelectOptionParams.class);
    params.selector = selector;
    params.elements = converted.elements;
    if (converted.options != null) {
      params.options = new ArrayList<>();
      for (ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions value : converted.options) {
        FrameSelectOptionParams.FrameSelectOptionParamsOptions option = new FrameSelectOptionParams.FrameSelectOptionParamsOptions();
        option.valueOrLabel = value.valueOrLabel;
        option.value = value.value;
        option.label = value.label;
        option.index = value.index;
        params.options.add(option);
      }
    }
    return channel.selectOption(params, timeout(options.timeout)).values;
  }

  @Override
  public void setInputFiles(String selector, Path files, SetInputFilesOptions options) {
    setInputFilesImpl(selector, files, options);
  }

  @Override
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    setInputFilesImpl(selector, files, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options) {
    setInputFilesImpl(selector, files, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options) {
    setInputFilesImpl(selector, files, options);
  }

  private void setInputFilesImpl(String selector, Object files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    ElementHandleSetInputFilesParams converted = ElementHandleImpl.convertInputFiles(files, page().context());
    FrameSetInputFilesParams params = toProtocol(options, FrameSetInputFilesParams.class);
    params.selector = selector;
    if (converted.payloads != null) {
      params.payloads = new ArrayList<>();
      for (ElementHandleSetInputFilesParams.ElementHandleSetInputFilesParamsPayloads file : converted.payloads) {
        FrameSetInputFilesParams.FrameSetInputFilesParamsPayloads payload = new FrameSetInputFilesParams.FrameSetInputFilesParamsPayloads();
        payload.name = file.name;
        payload.mimeType = file.mimeType;
        payload.buffer = file.buffer;
        params.payloads.add(payload);
      }
    }
    params.localDirectory = converted.localDirectory;
    params.directoryStream = converted.directoryStream;
    params.localPaths = converted.localPaths;
    params.streams = converted.streams;
    channel.setInputFiles(params, timeout(options.timeout));
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    FrameTypeParams params = toProtocol(options, FrameTypeParams.class);
    params.selector = selector;
    params.text = text;
    channel.type(params, timeout(options.timeout));
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    FramePressParams params = toProtocol(options, FramePressParams.class);
    params.selector = selector;
    params.key = key;
    channel.press(params, timeout(options.timeout));
  }

  @Override
  public void check(String selector, CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    FrameCheckParams params = toProtocol(options, FrameCheckParams.class);
    params.selector = selector;
    channel.check(params, timeout(options.timeout));
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    FrameUncheckParams params = toProtocol(options, FrameUncheckParams.class);
    params.selector = selector;
    channel.uncheck(params, timeout(options.timeout));
  }

  @Override
  public void setChecked(String selector, boolean checked, SetCheckedOptions options) {
    if (checked) {
      check(selector, convertType(options, CheckOptions.class));
    } else {
      uncheck(selector, convertType(options, UncheckOptions.class));
    }
  }

  @Override
  public void waitForTimeout(double timeout) {
    FrameWaitForTimeoutParams params = new FrameWaitForTimeoutParams();
    params.waitTimeout = timeout;
    channel.waitForTimeout(params, NO_TIMEOUT);
  }

  @Override
  public JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options) {
    if (options == null) {
      options = new WaitForFunctionOptions();
    }
    // java: the options have pollingInterval in place of polling, there is no 'raf' string to check.
    FrameWaitForFunctionParams params = toProtocol(options, FrameWaitForFunctionParams.class);
    params.expression = expression;
    params.arg = serializeArgument(arg);
    return channel.waitForFunction(params, timeout(options.timeout)).handle;
  }

  @Override
  public String title() {
    return channel.title(NO_TIMEOUT).value;
  }

  // java: the title is a parameter, the options are the seed FrameExpectOptions, the result the seed FrameExpectResult.
  FrameExpectResult expect(String expression, FrameExpectOptions options, String title) {
    return wrapApiCall(() -> {
      FrameExpectParams params = toProtocol(this, options, FrameExpectParams.class);
      params.expression = expression;
      params.isNot = options.isNot;
      // java: expectedValue is serialized by the assertion classes.
      FrameExpectResult result = new FrameExpectResult();
      try {
        channel.expect(params, options.timeout);
        result.matches = !params.isNot;
        return result;
      } catch (ServerErrorWithDetails e) {
        // java: no AbortSignal, so no AbortError branch; only errors with details are expect mismatches.
        FrameExpectErrorDetails details = gson().fromJson(e.errorDetails(), FrameExpectErrorDetails.class);
        result.matches = params.isNot;
        // java: AssertionsBase parses the received value, FrameExpectResult has no timedOut.
        result.received = details.received;
        result.log = e.log();
        result.errorMessage = details.customErrorMessage != null ? "Error: " + details.customErrorMessage : null;
        return result;
      }
    }, title, false);
  }

  static String verifyLoadState(String name, String waitUntil) {
    if ("networkidle0".equals(waitUntil)) {
      waitUntil = "networkidle";
    }
    if (!kLifecycleEvents.contains(waitUntil)) {
      throw new PlaywrightException(name + ": expected one of (load|domcontentloaded|networkidle|commit)");
    }
    return waitUntil;
  }

  // java: members with no upstream counterpart: kAnyFrameSelector of isomorphic/selectorParser.ts, kLifecycleEvents
  // of types.ts, the enum to lifecycle event conversion, a file reader and the webmcp() accessor of the webmcp field.
  static final String ANY_FRAME_SELECTOR = "internal:control=any-frame";

  private static final Set<String> kLifecycleEvents = new LinkedHashSet<>(Arrays.asList("load", "domcontentloaded", "networkidle", "commit"));

  private static String lifecycleEvent(WaitUntilState state) {
    return state.name().toLowerCase(Locale.ROOT);
  }

  private static String readFile(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read from file", e);
    }
  }

  @Override
  public WebMCP webmcp() {
    return webmcp;
  }
}
