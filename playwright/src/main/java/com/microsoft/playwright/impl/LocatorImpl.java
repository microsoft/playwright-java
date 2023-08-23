package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.LocatorUtils.*;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

class LocatorImpl implements Locator {
  final FrameImpl frame;
  final String selector;

  public LocatorImpl(FrameImpl frame, String selector, LocatorOptions options) {
    this.frame = frame;
    if (options != null) {
      if (options.hasText != null) {
        selector += " >> internal:has-text=" + escapeForTextSelector(options.hasText, false);
      }
      if (options.hasNotText != null) {
        selector += " >> internal:has-not-text=" + escapeForTextSelector(options.hasNotText, false);
      }
      if (options.has != null) {
        LocatorImpl locator = (LocatorImpl) options.has;
        if (locator.frame != frame)
          throw new Error("Inner 'has' locator must belong to the same frame.");
        selector += " >> internal:has=" + gson().toJson(locator.selector);
      }
      if (options.hasNot != null) {
        LocatorImpl locator = (LocatorImpl) options.hasNot;
        if (locator.frame != frame)
          throw new Error("Inner 'hasNot' locator must belong to the same frame.");
        selector += " >> internal:has-not=" + gson().toJson(locator.selector);
      }
    }
    this.selector = selector;
  }

  private static String escapeWithQuotes(String text) {
    return gson().toJson(text);
  }

  private <R, O> R withElement(BiFunction<ElementHandle, O, R> callback, O options) {
    ElementHandleOptions handleOptions = convertType(options, ElementHandleOptions.class);
    // TODO: support deadline based timeout
//    Double timeout = null;
//    if (handleOptions != null) {
//      timeout = handleOptions.timeout;
//    }
//    timeout = frame.page.timeoutSettings.timeout(timeout);
//    long deadline = System.nanoTime() + (long) timeout.doubleValue() * 1_000_000;
    ElementHandle handle = elementHandle(handleOptions);
    try {
      return callback.apply(handle, options);
    } finally {
      if (handle != null) {
        handle.dispose();
      }
    }
  }

  @Override
  public List<Locator> all() {
    List<Locator> result = new ArrayList<>();
    int count = this.count();
    for (int i = 0; i < count; i++) {
      result.add(nth(i));
    }
    return result;
  }

