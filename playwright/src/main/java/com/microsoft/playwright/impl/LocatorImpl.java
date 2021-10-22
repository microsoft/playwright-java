package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.Utils.convertViaJson;

class LocatorImpl implements Locator {
  private final FrameImpl frame;
  private final String selector;

  public LocatorImpl(FrameImpl frame, String selector) {
    this.frame = frame;
    this.selector = selector;
  }

  private <R, O> R withElement(BiFunction<ElementHandle, O, R> callback, O options) {
    ElementHandleOptions handleOptions = convertViaJson(options, ElementHandleOptions.class);
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
  public List<String> allInnerTexts() {
    return (List<String>) frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.innerText)");
  }

  @Override
  public List<String> allTextContents() {
    return (List<String>) frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.textContent || '')");
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
    frame.check(selector, convertViaJson(options, Frame.CheckOptions.class).setStrict(true));
  }

  @Override
  public void click(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    frame.click(selector, convertViaJson(options, Frame.ClickOptions.class).setStrict(true));
  }

  @Override
  public int count() {
    return ((Number) evaluateAll("ee => ee.length")).intValue();
  }

  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    frame.dblclick(selector, convertViaJson(options, Frame.DblclickOptions.class).setStrict(true));
  }

  @Override
  public void dispatchEvent(String type, Object eventInit, DispatchEventOptions options) {
    if (options == null) {
      options = new DispatchEventOptions();
    }
    frame.dispatchEvent(selector, type, eventInit, convertViaJson(options, Frame.DispatchEventOptions.class).setStrict(true));
  }

  @Override
  public ElementHandle elementHandle(ElementHandleOptions options) {
    if (options == null) {
      options = new ElementHandleOptions();
    }
    Frame.WaitForSelectorOptions frameOptions = convertViaJson(options, Frame.WaitForSelectorOptions.class);
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
    frame.fill(selector, value, convertViaJson(options, Frame.FillOptions.class).setStrict(true));
  }

  @Override
  public Locator first() {
    return new LocatorImpl(frame, selector + " >> nth=0");
  }

  @Override
  public void focus(FocusOptions options) {
    if (options == null) {
      options = new FocusOptions();
    }
    frame.focus(selector, convertViaJson(options, Frame.FocusOptions.class).setStrict(true));
  }

  @Override
  public String getAttribute(String name, GetAttributeOptions options) {
    if (options == null) {
      options = new GetAttributeOptions();
    }
    return frame.getAttribute(selector, name, convertViaJson(options, Frame.GetAttributeOptions.class).setStrict(true));
  }

  @Override
  public void hover(HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    frame.hover(selector, convertViaJson(options, Frame.HoverOptions.class).setStrict(true));
  }

  @Override
  public String innerHTML(InnerHTMLOptions options) {
    if (options == null) {
      options = new InnerHTMLOptions();
    }
    return frame.innerHTML(selector, convertViaJson(options, Frame.InnerHTMLOptions.class).setStrict(true));
  }

  @Override
  public String innerText(InnerTextOptions options) {
    if (options == null) {
      options = new InnerTextOptions();
    }
    return frame.innerText(selector, convertViaJson(options, Frame.InnerTextOptions.class).setStrict(true));
  }

  @Override
  public String inputValue(InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    return frame.inputValue(selector, convertViaJson(options, Frame.InputValueOptions.class).setStrict(true));
  }

  @Override
  public boolean isChecked(IsCheckedOptions options) {
    if (options == null) {
      options = new IsCheckedOptions();
    }
    return frame.isChecked(selector, convertViaJson(options, Frame.IsCheckedOptions.class).setStrict(true));
  }

  @Override
  public boolean isDisabled(IsDisabledOptions options) {
    if (options == null) {
      options = new IsDisabledOptions();
    }
    return frame.isDisabled(selector, convertViaJson(options, Frame.IsDisabledOptions.class).setStrict(true));
  }

  @Override
  public boolean isEditable(IsEditableOptions options) {
    if (options == null) {
      options = new IsEditableOptions();
    }
    return frame.isEditable(selector, convertViaJson(options, Frame.IsEditableOptions.class).setStrict(true));
  }

  @Override
  public boolean isEnabled(IsEnabledOptions options) {
    if (options == null) {
      options = new IsEnabledOptions();
    }
    return frame.isEnabled(selector, convertViaJson(options, Frame.IsEnabledOptions.class).setStrict(true));
  }

  @Override
  public boolean isHidden(IsHiddenOptions options) {
    if (options == null) {
      options = new IsHiddenOptions();
    }
    return frame.isHidden(selector, convertViaJson(options, Frame.IsHiddenOptions.class).setStrict(true));
  }

  @Override
  public boolean isVisible(IsVisibleOptions options) {
    if (options == null) {
      options = new IsVisibleOptions();
    }
    return frame.isVisible(selector, convertViaJson(options, Frame.IsVisibleOptions.class).setStrict(true));
  }

  @Override
  public Locator last() {
    return new LocatorImpl(frame, selector + " >> nth=-1");
  }

  @Override
  public Locator locator(String selector) {
    return new LocatorImpl(frame, this.selector + " >> " + selector);
  }

  @Override
  public Locator nth(int index) {
    return new LocatorImpl(frame, selector + " >> nth=" + index);
  }

  @Override
  public void press(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    frame.press(selector, key, convertViaJson(options, Frame.PressOptions.class).setStrict(true));
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return withElement((h, o) -> h.screenshot(o), convertViaJson(options, ElementHandle.ScreenshotOptions.class));
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    withElement((h, o) -> {
      h.scrollIntoViewIfNeeded(o);
      return null;
    }, convertViaJson(options, ElementHandle.ScrollIntoViewIfNeededOptions.class));
  }

  @Override
  public List<String> selectOption(String values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(ElementHandle values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(String[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(SelectOption values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    return frame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class).setStrict(true));
  }

  @Override
  public void selectText(SelectTextOptions options) {
    withElement((h, o) -> {
      h.selectText(o);
      return null;
    }, convertViaJson(options, ElementHandle.SelectTextOptions.class));
  }

  @Override
  public void setChecked(boolean checked, SetCheckedOptions options) {
    if (options == null) {
      options = new SetCheckedOptions();
    }
    frame.setChecked(selector, checked, convertViaJson(options, Frame.SetCheckedOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(Path files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(FilePayload files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void setInputFiles(FilePayload[] files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    frame.setInputFiles(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class).setStrict(true));
  }

  @Override
  public void tap(TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    frame.tap(selector, convertViaJson(options, Frame.TapOptions.class).setStrict(true));
  }

  @Override
  public String textContent(TextContentOptions options) {
    if (options == null) {
      options = new TextContentOptions();
    }
    return frame.textContent(selector, convertViaJson(options, Frame.TextContentOptions.class).setStrict(true));
  }

  @Override
  public void type(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    frame.type(selector, text, convertViaJson(options, Frame.TypeOptions.class).setStrict(true));
  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    frame.uncheck(selector, convertViaJson(options, Frame.UncheckOptions.class).setStrict(true));
  }

  @Override
  public void waitFor(WaitForOptions options) {
    if (options == null) {
      options = new WaitForOptions();
    }
    waitForImpl(options);
  }

  private void waitForImpl(WaitForOptions options) {
    frame.withLogging("Locator.waitFor", () -> frame.waitForSelectorImpl(selector, convertViaJson(options, Frame.WaitForSelectorOptions.class).setStrict(true), true));
  }

  @Override
  public String toString() {
    return "Locator@" + selector;
  }

  FrameExpectResult expect(String expression, FrameExpectOptions options) {
    return frame.withLogging("Locator.expect", () -> expectImpl(expression, options));
  }

  private FrameExpectResult expectImpl(String expression, FrameExpectOptions options) {
    if (options == null) {
      options = new FrameExpectOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("expression", expression);
    if (options.expectedValue != null) {
      params.remove("expectedValue");
      params.add("expectedValue", gson().toJsonTree(serializeArgument(options.expectedValue)));
    }
    JsonElement json = frame.sendMessage("expect", params);
    FrameExpectResult result = gson().fromJson(json, FrameExpectResult.class);
    return result;
  }
}
