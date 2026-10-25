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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/firefox-network-response.spec.ts. */
@EnabledOnBrowser(value = BrowserName.FIREFOX, reason = "Tests the Firefox network observer.")
public class TestFirefoxNetworkResponse {
  @BeforeEach
  void skipOnBidi() {
    assumeFalse(Env.isBidi(), "Tests the Firefox network observer.");
  }

  private static List<String> header(String name, String value) {
    return Arrays.asList(name, value);
  }

  private static void expectHeaderFields(Response response, List<List<String>> expected) {
    List<HttpHeader> headers = response.headersArray();
    Map<String, String> allHeaders = response.allHeaders();
    Set<String> names = new LinkedHashSet<>();
    for (List<String> header : expected) {
      names.add(header.get(0).toLowerCase());
    }
    for (String name : names) {
      List<List<String>> fields = new ArrayList<>();
      for (List<String> header : expected) {
        if (header.get(0).toLowerCase().equals(name)) {
          fields.add(header);
        }
      }
      List<String> values = new ArrayList<>();
      for (List<String> field : fields) {
        values.add(field.get(1));
      }
      List<List<String>> actualFields = new ArrayList<>();
      for (HttpHeader h : headers) {
        if (h.name.toLowerCase().equals(name)) {
          actualFields.add(header(h.name, h.value));
        }
      }
      assertEquals(fields, actualFields);
      assertEquals(values, response.headerValues(name));
      String combined = String.join(name.equals("set-cookie") ? "\n" : ", ", values);
      assertEquals(combined, response.headerValue(name));
      assertEquals(combined, allHeaders.get(name));
    }
  }

  private static class Fetched {
    Response response;
    Object fetched;
  }

  private static Fetched fetchResponse(Page page, String url) {
    Fetched result = new Fetched();
    // Promise.all
    result.response = page.waitForResponse(url, () -> {
      result.fetched = page.evaluate("async url => {\n" +
        "      const response = await fetch(url);\n" +
        "      return { status: response.status, body: await response.text() };\n" +
        "    }", url);
    });
    return result;
  }

  @Nested
  @ParameterizedClass
  @ValueSource(ints = {200, 302})
  class ShouldPreserveOriginalResponseHeaderFieldsForStatusResponses {
    @Parameter
    int status;

    @PlaywrightTest
    @DisplayName("FF-005 should preserve original response header fields for ${status} responses")
    @Fixme("harness: TestServer.Response.writeHead keeps one value per header name, so repeated header fields (X-Identical, Set-Cookie, ...) cannot be sent; first assertion that fails: expected [[X-Identical, same], [X-Identical, same]] but was [[X-Identical, same]]")
    void shouldPreserveOriginalResponseHeaderFieldsForStatusResponses(Page page, TestServer server) {
      List<List<String>> headers = Arrays.asList(
        header("Date", "Wed, 21 Oct 2037 07:28:00 GMT"),
        header("X-Literal", "first, \"second, third\""),
        header("X-Identical", "same"),
        header("X-Identical", "same"),
        header("X-Repeat", "first, literal"),
        header("x-repeat", "second"),
        header("Set-Cookie", "a=b; Expires=Wed, 21 Oct 2037 07:28:00 GMT"),
        header("Set-Cookie", "c=d; Expires=Wed, 21 Oct 2037 07:28:00 GMT; Path=/"),
        header("WWW-Authenticate", "Digest realm=\"one, two\", qop=\"auth, auth-int\""),
        header("WWW-Authenticate", "Basic realm=\"three, four\""),
        header("Proxy-Authenticate", "Basic realm=\"five, six\""),
        header("Proxy-Authenticate", "Basic realm=\"five, six\""));
      server.setRoute("/headers", (request, response) -> {
        List<String> pairs = new ArrayList<>(Arrays.asList("Content-Type", "text/html"));
        if (status == 302) {
          pairs.addAll(Arrays.asList("Location", "/empty.html"));
        }
        for (List<String> header : headers) {
          pairs.add(header.get(0));
          pairs.add(header.get(1));
        }
        response.writeHead(status, pairs.toArray(new String[0]));
        response.end("body");
      });
      Response finalResponse = page.navigate(server.PREFIX + "/headers");
      Response response = status == 302 ? finalResponse.request().redirectedFrom().response() : finalResponse;
      assertEquals(status, response.status());
      expectHeaderFields(response, headers);
    }
  }

  @PlaywrightTest
  @DisplayName("FF-006 should preserve synthesized response header fields")
  void shouldPreserveSynthesizedResponseHeaderFields(Page page, TestServer server) {
    List<String> cookies = Arrays.asList(
      "a=b; Expires=Wed, 21 Oct 2037 07:28:00 GMT",
      "c=d; Expires=Wed, 21 Oct 2037 07:28:00 GMT; Path=/");
    page.route("**/headers", route -> {
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("X-Literal", "first, \"second, third\"");
      headers.put("Date", "Wed, 21 Oct 2037 07:28:00 GMT");
      headers.put("Set-Cookie", String.join("\n", cookies));
      headers.put("WWW-Authenticate", "Digest realm=\"one, two\", qop=\"auth, auth-int\"");
      route.fulfill(new Route.FulfillOptions()
        .setContentType("text/html")
        .setHeaders(headers)
        .setBody("fulfilled"));
    });
    Response response = page.navigate(server.PREFIX + "/headers");
    assertEquals("fulfilled", response.text());
    expectHeaderFields(response, Arrays.asList(
      header("x-literal", "first, \"second, third\""),
      header("date", "Wed, 21 Oct 2037 07:28:00 GMT"),
      header("set-cookie", cookies.get(0)),
      header("set-cookie", cookies.get(1)),
      header("www-authenticate", "Digest realm=\"one, two\", qop=\"auth, auth-int\"")));
  }

