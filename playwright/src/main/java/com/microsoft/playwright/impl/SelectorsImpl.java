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

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SelectorsImpl extends LoggingSupport implements Selectors {
  protected final List<BrowserContextImpl> contextsForSelectors = new ArrayList<>();
  protected final List<JsonObject> selectorEngines = new ArrayList<>();

  String testIdAttributeName = "data-testid";

  @Override
  public void setTestIdAttribute(String attributeName) {
    if (attributeName == null) {
      throw new PlaywrightException("Test id attribute cannot be null");
    }
    testIdAttributeName = attributeName;
    for (BrowserContextImpl context : contextsForSelectors) {
      try {
        JsonObject params = new JsonObject();
        params.addProperty("testIdAttributeName", attributeName);
        context.sendMessageAsync("setTestIdAttributeName", params);
      } catch (PlaywrightException e) {  
      }
    }
  }

  @Override
  public void register(String name, String script, RegisterOptions options) {
    registerImpl(name, script, options);
  }

  @Override
  public void register(String name, Path path, RegisterOptions options) {
    byte[] buffer;
    try {
      buffer = Files.readAllBytes(path);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read selector from file: " + path, e);
    }
    registerImpl(name, new String(buffer, UTF_8), options);
  }

  private void registerImpl(String name, String script, RegisterOptions options) {
    JsonObject engine = new JsonObject();
    engine.addProperty("name", name);
    engine.addProperty("source", script);
    if (options != null && options.contentScript != null) {
      engine.addProperty("contentScript", options.contentScript);
    }
    for (BrowserContextImpl context : contextsForSelectors) {
      JsonObject params = new JsonObject();
      params.add("selectorEngine", engine);
      context.sendMessage("registerSelectorEngine", params);
    }
    selectorEngines.add(engine);
  }
}
