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

// Port of network.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.WebSocketRoute;
import com.microsoft.playwright.impl.protocol.BrowserContextSetWebSocketInterceptionPatternsParams.BrowserContextSetWebSocketInterceptionPatternsParamsPatterns;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

class WebSocketRouteHandler {
  private final String baseURL;
  final UrlMatcher url;
  final Consumer<WebSocketRoute> handler;

  // java: the UrlMatcher has resolved a glob against the base URL and validated it already, baseURL is
  // kept for the upstream signature only.
  WebSocketRouteHandler(String baseURL, UrlMatcher url, Consumer<WebSocketRoute> handler) {
    this.baseURL = baseURL;
    this.url = url;
    this.handler = handler;
  }

  // java: the base URL of the context as a java.net.URL.
  WebSocketRouteHandler(URL baseURL, UrlMatcher url, Consumer<WebSocketRoute> handler) {
    this(baseURL != null ? baseURL.toString() : null, url, handler);
  }

  // java: the result is a list of the BrowserContext pattern struct; the Page struct has the same fields
  // and serializes the same, so the element type is left to the caller.
  @SuppressWarnings("unchecked")
  static <T> List<T> prepareInterceptionPatterns(List<WebSocketRouteHandler> handlers) {
    List<BrowserContextSetWebSocketInterceptionPatternsParamsPatterns> patterns = new ArrayList<>();
    boolean all = false;
    for (WebSocketRouteHandler handler : handlers) {
      BrowserContextSetWebSocketInterceptionPatternsParamsPatterns serialized = serializeURLMatch(handler.url);
      if (serialized != null) {
        patterns.add(serialized);
      } else {
        all = true;
      }
    }
    if (all) {
      BrowserContextSetWebSocketInterceptionPatternsParamsPatterns pattern = new BrowserContextSetWebSocketInterceptionPatternsParamsPatterns();
      pattern.glob = "**/*";
      patterns = new ArrayList<>();
      patterns.add(pattern);
    }
    return (List<T>) (List<?>) patterns;
  }

  boolean matches(String wsURL) {
    return url.test(wsURL);
  }

  void handle(WebSocketRouteImpl webSocketRoute) {
    Consumer<WebSocketRoute> handler = this.handler;
    handler.accept(webSocketRoute);
    webSocketRoute.afterHandle();
  }

  // java: serializeURLMatch of isomorphic/urlMatch.ts for a UrlMatcher.
  private static BrowserContextSetWebSocketInterceptionPatternsParamsPatterns serializeURLMatch(UrlMatcher match) {
    BrowserContextSetWebSocketInterceptionPatternsParamsPatterns result = new BrowserContextSetWebSocketInterceptionPatternsParamsPatterns();
    if (match.glob != null) {
      result.glob = match.glob;
      return result;
    }
    if (match.pattern != null) {
      result.regexSource = match.pattern.pattern();
      result.regexFlags = toJsRegexFlags(match.pattern);
      return result;
    }
    // Functions cannot be serialized
    return null;
  }
}