  @PlaywrightTest
  @DisplayName("FF-007 should report current effective fields after cache revalidation")
  @Fixme("harness: TestServer.Response.writeHead keeps one value per header name, so repeated header fields (X-Keep, X-Change, Set-Cookie, ...) cannot be sent; first assertion that fails: expected <old, literal, old-second> but was <old-second>")
  void shouldReportCurrentEffectiveFieldsAfterCacheRevalidation(Page page, TestServer server) {
    List<String> validators = new ArrayList<>();
    server.setRoute("/cached-headers", (request, response) -> {
      validators.add(request.headers.get("if-none-match"));
      if (validators.size() == 1) {
        response.writeHead(200,
          "Content-Type", "text/plain",
          "ETag", "\"v1\"",
          "Cache-Control", "max-age=0, must-revalidate",
          "X-Keep", "first, literal",
          "X-Keep", "second",
          "X-Change", "old, literal",
          "X-Change", "old-second",
          "Set-Cookie", "old=value; Expires=Wed, 21 Oct 2037 07:28:00 GMT",
          "Set-Cookie", "old2=value; Path=/",
          "WWW-Authenticate", "Basic realm=\"old, realm\"",
          "WWW-Authenticate", "Basic realm=\"old-second\"",
          "Proxy-Authenticate", "Basic realm=\"old, proxy\"",
          "Proxy-Authenticate", "Basic realm=\"old-proxy-second\"");
        response.end("cached-body");
        return;
      }
      response.writeHead(304,
        "Cache-Control", "max-age=3600",
        "X-Change", "new, literal",
        "X-Change", "new-second",
        "Set-Cookie", "new=value; Expires=Wed, 21 Oct 2037 07:28:00 GMT",
        "Set-Cookie", "new2=value; Path=/",
        "WWW-Authenticate", "Basic realm=\"new, realm\"",
        "WWW-Authenticate", "Basic realm=\"new-second\"",
        "Proxy-Authenticate", "Basic realm=\"new, proxy\"",
        "Proxy-Authenticate", "Basic realm=\"new-proxy-second\"");
      response.end();
    });

    page.navigate(server.EMPTY_PAGE);
    String url = server.PREFIX + "/cached-headers";
    Fetched initial = fetchResponse(page, url);
    Fetched revalidated = fetchResponse(page, url);
    Fetched cached = fetchResponse(page, url);

    assertEquals(Arrays.asList(null, "\"v1\""), validators);
    for (Fetched f : Arrays.asList(initial, revalidated, cached)) {
      assertEquals(mapOf("status", 200, "body", "cached-body"), f.fetched);
    }
    assertEquals(Arrays.asList(200, 304, 200), Arrays.asList(initial.response.status(), revalidated.response.status(), cached.response.status()));
    assertEquals("old, literal, old-second", initial.response.headerValue("x-change"));
    assertEquals(Arrays.asList(), revalidated.response.headerValues("x-keep"));
    assertEquals(Arrays.asList("new, literal", "new-second"), revalidated.response.headerValues("x-change"));
    assertEquals(Arrays.asList(
      "new=value; Expires=Wed, 21 Oct 2037 07:28:00 GMT",
      "new2=value; Path=/"), revalidated.response.headerValues("set-cookie"));
    assertEquals(Arrays.asList("Basic realm=\"new, realm\"", "Basic realm=\"new-second\""), revalidated.response.headerValues("www-authenticate"));
    assertEquals(Arrays.asList("Basic realm=\"new, proxy\"", "Basic realm=\"new-proxy-second\""), revalidated.response.headerValues("proxy-authenticate"));
    assertEquals(Arrays.asList("first, literal, second"), cached.response.headerValues("x-keep"));
    assertEquals(Arrays.asList("new, literal, new-second"), cached.response.headerValues("x-change"));
    for (String name : Arrays.asList("set-cookie", "www-authenticate", "proxy-authenticate")) {
      assertEquals(Arrays.asList(), cached.response.headerValues(name));
    }
  }

  @PlaywrightTest
  @DisplayName("FF-008 should report a standalone 304 response without waiting for a cache merge")
  void shouldReportAStandalone304ResponseWithoutWaitingForACacheMerge(Page page, TestServer server) {
    server.setRoute("/standalone-304", (request, response) -> {
      response.writeHead(304,
        "X-Literal", "standalone, value",
        "Cache-Control", "no-store");
      response.end();
    });
    page.navigate(server.EMPTY_PAGE);
    Fetched f = fetchResponse(page, server.PREFIX + "/standalone-304");
    assertEquals(304, f.response.status());
    assertEquals(mapOf("status", 304, "body", ""), f.fetched);
    assertEquals(Arrays.asList("standalone, value"), f.response.headerValues("x-literal"));
    assertNull(f.response.finished());
  }
}
