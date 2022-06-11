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

import com.microsoft.playwright.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    boolean handle(Route route) {
      if (times != null && times <= 0) {
        return false;
      }
      if (!matcher.test(route.request().url())) {
        return false;
      }
      if (times != null) {
        --times;
      }
      handler.accept(route);
      return true;
    }

    boolean isDone() {
      return times != null && times <= 0;
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

  enum HandleResult { NoMatchingHandler, MatchedHandlerButNotHandled, Handled }
  HandleResult handle(RouteImpl route) {
    HandleResult result = HandleResult.NoMatchingHandler;
    for (Iterator<RouteInfo> it = routes.iterator(); it.hasNext();) {
      RouteInfo info = it.next();
      if (info.handle(route)) {
        if (info.isDone()) {
          it.remove();
        }
        if (route.takeLastHandlerGaveUp()) {
          result = HandleResult.MatchedHandlerButNotHandled;
          continue;
        }
        return HandleResult.Handled;
      }
    }
    return result;
  }
}
