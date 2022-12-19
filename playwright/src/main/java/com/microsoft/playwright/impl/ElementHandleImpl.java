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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ElementState;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.SelectOption;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.*;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.impl.Utils.addLargeFileUploadParams;
import static com.microsoft.playwright.options.ScreenshotType.JPEG;
import static com.microsoft.playwright.options.ScreenshotType.PNG;

public class ElementHandleImpl extends JSHandleImpl implements ElementHandle {
  ElementHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public ElementHandle asElement() {
    return this;
  }

  @Override
  public ElementHandle querySelector(String selector) {
    return withLogging("ElementHandle.querySelector", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("selector", selector);
      JsonElement json = sendMessage("querySelector", params);
      JsonObject element = json.getAsJsonObject().getAsJsonObject("element");
      if (element == null) {
        return null;
      }
      return connection.getExistingObject(element.get("guid").getAsString());
    });
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return withLogging("ElementHandle.<", () -> {
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
    });
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    return withLogging("ElementHandle.evalOnSelector", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("selector", selector);
      params.addProperty("expression", pageFunction);
      params.add("arg", gson().toJsonTree(serializeArgument(arg)));
      JsonElement json = sendMessage("evalOnSelector", params);
      SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
      return deserialize(value);
    });
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return withLogging("ElementHandle.evalOnSelectorAll", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("selector", selector);
      params.addProperty("expression", pageFunction);
      params.add("arg", gson().toJsonTree(serializeArgument(arg)));
      JsonElement json = sendMessage("evalOnSelectorAll", params);
      SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
      return deserialize(value);
    });
  }

  @Override
  public BoundingBox boundingBox() {
    return withLogging("ElementHandle.boundingBox", () -> {
      JsonObject json = sendMessage("boundingBox").getAsJsonObject();
      if (!json.has("value")) {
        return null;
      }
      return gson().fromJson(json.get("value"), BoundingBox.class);
    });
  }

  @Override
  public void check(CheckOptions options) {
    withLogging("ElementHandle.check", () -> checkImpl(options));
  }

  private void checkImpl(CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("check", params);
  }

  @Override
  public void click(ClickOptions options) {
    withLogging("ElementHandle.click", () -> clickImpl(options));
  }

  private void clickImpl(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("click", params);
  }

  @Override
  public Frame contentFrame() {
    return withLogging("ElementHandle.contentFrame", () -> contentFrameImpl());
  }

  private Frame contentFrameImpl() {
    JsonObject json = sendMessage("contentFrame").getAsJsonObject();
    if (!json.has("frame")) {
      return null;
    }
    return connection.getExistingObject(json.getAsJsonObject("frame").get("guid").getAsString());
  }

  @Override
  public void dblclick(DblclickOptions options) {
    withLogging("ElementHandle.dblclick", () -> dblclickImpl(options));
  }

  private void dblclickImpl(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("dblclick", params);
  }

  @Override
  public void dispatchEvent(String type, Object eventInit) {
    withLogging("ElementHandle.dispatchEvent", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("type", type);
      params.add("eventInit", gson().toJsonTree(serializeArgument(eventInit)));
      sendMessage("dispatchEvent", params);
    });
  }

  @Override
  public void fill(String value, FillOptions options) {
    withLogging("ElementHandle.fill", () -> fillImpl(value, options));
  }

  private void fillImpl(String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("value", value);
    sendMessage("fill", params);
  }

  @Override
  public void focus() {
    withLogging("ElementHandle.focus", () -> sendMessage("focus"));
  }

  @Override
  public String getAttribute(String name) {
    return withLogging("ElementHandle.getAttribute", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("name", name);
      JsonObject json = sendMessage("getAttribute", params).getAsJsonObject();
      return json.has("value") ? json.get("value").getAsString() : null;
    });
  }

  @Override
  public void hover(HoverOptions options) {
    withLogging("ElementHandle.hover", () -> hoverImpl(options));
  }

  private void hoverImpl(HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("hover", params);
  }

  @Override
  public String innerHTML() {
    return withLogging("ElementHandle.innerHTML", () -> {
      JsonObject json = sendMessage("innerHTML").getAsJsonObject();
      return json.get("value").getAsString();
    });
  }

  @Override
  public String innerText() {
    return withLogging("ElementHandle.innerText", () -> {
      JsonObject json = sendMessage("innerText").getAsJsonObject();
      return json.get("value").getAsString();
    });
  }

  @Override
  public String inputValue(InputValueOptions options) {
    return withLogging("ElementHandle.inputValue", () -> inputValueImpl(options));
  }

  private String inputValueImpl(InputValueOptions options) {
    if (options == null) {
      options = new InputValueOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = sendMessage("inputValue", params).getAsJsonObject();
    return json.get("value").getAsString();
  }

  @Override
  public boolean isChecked() {
    return withLogging("ElementHandle.isChecked", () -> {
      JsonObject json = sendMessage("isChecked").getAsJsonObject();
      return json.get("value").getAsBoolean();
    });
  }

  @Override
  public boolean isDisabled() {
    return withLogging("ElementHandle.isDisabled", () -> {
      JsonObject json = sendMessage("isDisabled").getAsJsonObject();
      return json.get("value").getAsBoolean();
    });
  }

  @Override
  public boolean isEditable() {
    return withLogging("ElementHandle.isEditable", () -> {
      JsonObject json = sendMessage("isEditable").getAsJsonObject();
      return json.get("value").getAsBoolean();
    });
  }

  @Override
  public boolean isEnabled() {
    return withLogging("ElementHandle.isEnabled", () -> {
      JsonObject json = sendMessage("isEnabled").getAsJsonObject();
      return json.get("value").getAsBoolean();
    });
  }

  @Override
  public boolean isHidden() {
    return withLogging("ElementHandle.isHidden", () -> {
      JsonObject json = sendMessage("isHidden").getAsJsonObject();
      return json.get("value").getAsBoolean();
    });
  }

  @Override
  public boolean isVisible() {
    return withLogging("ElementHandle.isVisible", () -> {
      JsonObject json = sendMessage("isVisible").getAsJsonObject();
      return json.get("value").getAsBoolean();
    });
  }

  @Override
  public FrameImpl ownerFrame() {
    return withLogging("ElementHandle.ownerFrame", () -> {
      JsonObject json = sendMessage("ownerFrame").getAsJsonObject();
      if (!json.has("frame")) {
        return null;
      }
      return connection.getExistingObject(json.getAsJsonObject("frame").get("guid").getAsString());
    });
  }

  @Override
  public void press(String key, PressOptions options) {
    withLogging("ElementHandle.press", () -> pressImpl(key, options));
  }
  private void pressImpl(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("key", key);
    sendMessage("press", params);
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return withLogging("ElementHandle.screenshot", () -> screenshotImpl(options));
  }

  private byte[] screenshotImpl(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    if (options.type == null) {
      options.type = PNG;
      if (options.path != null) {
        String fileName = options.path.getFileName().toString();
        int extStart = fileName.lastIndexOf('.');
        if (extStart != -1) {
          String extension = fileName.substring(extStart).toLowerCase();
          if (".jpeg".equals(extension) || ".jpg".equals(extension)) {
            options.type = JPEG;
          }
        }
      }
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("path");
    JsonObject json = sendMessage("screenshot", params).getAsJsonObject();

    byte[] buffer = Base64.getDecoder().decode(json.get("binary").getAsString());
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    withLogging("ElementHandle.scrollIntoViewIfNeeded", () -> scrollIntoViewIfNeededImpl(options));
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (values != null) {
      params.add("options", toSelectValueOrLabel(values));
    }
    return selectOption(params);
  }

  @Override
  public List<String> selectOption(SelectOption value, SelectOptionOptions options) {
    SelectOption[] values = value == null ? null : new SelectOption[]{ value };
    return selectOption(values, options);
  }

  private void scrollIntoViewIfNeededImpl(ScrollIntoViewIfNeededOptions options) {
    if (options == null) {
      options = new ScrollIntoViewIfNeededOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("scrollIntoViewIfNeeded", params);
  }

  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (values != null) {
      params.add("options", gson().toJsonTree(values));
    }
    return selectOption(params);
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (values != null) {
      params.add("elements", Serialization.toProtocol(values));
    }
    return selectOption(params);
  }

  private List<String> selectOption(JsonObject params) {
    return withLogging("SelectOption", () -> {
      JsonObject json = sendMessage("selectOption", params).getAsJsonObject();
      return parseStringList(json.getAsJsonArray("values"));
    });
  }

  @Override
  public void selectText(SelectTextOptions options) {
    withLogging("ElementHandle.selectText", () -> selectTextImpl(options));
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

  private void selectTextImpl(SelectTextOptions options) {
    if (options == null) {
      options = new SelectTextOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("selectText", params);
  }

  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    withLogging("ElementHandle.setInputFiles", () -> setInputFilesImpl(files, options));
  }

  void setInputFilesImpl(Path[] files, SetInputFilesOptions options) {
    FrameImpl frame = ownerFrame();
    if (frame == null) {
      throw new Error("Cannot set input files to detached element");
    }
    if (hasLargeFile(files)) {
      if (options == null) {
        options = new SetInputFilesOptions();
      }
      JsonObject params = gson().toJsonTree(options).getAsJsonObject();
      addLargeFileUploadParams(files, params, frame.page().context());
      sendMessage("setInputFilePaths", params);
    } else {
      setInputFilesImpl(Utils.toFilePayloads(files), options);
    }
  }

  @Override
  public void setInputFiles(FilePayload files, SetInputFilesOptions options) {
    setInputFiles(new FilePayload[]{files}, options);
  }

  @Override
  public void setInputFiles(FilePayload[] files, SetInputFilesOptions options) {
    withLogging("ElementHandle.setInputFiles", () -> setInputFilesImpl(files, options));
  }

  void setInputFilesImpl(FilePayload[] files, SetInputFilesOptions options) {
    checkFilePayloadSize(files);
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.add("files", Serialization.toJsonArray(files));
    sendMessage("setInputFiles", params);
  }

  @Override
  public void tap(TapOptions options) {
    withLogging("ElementHandle.tap", () -> tapImpl(options));
  }

  private void tapImpl(TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("tap", params);
  }

  @Override
  public String textContent() {
    return withLogging("ElementHandle.textContent", () -> textContentImpl());
  }

  private String textContentImpl() {
    return withLogging("ElementHandle.textContent", () -> {
      JsonObject json = sendMessage("textContent").getAsJsonObject();
      return json.has("value") ? json.get("value").getAsString() : null;
    });
  }

  @Override
  public void type(String text, TypeOptions options) {
    withLogging("ElementHandle.type", () -> typeImpl(text, options));
  }

  private void typeImpl(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("text", text);
    sendMessage("type", params);
  }

  @Override
  public void uncheck(UncheckOptions options) {
    withLogging("ElementHandle.uncheck", () -> uncheckImpl(options));
  }

  private void uncheckImpl(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("uncheck", params);
  }

  @Override
  public void waitForElementState(ElementState state, WaitForElementStateOptions options) {
    withLogging("ElementHandle.waitForElementState", () -> waitForElementStateImpl(state, options));
  }

  private void waitForElementStateImpl(ElementState state, WaitForElementStateOptions options) {
    if (options == null) {
      options = new WaitForElementStateOptions();
    }
    if (state == null) {
      throw new IllegalArgumentException("State cannot be null");
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("state", toProtocol(state));
    sendMessage("waitForElementState", params);
  }

  private static String toProtocol(ElementState state) {
    return state.toString().toLowerCase();
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return withLogging("ElementHandle.waitForSelector", () -> waitForSelectorImpl(selector, options));
  }

  private ElementHandle waitForSelectorImpl(String selector, WaitForSelectorOptions options) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("selector", selector);
    JsonElement json = sendMessage("waitForSelector", params);
    JsonObject element = json.getAsJsonObject().getAsJsonObject("element");
    if (element == null) {
      return null;
    }
    return connection.getExistingObject(element.get("guid").getAsString());
  }
}
