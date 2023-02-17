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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

class Router {
  private List<RouteInfo> routes = new ArrayList<>();

  private static class RouteInfo {
    final UrlMatcher matcher;
    final Consumer<Route> handler;
    Integer times;

    RouteInfo(UrlMatcher matcher, Consumer<Route> handler, Integer times) {
      this.matcher = matcher;
      this.handler = handler;
      this.times = times;
    }

    void handle(RouteImpl route) {
      handler.accept(route);
    }

    boolean decrementRemainingCallCount() {
      if (times == null) {
        return false;
      }
      --times;
      return times <= 0;
    }
  }

  void add(UrlMatcher matcher, Consumer<Route> handler, Integer times) {
    routes.add(0, new RouteInfo(matcher, handler, times));
  }

  void remove(UrlMatcher matcher, Consumer<Route> handler) {
    routes = routes.stream()
      .filter(info -> !info.matcher.equals(matcher) || (handler != null && info.handler != handler))
      .collect(Collectors.toList());
  }

  int size() {
    return routes.size();
  }

  enum HandleResult { NoMatchingHandler, Handled, Fallback, PendingHandler }
  HandleResult handle(RouteImpl route) {
    HandleResult result = HandleResult.NoMatchingHandler;
    for (Iterator<RouteInfo> it = routes.iterator(); it.hasNext();) {
      RouteInfo info = it.next();
      if (!info.matcher.test(route.request().url())) {
        continue;
      }
      if (info.decrementRemainingCallCount()) {
        it.remove();
      }
      route.fallbackCalled = false;
      info.handle(route);
      if (route.isHandled()) {
        return HandleResult.Handled;
      }
      // Not immediately handled and fallback() was not called => the route
      // must be handled asynchronously.
      if (!route.fallbackCalled) {
        route.shouldResumeIfFallbackIsCalled = true;
        return HandleResult.PendingHandler;
      }
      // Fallback was called, continue to the remaining handlers.
      result = HandleResult.Fallback;
    }
    return result;
  }

  JsonObject interceptionPatterns() {
    JsonArray jsonPatterns = new JsonArray();
    for (RouteInfo route : routes) {
      JsonObject jsonPattern = new JsonObject();
      Object urlFilter = route.matcher.rawSource;
      if (urlFilter instanceof String) {
        jsonPattern.addProperty("glob", (String) urlFilter);
      } else if (urlFilter instanceof Pattern) {
        Pattern pattern = (Pattern) urlFilter;
        jsonPattern.addProperty("regexSource", pattern.pattern());
        jsonPattern.addProperty("regexFlags", toJsRegexFlags(pattern));
      } else {
        // Match all requests.
        jsonPattern.addProperty("glob", "**/*");
        jsonPatterns = new JsonArray();
        jsonPatterns.add(jsonPattern);
        break;
      }
      jsonPatterns.add(jsonPattern);
    }
    JsonObject result = new JsonObject();
    result.add("patterns", jsonPatterns);
    return result;
  }
}
