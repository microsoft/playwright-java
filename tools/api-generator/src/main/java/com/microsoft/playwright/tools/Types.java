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
    add("Page.waitForFunction.options.polling", "\"raf\"|float", "double", new PollingOption());
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
    add("Frame.waitForFunction.options.polling", "\"raf\"|float", "double", new PollingOption());
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
    add("Page.addScriptTag.options.path", "path", "Path");
    add("Page.addStyleTag.options.path", "path", "Path");
    add("Page.pdf.options.path", "path", "Path");
    add("Page.screenshot.options.path", "path", "Path");
    add("Frame.addScriptTag.options.path", "path", "Path");
    add("Frame.addStyleTag.options.path", "path", "Path");
    add("ElementHandle.screenshot.options.path", "path", "Path");
    add("Route.fulfill.options.path", "path", "Path");
    add("Route.fulfill.options.status", "int", "int");
    add("Browser.newContext.options.recordHar.path", "path", "Path");
    add("Browser.newContext.options.recordVideo.dir", "path", "Path");
    add("Browser.newPage.options.recordHar.path", "path", "Path");
    add("Browser.newPage.options.recordVideo.dir", "path", "Path");
    add("BrowserType.launchPersistentContext.options.recordHar.path", "path", "Path");
    add("BrowserType.launchPersistentContext.options.recordVideo.dir", "path", "Path");
    add("BrowserType.launchPersistentContext.userDataDir", "path", "Path");
    add("BrowserType.launchPersistentContext.options.executablePath", "path", "Path");
    add("BrowserType.launchServer.options.executablePath", "path", "Path");
    add("BrowserType.launchPersistentContext.options.downloadsPath", "path", "Path");
    add("BrowserType.launch.options.executablePath", "path", "Path");
    add("BrowserType.launch.options.downloadsPath", "path", "Path");
    add("BrowserContext.storageState.options.path", "path", "Path");
    add("ChromiumBrowser.startTracing.options.path", "path", "Path");
    add("Video.path", "path", "Path");

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
    add("Dialog.type", "string", "Type", new Empty());
    add("ConsoleMessage.location", "Object", "Location");
    add("ElementHandle.boundingBox", "Object|null", "BoundingBox", new Empty());
    add("Accessibility.snapshot", "Object|null", "AccessibilityNode", new Empty());
    add("WebSocket.framereceived", "Object", "FrameData", new Empty());
    add("WebSocket.framesent", "Object", "FrameData", new Empty());

    add("Page.waitForRequest", "Request", "Deferred<Request>");
    add("Page.waitForResponse", "Response", "Deferred<Response>");
    add("Page.waitForNavigation", "Response|null", "Deferred<Response>");
    add("Frame.waitForNavigation", "Response|null", "Deferred<Response>");
    add("Page.waitForSelector", "ElementHandle|null", "ElementHandle", new Empty());
    add("Frame.waitForSelector", "ElementHandle|null", "ElementHandle", new Empty());
    add("ElementHandle.waitForSelector", "ElementHandle|null", "ElementHandle", new Empty());

    add("Frame.waitForLoadState", "void", "void", new Empty());
    add("Page.waitForLoadState", "void", "void", new Empty());
    add("Frame.waitForTimeout", "void", "void", new Empty());
    add("Page.waitForTimeout", "void", "void", new Empty());
    add("Frame.waitForFunction", "JSHandle", "JSHandle", new Empty());
    add("Page.waitForFunction", "JSHandle", "JSHandle", new Empty());
    add("ElementHandle.waitForElementState", "void", "void", new Empty());

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
    add("BrowserContext.cookies.expires", "float", "long");
    add("BrowserContext.addCookies.cookies", "Array<Object>", "AddCookie");
    add("BrowserContext.addCookies.cookies.sameSite", "\"Lax\"|\"None\"|\"Strict\"", "SameSite", new Empty());
    add("BrowserContext.addCookies.cookies.expires", "float", "Long", new Empty());
    add("BrowserContext.route.url", "RegExp|function(URL):boolean|string", "String");
    add("BrowserContext.unroute.url", "RegExp|function(URL):boolean|string", "String");
    add("BrowserContext.storageState", "Object", "StorageState", new Empty());
    add("BrowserContext.waitForEvent.event", "string", "EventType", new Empty());
    add("BrowserContext.waitForEvent.optionsOrPredicate", "Function|Object", "String");
    add("BrowserContext.waitForEvent", "Promise<any>", "Deferred<Event<EventType>>", new Empty());
    add("Page.waitForNavigation.options.url", "RegExp|function(URL):boolean|string", "Custom");
    add("Page.waitForNavigation.options", "Object", "WaitForNavigationOptions");
    add("Page.waitForRequest.options", "Object", "WaitForRequestOptions");
    add("Page.waitForResponse.options", "Object", "WaitForResponseOptions");
    add("Page.frame.options", "Object", "FrameOptions", new Empty());
    add("Page.route.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.selectOption.values", "Array<ElementHandle>|Array<Object>|Array<string>|ElementHandle|Object|null|string", "String");
    add("Page.setInputFiles.files", "Array<Object>|Array<path>|Object|path", "String");
    add("Page.unroute.url", "RegExp|function(URL):boolean|string", "String");
    add("Page.waitForEvent.event", "string", "EventType", new Empty());
    add("Page.waitForEvent.optionsOrPredicate", "Function|Object", "WaitForEventOptions");
    add("Page.waitForEvent", "Promise<any>", "Deferred<Event<EventType>>", new Empty());
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

    add("Browser.newContext.options.geolocation.latitude", "number", "double");
    add("Browser.newContext.options.geolocation.longitude", "number", "double");
    add("Browser.newContext.options.geolocation.accuracy", "number", "double");
    add("Browser.newPage.options.geolocation.latitude", "number", "double");
    add("Browser.newPage.options.geolocation.longitude", "number", "double");
    add("Browser.newPage.options.geolocation.accuracy", "number", "double");
    add("BrowserType.launchPersistentContext.options.geolocation.latitude", "number", "double");
    add("BrowserType.launchPersistentContext.options.geolocation.longitude", "number", "double");
    add("BrowserType.launchPersistentContext.options.geolocation.accuracy", "number", "double");

    add("BrowserContext.setGeolocation.geolocation", "Object|null", "Geolocation", new Empty());
    add("Browser.newContext.options.geolocation", "Object", "Geolocation", new Empty());
    add("Browser.newContext.options.storageState", "Object|path", "BrowserContext.StorageState", new Empty());
    add("Browser.newPage.options.storageState", "Object|path", "BrowserContext.StorageState", new Empty());
    add("Browser.newPage.options.geolocation", "Object", "Geolocation", new Empty());
    add("BrowserType.launchPersistentContext.options.geolocation", "Object", "Geolocation", new Empty());
    add("Download.saveAs.path", "path", "Path", new Empty());
    add("Download.path", "null|path", "Path", new Empty());
    add("Download.createReadStream", "Readable|null", "InputStream", new Empty());

    // Single field options
    add("Keyboard.type.options", "Object", "int", new Empty());
    add("Keyboard.press.options", "Object", "int", new Empty());

    // node.js types
    add("BrowserServer.process", "ChildProcess", "Object");

    add("Page.pdf", "Buffer", "byte[]", new Empty());
    add("Page.screenshot", "Buffer", "byte[]", new Empty());
    add("ElementHandle.screenshot", "Buffer", "byte[]", new Empty());
    add("Request.postDataBuffer", "Buffer|null", "byte[]", new Empty());
    add("Response.body", "Buffer", "byte[]", new Empty());
    add("Response.finished", "Error|null", "String");
    add("ChromiumBrowser.stopTracing", "Buffer", "byte[]", new Empty());
    add("WebSocket.framereceived.payload", "Buffer|string", "byte[]", new Empty());
    add("WebSocket.framesent.payload", "Buffer|string", "byte[]", new Empty());

    add("BrowserContext.browser", "Browser|null", "Browser");
    add("BrowserContext.cookies.urls", "Array<string>|string", "Custom", new Empty());
    add("Page.$", "ElementHandle|null", "ElementHandle");
    add("Page.frame", "Frame|null", "Frame");
    add("Page.frame.frameSelector", "Object|string", "Custom", new Empty());
    add("Page.getAttribute", "null|string", "String", new Empty());
    add("Page.goBack", "Response|null", "Response", new Empty());
    add("Page.goForward", "Response|null", "Response", new Empty());
    add("Page.goto", "Response|null", "Response", new Empty());
    add("Page.opener", "Page|null", "Page", new Empty());
    add("Page.reload", "Response|null", "Response", new Empty());
    add("Page.textContent", "null|string", "String", new Empty());
    add("Page.video", "Video|null", "Video", new Empty());
    add("Frame.$", "ElementHandle|null", "ElementHandle", new Empty());
    add("Frame.getAttribute", "null|string", "String", new Empty());
    add("Frame.goto", "Response|null", "Response", new Empty());
    add("Frame.parentFrame", "Frame|null", "Frame", new Empty());
    add("Frame.textContent", "null|string", "String", new Empty());
    add("ElementHandle.$", "ElementHandle|null", "ElementHandle", new Empty());
    add("ElementHandle.contentFrame", "Frame|null", "Frame", new Empty());
    add("ElementHandle.getAttribute", "null|string", "String", new Empty());
    add("ElementHandle.ownerFrame", "Frame|null", "Frame", new Empty());
    add("ElementHandle.textContent", "null|string", "String", new Empty());
    add("JSHandle.asElement", "ElementHandle|null", "ElementHandle", new Empty());
    add("Download.failure", "null|string", "String", new Empty());
