package com.microsoft.playwright.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.WebSocketRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

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
      if (routeInfo.matcher.test(route.url())) {
        routeInfo.handle(route);
        return true;
      }
    }
    return false;
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
