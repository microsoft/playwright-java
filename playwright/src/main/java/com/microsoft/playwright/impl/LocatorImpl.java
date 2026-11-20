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

// Port of locator.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.isomorphic.LocatorUtils.*;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.escapeForTextSelector;
import static com.microsoft.playwright.impl.isomorphic.Time.monotonicTime;
import static com.microsoft.playwright.impl.Utils.convertType;

class LocatorImpl implements Locator {
  final FrameImpl frame;
  String selector;

  // java: upstream has one constructor with optional LocatorOptions. Locator.LocatorOptions has no
  // 'visible', so filter() and visible() use the private constructor that takes FilterOptions.
  LocatorImpl(FrameImpl frame, String selector) {
    this(frame, selector, (FilterOptions) null);
  }

  LocatorImpl(FrameImpl frame, String selector, LocatorOptions options) {
    this(frame, selector, convertType(options, FilterOptions.class));
  }

  private LocatorImpl(FrameImpl frame, String selector, FilterOptions options) {
    this.frame = frame;
    this.selector = selector;

    if (options != null && options.hasText != null)
      this.selector += " >> internal:has-text=" + escapeForTextSelector(options.hasText, false);

    if (options != null && options.hasNotText != null)
      this.selector += " >> internal:has-not-text=" + escapeForTextSelector(options.hasNotText, false);

    if (options != null && options.has != null) {
      LocatorImpl locator = (LocatorImpl) options.has;
      if (locator.frame != frame)
        throw new PlaywrightException("Inner \"has\" locator must belong to the same frame.");
      this.selector += " >> internal:has=" + gson().toJson(locator.selector);
    }

    if (options != null && options.hasNot != null) {
      LocatorImpl locator = (LocatorImpl) options.hasNot;
      if (locator.frame != frame)
        throw new PlaywrightException("Inner \"hasNot\" locator must belong to the same frame.");
      this.selector += " >> internal:has-not=" + gson().toJson(locator.selector);
    }

    if (options != null && options.visible != null)
      this.selector += " >> visible=" + (options.visible ? "true" : "false");
  }

  <R> R wrapApiCall(Supplier<R> func, String title, boolean internal) {
    return frame.wrapApiCall(func, title, internal);
  }

  private <R> R withElement(BiFunction<ElementHandle, Double, R> task, Double timeoutOption) {
    double timeout = frame.timeout(timeoutOption);
    double deadline = timeout != 0 ? monotonicTime() + timeout : 0;

    FrameWaitForSelectorParams params = new FrameWaitForSelectorParams();
    params.selector = selector;
    params.strict = true;
    params.state = "attached";
    FrameWaitForSelectorResult result = frame.channel.waitForSelector(params, timeout);
    ElementHandle handle = result.element;
    if (handle == null)
      throw new PlaywrightException("Could not resolve " + selector + " to DOM Element");
    try {
      return task.apply(handle, deadline != 0 ? deadline - monotonicTime() : 0);
    } finally {
      handle.dispose();
    }
  }

