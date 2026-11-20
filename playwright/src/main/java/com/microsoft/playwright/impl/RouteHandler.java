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

import com.microsoft.playwright.Route;
import com.microsoft.playwright.impl.protocol.BrowserContextSetNetworkInterceptionPatternsParams.BrowserContextSetNetworkInterceptionPatternsParamsPatterns;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

class RouteHandler {
  private int handledCount = 0;
  private final String baseURL;
  private final int times;
  final UrlMatcher url;
  final Consumer<Route> handler;
  private boolean ignoreException = false;
  private final Set<HandlerInvocation> activeInvocations = new LinkedHashSet<>();

  // java: { complete: Promise<void>, route: Route }.
  private static class HandlerInvocation {
    final WaitableResult<Void> complete = new WaitableResult<>();
    final RouteImpl route;

    HandlerInvocation(RouteImpl route) {
      this.route = route;
    }
  }

  // java: UrlMatcher.forGlob() has resolved and validated a glob against the base URL already. A null times
  // is Number.MAX_SAFE_INTEGER.
  RouteHandler(String baseURL, UrlMatcher url, Consumer<Route> handler, Integer times) {
    this.baseURL = baseURL;
    this.times = times != null ? times : Integer.MAX_VALUE;
    this.url = url;
    this.handler = handler;
  }

  // java: the result is a list of the BrowserContext pattern struct; the Page struct has the same fields
  // and serializes the same, so the element type is left to the caller.
  @SuppressWarnings("unchecked")
  static <T> List<T> prepareInterceptionPatterns(List<RouteHandler> handlers) {
    List<BrowserContextSetNetworkInterceptionPatternsParamsPatterns> patterns = new ArrayList<>();
    boolean all = false;
    for (RouteHandler handler : handlers) {
      BrowserContextSetNetworkInterceptionPatternsParamsPatterns serialized = serializeURLMatch(handler.url);
      if (serialized != null) {
        patterns.add(serialized);
      } else {
        all = true;
      }
    }
    if (all) {
      BrowserContextSetNetworkInterceptionPatternsParamsPatterns pattern = new BrowserContextSetNetworkInterceptionPatternsParamsPatterns();
      pattern.glob = "**/*";
      patterns = new ArrayList<>();
      patterns.add(pattern);
    }
    return (List<T>) (List<?>) patterns;
  }

  boolean matches(String requestURL) {
    return url.test(requestURL);
  }

  // java: there are no zones to restore.
  boolean handle(RouteImpl route) {
    return handleImpl(route);
  }

  private boolean handleImpl(RouteImpl route) {
    HandlerInvocation handlerInvocation = new HandlerInvocation(route);
    activeInvocations.add(handlerInvocation);
    try {
      return handleInternal(route);
    } catch (RuntimeException e) {
      // If the handler was stopped (without waiting for completion), we ignore all exceptions.
      if (ignoreException) {
        return false;
      }
      if (e instanceof TargetClosedError) {
        // We are failing in the handler because the target close closed.
        // Give user a hint!
        // java: exceptions are immutable, a new one carries the rewritten message.
        throw new TargetClosedError("\"" + e.getMessage() + "\" while running route callback.\nConsider awaiting `await page.unrouteAll({ behavior: 'ignoreErrors' })`\nbefore the end of the test to ignore remaining routes in flight.", e);
      }
      throw e;
    } finally {
      handlerInvocation.complete.complete(null);
      activeInvocations.remove(handlerInvocation);
    }
  }

  void stop(String behavior) {
    // When a handler is manually unrouted or its page/context is closed we either
    // - wait for the current handler invocations to finish
    // - or do not wait, if the user opted out of it, but swallow all exceptions
    //   that happen after the unroute/close.
    if ("ignoreErrors".equals(behavior)) {
      ignoreException = true;
    } else {
      // java: the handlers are synchronous, so every active invocation that did not throw is a caller
      // further up this stack and completes only after this call returns; there is nothing to wait for.
    }
  }

  private boolean handleInternal(RouteImpl route) {
    ++handledCount;
    WaitableResult<Boolean> handledPromise = route.startHandling();
    // Extract handler into a variable to avoid [RouteHandler.handler] in the stack.
    Consumer<Route> handler = this.handler;
    handler.accept(route);
    // java: Promise.all([handledPromise, handler(route, route.request())]) cannot wait for a route the
    // handler keeps to handle later: the chain stops here as if the route was handled, and a later
    // fallback() continues the request (see RouteImpl.fallback).
    if (!handledPromise.isDone()) {
      route.handlerReturned = true;
      return true;
    }
    return handledPromise.get();
  }

  boolean willExpire() {
    return handledCount + 1 >= times;
  }

  // java: serializeURLMatch of isomorphic/urlMatch.ts for a UrlMatcher.
  private static BrowserContextSetNetworkInterceptionPatternsParamsPatterns serializeURLMatch(UrlMatcher match) {
    BrowserContextSetNetworkInterceptionPatternsParamsPatterns result = new BrowserContextSetNetworkInterceptionPatternsParamsPatterns();
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
