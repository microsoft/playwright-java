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

  enum InternalEventType { NAVIGATED, LOADSTATE }
  private final ListenerCollection<InternalEventType> internalListeners = new ListenerCollection<>();
  PageImpl page;
  boolean isDetached;

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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonElement json = sendMessage("querySelector", params);
    JsonObject element = json.getAsJsonObject().getAsJsonObject("element");
    if (element == null) {
      return null;
    }
    return connection.getExistingObject(element.get("guid").getAsString());
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    JsonElement json = sendMessage("querySelectorAll", params);
    JsonArray elements = json.getAsJsonObject().getAsJsonArray("elements");
    if (elements == null) {
      return null;
    }
    List<ElementHandle> handles = new ArrayList<>();
    for (JsonElement item : elements) {
      handles.add(connection.getExistingObject(item.getAsJsonObject().get("guid").getAsString()));
    }
    return handles;
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("expression", pageFunction);
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evalOnSelector", params);
    SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return evalOnSelectorAllImpl(selector, pageFunction, arg);
  }

  Object evalOnSelectorAllImpl(String selector, String pageFunction, Object arg) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    params.addProperty("expression", pageFunction);
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evalOnSelectorAll", params);
    SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options){
    return addScriptTagImpl(options);
  }

  ElementHandle addScriptTagImpl(AddScriptTagOptions options) {
    if (options == null) {
      options = new AddScriptTagOptions();
    }
    JsonObject jsonOptions = gson().toJsonTree(options).getAsJsonObject();
    if (options.path != null) {
      jsonOptions.remove("path");
      byte[] encoded;
      try {
        encoded = Files.readAllBytes(options.path);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
      String content = new String(encoded, StandardCharsets.UTF_8);
      content = addSourceUrlToScript(content, options.path);
      jsonOptions.addProperty("content", content);
    }
    JsonElement json = sendMessage("addScriptTag", jsonOptions);
    return connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("element").get("guid").getAsString());
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options){
    return addStyleTagImpl(options);
  }

  ElementHandle addStyleTagImpl(AddStyleTagOptions options) {
    if (options == null) {
      options = new AddStyleTagOptions();
    }
    JsonObject jsonOptions = gson().toJsonTree(options).getAsJsonObject();
    if (options.path != null) {
      jsonOptions.remove("path");
      byte[] encoded;
      try {
        encoded = Files.readAllBytes(options.path);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
      String content = new String(encoded, StandardCharsets.UTF_8);
      content += "/*# sourceURL=" + options.path.toString().replace("\n", "") + "*/";
      jsonOptions.addProperty("content", content);
    }
    JsonElement json = sendMessage("addStyleTag", jsonOptions);
    return connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("element").get("guid").getAsString());
  }

  @Override
  public void check(String selector, CheckOptions options){
    if (options == null) {
      options = new CheckOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("check", params);
  }

  @Override
  public List<Frame> childFrames() {
    return new ArrayList<>(childFrames);
  }

  @Override
  public void click(String selector, ClickOptions options) {
    clickImpl(selector, options);
  }

  void clickImpl(String selector, ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("click", params);
  }

  @Override
  public String content() {
    return sendMessage("content").getAsJsonObject().get("value").getAsString();
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("dblclick", params);
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    if (options == null) {
      options = new DispatchEventOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("type", type);
    params.add("eventInit", gson().toJsonTree(serializeArgument(eventInit)));
    sendMessage("dispatchEvent", params);
  }

  @Override
  public Object evaluate(String expression, Object arg) {
    JsonObject params = new JsonObject();
    params.addProperty("expression", expression);
    params.addProperty("world", "main");
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evaluateExpression", params);
    SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    JsonObject params = new JsonObject();
    params.addProperty("expression", pageFunction);
    params.addProperty("world", "main");
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evaluateExpressionHandle", params);
    return connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("handle").get("guid").getAsString());
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("value", value);
    sendMessage("fill", params);
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    if (options == null) {
      options = new FocusOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("focus", params);
  }

  @Override
  public ElementHandle frameElement() {
    JsonObject json = sendMessage("frameElement").getAsJsonObject();
    return connection.getExistingObject(json.getAsJsonObject("element").get("guid").getAsString());
  }

  @Override
  public FrameLocator frameLocator(String selector) {
    return new FrameLocatorImpl(this, selector);
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
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("name", name);
    JsonObject json = sendMessage("getAttribute", params).getAsJsonObject();
    if (json.has("value")) {
      return json.get("value").getAsString();
    }
    return null;
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    return navigateImpl(url, options);
  }

  ResponseImpl navigateImpl(String url, NavigateOptions options) {
    if (options == null) {
      options = new NavigateOptions();
    }
    options.timeout = navigationTimeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("url", url);
    JsonElement result = sendMessage("goto", params);
    JsonObject jsonResponse = result.getAsJsonObject().getAsJsonObject("response");
    if (jsonResponse == null) {
      return null;
    }
    return connection.getExistingObject(jsonResponse.get("guid").getAsString());
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    hoverImpl(selector, options);
  }

  void hoverImpl(String selector, HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("hover", params);
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    dragAndDropImpl(source, target, options);
  }

  void dragAndDropImpl(String source, String target, DragAndDropOptions options) {
    if (options == null) {
      options = new DragAndDropOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("source", source);
    params.addProperty("target", target);
    sendMessage("dragAndDrop", params);
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return innerHTMLImpl(selector, options);
  }

  String innerHTMLImpl(String selector, InnerHTMLOptions options) {
    if (options == null) {
      options = new InnerHTMLOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("innerHTML", params).getAsJsonObject();
    return json.get("value").getAsString();
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return innerTextImpl(selector, options);
  }

  String innerTextImpl(String selector, InnerTextOptions options) {
    if (options == null) {
      options = new InnerTextOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("innerText", params).getAsJsonObject();
    return json.get("value").getAsString();
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    return inputValueImpl(selector, options);
  }

  String inputValueImpl(String selector, InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("inputValue", params).getAsJsonObject();
    return json.get("value").getAsString();
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return isCheckedImpl(selector, options);
  }

  boolean isCheckedImpl(String selector, IsCheckedOptions options) {
    if (options == null) {
      options = new IsCheckedOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("isChecked", params).getAsJsonObject();
    return json.get("value").getAsBoolean();
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
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("isDisabled", params).getAsJsonObject();
    return json.get("value").getAsBoolean();
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return isEditableImpl(selector, options);
  }

  boolean isEditableImpl(String selector, IsEditableOptions options) {
    if (options == null) {
      options = new IsEditableOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("isEditable", params).getAsJsonObject();
    return json.get("value").getAsBoolean();
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return isEnabledImpl(selector, options);
  }

  boolean isEnabledImpl(String selector, IsEnabledOptions options) {
    if (options == null) {
      options = new IsEnabledOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("isEnabled", params).getAsJsonObject();
    return json.get("value").getAsBoolean();
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return isHiddenImpl(selector, options);
  }

  boolean isHiddenImpl(String selector, IsHiddenOptions options) {
    if (options == null) {
      options = new IsHiddenOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("isHidden", params).getAsJsonObject();
    return json.get("value").getAsBoolean();
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonObject json = sendMessage("isVisible", params).getAsJsonObject();
    return json.get("value").getAsBoolean();
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
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("key", key);
    sendMessage("press", params);
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  List<String> selectOptionImpl(String selector, SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    if (values != null) {
      params.add("options", gson().toJsonTree(values));
    }
    return selectOption(params);
  }

  List<String> selectOptionImpl(String selector, String[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    if (values != null) {
      params.add("options", toSelectValueOrLabel(values));
    }
    return selectOption(params);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return selectOptionImpl(selector, values, options);
  }

  List<String> selectOptionImpl(String selector, ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    if (values != null) {
      params.add("elements", Serialization.toProtocol(values));
    }
    return selectOption(params);
  }

  private List<String> selectOption(JsonObject params) {
    JsonObject json = sendMessage("selectOption", params).getAsJsonObject();
    return parseStringList(json.getAsJsonArray("values"));
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
    options.timeout = navigationTimeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("html", html);
    sendMessage("setContent", params);
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
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    addFilePathUploadParams(files, params, page.context());
    params.addProperty("selector", selector);
    sendMessage("setInputFiles", params);
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
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.add("payloads", toJsonArray(files));
    sendMessage("setInputFiles", params);
  }

  @Override
  public void tap(String selector, TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("tap", params);
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    if (options == null) {
      options = new TextContentOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    return sendMessage("textContent", params).getAsJsonObject().get("value").getAsString();
  }

  @Override
  public String title() {
    JsonElement json = sendMessage("title");
    return json.getAsJsonObject().get("value").getAsString();
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("text", text);
    sendMessage("type", params);
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    sendMessage("uncheck", params);
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
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("expression", pageFunction);
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("waitForFunction", params);
    JsonObject element = json.getAsJsonObject().getAsJsonObject("handle");
    return connection.getExistingObject(element.get("guid").getAsString());
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    withWaitLogging("Frame.waitForLoadState", logger -> {
      waitForLoadStateImpl(state, options, logger);
      return null;
    });
  }

  void waitForLoadStateImpl(LoadState state, WaitForLoadStateOptions options, Logger logger) {
    waitForLoadStateImpl(convertType(state, WaitUntilState.class), options, logger);
  }

  private void waitForLoadStateImpl(WaitUntilState state, WaitForLoadStateOptions options, Logger logger) {
    if (options == null) {
      options = new WaitForLoadStateOptions();
    }
    if (state == null) {
      state = LOAD;
    }

    List<Waitable<Void>> waitables = new ArrayList<>();
    waitables.add(new WaitForLoadStateHelper(state, logger));
    waitables.add(page.createWaitForCloseHelper());
    waitables.add(page.createWaitableTimeout(options.timeout));
    runUntil(() -> {}, new WaitableRace<>(waitables));
  }

  private class WaitForLoadStateHelper implements Waitable<Void>, Consumer<WaitUntilState> {
    private final WaitUntilState expectedState;
    private final Logger logger;
    private boolean isDone;

    WaitForLoadStateHelper(WaitUntilState state, Logger logger) {
      expectedState = state;
      this.logger = logger;
      isDone = loadStates.contains(state);
      if (!isDone) {
        internalListeners.add(InternalEventType.LOADSTATE, this);
      }
    }

    @Override
    public void accept(WaitUntilState state) {
      logger.log("  load state changed to " + state);
      if (expectedState.equals(state)) {
        isDone = true;
        dispose();
      }
    }

    public void dispose() {
      internalListeners.remove(InternalEventType.LOADSTATE, this);
    }

    public boolean isDone() {
      return isDone;
    }

    @Override
    public Void get() {
      return null;
    }
  }

  private class WaitForNavigationHelper implements Waitable<Response>, Consumer<JsonObject> {
    private final UrlMatcher matcher;
    private final WaitUntilState expectedLoadState;
    private final Logger logger;
    private WaitForLoadStateHelper loadStateHelper;

    private RequestImpl request;
    private RuntimeException exception;

    WaitForNavigationHelper(UrlMatcher matcher, WaitUntilState expectedLoadState, Logger logger) {
      this.matcher = matcher;
      this.expectedLoadState = expectedLoadState;
      this.logger = logger;
      internalListeners.add(InternalEventType.NAVIGATED, this);
    }

    @Override
    public void accept(JsonObject params) {
      String url = params.get("url").getAsString();
      logger.log("  navigated to " + url);
      if (!matcher.test(url)) {
        return;
      }
      if (params.has("error")) {
        exception = new PlaywrightException(params.get("error").getAsString());
      } else {
        if (params.has("newDocument")) {
          JsonObject jsonReq = params.getAsJsonObject("newDocument").getAsJsonObject("request");
          if (jsonReq != null) {
            request = connection.getExistingObject(jsonReq.get("guid").getAsString());
          }
        }
        loadStateHelper = new WaitForLoadStateHelper(expectedLoadState, logger);
      }
      internalListeners.remove(InternalEventType.NAVIGATED, this);
    }

    @Override
    public void dispose() {
      internalListeners.remove(InternalEventType.NAVIGATED, this);
      if (loadStateHelper != null) {
        loadStateHelper.dispose();
      }
    }

    @Override
    public boolean isDone() {
      if (exception != null) {
        return true;
      }
      if (loadStateHelper != null) {
        return loadStateHelper.isDone();
      }
      return false;
    }

    @Override
    public Response get() {
      if (exception != null) {
        throw exception;
      }

      if (request == null) {
        return null;
      }
      return request.finalRequest().response();
    }
  }

  @Override
  public Response waitForNavigation(WaitForNavigationOptions options, Runnable code) {
    return withWaitLogging("Frame.waitForNavigation", logger -> waitForNavigationImpl(logger, code, options, null));
  }

  Response waitForNavigationImpl(Logger logger, Runnable code, WaitForNavigationOptions options) {
    return waitForNavigationImpl(logger, code, options, null);
  }

  private Response waitForNavigationImpl(Logger logger, Runnable code, WaitForNavigationOptions options, UrlMatcher matcher) {
    if (options == null) {
      options = new WaitForNavigationOptions();
    }
    if (options.waitUntil == null) {
      options.waitUntil = WaitUntilState.LOAD;
    }

    List<Waitable<Response>> waitables = new ArrayList<>();
    if (matcher == null) {
      matcher = UrlMatcher.forOneOf(page.context().baseUrl(), options.url, this.connection.localUtils, false);
    }
    logger.log("waiting for navigation " + matcher);
    waitables.add(new WaitForNavigationHelper(matcher, options.waitUntil, logger));
    waitables.add(page.createWaitForCloseHelper());
    waitables.add(page.createWaitableFrameDetach(this));
    waitables.add(page.createWaitableNavigationTimeout(options.timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return waitForSelectorImpl(selector, options, false);
  }

  ElementHandle waitForSelectorImpl(String selector, WaitForSelectorOptions options, boolean omitReturnValue) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    options.timeout = timeout(options.timeout);
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    params.addProperty("omitReturnValue", omitReturnValue);
    JsonElement json = sendMessage("waitForSelector", params);
    JsonObject element = json.getAsJsonObject().getAsJsonObject("element");
    if (element == null) {
      return null;
    }
    return connection.getExistingObject(element.get("guid").getAsString());
  }

  @Override
  public void waitForTimeout(double timeout) {
    JsonObject params = new JsonObject();
    params.addProperty("timeout", timeout);
    sendMessage("waitForTimeout", params);
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    waitForURL(UrlMatcher.forGlob(page.context().baseUrl(), url, this.connection.localUtils, false), options);
  }

  @Override
  public void waitForURL(Pattern url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(url), options);
  }

  @Override
  public void waitForURL(Predicate<String> url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(url), options);
  }

  private void waitForURL(UrlMatcher matcher, WaitForURLOptions options) {
    withWaitLogging("Frame.waitForURL", logger -> {
      waitForURLImpl(logger, matcher, options);
      return null;
    });
  }

  void waitForURLImpl(Logger logger, UrlMatcher matcher, WaitForURLOptions options) {
    logger.log("waiting for url " + matcher);
    if (options == null) {
      options = new WaitForURLOptions();
    }
    if (matcher.test(url())) {
      waitForLoadStateImpl(options.waitUntil, convertType(options, WaitForLoadStateOptions.class), logger);
      return;
    }
    waitForNavigationImpl(logger, () -> {}, convertType(options, WaitForNavigationOptions.class), matcher);
  }

  int queryCount(String selector) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    JsonObject result = sendMessage("queryCount", params).getAsJsonObject();
    return result.get("value").getAsInt();
  }

  void highlightImpl(String selector) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    sendMessage("highlight", params);
  }

  protected void handleEvent(String event, JsonObject params) {
    if ("loadstate".equals(event)) {
      JsonElement add = params.get("add");
      if (add != null) {
        WaitUntilState state = loadStateFromProtocol(add.getAsString());
        loadStates.add(state);
        if (parentFrame == null && page != null) {
          if (state == LOAD) {
            page.listeners.notify(PageImpl.EventType.LOAD, page);
          } else if (state == DOMCONTENTLOADED) {
            page.listeners.notify(PageImpl.EventType.DOMCONTENTLOADED, page);
          }
        }
        internalListeners.notify(InternalEventType.LOADSTATE, state);
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
      internalListeners.notify(InternalEventType.NAVIGATED, params);
    }
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
}
