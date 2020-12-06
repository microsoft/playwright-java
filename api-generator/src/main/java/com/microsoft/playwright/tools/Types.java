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
    add("ElementHandle.waitForElementState.state", "\"disabled\"|\"enabled\"|\"hidden\"|\"stable\"|\"visible\"", "ElementState");
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
    add("Page.emulateMedia.options.media", "null|\"print\"|\"screen\"", "Media");
    add("Page.emulateMedia.options.colorScheme", "null|\"dark\"|\"light\"|\"no-preference\"", "ColorScheme", new Empty());
    add("Page.goBack.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.goForward.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.goto.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.hover.options.modifiers", "Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">", "Set<Keyboard.Modifier>", new Empty());
    add("Page.reload.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.screenshot.options.type", "\"jpeg\"|\"png\"", "Type");
    add("Page.setContent.options.waitUntil", "\"domcontentloaded\"|\"load\"|\"networkidle\"", "Frame.LoadState", new Empty());
    add("Page.waitForFunction.options.polling", "number|\"raf\"", "double", new PollingOption());
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
    add("Frame.waitForFunction.options.polling", "number|\"raf\"", "double", new PollingOption());
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

    // File
    add("Page.addScriptTag.options.path", "string", "Path");
    add("Page.addStyleTag.options.path", "string", "Path");
    add("Page.pdf.options.path", "string", "Path");
    add("Page.screenshot.options.path", "string", "Path");
    add("Frame.addScriptTag.options.path", "string", "Path");
    add("Frame.addStyleTag.options.path", "string", "Path");
    add("ElementHandle.screenshot.options.path", "string", "Path");
    add("Route.fulfill.response.path", "string", "Path");
    add("Route.fulfill.response.status", "number", "int");
    add("Browser.newContext.options.recordHar.path", "string", "Path");
    add("Browser.newContext.options.recordVideo.dir", "string", "Path");
    add("Browser.newPage.options.recordHar.path", "string", "Path");
    add("Browser.newPage.options.recordVideo.dir", "string", "Path");
    add("BrowserType.launchPersistentContext.options.recordHar.path", "string", "Path");
    add("BrowserType.launchPersistentContext.options.recordVideo.dir", "string", "Path");
    add("BrowserType.launchPersistentContext.userDataDir", "string", "Path");
    add("BrowserType.launchPersistentContext.options.executablePath", "string", "Path");
    add("BrowserType.launchServer.options.executablePath", "string", "Path");
    add("BrowserType.launchPersistentContext.options.downloadsPath", "string", "Path");
    add("BrowserType.launch.options.executablePath", "string", "Path");
    add("BrowserType.launch.options.downloadsPath", "string", "Path");
    add("ChromiumBrowser.startTracing.options.path", "string", "Path");

    // Route
    add("BrowserContext.route.handler", "function(Route, Request)", "Consumer<Route>");
    add("BrowserContext.unroute.handler", "function(Route, Request)", "Consumer<Route>");
    add("Page.route.handler", "function(Route, Request)", "Consumer<Route>");
    add("Page.unroute.handler", "function(Route, Request)", "Consumer<Route>");

    // Viewport size.
    add("Browser.newContext.options.viewport", "null|Object", "Page.Viewport", new Empty());
    add("Browser.newPage.options.viewport", "null|Object", "Page.Viewport", new Empty());
    add("Page.setViewportSize.viewportSize", "Object", "Viewport", new Empty());
    add("Page.viewportSize", "null|Object", "Viewport", new Empty());
    add("BrowserType.launchPersistentContext.options.viewport", "null|Object", "Page.Viewport", new Empty());

    // RecordVideo size.
    add("Browser.newContext.options.recordVideo.size", "Object", "VideoSize", new Empty());
    add("Browser.newPage.options.recordVideo.size", "Object", "VideoSize", new Empty());
    add("BrowserType.launchPersistentContext.recordVideo.size", "Object", "Browser.VideoSize", new Empty());

    // HTTP credentials.
    add("Browser.newContext.options.httpCredentials", "Object", "BrowserContext.HTTPCredentials", new Empty());
    add("Browser.newPage.options.httpCredentials", "Object", "BrowserContext.HTTPCredentials", new Empty());
    add("BrowserType.launchPersistentContext.options.httpCredentials", "Object", "BrowserContext.HTTPCredentials", new Empty());
    add("BrowserContext.setHTTPCredentials.httpCredentials", "null|Object", "do nothing", new Empty());

    // EvaluationArgument
    add("Page.$eval.arg", "EvaluationArgument", "Object");
    add("Page.$$eval.arg", "EvaluationArgument", "Object");
    add("Page.dispatchEvent.eventInit", "EvaluationArgument", "Object");
    add("Page.evaluate.arg", "EvaluationArgument", "Object");
    add("Page.evaluateHandle.arg", "EvaluationArgument", "Object");
    add("Page.waitForFunction.arg", "EvaluationArgument", "Object");
    add("Frame.$eval.arg", "EvaluationArgument", "Object");
    add("Frame.$$eval.arg", "EvaluationArgument", "Object");
    add("Frame.dispatchEvent.eventInit", "EvaluationArgument", "Object");
    add("Frame.evaluate.arg", "EvaluationArgument", "Object");
    add("Frame.evaluateHandle.arg", "EvaluationArgument", "Object");
    add("Frame.waitForFunction.arg", "EvaluationArgument", "Object");
    add("ElementHandle.$eval.arg", "EvaluationArgument", "Object");
    add("ElementHandle.$$eval.arg", "EvaluationArgument", "Object");
    add("ElementHandle.dispatchEvent.eventInit", "EvaluationArgument", "Object");
    add("ElementHandle.evaluate.arg", "EvaluationArgument", "Object");
    add("ElementHandle.evaluateHandle.arg", "EvaluationArgument", "Object");
    add("JSHandle.evaluate.arg", "EvaluationArgument", "Object");
    add("JSHandle.evaluateHandle.arg", "EvaluationArgument", "Object");
    add("Worker.evaluate.arg", "EvaluationArgument", "Object");
    add("Worker.evaluateHandle.arg", "EvaluationArgument", "Object");


    // js functions are always passed as text in java.
    add("Page.$eval.pageFunction", "function(Element)", "String");
    add("Page.$$eval.pageFunction", "function(Array<Element>)", "String");
    add("Frame.$eval.pageFunction", "function(Element)", "String");
    add("Frame.$$eval.pageFunction", "function(Array<Element>)", "String");
    add("ElementHandle.$eval.pageFunction", "function(Element)", "String");
    add("ElementHandle.$$eval.pageFunction", "function(Array<Element>)", "String");
    add("ElementHandle.evaluate.pageFunction", "function", "String");
    add("JSHandle.evaluate.pageFunction", "function", "String");

    add("BrowserContext.exposeBinding.playwrightBinding", "function", "Page.Binding");
    add("BrowserContext.exposeFunction.playwrightFunction", "function", "Page.Function");
    add("Page.exposeBinding.playwrightBinding", "function", "Binding");
    add("Page.exposeFunction.playwrightFunction", "function", "Function");

    add("BrowserContext.addInitScript.script", "function|string|Object", "String");
    add("Page.addInitScript.script", "function|string|Object", "String");
    add("Page.evaluate.pageFunction", "function|string", "String");
    add("Page.evaluateHandle.pageFunction", "function|string", "String");
    add("Page.waitForFunction.pageFunction", "function|string", "String");
    add("Frame.evaluate.pageFunction", "function|string", "String");
    add("Frame.evaluateHandle.pageFunction", "function|string", "String");
    add("Frame.waitForFunction.pageFunction", "function|string", "String");
    add("ElementHandle.evaluateHandle.pageFunction", "function|string", "String");
    add("JSHandle.evaluateHandle.pageFunction", "function|string", "String");
    add("Selectors.register.script", "function|string|Object", "String");
    add("Worker.evaluate.pageFunction", "function|string", "String");
    add("Worker.evaluateHandle.pageFunction", "function|string", "String");
    add("WebSocket.waitForEvent.optionsOrPredicate", "Function|Object", "String");

    // Return structures
    add("Dialog.type", "string", "Type", new Empty());
    add("ConsoleMessage.location", "Object", "Location");
    add("ElementHandle.boundingBox", "Promise<null|Object>", "BoundingBox", new Empty());
    add("Accessibility.snapshot", "Promise<null|Object>", "AccessibilityNode", new Empty());
    add("WebSocket.framereceived", "Object", "FrameData", new Empty());
    add("WebSocket.framesent", "Object", "FrameData", new Empty());

    add("Page.waitForRequest", "Promise<Request>", "Deferred<Request>");
    add("Page.waitForResponse", "Promise<Response>", "Deferred<Response>");
    add("Page.waitForNavigation", "Promise<null|Response>", "Deferred<Response>");
    add("Frame.waitForNavigation", "Promise<null|Response>", "Deferred<Response>");
    add("Page.waitForSelector", "Promise<null|ElementHandle>", "Deferred<ElementHandle>", new Empty());
    add("Frame.waitForSelector", "Promise<null|ElementHandle>", "Deferred<ElementHandle>", new Empty());
    add("ElementHandle.waitForSelector", "Promise<null|ElementHandle>", "Deferred<ElementHandle>", new Empty());

    add("Frame.waitForLoadState", "Promise", "Deferred<Void>", new Empty());
    add("Page.waitForLoadState", "Promise", "Deferred<Void>", new Empty());
    add("Frame.waitForTimeout", "Promise", "Deferred<Void>", new Empty());
    add("Page.waitForTimeout", "Promise", "Deferred<Void>", new Empty());
    add("Frame.waitForFunction", "Promise<JSHandle>", "Deferred<JSHandle>", new Empty());
    add("Page.waitForFunction", "Promise<JSHandle>", "Deferred<JSHandle>", new Empty());
    add("ElementHandle.waitForElementState", "Promise", "Deferred<Void>", new Empty());

    // Custom options
    add("Page.pdf.options.margin.top", "string|number", "String");
    add("Page.pdf.options.margin.right", "string|number", "String");
    add("Page.pdf.options.margin.bottom", "string|number", "String");
    add("Page.pdf.options.margin.left", "string|number", "String");
    add("Page.pdf.options.width", "string|number", "String");
    add("Page.pdf.options.height", "string|number", "String");

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
    add("BrowserContext.cookies", "Promise<Array<Object>>", "Cookie");
    add("BrowserContext.cookies.sameSite", "\"Lax\"|\"None\"|\"Strict\"", "SameSite", new Empty());
    add("BrowserContext.cookies.expires", "number", "long");
    add("BrowserContext.addCookies.cookies", "Array<Object>", "AddCookie");
    add("BrowserContext.addCookies.cookies.sameSite", "\"Lax\"|\"None\"|\"Strict\"", "SameSite", new Empty());
    add("BrowserContext.addCookies.cookies.expires", "number", "Long", new Empty());
    add("BrowserContext.route.url", "string|RegExp|function(URL):boolean", "String");
    add("BrowserContext.unroute.url", "string|RegExp|function(URL):boolean", "String");
    add("BrowserContext.storageState", "Promise<Object>", "StorageState", new Empty());
    add("BrowserContext.waitForEvent.event", "string", "EventType", new Empty());
    add("BrowserContext.waitForEvent.optionsOrPredicate", "Function|Object", "String");
    add("BrowserContext.waitForEvent", "Promise<Object>", "Deferred<Event<EventType>>", new Empty());
    add("Page.waitForNavigation.options.url", "string|RegExp|Function", "String");
    add("Page.frame.options", "string|Object", "FrameOptions", new Empty());
    add("Page.route.url", "string|RegExp|function(URL):boolean", "String");
    add("Page.selectOption.values", "null|string|ElementHandle|Array<string>|Object|Array<ElementHandle>|Array<Object>", "String");
    add("Page.setInputFiles.files", "string|Array<string>|Object|Array<Object>", "String");
    add("Page.unroute.url", "string|RegExp|function(URL):boolean", "String");
    add("Page.waitForEvent.event", "string", "EventType", new Empty());
    add("Page.waitForEvent.optionsOrPredicate", "Function|Object", "WaitForEventOptions");
    add("Page.waitForEvent", "Promise<Object>", "Deferred<Event<EventType>>", new Empty());
    add("Page.waitForRequest.urlOrPredicate", "string|RegExp|Function", "String");
    add("Page.waitForResponse.urlOrPredicate", "string|RegExp|function(Response):boolean", "String");
    add("Frame.waitForNavigation.options.url", "string|RegExp|Function", "String");
    add("Frame.selectOption.values", "null|string|ElementHandle|Array<string>|Object|Array<ElementHandle>|Array<Object>", "String");
    add("Frame.setInputFiles.files", "string|Array<string>|Object|Array<Object>", "String");
    add("ElementHandle.selectOption.values", "null|string|ElementHandle|Array<string>|Object|Array<ElementHandle>|Array<Object>", "String");
    add("ElementHandle.setInputFiles.files", "string|Array<string>|Object|Array<Object>", "String");
    add("FileChooser.setFiles.files", "string|Array<string>|Object|Array<Object>", "String");
    add("Route.continue.overrides.postData", "string|Buffer", "byte[]");
    add("Route.fulfill.response.body", "string|Buffer", "String");
    add("BrowserType.launch.options.ignoreDefaultArgs", "boolean|Array<string>", "Boolean");
    add("BrowserType.launch.options.firefoxUserPrefs", "Object<string, string|number|boolean>", "String");
    add("BrowserType.launch.options.env", "Object<string, string|number|boolean>", "String");
    add("BrowserType.launchPersistentContext.options.ignoreDefaultArgs", "boolean|Array<string>", "String");
    add("BrowserType.launchPersistentContext.options.env", "Object<string, string|number|boolean>", "String");
    add("BrowserType.launchServer.options.ignoreDefaultArgs", "boolean|Array<string>", "String");
    add("BrowserType.launchServer.options.firefoxUserPrefs", "Object<string, string|number|boolean>", "String");
    add("BrowserType.launchServer.options.env", "Object<string, string|number|boolean>", "String");
    add("Logger.log.message", "string|Error", "String");

    add("Browser.newContext.options.geolocation.latitude", "number", "double");
    add("Browser.newContext.options.geolocation.longitude", "number", "double");
    add("Browser.newContext.options.geolocation.accuracy", "number", "double");
    add("Browser.newPage.options.geolocation.latitude", "number", "double");
    add("Browser.newPage.options.geolocation.longitude", "number", "double");
    add("Browser.newPage.options.geolocation.accuracy", "number", "double");
    add("BrowserType.launchPersistentContext.options.geolocation.latitude", "number", "double");
    add("BrowserType.launchPersistentContext.options.geolocation.longitude", "number", "double");
    add("BrowserType.launchPersistentContext.options.geolocation.accuracy", "number", "double");

    add("BrowserContext.setGeolocation.geolocation", "null|Object", "Geolocation", new Empty());
    add("Browser.newContext.options.geolocation", "Object", "Geolocation", new Empty());
    add("Browser.newContext.options.storageState", "Object", "BrowserContext.StorageState", new Empty());
    add("Browser.newPage.options.storageState", "Object", "BrowserContext.StorageState", new Empty());
    add("Browser.newPage.options.geolocation", "Object", "Geolocation", new Empty());
    add("BrowserType.launchPersistentContext.options.geolocation", "Object", "Geolocation", new Empty());
    add("Download.saveAs.path", "string", "Path", new Empty());
    add("Download.path", "Promise<null|string>", "Path", new Empty());
    add("Download.createReadStream", "Promise<null|Readable>", "InputStream", new Empty());

    // Single field options
    add("Keyboard.type.options", "Object", "int", new Empty());
    add("Keyboard.press.options", "Object", "int", new Empty());

    // node.js types
    add("BrowserServer.process", "ChildProcess", "Object");

    add("Page.pdf", "Promise<Buffer>", "byte[]");
    add("Page.screenshot", "Promise<Buffer>", "byte[]");
    add("ElementHandle.screenshot", "Promise<Buffer>", "byte[]");
    add("Request.postDataBuffer", "null|Buffer", "byte[]");
    add("Response.body", "Promise<Buffer>", "byte[]");
    add("Response.finished", "Promise<null|Error>", "String");
    add("ChromiumBrowser.stopTracing", "Promise<Buffer>", "byte[]");
    add("WebSocket.framereceived.payload", "string|Buffer", "byte[]");
    add("WebSocket.framesent.payload", "string|Buffer", "byte[]");


    // JSON type
    add("BrowserContext.addInitScript.arg", "Serializable", "Object");
    add("Page.$eval", "Promise<Serializable>", "Object");
    add("Page.$$eval", "Promise<Serializable>", "Object");
    add("Page.addInitScript.arg", "Serializable", "Object");
    add("Page.evaluate", "Promise<Serializable>", "Object");
    add("Frame.$eval", "Promise<Serializable>", "Object");
    add("Frame.$$eval", "Promise<Serializable>", "Object");
    add("Frame.evaluate", "Promise<Serializable>", "Object");
    add("ElementHandle.$eval", "Promise<Serializable>", "Object");
    add("ElementHandle.$$eval", "Promise<Serializable>", "Object");
    add("ElementHandle.evaluate", "Promise<Serializable>", "Object");
    add("ElementHandle.jsonValue", "Promise<Serializable>", "Object");
    add("JSHandle.evaluate", "Promise<Serializable>", "Object");
    add("JSHandle.jsonValue", "Promise<Serializable>", "Object");
    add("Response.json", "Promise<Serializable>", "Object");
    add("Worker.evaluate", "Promise<Serializable>", "Object");

    add("CDPSession.send.params", "Object", "Object", new Empty());
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

  private static class PollingOption implements CustomMapping {
    @Override
    public void defineTypesIn(TypeDefinition scope) {
    }
  }

  private static class Empty implements CustomMapping {
    @Override
    public void defineTypesIn(TypeDefinition scope) {
    }
  }
}
