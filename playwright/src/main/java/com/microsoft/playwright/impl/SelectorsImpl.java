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

// Port of selectors.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.ClientHelper.evaluationScript;

public class SelectorsImpl extends LoggingSupport implements Selectors {
  protected final List<SelectorEngine> selectorEngines = new ArrayList<>();
  // java: defaults to data-testid, upstream leaves it undefined until set.
  String testIdAttributeName = "data-testid";
  protected final List<BrowserContextImpl> contextsForSelectors = new ArrayList<>();

  @Override
  public void register(String name, String script, RegisterOptions options) {
    registerImpl(name, script, options);
  }

  @Override
  public void register(String name, Path path, RegisterOptions options) {
    registerImpl(name, path, options);
  }

  // java: the core of the String and Path overloads.
  private void registerImpl(String name, Object script, RegisterOptions options) {
    if (selectorEngines.stream().anyMatch(engine -> name.equals(engine.name))) {
      throw new PlaywrightException("selectors.register: \"" + name + "\" selector engine has been already registered");
    }

    String source = evaluationScript(script, false);
    SelectorEngine engine = new SelectorEngine();
    engine.name = name;
    engine.source = source;
    if (options != null) {
      engine.contentScript = options.contentScript;
    }
    for (BrowserContextImpl context : contextsForSelectors) {
      BrowserContextRegisterSelectorEngineParams params = new BrowserContextRegisterSelectorEngineParams();
      params.selectorEngine = engine;
      context.channel.registerSelectorEngine(params, NO_TIMEOUT);
    }
    selectorEngines.add(engine);
  }

  @Override
  public void setTestIdAttribute(String attributeName) {
    if (attributeName == null) {
      throw new PlaywrightException("Test id attribute cannot be null");
    }
    testIdAttributeName = attributeName;
    for (BrowserContextImpl context : contextsForSelectors) {
      try {
        BrowserContextSetTestIdAttributeNameParams params = new BrowserContextSetTestIdAttributeNameParams();
        params.testIdAttributeName = attributeName;
        context.sendMessageAsync("setTestIdAttributeName", params);
      } catch (PlaywrightException e) {
      }
    }
  }
}
