/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ElementState;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.SelectOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.*;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.impl.Utils.addFilePathUploadParams;

public class ElementHandleImpl extends JSHandleImpl implements ElementHandle {
  private final FrameImpl frame;

  // Hides JSHandleImpl.channel on purpose: upstream ElementHandle narrows the type of _channel the same way.
  final ElementHandleChannel channel = new ElementHandleChannel(this);

  ElementHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.frame = (FrameImpl)parent;
  }

  @Override
  public ElementHandle asElement() {
    return this;
  }

  @Override
  public ElementHandle querySelector(String selector) {
    ElementHandleQuerySelectorParams params = new ElementHandleQuerySelectorParams();
    params.selector = selector;
    return channel.querySelector(params, NO_TIMEOUT).element;
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    ElementHandleQuerySelectorAllParams params = new ElementHandleQuerySelectorAllParams();
    params.selector = selector;
    List<ElementHandleImpl> elements = channel.querySelectorAll(params, NO_TIMEOUT).elements;
    if (elements == null) {
      return null;
    }
    return new ArrayList<ElementHandle>(elements);
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    ElementHandleEvalOnSelectorParams params = new ElementHandleEvalOnSelectorParams();
    params.selector = selector;
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evalOnSelector(params, NO_TIMEOUT).value);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    ElementHandleEvalOnSelectorAllParams params = new ElementHandleEvalOnSelectorAllParams();
    params.selector = selector;
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evalOnSelectorAll(params, NO_TIMEOUT).value);
  }

  @Override
  public BoundingBox boundingBox() {
    Rect value = channel.boundingBox(NO_TIMEOUT).value;
    if (value == null) {
      return null;
    }
    BoundingBox box = new BoundingBox();
    box.x = value.x;
    box.y = value.y;
    box.width = value.width;
    box.height = value.height;
    return box;
  }

  @Override
  public void check(CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    ElementHandleCheckParams params = Utils.toProtocol(options, ElementHandleCheckParams.class);
    channel.check(params, frame.timeout(options.timeout));
  }

  @Override
  public void click(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    ElementHandleClickParams params = Utils.toProtocol(options, ElementHandleClickParams.class);
    channel.click(params, frame.timeout(options.timeout));
  }

  @Override
  public Frame contentFrame() {
    return channel.contentFrame(NO_TIMEOUT).frame;
  }

  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    ElementHandleDblclickParams params = Utils.toProtocol(options, ElementHandleDblclickParams.class);
    channel.dblclick(params, frame.timeout(options.timeout));
  }

  @Override
  public void dispatchEvent(String type, Object eventInit) {
    ElementHandleDispatchEventParams params = new ElementHandleDispatchEventParams();
    params.type = type;
    params.eventInit = serializeArgument(eventInit);
    channel.dispatchEvent(params, NO_TIMEOUT);
  }

  @Override
  public void fill(String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    ElementHandleFillParams params = Utils.toProtocol(options, ElementHandleFillParams.class);
    params.value = value;
    channel.fill(params, frame.timeout(options.timeout));
  }

  @Override
  public void focus() {
    channel.focus(NO_TIMEOUT);
  }

  @Override
  public String getAttribute(String name) {
    ElementHandleGetAttributeParams params = new ElementHandleGetAttributeParams();
    params.name = name;
    return channel.getAttribute(params, NO_TIMEOUT).value;
  }

  @Override
  public void hover(HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    ElementHandleHoverParams params = Utils.toProtocol(options, ElementHandleHoverParams.class);
    channel.hover(params, frame.timeout(options.timeout));
  }

  @Override
  public String innerHTML() {
    return channel.innerHTML(NO_TIMEOUT).value;
  }

  @Override
  public String innerText() {
    return channel.innerText(NO_TIMEOUT).value;
  }

  @Override
  public String inputValue(InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    ElementHandleInputValueParams params = Utils.toProtocol(options, ElementHandleInputValueParams.class);
    return channel.inputValue(params, NO_TIMEOUT).value;
  }

  @Override
  public boolean isChecked() {
    return channel.isChecked(NO_TIMEOUT).value;
  }

  @Override
  public boolean isDisabled() {
    return channel.isDisabled(NO_TIMEOUT).value;
  }

  @Override
  public boolean isEditable() {
    return channel.isEditable(NO_TIMEOUT).value;
  }

  @Override
  public boolean isEnabled() {
    return channel.isEnabled(NO_TIMEOUT).value;
  }

  @Override
  public boolean isHidden() {
    return channel.isHidden(NO_TIMEOUT).value;
  }

  @Override
  public boolean isVisible() {
    return channel.isVisible(NO_TIMEOUT).value;
  }

  @Override
  public FrameImpl ownerFrame() {
    return channel.ownerFrame(NO_TIMEOUT).frame;
  }

  @Override
  public void press(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    ElementHandlePressParams params = Utils.toProtocol(options, ElementHandlePressParams.class);
    params.key = key;
    channel.press(params, frame.timeout(options.timeout));
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    if (options.type == null && options.path != null) {
      options.type = Utils.screenshotTypeForPath(options.path);
    }
    // The mask locators cannot be read back through Gson, they are converted below.
    JsonObject json = gson().toJsonTree(options).getAsJsonObject();
    json.remove("mask");
    ElementHandleScreenshotParams params = gson().fromJson(json, ElementHandleScreenshotParams.class);
    if (options.mask != null) {
      params.mask = new ArrayList<>();
      for (Locator locator : options.mask) {
        ElementHandleScreenshotParams.ElementHandleScreenshotParamsMask item = new ElementHandleScreenshotParams.ElementHandleScreenshotParamsMask();
        item.frame = ((LocatorImpl) locator).frame;
        item.selector = ((LocatorImpl) locator).selector;
        params.mask.add(item);
      }
    }
    byte[] buffer = channel.screenshot(params, frame.timeout(options.timeout)).binary;
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    if (options == null) {
      options = new ScrollIntoViewIfNeededOptions();
    }
    ElementHandleScrollIntoViewIfNeededParams params = Utils.toProtocol(options, ElementHandleScrollIntoViewIfNeededParams.class);
    channel.scrollIntoViewIfNeeded(params, frame.timeout(options.timeout));
  }

  @Override
  public List<String> selectOption(String value, SelectOptionOptions options) {
    String[] values = value == null ? null : new String[]{ value };
    return selectOption(values, options);
  }

  @Override
  public List<String> selectOption(ElementHandle value, SelectOptionOptions options) {
    ElementHandle[] values = value == null ? null : new ElementHandle[]{ value };
    return selectOption(values, options);
  }

  @Override
  public List<String> selectOption(String[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    ElementHandleSelectOptionParams params = Utils.toProtocol(options, ElementHandleSelectOptionParams.class);
    if (values != null) {
      params.options = new ArrayList<>();
      for (String value : values) {
        ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions option = new ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions();
        option.valueOrLabel = value;
        params.options.add(option);
      }
    }
    return selectOption(params, options.timeout);
  }

  @Override
  public List<String> selectOption(SelectOption value, SelectOptionOptions options) {
    SelectOption[] values = value == null ? null : new SelectOption[]{ value };
    return selectOption(values, options);
  }


  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    ElementHandleSelectOptionParams params = Utils.toProtocol(options, ElementHandleSelectOptionParams.class);
    if (values != null) {
      params.options = new ArrayList<>();
      for (SelectOption value : values) {
        ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions option = new ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions();
        option.value = value.value;
        option.label = value.label;
        option.index = value.index;
        params.options.add(option);
      }
    }
    return selectOption(params, options.timeout);
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    ElementHandleSelectOptionParams params = Utils.toProtocol(options, ElementHandleSelectOptionParams.class);
    if (values != null) {
      params.elements = new ArrayList<>();
      for (ElementHandle value : values) {
        params.elements.add((ElementHandleImpl) value);
      }
    }
    return selectOption(params, options.timeout);
  }

  private List<String> selectOption(ElementHandleSelectOptionParams params, Double timeout) {
    return channel.selectOption(params, frame.timeout(timeout)).values;
  }

  @Override
  public void selectText(SelectTextOptions options) {
    if (options == null) {
      options = new SelectTextOptions();
    }
    ElementHandleSelectTextParams params = Utils.toProtocol(options, ElementHandleSelectTextParams.class);
    channel.selectText(params, frame.timeout(options.timeout));
  }

  @Override
  public void setChecked(boolean checked, SetCheckedOptions options) {
    if (checked) {
      check(convertType(options, CheckOptions.class));
    } else {
      uncheck(convertType(options, UncheckOptions.class));
    }
  }

  @Override
  public void setInputFiles(Path files, SetInputFilesOptions options) {
    setInputFiles(new Path[]{files}, options);
  }


  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    FrameImpl frame = ownerFrame();
    if (frame == null) {
      throw new Error("Cannot set input files to detached element");
    }
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    ElementHandleSetInputFilesParams params = Utils.toProtocol(options, ElementHandleSetInputFilesParams.class);
    Utils.UploadParams upload = addFilePathUploadParams(files, frame.page().context());
    if (upload.emptyPayloads) {
      params.payloads = new ArrayList<>();
    }
    params.localDirectory = upload.localDirectory;
    params.directoryStream = upload.directoryStream;
    params.localPaths = upload.localPaths;
    params.streams = upload.streams;
    channel.setInputFiles(params, frame.timeout(options.timeout));
  }

  @Override
  public void setInputFiles(FilePayload files, SetInputFilesOptions options) {
    setInputFiles(new FilePayload[]{files}, options);
  }

  @Override
  public void setInputFiles(FilePayload[] files, SetInputFilesOptions options) {
    checkFilePayloadSize(files);
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    ElementHandleSetInputFilesParams params = Utils.toProtocol(options, ElementHandleSetInputFilesParams.class);
    params.payloads = new ArrayList<>();
    for (FilePayload file : files) {
      ElementHandleSetInputFilesParams.ElementHandleSetInputFilesParamsPayloads payload = new ElementHandleSetInputFilesParams.ElementHandleSetInputFilesParamsPayloads();
      payload.name = file.name;
      payload.mimeType = file.mimeType;
      payload.buffer = file.buffer;
      params.payloads.add(payload);
    }
    channel.setInputFiles(params, frame.timeout(options.timeout));
  }

  @Override
  public void tap(TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    ElementHandleTapParams params = Utils.toProtocol(options, ElementHandleTapParams.class);
    channel.tap(params, frame.timeout(options.timeout));
  }

  @Override
  public String textContent() {
    return channel.textContent(NO_TIMEOUT).value;
  }

  @Override
  public void type(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    ElementHandleTypeParams params = Utils.toProtocol(options, ElementHandleTypeParams.class);
    params.text = text;
    channel.type(params, frame.timeout(options.timeout));
  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    ElementHandleUncheckParams params = Utils.toProtocol(options, ElementHandleUncheckParams.class);
    channel.uncheck(params, frame.timeout(options.timeout));
  }

  @Override
  public void waitForElementState(ElementState state, WaitForElementStateOptions options) {
    if (options == null) {
      options = new WaitForElementStateOptions();
    }
    if (state == null) {
      throw new IllegalArgumentException("State cannot be null");
    }
    ElementHandleWaitForElementStateParams params = Utils.toProtocol(options, ElementHandleWaitForElementStateParams.class);
    params.state = state.toString().toLowerCase();
    channel.waitForElementState(params, frame.timeout(options.timeout));
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    ElementHandleWaitForSelectorParams params = Utils.toProtocol(options, ElementHandleWaitForSelectorParams.class);
    params.selector = selector;
    return channel.waitForSelector(params, frame.timeout(options.timeout)).element;
  }
}