  // java: upstream _equals(), used by page.ts; equals() so that Java collections and assertEquals agree.
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LocatorImpl))
      return false;
    LocatorImpl locator = (LocatorImpl) obj;
    return frame == locator.frame && selector.equals(locator.selector);
  }

  @Override
  public Page page() {
    return frame.page();
  }

  @Override
  public BoundingBox boundingBox(BoundingBoxOptions options) {
    return frame.withTitle("Bounding box", () -> withElement((h, timeout) -> h.boundingBox(), options == null ? null : options.timeout));
  }

  @Override
  public void check(CheckOptions options) {
    if (options == null)
      options = new CheckOptions();
    frame.check(selector, convertType(options, Frame.CheckOptions.class).setStrict(true));
  }

  // java: Frame.ClickOptions has no 'steps', so this sends what upstream frame.click() sends.
  @Override
  public void click(ClickOptions options) {
    if (options == null)
      options = new ClickOptions();
    FrameClickParams params = Utils.toProtocol(options, FrameClickParams.class);
    params.selector = selector;
    params.strict = true;
    frame.channel.click(params, frame.timeout(options.timeout));
  }

  // java: Frame.DblclickOptions has no 'steps', so this sends what upstream frame.dblclick() sends.
  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null)
      options = new DblclickOptions();
    FrameDblclickParams params = Utils.toProtocol(options, FrameDblclickParams.class);
    params.selector = selector;
    params.strict = true;
    frame.channel.dblclick(params, frame.timeout(options.timeout));
  }

  @Override
  public void dispatchEvent(String type, Object eventInit, DispatchEventOptions options) {
    if (eventInit == null)
      eventInit = new LinkedHashMap<>();
    if (options == null)
      options = new DispatchEventOptions();
    frame.dispatchEvent(selector, type, eventInit, convertType(options, Frame.DispatchEventOptions.class).setStrict(true));
  }

  @Override
  public void dragTo(Locator target, DragToOptions options) {
    if (options == null)
      options = new DragToOptions();
    frame.dragAndDrop(selector, ((LocatorImpl) target).selector, convertType(options, Frame.DragAndDropOptions.class).setStrict(true));
  }

  @Override
  public void drop(DropPayload payload, DropOptions options) {
    // java: there is no Frame.DropOptions with 'strict', FrameImpl.drop() sets strict: true.
    frame.drop(selector, payload, options);
  }

  @Override
  public Object evaluate(String expression, Object arg, EvaluateOptions options) {
    return frame.withTitle("Evaluate", () -> withElement((h, timeout) -> h.evaluate(expression, arg), options == null ? null : options.timeout));
  }

  @Override
  public Object evaluateAll(String expression, Object arg) {
    return frame.evalOnSelectorAll(selector, expression, arg);
  }

  @Override
  public JSHandle evaluateHandle(String expression, Object arg, EvaluateHandleOptions options) {
    return frame.withTitle("Evaluate", () -> withElement((h, timeout) -> h.evaluateHandle(expression, arg), options == null ? null : options.timeout));
  }

  @Override
  public void fill(String value, FillOptions options) {
    if (options == null)
      options = new FillOptions();
    frame.fill(selector, value, convertType(options, Frame.FillOptions.class).setStrict(true));
  }

  @Override
  public void clear(ClearOptions options) {
    frame.withTitle("Clear", () -> fill("", convertType(options, FillOptions.class)));
  }

  // java: HighlightOptions.style is a string only, cssObjectToString() is not needed.
  @Override
  public AutoCloseable highlight(HighlightOptions options) {
    if (options == null)
      options = new HighlightOptions();
    frame.highlight(selector, options.style);
    return new DisposableStub(this::hideHighlight);
  }

  @Override
  public void hideHighlight() {
    frame.hideHighlight(selector);
  }

  @Override
  public Locator locator(String selectorOrLocator, LocatorOptions options) {
    return new LocatorImpl(frame, selector + " >> " + selectorOrLocator, options);
  }

  @Override
  public Locator locator(Locator selectorOrLocator, LocatorOptions options) {
    if (((LocatorImpl) selectorOrLocator).frame != frame)
      throw new PlaywrightException("Locators must belong to the same frame.");
    return new LocatorImpl(frame, selector + " >> internal:chain=" + gson().toJson(((LocatorImpl) selectorOrLocator).selector), options);
  }

  @Override
  public Locator within(Locator locator) {
    return locator.locator(this);
  }

  // java: testIdAttributeName() of locator.ts is a field of SelectorsImpl of the Playwright instance.
  @Override
  public Locator getByTestId(String testId) {
    return locator(getByTestIdSelector(frame.connection.playwright.selectors.testIdAttributeName, testId));
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return locator(getByTestIdSelector(frame.connection.playwright.selectors.testIdAttributeName, testId));
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return locator(getByAltTextSelector(text, options));
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return locator(getByAltTextSelector(text, options));
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return locator(getByLabelSelector(text, options));
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return locator(getByLabelSelector(text, options));
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return locator(getByPlaceholderSelector(text, options));
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return locator(getByPlaceholderSelector(text, options));
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return locator(getByTextSelector(text, options));
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return locator(getByTextSelector(text, options));
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return locator(getByTitleSelector(text, options));
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return locator(getByTitleSelector(text, options));
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return locator(getByRoleSelector(role, options));
  }

  @Override
  public FrameLocator frameLocator(String selector) {
    return new FrameLocatorImpl(frame, this.selector + " >> " + selector);
  }

  @Override
  public Locator filter(FilterOptions options) {
    return new LocatorImpl(frame, selector, options);
  }

  @Override
  public Locator visible() {
    return new LocatorImpl(frame, selector, new FilterOptions().setVisible(true));
  }

  @Override
  public ElementHandle elementHandle(ElementHandleOptions options) {
    if (options == null)
      options = new ElementHandleOptions();
    Frame.WaitForSelectorOptions frameOptions = convertType(options, Frame.WaitForSelectorOptions.class);
    frameOptions.setStrict(true);
    frameOptions.setState(WaitForSelectorState.ATTACHED);
    return frame.waitForSelector(selector, frameOptions);
  }

  @Override
  public List<ElementHandle> elementHandles() {
    return frame.querySelectorAll(selector);
  }

  @Override
  public FrameLocator contentFrame() {
    return new FrameLocatorImpl(frame, selector);
  }

  @Override
  public Locator describe(String description) {
    return new LocatorImpl(frame, selector + " >> internal:describe=" + gson().toJson(description));
  }

  // java: there is no port of the selector parser; this matches the trailing internal:describe part
  // that upstream locatorCustomDescription() reads.
  @Override
  public String description() {
    Matcher matcher = Pattern.compile(" >> internal:describe=(\"(?:[^\"\\\\]|\\\\.)*\")$").matcher(selector);
    if (!matcher.find())
      return null;
    try {
      return gson().fromJson(matcher.group(1), String.class);
    } catch (RuntimeException e) {
      return null;
    }
  }

  @Override
  public Locator first() {
    return new LocatorImpl(frame, selector + " >> nth=0");
  }

  @Override
  public Locator last() {
    return new LocatorImpl(frame, selector + " >> nth=-1");
  }

  @Override
  public Locator nth(int index) {
    return new LocatorImpl(frame, selector + " >> nth=" + index);
  }

  @Override
  public Locator and(Locator locator) {
    if (((LocatorImpl) locator).frame != frame)
      throw new PlaywrightException("Locators must belong to the same frame.");
    return new LocatorImpl(frame, selector + " >> internal:and=" + gson().toJson(((LocatorImpl) locator).selector));
  }

  @Override
  public Locator or(Locator locator) {
    if (((LocatorImpl) locator).frame != frame)
      throw new PlaywrightException("Locators must belong to the same frame.");
    return new LocatorImpl(frame, selector + " >> internal:or=" + gson().toJson(((LocatorImpl) locator).selector));
  }

  @Override
  public void focus(FocusOptions options) {
    if (options == null)
      options = new FocusOptions();
    frame.focus(selector, convertType(options, Frame.FocusOptions.class).setStrict(true));
  }

  @Override
  public void blur(BlurOptions options) {
    if (options == null)
      options = new BlurOptions();
    FrameBlurParams params = Utils.toProtocol(options, FrameBlurParams.class);
    params.selector = selector;
    params.strict = true;
    frame.channel.blur(params, frame.timeout(options.timeout));
  }

  @Override
  public int count() {
    return frame.queryCount(selector);
  }

  // java: sends sdkLanguage so that toString() prints the Java locator code the server returns.
  @Override
  public Locator normalize() {
    FrameResolveSelectorParams params = new FrameResolveSelectorParams();
    params.selector = selector;
    params.sdkLanguage = "java";
    FrameResolveSelectorResult result = frame.channel.resolveSelector(params, NO_TIMEOUT);
    LocatorImpl locator = new LocatorImpl(frame, result.resolvedSelector);
    locator.stringRepresentation = result.locatorCode;
    return locator;
  }

  @Override
  public String getAttribute(String name, GetAttributeOptions options) {
    if (options == null)
      options = new GetAttributeOptions();
    return frame.getAttribute(selector, name, convertType(options, Frame.GetAttributeOptions.class).setStrict(true));
  }

  @Override
  public void hover(HoverOptions options) {
    if (options == null)
      options = new HoverOptions();
    frame.hover(selector, convertType(options, Frame.HoverOptions.class).setStrict(true));
  }

  @Override
  public String innerHTML(InnerHTMLOptions options) {
    if (options == null)
      options = new InnerHTMLOptions();
    return frame.innerHTML(selector, convertType(options, Frame.InnerHTMLOptions.class).setStrict(true));
  }

  @Override
  public String innerText(InnerTextOptions options) {
    if (options == null)
      options = new InnerTextOptions();
    return frame.innerText(selector, convertType(options, Frame.InnerTextOptions.class).setStrict(true));
  }

  @Override
  public String inputValue(InputValueOptions options) {
    if (options == null)
      options = new InputValueOptions();
    return frame.inputValue(selector, convertType(options, Frame.InputValueOptions.class).setStrict(true));
  }

  @Override
  public boolean isChecked(IsCheckedOptions options) {
    if (options == null)
      options = new IsCheckedOptions();
    return frame.isChecked(selector, convertType(options, Frame.IsCheckedOptions.class).setStrict(true));
  }

  @Override
  public boolean isDisabled(IsDisabledOptions options) {
    if (options == null)
      options = new IsDisabledOptions();
    return frame.isDisabled(selector, convertType(options, Frame.IsDisabledOptions.class).setStrict(true));
  }

  @Override
  public boolean isEditable(IsEditableOptions options) {
    if (options == null)
      options = new IsEditableOptions();
    return frame.isEditable(selector, convertType(options, Frame.IsEditableOptions.class).setStrict(true));
  }

  @Override
  public boolean isEnabled(IsEnabledOptions options) {
    if (options == null)
      options = new IsEnabledOptions();
    return frame.isEnabled(selector, convertType(options, Frame.IsEnabledOptions.class).setStrict(true));
  }

  @Override
  public boolean isHidden(IsHiddenOptions options) {
    if (options == null)
      options = new IsHiddenOptions();
    return frame.isHidden(selector, convertType(options, Frame.IsHiddenOptions.class).setStrict(true));
  }

  @Override
  public boolean isVisible(IsVisibleOptions options) {
    if (options == null)
      options = new IsVisibleOptions();
    return frame.isVisible(selector, convertType(options, Frame.IsVisibleOptions.class).setStrict(true));
  }

  @Override
  public void press(String key, PressOptions options) {
    if (options == null)
      options = new PressOptions();
    frame.press(selector, key, convertType(options, Frame.PressOptions.class).setStrict(true));
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    ScreenshotOptions screenshotOptions = options == null ? new ScreenshotOptions() : options;
    return frame.withTitle("Screenshot", () -> withElement((h, timeout) -> {
      ElementHandle.ScreenshotOptions handleOptions = convertType(screenshotOptions, ElementHandle.ScreenshotOptions.class);
      handleOptions.timeout = timeout;
      return h.screenshot(handleOptions);
    }, screenshotOptions.timeout));
  }

  @Override
  public String ariaSnapshot(AriaSnapshotOptions options) {
    if (options == null)
      options = new AriaSnapshotOptions();
    FrameAriaSnapshotParams params = Utils.toProtocol(options, FrameAriaSnapshotParams.class);
    params.selector = selector;
    return frame.channel.ariaSnapshot(params, frame.timeout(options.timeout)).snapshot;
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    ScrollIntoViewIfNeededOptions scrollOptions = options == null ? new ScrollIntoViewIfNeededOptions() : options;
    frame.withTitle("Scroll into view", () -> withElement((h, timeout) -> {
      ElementHandle.ScrollIntoViewIfNeededOptions handleOptions = convertType(scrollOptions, ElementHandle.ScrollIntoViewIfNeededOptions.class);
      handleOptions.timeout = timeout;
      h.scrollIntoViewIfNeeded(handleOptions);
      return null;
    }, scrollOptions.timeout));
  }

  @Override
  public List<String> selectOption(String values, SelectOptionOptions options) {
    return frame.selectOption(selector, values, frameSelectOptionOptions(options));
  }

  @Override
  public List<String> selectOption(ElementHandle values, SelectOptionOptions options) {
    return frame.selectOption(selector, values, frameSelectOptionOptions(options));
  }

  @Override
  public List<String> selectOption(String[] values, SelectOptionOptions options) {
    return frame.selectOption(selector, values, frameSelectOptionOptions(options));
  }

  @Override
  public List<String> selectOption(SelectOption values, SelectOptionOptions options) {
    return frame.selectOption(selector, values, frameSelectOptionOptions(options));
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    return frame.selectOption(selector, values, frameSelectOptionOptions(options));
  }

  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    return frame.selectOption(selector, values, frameSelectOptionOptions(options));
  }

  @Override
  public void selectText(SelectTextOptions options) {
    SelectTextOptions selectOptions = options == null ? new SelectTextOptions() : options;
    frame.withTitle("Select text", () -> withElement((h, timeout) -> {
      ElementHandle.SelectTextOptions handleOptions = convertType(selectOptions, ElementHandle.SelectTextOptions.class);
      handleOptions.timeout = timeout;
      h.selectText(handleOptions);
      return null;
    }, selectOptions.timeout));
  }

  @Override
  public void setChecked(boolean checked, SetCheckedOptions options) {
    if (checked)
      check(convertType(options, CheckOptions.class));
    else
      uncheck(convertType(options, UncheckOptions.class));
  }

  @Override
  public void setInputFiles(Path files, SetInputFilesOptions options) {
    frame.setInputFiles(selector, files, frameSetInputFilesOptions(options));
  }

  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    frame.setInputFiles(selector, files, frameSetInputFilesOptions(options));
  }

  @Override
  public void setInputFiles(FilePayload files, SetInputFilesOptions options) {
    frame.setInputFiles(selector, files, frameSetInputFilesOptions(options));
  }

  @Override
  public void setInputFiles(FilePayload[] files, SetInputFilesOptions options) {
    frame.setInputFiles(selector, files, frameSetInputFilesOptions(options));
  }

  @Override
  public void tap(TapOptions options) {
    if (options == null)
      options = new TapOptions();
    frame.tap(selector, convertType(options, Frame.TapOptions.class).setStrict(true));
  }

  @Override
  public String textContent(TextContentOptions options) {
    if (options == null)
      options = new TextContentOptions();
    return frame.textContent(selector, convertType(options, Frame.TextContentOptions.class).setStrict(true));
  }

  @Override
  public void type(String text, TypeOptions options) {
    if (options == null)
      options = new TypeOptions();
    frame.type(selector, text, convertType(options, Frame.TypeOptions.class).setStrict(true));
  }

  @Override
  public void pressSequentially(String text, PressSequentiallyOptions options) {
    type(text, convertType(options, TypeOptions.class));
  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null)
      options = new UncheckOptions();
    frame.uncheck(selector, convertType(options, Frame.UncheckOptions.class).setStrict(true));
  }

  @Override
  public List<Locator> all() {
    List<Locator> result = new ArrayList<>();
    int count = count();
    for (int i = 0; i < count; i++)
      result.add(nth(i));
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> allInnerTexts() {
    return (List<String>) frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.innerText)");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> allTextContents() {
    return (List<String>) frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.textContent || '')");
  }

  @Override
  public void waitFor(WaitForOptions options) {
    if (options == null)
      options = new WaitForOptions();
    FrameWaitForSelectorParams params = Utils.toProtocol(options, FrameWaitForSelectorParams.class);
    params.selector = selector;
    params.strict = true;
    params.omitReturnValue = true;
    frame.channel.waitForSelector(params, frame.timeout(options.timeout));
  }

  // java: functions are passed as strings, so isFunction is not sent and the server detects it.
  @Override
  public void waitForFunction(String expression, Object arg, WaitForFunctionOptions options) {
    FrameWaitForFunctionParams params = new FrameWaitForFunctionParams();
    params.selector = selector;
    params.strict = true;
    params.expression = expression;
    params.arg = serializeArgument(arg);
    frame.channel.waitForFunction(params, frame.timeout(options == null ? null : options.timeout));
  }

  // java: upstream options.title is the separate title parameter, as in FrameImpl.expect.
  FrameExpectResult expect(String expression, FrameExpectOptions options, String title) {
    options.selector = selector;
    return frame.expect(expression, options, title);
  }

  // java: there is no port of the locator generators. The text of the old Java client is kept,
  // tests pin it: the server's Java locator code after normalize(), the description, or Locator@<selector>.
  @Override
  public String toString() {
    if (stringRepresentation != null)
      return stringRepresentation;
    String description = description();
    if (description != null)
      return description;
    return "Locator@" + selector;
  }

  // java: helpers and members with no upstream counterpart.
  private String stringRepresentation;

  private Frame.SelectOptionOptions frameSelectOptionOptions(SelectOptionOptions options) {
    if (options == null)
      options = new SelectOptionOptions();
    return convertType(options, Frame.SelectOptionOptions.class).setStrict(true);
  }

  private Frame.SetInputFilesOptions frameSetInputFilesOptions(SetInputFilesOptions options) {
    if (options == null)
      options = new SetInputFilesOptions();
    return convertType(options, Frame.SetInputFilesOptions.class).setStrict(true);
  }

  @Override
  public int hashCode() {
    return frame.hashCode() ^ selector.hashCode();
  }

  JsonObject toProtocol() {
    JsonObject result = new JsonObject();
    result.add("frame", frame.toProtocolRef());
    result.addProperty("selector", selector);
    return result;
  }
}
