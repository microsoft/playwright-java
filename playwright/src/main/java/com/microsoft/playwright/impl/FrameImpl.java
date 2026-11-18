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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.LocatorUtils.*;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.options.WaitUntilState.*;
import static com.microsoft.playwright.impl.Serialization.*;

public class FrameImpl extends ChannelOwner implements Frame {
  private String name;
  private String url;
  FrameImpl parentFrame;
  Set<FrameImpl> childFrames = new LinkedHashSet<>();
  private final Set<WaitUntilState> loadStates = new HashSet<>();

  PageImpl page;
  boolean isDetached;
  private final WebMCPImpl webmcp = new WebMCPImpl(this);

  final FrameChannel channel = new FrameChannel(this);

  FrameImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);

    name = initializer.get("name").getAsString();
    url = initializer.get("url").getAsString();
    if (initializer.has("parentFrame")) {
      parentFrame = connection.getExistingObject(initializer.getAsJsonObject("parentFrame").get("guid").getAsString());
      parentFrame.childFrames.add(this);
    }
    for (JsonElement item : initializer.get("loadStates").getAsJsonArray()) {
      loadStates.add(loadStateFromProtocol(item.getAsString()));
    }
  }

  private static WaitUntilState loadStateFromProtocol(String value) {
    switch (value) {
      case "load": return LOAD;
      case "domcontentloaded": return DOMCONTENTLOADED;
      case "networkidle": return NETWORKIDLE;
      case "commit": return COMMIT;
      default: throw new PlaywrightException("Unexpected value: " + value);
    }
  }

  @Override
  public ElementHandle querySelector(String selector, QuerySelectorOptions options) {
    if (options == null) {
      options = new QuerySelectorOptions();
    }
    FrameQuerySelectorParams params = toProtocol(options, FrameQuerySelectorParams.class);
    params.selector = selector;
    return channel.querySelector(params, NO_TIMEOUT).element;
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    FrameQuerySelectorAllParams params = new FrameQuerySelectorAllParams();
    params.selector = selector;
    FrameQuerySelectorAllResult result = channel.querySelectorAll(params, NO_TIMEOUT);
    if (result.elements == null) {
      return null;
    }
    return new ArrayList<ElementHandle>(result.elements);
  }

  @Override
  public List<String> selectOption(String selector, String value, SelectOptionOptions options) {
    String[] values = value == null ? null : new String[]{ value };
    return selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle value, SelectOptionOptions options) {
    ElementHandle[] values = value == null ? null : new ElementHandle[]{value};
    return selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, SelectOption value, SelectOptionOptions options) {
    SelectOption[] values = value == null ? null : new SelectOption[]{value};
    return selectOption(selector, values, options);
  }


  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg, EvalOnSelectorOptions options) {
    return evalOnSelectorImpl(selector, pageFunction, arg, options);
  }

  Object evalOnSelectorImpl(String selector, String pageFunction, Object arg, EvalOnSelectorOptions options) {
    if (options == null) {
      options = new EvalOnSelectorOptions();
    }
    FrameEvalOnSelectorParams params = toProtocol(options, FrameEvalOnSelectorParams.class);
    params.selector = selector;
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evalOnSelector(params, NO_TIMEOUT).value);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return evalOnSelectorAllImpl(selector, pageFunction, arg);
  }

  Object evalOnSelectorAllImpl(String selector, String pageFunction, Object arg) {
    FrameEvalOnSelectorAllParams params = new FrameEvalOnSelectorAllParams();
    params.selector = selector;
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evalOnSelectorAll(params, NO_TIMEOUT).value);
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options){
    return addScriptTagImpl(options);
  }

  ElementHandle addScriptTagImpl(AddScriptTagOptions options) {
    if (options == null) {
      options = new AddScriptTagOptions();
    }
    FrameAddScriptTagParams params = toProtocol(options, FrameAddScriptTagParams.class);
    if (options.path != null) {
      byte[] encoded;
      try {
        encoded = Files.readAllBytes(options.path);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
      String content = new String(encoded, StandardCharsets.UTF_8);
      content = ClientHelper.addSourceUrlToScript(content, options.path);
      params.content = content;
    }
    return channel.addScriptTag(params, NO_TIMEOUT).element;
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options){
    return addStyleTagImpl(options);
  }

  ElementHandle addStyleTagImpl(AddStyleTagOptions options) {
    if (options == null) {
      options = new AddStyleTagOptions();
    }
    FrameAddStyleTagParams params = toProtocol(options, FrameAddStyleTagParams.class);
    if (options.path != null) {
      byte[] encoded;
      try {
        encoded = Files.readAllBytes(options.path);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
      String content = new String(encoded, StandardCharsets.UTF_8);
      content += "/*# sourceURL=" + options.path.toString().replace("\n", "") + "*/";
      params.content = content;
    }
    return channel.addStyleTag(params, NO_TIMEOUT).element;
  }

  @Override
  public void check(String selector, CheckOptions options){
    if (options == null) {
      options = new CheckOptions();
    }
    FrameCheckParams params = toProtocol(options, FrameCheckParams.class);
    params.selector = selector;
    channel.check(params, timeout(options.timeout));
  }

  @Override
  public List<Frame> childFrames() {
    return new ArrayList<>(childFrames);
  }

  @Override
  public void click(String selector, ClickOptions options) {
    clickImpl(selector, options, null);
  }

  void clickImpl(String selector, ClickOptions options, Integer steps) {
    if (options == null) {
      options = new ClickOptions();
    }
    FrameClickParams params = toProtocol(options, FrameClickParams.class);
    params.selector = selector;
    if (steps != null) {
      params.steps = steps;
    }
    channel.click(params, timeout(options.timeout));
  }

  @Override
  public String content(ContentOptions options) {
    if (options == null) {
      options = new ContentOptions();
    }
    FrameContentParams params = toProtocol(options, FrameContentParams.class);
    return channel.content(params, NO_TIMEOUT).value;
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    dblclickImpl(selector, options, null);
  }

  void dblclickImpl(String selector, DblclickOptions options, Integer steps) {
    if (options == null) {
      options = new DblclickOptions();
    }
    FrameDblclickParams params = toProtocol(options, FrameDblclickParams.class);
    params.selector = selector;
    if (steps != null) {
      params.steps = steps;
    }
    channel.dblclick(params, timeout(options.timeout));
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
  public Object evaluate(String expression, Object arg) {
    FrameEvaluateExpressionParams params = new FrameEvaluateExpressionParams();
    params.expression = expression;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evaluateExpression(params, NO_TIMEOUT).value);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    FrameEvaluateExpressionHandleParams params = new FrameEvaluateExpressionHandleParams();
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return channel.evaluateExpressionHandle(params, NO_TIMEOUT).handle;
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
  public ElementHandle frameElement() {
    return channel.frameElement(NO_TIMEOUT).element;
  }

  @Override
  public FrameLocator frameLocator(String selector) {
    return new FrameLocatorImpl(this, selector == null ? FrameLocatorImpl.ANY_FRAME_SELECTOR : selector);
  }

  @Override
  public String getAttribute(String selector, String name, GetAttributeOptions options) {
    return getAttributeImpl(selector, name, options);
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
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return locator(getByRoleSelector(role, convertType(options, Locator.GetByRoleOptions.class)));
  }

  @Override
  public Locator getByTestId(String testId) {
    return locator(getByTestIdSelector(testId, connection.playwright));
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return locator(getByTestIdSelector(testId, connection.playwright));
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

  String getAttributeImpl(String selector, String name, GetAttributeOptions options) {
    if (options == null) {
      options = new GetAttributeOptions();
    }
    FrameGetAttributeParams params = toProtocol(options, FrameGetAttributeParams.class);
    params.selector = selector;
    params.name = name;
    FrameGetAttributeResult result = channel.getAttribute(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    return navigateImpl(url, options);
  }

  ResponseImpl navigateImpl(String url, NavigateOptions options) {
    if (options == null) {
      options = new NavigateOptions();
    }
    FrameGotoParams params = toProtocol(options, FrameGotoParams.class);
    params.url = url;
    return channel.goto_(params, navigationTimeout(options.timeout)).response;
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    hoverImpl(selector, options);
  }

  void hoverImpl(String selector, HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    FrameHoverParams params = toProtocol(options, FrameHoverParams.class);
    params.selector = selector;
    channel.hover(params, timeout(options.timeout));
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    dragAndDropImpl(source, target, options, null);
  }

  void dragAndDropImpl(String source, String target, DragAndDropOptions options, Integer steps) {
    if (options == null) {
      options = new DragAndDropOptions();
    }
    FrameDragAndDropParams params = toProtocol(options, FrameDragAndDropParams.class);
    params.source = source;
    params.target = target;
    if (steps != null) {
      params.steps = steps;
    }
    channel.dragAndDrop(params, timeout(options.timeout));
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return innerHTMLImpl(selector, options);
  }

  String innerHTMLImpl(String selector, InnerHTMLOptions options) {
    if (options == null) {
      options = new InnerHTMLOptions();
    }
    FrameInnerHTMLParams params = toProtocol(options, FrameInnerHTMLParams.class);
    params.selector = selector;
    FrameInnerHTMLResult result = channel.innerHTML(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return innerTextImpl(selector, options);
  }

  String innerTextImpl(String selector, InnerTextOptions options) {
    if (options == null) {
      options = new InnerTextOptions();
    }
    FrameInnerTextParams params = toProtocol(options, FrameInnerTextParams.class);
    params.selector = selector;
    FrameInnerTextResult result = channel.innerText(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    return inputValueImpl(selector, options);
  }

  String inputValueImpl(String selector, InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    FrameInputValueParams params = toProtocol(options, FrameInputValueParams.class);
    params.selector = selector;
    FrameInputValueResult result = channel.inputValue(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return isCheckedImpl(selector, options);
  }

  boolean isCheckedImpl(String selector, IsCheckedOptions options) {
    if (options == null) {
      options = new IsCheckedOptions();
    }
    FrameIsCheckedParams params = toProtocol(options, FrameIsCheckedParams.class);
    params.selector = selector;
    FrameIsCheckedResult result = channel.isChecked(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public boolean isDetached() {
    return isDetached;
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    return isDisabledImpl(selector, options);
  }

  boolean isDisabledImpl(String selector, IsDisabledOptions options) {
    if (options == null) {
      options = new IsDisabledOptions();
    }
    FrameIsDisabledParams params = toProtocol(options, FrameIsDisabledParams.class);
    params.selector = selector;
    FrameIsDisabledResult result = channel.isDisabled(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return isEditableImpl(selector, options);
  }

  boolean isEditableImpl(String selector, IsEditableOptions options) {
    if (options == null) {
      options = new IsEditableOptions();
    }
    FrameIsEditableParams params = toProtocol(options, FrameIsEditableParams.class);
    params.selector = selector;
    FrameIsEditableResult result = channel.isEditable(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return isEnabledImpl(selector, options);
  }

  boolean isEnabledImpl(String selector, IsEnabledOptions options) {
    if (options == null) {
      options = new IsEnabledOptions();
    }
    FrameIsEnabledParams params = toProtocol(options, FrameIsEnabledParams.class);
    params.selector = selector;
    FrameIsEnabledResult result = channel.isEnabled(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return isHiddenImpl(selector, options);
  }

  boolean isHiddenImpl(String selector, IsHiddenOptions options) {
    if (options == null) {
      options = new IsHiddenOptions();
    }
    FrameIsHiddenParams params = toProtocol(options, FrameIsHiddenParams.class);
    params.selector = selector;
    FrameIsHiddenResult result = channel.isHidden(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    return isVisibleImpl(selector, options);
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return new LocatorImpl(this, selector, convertType(options, Locator.LocatorOptions.class));
  }

  boolean isVisibleImpl(String selector, IsVisibleOptions options) {
    if (options == null) {
      options = new IsVisibleOptions();
    }
    FrameIsVisibleParams params = toProtocol(options, FrameIsVisibleParams.class);
    params.selector = selector;
    FrameIsVisibleResult result = channel.isVisible(params, timeout(options.timeout));
    return result.value;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public PageImpl page() {
    return page;
  }

  @Override
  public Frame parentFrame() {
    return parentFrame;
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    pressImpl(selector, key, options);
  }

  void pressImpl(String selector, String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    FramePressParams params = toProtocol(options, FramePressParams.class);
    params.selector = selector;
    params.key = key;
    channel.press(params, timeout(options.timeout));
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  List<String> selectOptionImpl(String selector, SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    FrameSelectOptionParams params = toProtocol(options, FrameSelectOptionParams.class);
    params.selector = selector;
    if (values != null) {
      params.options = new ArrayList<>();
      for (SelectOption value : values) {
        FrameSelectOptionParams.FrameSelectOptionParamsOptions option = new FrameSelectOptionParams.FrameSelectOptionParamsOptions();
        option.value = value.value;
        option.label = value.label;
        option.index = value.index;
        params.options.add(option);
      }
    }
    return selectOption(params, options.timeout);
  }

  List<String> selectOptionImpl(String selector, String[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    FrameSelectOptionParams params = toProtocol(options, FrameSelectOptionParams.class);
    params.selector = selector;
    if (values != null) {
      params.options = new ArrayList<>();
      for (String value : values) {
        FrameSelectOptionParams.FrameSelectOptionParamsOptions option = new FrameSelectOptionParams.FrameSelectOptionParamsOptions();
        option.valueOrLabel = value;
        params.options.add(option);
      }
    }
    return selectOption(params, options.timeout);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  List<String> selectOptionImpl(String selector, ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    FrameSelectOptionParams params = toProtocol(options, FrameSelectOptionParams.class);
    params.selector = selector;
    if (values != null) {
      params.elements = new ArrayList<>();
      for (ElementHandle handle : values) {
        params.elements.add((ElementHandleImpl) handle);
      }
    }
    return selectOption(params, options.timeout);
  }

  private List<String> selectOption(FrameSelectOptionParams params, Double timeout) {
    return channel.selectOption(params, timeout(timeout)).values;
  }

  @Override
  public void setChecked(String selector, boolean checked, SetCheckedOptions options) {
    setCheckedImpl(selector, checked, options);
  }

  void setCheckedImpl(String selector, boolean checked, SetCheckedOptions options) {
    if (checked) {
      check(selector, convertType(options, CheckOptions.class));
    } else {
      uncheck(selector, convertType(options, UncheckOptions.class));
    }
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    if (options == null) {
      options = new SetContentOptions();
    }
    FrameSetContentParams params = toProtocol(options, FrameSetContentParams.class);
    params.html = html;
    channel.setContent(params, navigationTimeout(options.timeout));
  }

  @Override
  public void setInputFiles(String selector, Path files, SetInputFilesOptions options) {
    setInputFiles(selector, new Path[] {files}, options);
  }

  @Override
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    setInputFilesImpl(selector, files, options);
  }

  void setInputFilesImpl(String selector, Path[] files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    FrameSetInputFilesParams params = toProtocol(options, FrameSetInputFilesParams.class);
    Utils.UploadParams upload = addFilePathUploadParams(files, page.context());
    if (upload.emptyPayloads) {
      params.payloads = new ArrayList<>();
    }
    params.localDirectory = upload.localDirectory;
    params.directoryStream = upload.directoryStream;
    params.localPaths = upload.localPaths;
    params.streams = upload.streams;
    params.selector = selector;
    channel.setInputFiles(params, timeout(options.timeout));
  }

  @Override
  public void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options) {
    setInputFiles(selector, new FilePayload[]{files}, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options) {
    setInputFilesImpl(selector, files, options);
  }

  void setInputFilesImpl(String selector, FilePayload[] files, SetInputFilesOptions options) {
    checkFilePayloadSize(files);
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    FrameSetInputFilesParams params = toProtocol(options, FrameSetInputFilesParams.class);
    params.selector = selector;
    params.payloads = new ArrayList<>();
    for (FilePayload file : files) {
      FrameSetInputFilesParams.FrameSetInputFilesParamsPayloads payload = new FrameSetInputFilesParams.FrameSetInputFilesParamsPayloads();
      payload.name = file.name;
      payload.mimeType = file.mimeType;
      payload.buffer = file.buffer;
      params.payloads.add(payload);
    }
    channel.setInputFiles(params, timeout(options.timeout));
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
  public String textContent(String selector, TextContentOptions options) {
    if (options == null) {
      options = new TextContentOptions();
    }
    FrameTextContentParams params = toProtocol(options, FrameTextContentParams.class);
    params.selector = selector;
    return channel.textContent(params, timeout(options.timeout)).value;
  }

  @Override
  public String title() {
    return channel.title(NO_TIMEOUT).value;
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
  public void uncheck(String selector, UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    FrameUncheckParams params = toProtocol(options, FrameUncheckParams.class);
    params.selector = selector;
    channel.uncheck(params, timeout(options.timeout));
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options) {
    if (options == null) {
      options = new WaitForFunctionOptions();
    }
    FrameWaitForFunctionParams params = toProtocol(options, FrameWaitForFunctionParams.class);
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return channel.waitForFunction(params, timeout(options.timeout)).handle;
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
  public Response waitForNavigation(WaitForNavigationOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForNavigationOptions();
    }
    UrlMatcher matcher = UrlMatcher.forOneOf(page.context().baseUrl(), options.url, this.connection.localUtils(), false);
    return waitForNavigation(matcher, options.url instanceof String ? (String) options.url : null, options.waitUntil, options.timeout, code);
  }

  private Response waitForNavigation(UrlMatcher matcher, String url, WaitUntilState waitUntil, Double timeout, Runnable code) {
    if (waitUntil == null) {
      waitUntil = LOAD;
    }
    final WaitUntilState expectedState = waitUntil;
    // java: the code runs outside of the api zone, so that its calls are not hidden as internal.
    Waiter waiter = withTitle("Wait for navigation", () -> setupNavigationWaiter(timeout));

    String toUrl = url != null ? " to \"" + url + "\"" : "";
    waiter.log("waiting for navigation" + toUrl + " until \"" + loadStateName(waitUntil) + "\"");

    JsonObject navigatedEvent = waiter.<JsonObject>waitForEvent(this, Events.Frame.NAVIGATED, event -> {
      // Any failed navigation results in a rejection.
      if (event.has("error")) {
        return true;
      }
      String eventUrl = event.get("url").getAsString();
      waiter.log("  navigated to \"" + eventUrl + "\"");
      return matcher.test(eventUrl);
    }, code);
    if (navigatedEvent.has("error")) {
      WaitableResult<Object> failure = new WaitableResult<>();
      failure.completeExceptionally(new PlaywrightException(navigatedEvent.get("error").getAsString()));
      waiter.waitForPromise(failure);
    }

    if (!loadStates.contains(expectedState)) {
      waiter.waitForPromise(waiter.<WaitUntilState>waitForEvent(this, Events.Frame.LOAD_STATE, s -> {
        waiter.log("  \"" + loadStateName(s) + "\" event fired");
        return s == expectedState;
      }));
    }

    RequestImpl request = null;
    if (navigatedEvent.has("newDocument")) {
      JsonObject jsonReq = navigatedEvent.getAsJsonObject("newDocument").getAsJsonObject("request");
      if (jsonReq != null) {
        request = connection.getExistingObject(jsonReq.get("guid").getAsString());
      }
    }
    Response response = null;
    if (request != null) {
      WaitableResult<Response> result = new WaitableResult<>();
      try {
        result.complete(request.finalRequest().response());
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
    if (state == null) {
      state = LoadState.LOAD;
    }
    waitForLoadState(convertType(state, WaitUntilState.class), options == null ? null : options.timeout);
  }

  private void waitForLoadState(WaitUntilState state, Double timeout) {
    if (state == null) {
      state = LOAD;
    }
    final WaitUntilState expectedState = state;
    withTitle("Wait for load state \"" + loadStateName(state) + "\"", () -> {
      Waiter waiter = setupNavigationWaiter(timeout);
      try {
        if (loadStates.contains(expectedState)) {
          waiter.log("  not waiting, \"" + loadStateName(expectedState) + "\" event already fired");
          waiter.throwIfImmediatelyRejected();
        } else {
          waiter.waitForPromise(waiter.<WaitUntilState>waitForEvent(this, Events.Frame.LOAD_STATE, s -> {
            waiter.log("  \"" + loadStateName(s) + "\" event fired");
            return s == expectedState;
          }));
        }
      } finally {
        waiter.dispose();
      }
    });
  }

  private static String loadStateName(WaitUntilState state) {
    return state.name().toLowerCase();
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return waitForSelectorImpl(selector, options, false);
  }

  ElementHandle waitForSelectorImpl(String selector, WaitForSelectorOptions options, boolean omitReturnValue) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    FrameWaitForSelectorParams params = toProtocol(options, FrameWaitForSelectorParams.class);
    params.selector = selector;
    params.omitReturnValue = omitReturnValue;
    return channel.waitForSelector(params, timeout(options.timeout)).element;
  }

  @Override
  public void waitForTimeout(double timeout) {
    FrameWaitForTimeoutParams params = new FrameWaitForTimeoutParams();
    params.waitTimeout = timeout;
    channel.waitForTimeout(params, NO_TIMEOUT);
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    waitForURL(UrlMatcher.forGlob(page.context().baseUrl(), url, this.connection.localUtils(), false), url, options);
  }

  @Override
  public void waitForURL(Pattern url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(url), null, options);
  }

  @Override
  public void waitForURL(Predicate<String> url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(url), null, options);
  }

  private void waitForURL(UrlMatcher matcher, String url, WaitForURLOptions options) {
    if (options == null) {
      options = new WaitForURLOptions();
    }
    if (matcher.test(url())) {
      waitForLoadState(options.waitUntil, options.timeout);
      return;
    }
    waitForNavigation(matcher, url, options.waitUntil, options.timeout, () -> {});
  }

  int queryCount(String selector) {
    FrameQueryCountParams params = new FrameQueryCountParams();
    params.selector = selector;
    return channel.queryCount(params, NO_TIMEOUT).value;
  }

  void dropImpl(String selector, DropPayload payload, com.microsoft.playwright.Locator.DropOptions options) {
    if (options == null) {
      options = new com.microsoft.playwright.Locator.DropOptions();
    }
    FrameDropParams params = toProtocol(options, FrameDropParams.class);
    params.selector = selector;
    params.strict = true;
    if (payload != null) {
      if (payload.files != null) {
        if (payload.files instanceof Path) {
          addDropFilePaths(new Path[] { (Path) payload.files }, params);
        } else if (payload.files instanceof Path[]) {
          addDropFilePaths((Path[]) payload.files, params);
        } else if (payload.files instanceof com.microsoft.playwright.options.FilePayload) {
          addDropPayloads(new com.microsoft.playwright.options.FilePayload[] { (com.microsoft.playwright.options.FilePayload) payload.files }, params);
        } else if (payload.files instanceof com.microsoft.playwright.options.FilePayload[]) {
          addDropPayloads((com.microsoft.playwright.options.FilePayload[]) payload.files, params);
        } else {
          throw new com.microsoft.playwright.PlaywrightException("Unsupported files type: " + payload.files.getClass());
        }
      }
      if (payload.data != null) {
        params.data = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : payload.data.entrySet()) {
          FrameDropParams.FrameDropParamsData e = new FrameDropParams.FrameDropParamsData();
          e.mimeType = entry.getKey();
          e.value = entry.getValue();
          params.data.add(e);
        }
      }
    }
    channel.drop(params, timeout(options.timeout));
  }

  private void addDropFilePaths(Path[] files, FrameDropParams params) {
    Utils.UploadParams upload = addFilePathUploadParams(files, page.context());
    if (upload.emptyPayloads) {
      params.payloads = new ArrayList<>();
    }
    params.localPaths = upload.localPaths;
    params.streams = upload.streams;
  }

  private void addDropPayloads(com.microsoft.playwright.options.FilePayload[] files, FrameDropParams params) {
    checkFilePayloadSize(files);
    params.payloads = new ArrayList<>();
    for (com.microsoft.playwright.options.FilePayload file : files) {
      FrameDropParams.FrameDropParamsPayloads payload = new FrameDropParams.FrameDropParamsPayloads();
      payload.name = file.name;
      payload.mimeType = file.mimeType;
      payload.buffer = file.buffer;
      params.payloads.add(payload);
    }
  }

  void highlightImpl(String selector, String style) {
    FrameHighlightParams params = new FrameHighlightParams();
    params.selector = selector;
    params.style = style;
    channel.highlight(params, NO_TIMEOUT);
  }

  void hideHighlightImpl(String selector) {
    FrameHideHighlightParams params = new FrameHideHighlightParams();
    params.selector = selector;
    channel.hideHighlight(params, NO_TIMEOUT);
  }

  protected void handleEvent(String event, JsonObject params) {
    if ("loadstate".equals(event)) {
      JsonElement add = params.get("add");
      if (add != null) {
        WaitUntilState state = loadStateFromProtocol(add.getAsString());
        loadStates.add(state);
        if (parentFrame == null && page != null) {
          if (state == LOAD) {
            page.emit(Events.Page.LOAD, page);
            page.browserContext.notifyPageLoad(page);
          } else if (state == DOMCONTENTLOADED) {
            page.emit(Events.Page.DOM_CONTENT_LOADED, page);
          }
        }
        emit(Events.Frame.LOAD_STATE, state);
      }
      JsonElement remove = params.get("remove");
      if (remove != null) {
        loadStates.remove(loadStateFromProtocol(remove.getAsString()));
      }
    } else if ("navigated".equals(event)) {
      url = params.get("url").getAsString();
      name = params.get("name").getAsString();
      if (!params.has("error") && page != null) {
        page.frameNavigated(this);
      }
      emit(Events.Frame.NAVIGATED, params);
    }
  }

  @Override
  public WebMCP webmcp() {
    return webmcp;
  }

  protected double timeout(Double timeout) {
    if (page != null) {
      return page.timeoutSettings.timeout(timeout);
    }
    return new TimeoutSettings().timeout(timeout);
  }

  protected double navigationTimeout(Double timeout) {
    if (page != null) {
      return page.timeoutSettings.navigationTimeout(timeout);
    }
    return new TimeoutSettings().navigationTimeout(timeout);
  }

  FrameExpectResult expect(String expression, FrameExpectOptions options, String title) {
    return withTitle(title, () -> expect(expression, options));
  }

  FrameExpectResult expect(String expression, FrameExpectOptions options) {
    FrameExpectParams params = toProtocol(this, options, FrameExpectParams.class);
    params.expression = expression;
    FrameExpectResult result = new FrameExpectResult();
    try {
      channel.expect(params, options.timeout);
      result.matches = !options.isNot;
    } catch (ServerErrorWithDetails e) {
      FrameExpectErrorDetails details = gson().fromJson(e.errorDetails(), FrameExpectErrorDetails.class);
      result.matches = options.isNot;
      result.received = details.received;
      result.errorMessage = details.customErrorMessage == null ? null : "Error: " + details.customErrorMessage;
      result.log = e.log();
    }
    return result;
  }
}
