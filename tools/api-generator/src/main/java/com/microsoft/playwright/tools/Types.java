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
    // State enums
    add("Page.waitForLoadState.state", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "LoadState");
    add("Frame.waitForLoadState.state", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "LoadState");
    add("ElementHandle.waitForElementState.state", "\"disabled\"|\"editable\"|\"enabled\"|\"hidden\"|\"stable\"|\"visible\"", "ElementState");
    add("Logger.isEnabled.severity", "\"error\"|\"info\"|\"verbose\"|\"warning\"", "Severity");
    add("Logger.log.severity", "\"error\"|\"info\"|\"verbose\"|\"warning\"", "Severity");

    // Option enums
    add("Browser.newContext.options.colorScheme", "\"dark\"|\"light\"|\"no-preference\"", "ColorScheme", new Empty());
    add("Browser.newPage.options.colorScheme", "\"dark\"|\"light\"|\"no-preference\"", "ColorScheme", new Empty());
    add("Page.click.options.button", "\"left\"|\"middle\"|\"right\"", "Mouse.Button", new Empty());
    add("Page.click.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Page.dblclick.options.button", "\"left\"|\"middle\"|\"right\"", "Mouse.Button", new Empty());
    add("Page.dblclick.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Page.tap.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Page.emulateMedia.params.media", "\"print\"|\"screen\"|null", "Media");
    add("Page.emulateMedia.params.colorScheme", "\"dark\"|\"light\"|\"no-preference\"|null", "ColorScheme", new Empty());
    add("Page.goBack.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.goForward.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.goto.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.hover.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Page.reload.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.screenshot.options.type", "\"jpeg\"|\"png\"", "Type");
    add("Page.setContent.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.waitForFunction.options.polling", "\"raf\"|float", "double", new Empty());
    add("Page.waitForNavigation.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.waitForSelector.options.state", "\"attached\"|\"detached\"|\"hidden\"|\"visible\"", "State");
    add("Frame.click.options.button", "\"left\"|\"middle\"|\"right\"", "Mouse.Button", new Empty());
    add("Frame.click.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Frame.dblclick.options.button", "\"left\"|\"middle\"|\"right\"", "Mouse.Button", new Empty());
    add("Frame.dblclick.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Frame.tap.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Frame.goto.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "LoadState", new Empty());
    add("Frame.hover.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Frame.setContent.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "LoadState", new Empty());
    add("Frame.waitForFunction.options.polling", "\"raf\"|float", "double", new Empty());
    add("Frame.waitForNavigation.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "LoadState", new Empty());
    add("Frame.waitForSelector.options.state", "\"attached\"|\"detached\"|\"hidden\"|\"visible\"", "State");
    add("ElementHandle.click.options.button", "\"left\"|\"middle\"|\"right\"", "Mouse.Button", new Empty());
    add("ElementHandle.click.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("ElementHandle.dblclick.options.button", "\"left\"|\"middle\"|\"right\"", "Mouse.Button", new Empty());
    add("ElementHandle.dblclick.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("ElementHandle.tap.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("ElementHandle.hover.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("ElementHandle.screenshot.options.type", "\"jpeg\"|\"png\"", "Type");
    add("ElementHandle.waitForSelector.options.state", "\"attached\"|\"detached\"|\"hidden\"|\"visible\"", "State");
    add("Mouse.click.options.button", "\"left\"|\"middle\"|\"right\"", "Button", new Empty());
    add("Mouse.dblclick.options.button", "\"left\"|\"middle\"|\"right\"", "Button", new Empty());
    add("Mouse.down.options.button", "\"left\"|\"middle\"|\"right\"", "Button", new Empty());
    add("Mouse.up.options.button", "\"left\"|\"middle\"|\"right\"", "Button", new Empty());
    add("BrowserType.launchPersistentContext.options.colorScheme", "\"dark\"|\"light\"|\"no-preference\"", "ColorScheme", new Empty());

    // Route
    add("BrowserContext.route.handler", "function(Route, Request)", "Consumer<Route>");
    add("BrowserContext.unroute.handler", "function(Route, Request)", "Consumer<Route>");
    add("Page.route.handler", "function(Route, Request)", "Consumer<Route>");
    add("Page.unroute.handler", "function(Route, Request)", "Consumer<Route>");

    // Viewport size.
    add("Browser.newContext.options.viewport", "Object|null", "Page.Viewport", new Empty());
    add("Browser.newPage.options.viewport", "Object|null", "Page.Viewport", new Empty());
    add("Page.setViewportSize.viewportSize", "Object", "Viewport", new Empty());
    add("Page.viewportSize", "Object|null", "Viewport", new Empty());
    add("BrowserType.launchPersistentContext.options.viewport", "Object|null", "Page.Viewport", new Empty());

    // RecordVideo size.
    add("Browser.newContext.options.recordVideo.size", "Object", "VideoSize", new Empty());
    add("Browser.newPage.options.recordVideo.size", "Object", "VideoSize", new Empty());
    add("BrowserType.launchPersistentContext.recordVideo.size", "Object", "Browser.VideoSize", new Empty());

    // HTTP credentials.
    add("Browser.newContext.options.httpCredentials", "Object", "BrowserContext.HTTPCredentials", new Empty());
    add("Browser.newPage.options.httpCredentials", "Object", "BrowserContext.HTTPCredentials", new Empty());
    add("BrowserType.launchPersistentContext.options.httpCredentials", "Object", "BrowserContext.HTTPCredentials", new Empty());
    add("BrowserContext.setHTTPCredentials.httpCredentials", "Object|null", "do nothing", new Empty());

    // js functions are always passed as text in java.
    add("Page.$eval.pageFunction", "function(Element)", "String");
    add("Page.$$eval.pageFunction", "function(Array<Element>)", "String");
    add("Frame.$eval.pageFunction", "function(Element)", "String");
    add("Frame.$$eval.pageFunction", "function(Array<Element>)", "String");
    add("ElementHandle.$eval.pageFunction", "function(Element)", "String");
    add("ElementHandle.$$eval.pageFunction", "function(Array<Element>)", "String");
    add("ElementHandle.evaluate.pageFunction", "function", "String");
    add("JSHandle.evaluate.pageFunction", "function", "String");

    add("BrowserContext.exposeBinding.callback", "function", "Page.Binding");
    add("BrowserContext.exposeFunction.callback", "function", "Page.Function");
    add("Page.exposeBinding.callback", "function", "Binding");
    add("Page.exposeFunction.callback", "function", "Function");

    add("BrowserContext.addInitScript.script", "Object|function|string", "String");
    add("Page.addInitScript.script", "Object|function|string", "String");
    add("Page.evaluate.pageFunction", "function|string", "String");
    add("Page.evaluateHandle.pageFunction", "function|string", "String");
    add("Page.waitForFunction.pageFunction", "function|string", "String");
    add("Frame.evaluate.pageFunction", "function|string", "String");
    add("Frame.evaluateHandle.pageFunction", "function|string", "String");
    add("Frame.waitForFunction.pageFunction", "function|string", "String");
    add("ElementHandle.evaluateHandle.pageFunction", "function|string", "String");
    add("JSHandle.evaluateHandle.pageFunction", "function|string", "String");
    add("Selectors.register.script", "Object|function|string", "String");
    add("Worker.evaluate.pageFunction", "function|string", "String");
    add("Worker.evaluateHandle.pageFunction", "function|string", "String");
    add("WebSocket.waitForEvent.optionsOrPredicate", "Function|Object", "String");

    // Return structures
    add("ConsoleMessage.location", "Object", "Location");
    add("ElementHandle.boundingBox", "Object|null", "BoundingBox", new Empty());
    add("Accessibility.snapshot", "Object|null", "AccessibilityNode", new Empty());
    add("WebSocket.framereceived", "Object", "FrameData", new Empty());
    add("WebSocket.framesent", "Object", "FrameData", new Empty());

    // Custom options
    add("Page.pdf.options.margin.top", "float|string", "String");
    add("Page.pdf.options.margin.right", "float|string", "String");
    add("Page.pdf.options.margin.bottom", "float|string", "String");
    add("Page.pdf.options.margin.left", "float|string", "String");
    add("Page.pdf.options.width", "float|string", "String");
    add("Page.pdf.options.height", "float|string", "String");
    add("Page.pdf.options.scale", "float", "Double");

    add("Page.goto.options", "Object", "NavigateOptions");
    add("Frame.goto.options", "Object", "NavigateOptions");

    add("Page.click.options.position", "Object", "Position", new Empty());
    add("Page.dblclick.options.position", "Object", "Position", new Empty());
    add("Page.hover.options.position", "Object", "Position", new Empty());
    add("Frame.click.options.position", "Object", "Position", new Empty());
    add("Frame.dblclick.options.position", "Object", "Position", new Empty());
    add("Frame.hover.options.position", "Object", "Position", new Empty());
    add("ElementHandle.click.options.position", "Object", "Position", new Empty());
    add("ElementHandle.dblclick.options.position", "Object", "Position", new Empty());
    add("ElementHandle.hover.options.position", "Object", "Position", new Empty());

    // The method has custom signatures
    add("BrowserContext.cookies", "Array<Object>", "Cookie");
    add("BrowserContext.cookies.sameSite", "\"Lax\"|\"None\"|\"Strict\"", "SameSite", new Empty());
    add("BrowserContext.addCookies.cookies", "Array<Object>", "AddCookie");
    add("BrowserContext.addCookies.cookies.sameSite", "\"Lax\"|\"None\"|\"Strict\"", "SameSite", new Empty());
    add("BrowserContext.route.url", "RegExp|function(URL):boolean|string", "String");
    add("BrowserContext.unroute.url", "RegExp|function(URL):boolean|string", "String");
    add("BrowserContext.storageState", "Object", "StorageState", new Empty());
    add("Page.waitForNavigation.options.url", "RegExp|function(URL):boolean|string", "Custom");
    add("Page.waitForNavigation.options", "Object", "WaitForNavigationOptions");
    add("Page.waitForRequest.options", "Object", "WaitForRequestOptions");
    add("Page.waitForResponse.options", "Object", "WaitForResponseOptions");
    add("Page.frame.options", "Object", "FrameOptions", new Empty());
    add("Page.route.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("Page.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("Page.unroute.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.waitForRequest.urlOrPredicate", "RegExp|function(Request):boolean|string", "String");
    add("Page.waitForResponse.urlOrPredicate", "RegExp|function(Response):boolean|string", "String");
    add("Frame.waitForNavigation.options.url", "RegExp|function(URL):boolean|string", "Custom");
    add("Frame.waitForNavigation.options", "Object", "WaitForNavigationOptions");
    add("Frame.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("Frame.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("ElementHandle.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("ElementHandle.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("FileChooser.setFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("Route.continue.options.postData", "Buffer|string", "byte[]", new Empty());
    add("Route.fulfill.options.body", "Buffer|string", "String");
    add("BrowserType.launch.options.ignoreDefaultArgs", "Array<string>|boolean", "Custom");
    add("BrowserType.launch.options.firefoxUserPrefs", "Object<string, boolean|float|string>", "Map<String, Object>", new Empty());
    add("BrowserType.launch.options.env", "Object<string, boolean|float|string>", "Map<String, String>", new Empty());
    add("BrowserType.launchPersistentContext.options.ignoreDefaultArgs", "Array<string>|boolean", "Custom");
    add("BrowserType.launchPersistentContext.options.env", "Object<string, boolean|float|string>", "Map<String, String>", new Empty());
    add("BrowserType.launchServer.options.ignoreDefaultArgs", "Array<string>|boolean", "Custom");
    add("BrowserType.launchServer.options.firefoxUserPrefs", "Object<string, boolean|float|string>", "Map<String, Object>", new Empty());
    add("BrowserType.launchServer.options.env", "Object<string, boolean|float|string>", "Map<String, String>", new Empty());
    add("Logger.log.message", "string|Error", "String");

    add("BrowserContext.setGeolocation.geolocation", "Object|null", "Geolocation", new Empty());
    add("Browser.newContext.options.geolocation", "Object", "Geolocation", new Empty());
    add("Browser.newContext.options.storageState", "Object|path", "BrowserContext.StorageState", new Empty());
    add("Browser.newPage.options.storageState", "Object|path", "BrowserContext.StorageState", new Empty());
    add("Browser.newPage.options.geolocation", "Object", "Geolocation", new Empty());
    add("BrowserType.launchPersistentContext.options.geolocation", "Object", "Geolocation", new Empty());
    add("Download.createReadStream", "Readable|null", "InputStream", new Empty());

    // node.js types
    add("BrowserServer.process", "ChildProcess", "Object");

    add("Response.finished", "Error|null", "String");

    // TODO: fix upstream types!
    add("Request.headers", "Object<string, string>", "Map<String, String>", new Empty());
    add("Response.headers", "Object<string, string>", "Map<String, String>", new Empty());
    add("Browser.newContext.options.extraHTTPHeaders", "Object<string, string>", "Map<String, String>", new Empty());
    add("Browser.newPage.options.extraHTTPHeaders", "Object<string, string>", "Map<String, String>", new Empty());
    add("BrowserType.launchPersistentContext.options.extraHTTPHeaders", "Object<string, string>", "Map<String, String>", new Empty());
    add("Page.setExtraHTTPHeaders.headers", "Object<string, string>", "Map<String, String>", new Empty());
    add("BrowserContext.setExtraHTTPHeaders.headers", "Object<string, string>", "Map<String, String>", new Empty());
    add("Route.continue.options.headers", "Object<string, string>", "Map<String, String>", new Empty());
    add("Route.fulfill.options.headers", "Object<string, string>", "Map<String, String>", new Empty());
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