  @Override
  public List<String> allInnerTexts() {
    return (List<String>) frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.innerText)");
  }

  @Override
  public List<String> allTextContents() {
    return (List<String>) frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.textContent || '')");
  }

  @Override
  public Locator and(Locator locator) {
    LocatorImpl other = (LocatorImpl) locator;
    if (other.frame != frame)
      throw new Error("Locators must belong to the same frame.");
    return new LocatorImpl(frame, selector + " >> internal:and=" + gson().toJson(other.selector), null);
  }

  @Override
  public void blur(BlurOptions options) {
    frame.withLogging("Locator.blur", () -> blurImpl(options));
  }

  private void blurImpl(BlurOptions options) {
    if (options == null) {
      options = new BlurOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("strict", true);
    frame.sendMessage("blur", params);
  }

  @Override
  public BoundingBox boundingBox(BoundingBoxOptions options) {
    return withElement((h, o) -> h.boundingBox(), options);
  }

  @Override
  public void check(CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    frame.check(selector, convertType(options, Frame.CheckOptions.class).setStrict(true));
  }

  @Override
  public void clear(ClearOptions options) {
    fill("", convertType(options, FillOptions.class));
  }

  @Override
  public void click(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    frame.click(selector, convertType(options, Frame.ClickOptions.class).setStrict(true));
  }

  @Override
  public int count() {
    return frame.queryCount(selector);
  }

  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    frame.dblclick(selector, convertType(options, Frame.DblclickOptions.class).setStrict(true));
  }

  @Override
  public void dispatchEvent(String type, Object eventInit, DispatchEventOptions options) {
    if (options == null) {
      options = new DispatchEventOptions();
    }
    frame.dispatchEvent(selector, type, eventInit, convertType(options, Frame.DispatchEventOptions.class).setStrict(true));
  }

  @Override
  public void dragTo(Locator target, DragToOptions options) {
    if (options == null) {
      options = new DragToOptions();
    }
    Frame.DragAndDropOptions frameOptions = convertType(options, Frame.DragAndDropOptions.class);
    frameOptions.setStrict(true);
    frame.dragAndDrop(selector, ((LocatorImpl) target).selector, frameOptions);
  }

  @Override
  public ElementHandle elementHandle(ElementHandleOptions options) {
    if (options == null) {
      options = new ElementHandleOptions();
    }
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
  public Object evaluate(String expression, Object arg, EvaluateOptions options) {
    return withElement((h, o) -> h.evaluate(expression, arg), options);
  }

  @Override
  public Object evaluateAll(String expression, Object arg) {
    return frame.evalOnSelectorAll(selector, expression, arg);
  }

  @Override
  public JSHandle evaluateHandle(String expression, Object arg, EvaluateHandleOptions options) {
    return withElement((h, o) -> h.evaluateHandle(expression, arg), options);
  }

  @Override
  public void fill(String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    frame.fill(selector, value, convertType(options, Frame.FillOptions.class).setStrict(true));
  }

  @Override
  public Locator filter(FilterOptions options) {
    return new LocatorImpl(frame, selector, convertType(options,LocatorOptions.class));
  }

  @Override
  public Locator first() {
    return new LocatorImpl(frame, selector + " >> nth=0", null);
  }

  @Override
  public void focus(FocusOptions options) {
    if (options == null) {
      options = new FocusOptions();
    }
    frame.focus(selector, convertType(options, Frame.FocusOptions.class).setStrict(true));
  }

  @Override
  public FrameLocatorImpl frameLocator(String selector) {
    return new FrameLocatorImpl(frame, this.selector + " >> " + selector);
  }

  @Override
  public String getAttribute(String name, GetAttributeOptions options) {
    if (options == null) {
      options = new GetAttributeOptions();
    }
    return frame.getAttribute(selector, name, convertType(options, Frame.GetAttributeOptions.class).setStrict(true));
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
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return locator(getByRoleSelector(role, options));
  }

  @Override
  public Locator getByTestId(String testId) {
    return locator(getByTestIdSelector(testId));
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return locator(getByTestIdSelector(testId));
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
  public void highlight() {
    frame.highlightImpl(selector);
  }

  @Override
  public void hover(HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    frame.hover(selector, convertType(options, Frame.HoverOptions.class).setStrict(true));
  }

  @Override
  public String innerHTML(InnerHTMLOptions options) {
    if (options == null) {
      options = new InnerHTMLOptions();
    }
    return frame.innerHTML(selector, convertType(options, Frame.InnerHTMLOptions.class).setStrict(true));
  }

  @Override
  public String innerText(InnerTextOptions options) {
    if (options == null) {
      options = new InnerTextOptions();
    }
    return frame.innerText(selector, convertType(options, Frame.InnerTextOptions.class).setStrict(true));
  }

  @Override
  public String inputValue(InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    return frame.inputValue(selector, convertType(options, Frame.InputValueOptions.class).setStrict(true));
  }

  @Override
  public boolean isChecked(IsCheckedOptions options) {
    if (options == null) {
      options = new IsCheckedOptions();
    }
    return frame.isChecked(selector, convertType(options, Frame.IsCheckedOptions.class).setStrict(true));
  }

  @Override
  public boolean isDisabled(IsDisabledOptions options) {
    if (options == null) {
      options = new IsDisabledOptions();
    }
    return frame.isDisabled(selector, convertType(options, Frame.IsDisabledOptions.class).setStrict(true));
  }

  @Override
  public boolean isEditable(IsEditableOptions options) {
    if (options == null) {
      options = new IsEditableOptions();
    }
    return frame.isEditable(selector, convertType(options, Frame.IsEditableOptions.class).setStrict(true));
  }

  @Override
  public boolean isEnabled(IsEnabledOptions options) {
    if (options == null) {
      options = new IsEnabledOptions();
    }
    return frame.isEnabled(selector, convertType(options, Frame.IsEnabledOptions.class).setStrict(true));
  }

  @Override
  public boolean isHidden(IsHiddenOptions options) {
    if (options == null) {
      options = new IsHiddenOptions();
    }
    return frame.isHidden(selector, convertType(options, Frame.IsHiddenOptions.class).setStrict(true));
  }

  @Override
  public boolean isVisible(IsVisibleOptions options) {
    if (options == null) {
      options = new IsVisibleOptions();
    }
    return frame.isVisible(selector, convertType(options, Frame.IsVisibleOptions.class).setStrict(true));
  }

  @Override
  public Locator last() {
    return new LocatorImpl(frame, selector + " >> nth=-1", null);
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return new LocatorImpl(frame, this.selector + " >> " + selector, options);
  }

  @Override
  public Locator locator(Locator selectorOrLocator, LocatorOptions options) {
    LocatorImpl other = (LocatorImpl) selectorOrLocator;
    if (other.frame != frame) {
      throw new PlaywrightException("Locators must belong to the same frame.");
    }
    return new LocatorImpl(frame, this.selector + " >> internal:chain=" + gson().toJson(other.selector), options);
  }

  @Override
  public Locator nth(int index) {
    return new LocatorImpl(frame, selector + " >> nth=" + index, null);
  }

  @Override
  public Locator or(Locator locator) {
    LocatorImpl other = (LocatorImpl) locator;
    if (other.frame != frame)
      throw new Error("Locators must belong to the same frame.");
    return new LocatorImpl(frame, selector + " >> internal:or=" + gson().toJson(other.selector), null);
  }

  @Override
  public Page page() {
    return frame.page();
  }

  @Override
  public void press(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    frame.press(selector, key, convertType(options, Frame.PressOptions.class).setStrict(true));
  }

  @Override
  public void pressSequentially(String text, PressSequentiallyOptions options) {
    type(text, convertType(options, TypeOptions.class));
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return withElement((h, o) -> h.screenshot(o), convertType(options, ElementHandle.ScreenshotOptions.class));
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    withElement((h, o) -> {
      h.scrollIntoViewIfNeeded(o);
      return null;
    }, convertType(options, ElementHandle.ScrollIntoViewIfNeededOptions.class));
  }

  @Override
  public List<String> selectOption(String values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(ElementHandle values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(String[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(SelectOption values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public void selectText(SelectTextOptions options) {
    withElement((h, o) -> {
      h.selectText(o);
      return null;
    }, convertType(options, ElementHandle.SelectTextOptions.class));
  }

  @Override
  public void setChecked(boolean checked, SetCheckedOptions options) {
    if (options == null) {
      options = new SetCheckedOptions();
    }
    frame.setChecked(selector, checked, convertType(options, Frame.SetCheckedOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(Path files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(FilePayload files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(FilePayload[] files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void tap(TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    frame.tap(selector, convertType(options, Frame.TapOptions.class).setStrict(true));
  }

  @Override
  public String textContent(TextContentOptions options) {
    if (options == null) {
      options = new TextContentOptions();
    }
    return frame.textContent(selector, convertType(options, Frame.TextContentOptions.class).setStrict(true));
  }

  @Override
  public void type(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    frame.type(selector, text, convertType(options, Frame.TypeOptions.class).setStrict(true));
  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    frame.uncheck(selector, convertType(options, Frame.UncheckOptions.class).setStrict(true));
  }

  @Override
  public void waitFor(WaitForOptions options) {
    if (options == null) {
      options = new WaitForOptions();
    }
    waitForImpl(options);
  }

  private void waitForImpl(WaitForOptions options) {
    frame.withLogging("Locator.waitFor", () -> frame.waitForSelectorImpl(selector, convertType(options, Frame.WaitForSelectorOptions.class).setStrict(true), true));
  }

  @Override
  public String toString() {
    return "Locator@" + selector;
  }

  FrameExpectResult expect(String expression, FrameExpectOptions options) {
    return frame.withLogging("Locator.expect", () -> expectImpl(expression, options));
  }

  JsonObject toProtocol() {
    JsonObject result = new JsonObject();
    result.add("frame", frame.toProtocolRef());
    result.addProperty("selector", selector);
    return result;
  }

  private FrameExpectResult expectImpl(String expression, FrameExpectOptions options) {
    if (options == null) {
      options = new FrameExpectOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("expression", expression);
    JsonElement json = frame.sendMessage("expect", params);
    FrameExpectResult result = gson().fromJson(json, FrameExpectResult.class);
    return result;
  }
}
