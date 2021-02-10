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

package com.microsoft.playwright.tools;

import java.util.HashMap;
import java.util.Map;

class Types {

  interface CustomMapping {
    void defineTypesIn(TypeDefinition scope);
  }

  class Mapping {
    final String from;
    final String to;

    final CustomMapping customMapping;

    Mapping(String from, String to) {
      this(from, to, null);
    }

    Mapping(String from, String to, CustomMapping customMapping) {
      this.from = from;
      this.to = to;
      this.customMapping = customMapping;
    }
  }

  private final Map<String, Mapping> jsonPathToMapping = new HashMap<>();

  Types() {
    // Viewport size.
    add("Page.viewportSize", "Object|null", "ViewportSize", new Empty());

    add("BrowserContext.exposeBinding.callback", "function", "Page.Binding");
    add("BrowserContext.exposeFunction.callback", "function", "Page.Function");
    add("Page.exposeBinding.callback", "function", "Binding");
    add("Page.exposeFunction.callback", "function", "Function");

    add("BrowserContext.addInitScript.script", "Object|function|string", "String");
    add("Page.addInitScript.script", "Object|function|string", "String");
    add("Selectors.register.script", "Object|function|string", "String");

    // The method has custom signatures
    add("BrowserContext.cookies", "Array<Object>", "Cookie");
    add("BrowserContext.route.url", "RegExp|function(URL):boolean|string", "String");
    add("BrowserContext.unroute.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.waitForNavigation.options.url", "RegExp|function(URL):boolean|string", "Custom");
    add("Page.frame.frameSelector", "Object|string", "Custom", new Empty());
    add("Page.frame.options", "Object", "FrameOptions", new Empty());
    add("Page.route.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("Page.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("Page.unroute.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.waitForRequest.urlOrPredicate", "RegExp|function(Request):boolean|string", "String");
    add("Page.waitForResponse.urlOrPredicate", "RegExp|function(Response):boolean|string", "String");
    add("Frame.waitForNavigation.options.url", "RegExp|function(URL):boolean|string", "Custom");
    add("Frame.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("Frame.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("ElementHandle.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("ElementHandle.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("FileChooser.setFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("Route.resume.options.postData", "Buffer|string", "byte[]", new Empty());

    // TODO: fix upstream types!
    add("Playwright.devices", "Object", "Map<String, DeviceDescriptor>", new Empty());
  }

  Mapping findForPath(String jsonPath) {
    return jsonPathToMapping.get(jsonPath);
  }

  private void add(String jsonPath, String fromType, String toType) {
    if (jsonPathToMapping.containsKey(jsonPath)) {
      throw new RuntimeException("Duplicate entry: " + jsonPath);
    }
    jsonPathToMapping.put(jsonPath, new Mapping(fromType, toType));
  }

  private void add(String jsonPath, String fromType, String toType, CustomMapping factory) {
    jsonPathToMapping.put(jsonPath, new Mapping(fromType, toType, factory));
  }

  private static class Empty implements CustomMapping {
    @Override
    public void defineTypesIn(TypeDefinition scope) {
    }
  }
}
