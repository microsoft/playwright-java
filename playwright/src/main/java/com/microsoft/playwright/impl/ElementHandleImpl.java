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

// Port of elementHandle.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ElementState;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.SelectOption;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.microsoft.playwright.impl.FileUtils.fileUploadSizeLimit;
import static com.microsoft.playwright.impl.isomorphic.MimeType.getMimeTypeForPath;
import static com.microsoft.playwright.impl.Serialization.deserialize;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class ElementHandleImpl extends JSHandleImpl implements ElementHandle {
  FrameImpl frame;
  final ElementHandleChannel elementChannel = new ElementHandleChannel(this);

  // java: no from and fromNullable, generated results already hold the ElementHandleImpl.

  ElementHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.frame = (FrameImpl) parent;
  }

  @Override
  public ElementHandle asElement() {
    return this;
  }

  @Override
  public FrameImpl ownerFrame() {
    return elementChannel.ownerFrame(NO_TIMEOUT).frame;
  }

  @Override
  public FrameImpl contentFrame() {
    return elementChannel.contentFrame(NO_TIMEOUT).frame;
  }

  @Override
  public String getAttribute(String name) {
    ElementHandleGetAttributeParams params = new ElementHandleGetAttributeParams();
    params.name = name;
    return elementChannel.getAttribute(params, NO_TIMEOUT).value;
  }

  // java: InputValueOptions has a timeout; like upstream, it is not used.
  @Override
  public String inputValue(InputValueOptions options) {
    return elementChannel.inputValue(NO_TIMEOUT).value;
  }

  @Override
  public String textContent() {
    return elementChannel.textContent(NO_TIMEOUT).value;
  }

  @Override
  public String innerText() {
    return elementChannel.innerText(NO_TIMEOUT).value;
  }

  @Override
  public String innerHTML() {
    return elementChannel.innerHTML(NO_TIMEOUT).value;
  }

  @Override
  public boolean isChecked() {
    return elementChannel.isChecked(NO_TIMEOUT).value;
  }

  @Override
  public boolean isDisabled() {
    return elementChannel.isDisabled(NO_TIMEOUT).value;
  }

  @Override
  public boolean isEditable() {
    return elementChannel.isEditable(NO_TIMEOUT).value;
  }

  @Override
  public boolean isEnabled() {
    return elementChannel.isEnabled(NO_TIMEOUT).value;
  }

  @Override
  public boolean isHidden() {
    return elementChannel.isHidden(NO_TIMEOUT).value;
  }

  @Override
  public boolean isVisible() {
    return elementChannel.isVisible(NO_TIMEOUT).value;
  }

  @Override
  public void dispatchEvent(String type, Object eventInit) {
    if (eventInit == null) {
      eventInit = Collections.emptyMap();
    }
    ElementHandleDispatchEventParams params = new ElementHandleDispatchEventParams();
    params.type = type;
    params.eventInit = serializeArgument(eventInit);
    elementChannel.dispatchEvent(params, NO_TIMEOUT);
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    if (options == null) {
      options = new ScrollIntoViewIfNeededOptions();
    }
    ElementHandleScrollIntoViewIfNeededParams params = toProtocol(options, ElementHandleScrollIntoViewIfNeededParams.class);
    elementChannel.scrollIntoViewIfNeeded(params, frame.timeout(options.timeout));
  }

  @Override
  public void hover(HoverOptions options) {
    if (options == null) {
      options = new HoverOptions();
    }
    ElementHandleHoverParams params = toProtocol(options, ElementHandleHoverParams.class);
    elementChannel.hover(params, frame.timeout(options.timeout));
  }

  @Override
  public void click(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    ElementHandleClickParams params = toProtocol(options, ElementHandleClickParams.class);
    elementChannel.click(params, frame.timeout(options.timeout));
  }

  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    ElementHandleDblclickParams params = toProtocol(options, ElementHandleDblclickParams.class);
    elementChannel.dblclick(params, frame.timeout(options.timeout));
  }

  @Override
  public void tap(TapOptions options) {
    if (options == null) {
      options = new TapOptions();
    }
    ElementHandleTapParams params = toProtocol(options, ElementHandleTapParams.class);
    elementChannel.tap(params, frame.timeout(options.timeout));
  }

  // java: one overload per value type of the public API, all delegating to selectOption(Object, ...).
  @Override
  public List<String> selectOption(String values, SelectOptionOptions options) {
    return selectOption((Object) values, options);
  }

  @Override
  public List<String> selectOption(ElementHandle values, SelectOptionOptions options) {
    return selectOption((Object) values, options);
  }

  @Override
  public List<String> selectOption(String[] values, SelectOptionOptions options) {
    return selectOption((Object) values, options);
  }

  @Override
  public List<String> selectOption(SelectOption values, SelectOptionOptions options) {
    return selectOption((Object) values, options);
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    return selectOption((Object) values, options);
  }

  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    return selectOption((Object) values, options);
  }

  private List<String> selectOption(Object values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    ElementHandleSelectOptionParams converted = convertSelectOptionValues(values);
    ElementHandleSelectOptionParams params = toProtocol(options, ElementHandleSelectOptionParams.class);
    params.elements = converted.elements;
    params.options = converted.options;
    ElementHandleSelectOptionResult result = elementChannel.selectOption(params, frame.timeout(options.timeout));
    return result.values;
  }

  @Override
  public void fill(String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    ElementHandleFillParams params = toProtocol(options, ElementHandleFillParams.class);
    params.value = value;
    elementChannel.fill(params, frame.timeout(options.timeout));
  }

  @Override
  public void selectText(SelectTextOptions options) {
    if (options == null) {
      options = new SelectTextOptions();
    }
    ElementHandleSelectTextParams params = toProtocol(options, ElementHandleSelectTextParams.class);
    elementChannel.selectText(params, frame.timeout(options.timeout));
  }

  // java: one overload per file type of the public API, all delegating to setInputFiles(Object, ...).
  @Override
  public void setInputFiles(Path files, SetInputFilesOptions options) {
    setInputFiles((Object) files, options);
  }

  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    setInputFiles((Object) files, options);
  }

  @Override
  public void setInputFiles(FilePayload files, SetInputFilesOptions options) {
    setInputFiles((Object) files, options);
  }

  @Override
  public void setInputFiles(FilePayload[] files, SetInputFilesOptions options) {
    setInputFiles((Object) files, options);
  }

  private void setInputFiles(Object files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    FrameImpl frame = ownerFrame();
    if (frame == null) {
      throw new PlaywrightException("Cannot set input files to detached element");
    }
    ElementHandleSetInputFilesParams converted = convertInputFiles(files, (BrowserContextImpl) frame.page().context());
    elementChannel.setInputFiles(converted, this.frame.timeout(options.timeout));
  }

  @Override
  public void focus() {
    elementChannel.focus(NO_TIMEOUT);
  }

  @Override
  public void type(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    ElementHandleTypeParams params = toProtocol(options, ElementHandleTypeParams.class);
    params.text = text;
    elementChannel.type(params, frame.timeout(options.timeout));
  }

  @Override
  public void press(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    ElementHandlePressParams params = toProtocol(options, ElementHandlePressParams.class);
    params.key = key;
    elementChannel.press(params, frame.timeout(options.timeout));
  }

  @Override
  public void check(CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    ElementHandleCheckParams params = toProtocol(options, ElementHandleCheckParams.class);
    elementChannel.check(params, frame.timeout(options.timeout));
  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    ElementHandleUncheckParams params = toProtocol(options, ElementHandleUncheckParams.class);
    elementChannel.uncheck(params, frame.timeout(options.timeout));
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
  public BoundingBox boundingBox() {
    Rect value = elementChannel.boundingBox(NO_TIMEOUT).value;
    return value == null ? null : convertType(value, BoundingBox.class);
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    // java: toProtocol converts the mask locators to { frame, selector }.
    ElementHandleScreenshotParams copy = toProtocol(this, options, ElementHandleScreenshotParams.class);
    if (copy.type == null) {
      copy.type = determineScreenshotType(options);
    }
    ElementHandleScreenshotResult result = elementChannel.screenshot(copy, frame.timeout(options.timeout));
    if (options.path != null) {
      Utils.writeToFile(result.binary, options.path);
    }
    return result.binary;
  }

  @Override
  public ElementHandle querySelector(String selector) {
    ElementHandleQuerySelectorParams params = new ElementHandleQuerySelectorParams();
    params.selector = selector;
    return elementChannel.querySelector(params, NO_TIMEOUT).element;
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    ElementHandleQuerySelectorAllParams params = new ElementHandleQuerySelectorAllParams();
    params.selector = selector;
    ElementHandleQuerySelectorAllResult result = elementChannel.querySelectorAll(params, NO_TIMEOUT);
    return new ArrayList<ElementHandle>(result.elements);
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    ElementHandleEvalOnSelectorParams params = new ElementHandleEvalOnSelectorParams();
    params.selector = selector;
    params.expression = pageFunction;
    // java: functions are passed as strings, so isFunction is not sent and the server detects it.
    params.arg = serializeArgument(arg);
    ElementHandleEvalOnSelectorResult result = elementChannel.evalOnSelector(params, NO_TIMEOUT);
    return deserialize(result.value);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    ElementHandleEvalOnSelectorAllParams params = new ElementHandleEvalOnSelectorAllParams();
    params.selector = selector;
    params.expression = pageFunction;
    // java: functions are passed as strings, so isFunction is not sent and the server detects it.
    params.arg = serializeArgument(arg);
    ElementHandleEvalOnSelectorAllResult result = elementChannel.evalOnSelectorAll(params, NO_TIMEOUT);
    return deserialize(result.value);
  }

  @Override
  public void waitForElementState(ElementState state, WaitForElementStateOptions options) {
    if (options == null) {
      options = new WaitForElementStateOptions();
    }
    ElementHandleWaitForElementStateParams params = toProtocol(options, ElementHandleWaitForElementStateParams.class);
    params.state = state.toString().toLowerCase();
    elementChannel.waitForElementState(params, frame.timeout(options.timeout));
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    ElementHandleWaitForSelectorParams params = toProtocol(options, ElementHandleWaitForSelectorParams.class);
    params.selector = selector;
    ElementHandleWaitForSelectorResult result = elementChannel.waitForSelector(params, frame.timeout(options.timeout));
    return result.element;
  }

  // java: values is any of String, String[], SelectOption, SelectOption[], ElementHandle, ElementHandle[] or null;
  // the result holds only elements and options, the caller copies them into its own params.
  static ElementHandleSelectOptionParams convertSelectOptionValues(Object values) {
    ElementHandleSelectOptionParams result = new ElementHandleSelectOptionParams();
    if (values == null) {
      return result;
    }
    Object[] items = values instanceof Object[] ? (Object[]) values : new Object[] { values };
    if (items.length == 0) {
      return result;
    }
    for (int i = 0; i < items.length; i++) {
      if (items[i] == null) {
        throw new PlaywrightException("options[" + i + "]: expected object, got null");
      }
    }
    if (items[0] instanceof ElementHandle) {
      result.elements = new ArrayList<>();
      for (Object v : items) {
        result.elements.add((ElementHandleImpl) v);
      }
      return result;
    }
    if (items[0] instanceof String) {
      result.options = new ArrayList<>();
      for (Object valueOrLabel : items) {
        ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions option = new ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions();
        option.valueOrLabel = (String) valueOrLabel;
        result.options.add(option);
      }
      return result;
    }
    result.options = new ArrayList<>();
    for (Object v : items) {
      SelectOption value = (SelectOption) v;
      ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions option = new ElementHandleSelectOptionParams.ElementHandleSelectOptionParamsOptions();
      option.value = value.value;
      option.label = value.label;
      option.index = value.index;
      result.options.add(option);
    }
    return result;
  }

  private static boolean filePayloadExceedsSizeLimit(List<FilePayload> payloads) {
    long size = 0;
    for (FilePayload item : payloads) {
      size += item.buffer != null ? item.buffer.length : 0;
    }
    return size >= fileUploadSizeLimit;
  }

  // java: returns the directory and adds the files to localPaths, which stays empty when there are none.
  private static Path resolvePathsAndDirectoryForInputFiles(List<Object> items, List<Path> localPaths) {
    Path localDirectory = null;
    for (Object item : items) {
      BasicFileAttributes stat = stat((Path) item);
      if (stat.isDirectory()) {
        if (localDirectory != null) {
          throw new PlaywrightException("Multiple directories are not supported");
        }
        localDirectory = ((Path) item).toAbsolutePath().normalize();
      } else {
        localPaths.add(((Path) item).toAbsolutePath().normalize());
      }
    }
    if (!localPaths.isEmpty() && localDirectory != null) {
      throw new PlaywrightException("File paths must be all files or a single directory");
    }
    return localDirectory;
  }

  // java: files is any of Path, Path[], FilePayload or FilePayload[]; a Path stands for upstream's string.
  // The result holds only the file fields, the caller copies them into its own params.
  static ElementHandleSetInputFilesParams convertInputFiles(Object files, BrowserContextImpl context) {
    List<Object> items = files instanceof Object[] ? new ArrayList<>(Arrays.asList((Object[]) files)) : new ArrayList<>(Collections.singletonList(files));

    if (items.stream().anyMatch(item -> item instanceof Path)) {
      if (!items.stream().allMatch(item -> item instanceof Path)) {
        throw new PlaywrightException("File paths cannot be mixed with buffers");
      }

      List<Path> localPaths = new ArrayList<>();
      Path localDirectory = resolvePathsAndDirectoryForInputFiles(items, localPaths);

      if (context.connection.isRemote()) {
        List<Path> filesToStream = localDirectory != null ? listFilesRecursively(localDirectory) : localPaths;
        BrowserContextCreateTempFilesResult tempFiles = context.wrapApiCall(() -> {
          BrowserContextCreateTempFilesParams params = new BrowserContextCreateTempFilesParams();
          params.rootDirName = localDirectory != null ? localDirectory.getFileName().toString() : null;
          params.items = new ArrayList<>();
          for (Path file : filesToStream) {
            double lastModifiedMs = (double) stat(file).lastModifiedTime().toMillis();
            BrowserContextCreateTempFilesParams.BrowserContextCreateTempFilesParamsItems item = new BrowserContextCreateTempFilesParams.BrowserContextCreateTempFilesParamsItems();
            item.name = localDirectory != null ? localDirectory.relativize(file).toString() : file.getFileName().toString();
            item.lastModifiedMs = lastModifiedMs;
            params.items.add(item);
          }
          return context.channel.createTempFiles(params, NO_TIMEOUT);
        }, null, true);
        List<WritableStream> writableStreams = tempFiles.writableStreams;
        WritableStream rootDir = tempFiles.rootDir;
        for (int i = 0; i < filesToStream.size(); i++) {
          WritableStream writable = writableStreams.get(i);
          // java: Files.copy into the stream of the WritableStream stands for stream.pipeline.
          try (OutputStream out = writable.stream()) {
            Files.copy(filesToStream.get(i), out);
          } catch (IOException e) {
            throw new PlaywrightException("Failed to copy file to remote server.", e);
          }
        }
        ElementHandleSetInputFilesParams result = new ElementHandleSetInputFilesParams();
        result.directoryStream = rootDir;
        result.streams = localDirectory != null ? null : writableStreams;
        return result;
      }
      ElementHandleSetInputFilesParams result = new ElementHandleSetInputFilesParams();
      if (!localPaths.isEmpty()) {
        result.localPaths = new ArrayList<>();
        for (Path path : localPaths) {
          result.localPaths.add(path.toString());
        }
      }
      result.localDirectory = localDirectory != null ? localDirectory.toString() : null;
      return result;
    }

    List<FilePayload> payloads = new ArrayList<>();
    for (Object item : items) {
      payloads.add((FilePayload) item);
    }
    if (filePayloadExceedsSizeLimit(payloads)) {
      throw new PlaywrightException("Cannot set buffer larger than 50Mb, please write it to a file and pass its path instead.");
    }
    ElementHandleSetInputFilesParams result = new ElementHandleSetInputFilesParams();
    result.payloads = new ArrayList<>();
    for (FilePayload file : payloads) {
      ElementHandleSetInputFilesParams.ElementHandleSetInputFilesParamsPayloads payload = new ElementHandleSetInputFilesParams.ElementHandleSetInputFilesParamsPayloads();
      payload.name = file.name;
      payload.mimeType = file.mimeType;
      payload.buffer = file.buffer;
      result.payloads.add(payload);
    }
    return result;
  }

  // java: one overload per screenshot options class; the result is the protocol string.
  static String determineScreenshotType(ElementHandle.ScreenshotOptions options) {
    return determineScreenshotType(options.path, options.type);
  }

  static String determineScreenshotType(Page.ScreenshotOptions options) {
    return determineScreenshotType(options.path, options.type);
  }

  static String determineScreenshotType(Locator.ScreenshotOptions options) {
    return determineScreenshotType(options.path, options.type);
  }

  private static String determineScreenshotType(Path path, ScreenshotType type) {
    if (path != null) {
      String mimeType = getMimeTypeForPath(path.toString());
      if ("image/png".equals(mimeType))
        return "png";
      else if ("image/jpeg".equals(mimeType))
        return "jpeg";
      else if ("image/webp".equals(mimeType))
        return "webp";
      throw new PlaywrightException("path: unsupported mime type \"" + mimeType + "\"");
    }
    return type == null ? null : type.toString().toLowerCase();
  }

  // java: fs.promises.stat and fs.promises.readdir with recursive: true.
  private static BasicFileAttributes stat(Path path) {
    try {
      return Files.readAttributes(path, BasicFileAttributes.class);
    } catch (NoSuchFileException e) {
      throw new PlaywrightException("ENOENT: no such file or directory, stat '" + path + "'", e);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read file attributes: " + path, e);
    }
  }

  private static List<Path> listFilesRecursively(Path directory) {
    try (Stream<Path> paths = Files.walk(directory)) {
      return paths.filter(Files::isRegularFile).collect(Collectors.toList());
    } catch (IOException e) {
      throw new PlaywrightException("Failed to traverse directory", e);
    }
  }
}
