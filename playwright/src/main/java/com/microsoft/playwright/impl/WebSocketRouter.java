package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.WebSocketRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WebSocketRouter {
    private List<RouteInfo> routes = new ArrayList<>();

  private static class RouteInfo {
    final UrlMatcher matcher;
    private final Consumer<WebSocketRoute> handler;

    RouteInfo(UrlMatcher matcher, Consumer<WebSocketRoute> handler) {
      this.matcher = matcher;
      this.handler = handler;
    }

    void handle(WebSocketRouteImpl route) {
      handler.accept(route);
      route.afterHandle();
    }
  }

  void add(UrlMatcher matcher, Consumer<WebSocketRoute> handler) {
    routes.add(0, new RouteInfo(matcher, handler));
  }

  boolean handle(WebSocketRouteImpl route) {
    for (RouteInfo routeInfo: routes) {
      if (routeInfo.matcher.testWebsocket(route.url())) {
        routeInfo.handle(route);
        return true;
      }
    }
    return false;
  }

  JsonObject interceptionPatterns() {
    List<UrlMatcher> matchers = routes.stream().map(r -> r.matcher).collect(Collectors.toList());
    return Utils.interceptionPatterns(matchers);
  }
}
