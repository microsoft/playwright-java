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

// Port of harRouter.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.impl.protocol.LocalUtilsHarCloseParams;
import com.microsoft.playwright.impl.protocol.LocalUtilsHarLookupParams;
import com.microsoft.playwright.impl.protocol.LocalUtilsHarLookupResult;
import com.microsoft.playwright.impl.protocol.LocalUtilsHarOpenParams;
import com.microsoft.playwright.impl.protocol.LocalUtilsHarOpenResult;
import com.microsoft.playwright.impl.protocol.NameValue;
import com.microsoft.playwright.options.HarNotFound;
import com.microsoft.playwright.options.HttpHeader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.LoggingSupport.logApiIfEnabled;

class HarRouter {
  private final LocalUtils localUtils;
  private final String harId;
  private final HarNotFound notFoundAction;
  // java: options.urlMatch, a String or a Pattern; there is no baseURL option.
  private final Object urlMatch;

  static HarRouter create(LocalUtils localUtils, String file, HarNotFound notFoundAction, Object urlMatch) {
    LocalUtilsHarOpenParams params = new LocalUtilsHarOpenParams();
    params.file = Paths.get(file).toAbsolutePath().normalize().toString();
    LocalUtilsHarOpenResult result = localUtils.harOpen(params);
    if (result.error != null) {
      throw new PlaywrightException(result.error);
    }
    return new HarRouter(localUtils, result.harId, notFoundAction, urlMatch);
  }

  // java: the Java API passes the HAR file as a Path.
  static HarRouter create(LocalUtils localUtils, Path file, HarNotFound notFoundAction, Object urlMatch) {
    return create(localUtils, file.toString(), notFoundAction, urlMatch);
  }

  private HarRouter(LocalUtils localUtils, String harId, HarNotFound notFoundAction, Object urlMatch) {
    this.localUtils = localUtils;
    this.harId = harId;
    this.urlMatch = urlMatch;
    this.notFoundAction = notFoundAction;
  }

  private void handle(Route route) {
    Request request = route.request();

    LocalUtilsHarLookupParams params = new LocalUtilsHarLookupParams();
    params.harId = harId;
    params.url = request.url();
    params.method = request.method();
    params.headers = toNameValues(request.headersArray());
    params.postData = request.postDataBuffer();
    params.isNavigationRequest = request.isNavigationRequest();
    LocalUtilsHarLookupResult response = localUtils.harLookup(params);

    if ("redirect".equals(response.action)) {
      logApiIfEnabled("HAR: " + route.request().url() + " redirected to " + response.redirectURL);
      ((RouteImpl) route).redirectNavigationRequest(response.redirectURL);
      return;
    }

    if ("fulfill".equals(response.action)) {
      // If the response status is -1, the request was canceled or stalled, so we just stall it here.
      // See https://github.com/microsoft/playwright/issues/29311.
      // TODO: it'd be better to abort such requests, but then we likely need to respect the timing,
      // because the request might have been stalled for a long time until the very end of the
      // test when HAR was recorded but we'd abort it immediately.
      if (response.status == -1) {
        return;
      }

      // route.fulfill does not support multiple set-cookie headers. We need to merge them into one.
      Map<String, String> transformedHeaders = new LinkedHashMap<>();
      for (NameValue header : response.headers) {
        if (!"set-cookie".equals(header.name.toLowerCase())) {
          // non-set-cookie header gets set as-is
          transformedHeaders.put(header.name, header.value);
        } else {
          // first set-cookie header gets included as-is
          if (!transformedHeaders.containsKey("set-cookie")) {
            transformedHeaders.put("set-cookie", header.value);
          } else {
            // subsequent set-cookie headers get appended to existing header
            transformedHeaders.put("set-cookie", transformedHeaders.get("set-cookie") + "\n" + header.value);
          }
        }
      }

      route.fulfill(new Route.FulfillOptions()
        .setStatus(response.status)
        .setHeaders(transformedHeaders)
        .setBodyBytes(response.body));
      return;
    }

    if ("error".equals(response.action)) {
      logApiIfEnabled("HAR: " + response.message);
    }
    // Report the error, but fall through to the default handler.

    if (notFoundAction == HarNotFound.ABORT) {
      route.abort();
      return;
    }

    route.fallback();
  }

  void addContextRoute(BrowserContext context) {
    Object urlMatch = this.urlMatch != null ? this.urlMatch : "**/*";
    // java: route() has an overload per URLMatch type.
    if (urlMatch instanceof Pattern) {
      context.route((Pattern) urlMatch, route -> handle(route));
    } else {
      context.route((String) urlMatch, route -> handle(route));
    }
  }

  void addPageRoute(Page page) {
    Object urlMatch = this.urlMatch != null ? this.urlMatch : "**/*";
    // java: route() has an overload per URLMatch type.
    if (urlMatch instanceof Pattern) {
      page.route((Pattern) urlMatch, route -> handle(route));
    } else {
      page.route((String) urlMatch, route -> handle(route));
    }
  }

  // java: [Symbol.asyncDispose] is not ported, Java has no async dispose.

  void dispose() {
    LocalUtilsHarCloseParams params = new LocalUtilsHarCloseParams();
    params.harId = harId;
    try {
      localUtils.harClose(params);
    } catch (PlaywrightException e) {
    }
  }

  // java: the public HttpHeader list of Request.headersArray() as protocol headers.
  private static List<NameValue> toNameValues(List<HttpHeader> headers) {
    List<NameValue> result = new ArrayList<>();
    for (HttpHeader header : headers) {
      NameValue nv = new NameValue();
      nv.name = header.name;
      nv.value = header.value;
      result.add(nv);
    }
    return result;
  }
}