//    add("Request.failure", "Object|null", "Object", new Empty());
    add("Request.postData", "null|string", "String", new Empty());
    add("Request.redirectedFrom", "Request|null", "Request", new Empty());
    add("Request.redirectedTo", "Request|null", "Request", new Empty());
    add("Request.response", "Response|null", "Response", new Empty());

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

    // JSON type
    add("BrowserContext.addInitScript.arg", "Serializable", "Object");
    add("Page.$eval", "Serializable", "Object");
    add("Page.$$eval", "Serializable", "Object");
    add("Page.addInitScript.arg", "Serializable", "Object");
    add("Page.evaluate", "Serializable", "Object");
    add("Frame.$eval", "Serializable", "Object");
    add("Frame.$$eval", "Serializable", "Object");
    add("Frame.evaluate", "Serializable", "Object");
    add("ElementHandle.$eval", "Serializable", "Object");
    add("ElementHandle.$$eval", "Serializable", "Object");
    add("ElementHandle.evaluate", "Serializable", "Object");
    add("ElementHandle.jsonValue", "Serializable", "Object");
    add("JSHandle.evaluate", "Serializable", "Object");
    add("JSHandle.jsonValue", "Serializable", "Object");
    add("Response.json", "Serializable", "Object");
    add("Worker.evaluate", "Serializable", "Object");

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
