package com.microsoft.playwright;

import com.google.gson.Gson;
import com.microsoft.playwright.options.HttpCredentials;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestGlobalFetch extends TestBase {
  @Test
  void shouldHaveJavaInDefaultUesrAgent() throws ExecutionException, InterruptedException {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions());
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertEquals(server.EMPTY_PAGE, response.url());
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version =  version.substring(0, dot);
      }
    }
    assertTrue(serverRequest.get().headers.get("user-agent").get(0).contains("java/" + version));
  }

  @Test
  void fetchShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.fetch(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void deleteShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.delete(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void getShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void headShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.head(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("text/html", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("text/html", contentType.get().value);
    assertEquals("", response.text());
  }

  @Test
  void patchShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.patch(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void postShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.post(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void putShouldWork() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.put(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldDisposeGlobalRequest() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.PREFIX + "/simple.json");
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
    request.dispose();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(e.getMessage().contains("Response has been disposed"), e.getMessage());
  }

  @Test
  void shouldSupportGlobalUserAgentOption() throws ExecutionException, InterruptedException {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setUserAgent("My Agent"));
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertEquals(server.EMPTY_PAGE, response.url());
    assertEquals(asList("My Agent"), serverRequest.get().headers.get("user-agent"));
  }

  @Test
  void shouldSupportGlobalTimeoutOption() {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setTimeout(100));
    server.setRoute("/empty.html", exchange -> {});
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> request.get(server.EMPTY_PAGE));
    assertTrue(e.getMessage().contains("Request timed out after 100ms"), e.getMessage());
  }


  @Test
  void shouldPropagateExtraHttpHeadersWithRedirects() throws ExecutionException, InterruptedException {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setExtraHTTPHeaders(mapOf("My-Secret", "Value")));
    Future<Server.Request> req1 = server.futureRequest("/a/redirect1");
    Future<Server.Request> req2 = server.futureRequest("/b/c/redirect2");
    Future<Server.Request> req3 = server.futureRequest("/simple.json");
    request.get(server.PREFIX + "/a/redirect1");
    assertEquals(asList("Value"), req1.get().headers.get("my-secret"));
    assertEquals(asList("Value"), req2.get().headers.get("my-secret"));
    assertEquals(asList("Value"), req3.get().headers.get("my-secret"));
  }

  @Test
  void shouldSupportGlobalHttpCredentialsOption() {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request1 = playwright.request().newContext();
    APIResponse response1 = request1.get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());
    request1.dispose();

    APIRequestContext request2 = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials("user", "pass"));
    APIResponse response2 = request2.get(server.EMPTY_PAGE);
    assertEquals(200, response2.status());
    request2.dispose();
  }

  @Test
  void shouldReturnErrorWithWrongCredentials() {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials("user", "wrong"));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
  }

  void shouldUseSocksProxy() {
  }

  void shouldPassProxyCredentials() {
  }

  @Test
  @Disabled("Error: socket hang up")
  void shouldSupportGlobalIgnoreHTTPSErrorsOption() {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
    APIResponse response = request.get(httpsServer.EMPTY_PAGE);
    assertEquals(200, response.status());
  }

  @Test
  @Disabled("Error: socket hang up")
  void shouldPropagateIgnoreHTTPSErrorsOnRedirects() {
    httpsServer.setRedirect("/redir", "/empty.html");
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(httpsServer.PREFIX + "/redir", RequestOptions.create().setIgnoreHTTPSErrors(true));
    assertEquals(200, response.status());
  }

  @Test
  void shouldResolveUrlRelativeToGobalBaseURLOption() {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(server.PREFIX));
    APIResponse response = request.get("/empty.html");
    assertEquals(server.EMPTY_PAGE, response.url());
  }

  @Test
  void shouldSetPlaywrightAsUserAgent() throws ExecutionException, InterruptedException {
    APIRequestContext request = playwright.request().newContext();
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    List<String> headers = serverRequest.get().headers.get("user-agent");
    assertNotNull(headers);
    assertEquals(1, headers.size());
    assertTrue(headers.get(0).startsWith("Playwright/"), headers.get(0));
  }

  void shouldBeAbleToConstructWithContextOptions() {
  }

  @Test
  void shouldReturnEmptyBody() {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.EMPTY_PAGE);
    byte[] body = response.body();
    assertEquals(0, body.length);
    assertEquals("", response.text());
    request.dispose();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(e.getMessage().contains("Response has been disposed"), e.getMessage());
  }

  @Test
  void shouldRemoveContentLengthFromReidrectedPostRequests() throws ExecutionException, InterruptedException {
    server.setRedirect("/redirect", "/empty.html");
    APIRequestContext request = playwright.request().newContext();
    Future<Server.Request> req1 = server.futureRequest("/redirect");
    Future<Server.Request> req2 = server.futureRequest("/empty.html");
    APIResponse result = request.post(server.PREFIX + "/redirect", RequestOptions.create().setData(mapOf("foo", "bar")));

    assertEquals(200, result.status());
    assertEquals(asList("13"), req1.get().headers.get("content-length"));
    assertNull(req2.get().headers.get("content-length"));
    request.dispose();
  }

  private static final List<Object> values = asList(
    mapOf("foo", "bar"),
    new Object[] {"foo", "bar", 2021},
    "foo",
    true,
    2021
  );

  @Test
  void shouldJsonStringifyTypeBodyWhenContentTypeIsApplicationJson() throws ExecutionException, InterruptedException {
    APIRequestContext request = playwright.request().newContext();
    for (Object value : values) {
      Future<Server.Request> req = server.futureRequest("/empty.html");
      request.post(server.EMPTY_PAGE, RequestOptions.create().setHeader("content-type", "application/json").setData(value));
      byte[] body = req.get().postBody;
      assertEquals(new Gson().toJson(value), new String(body));
    }
    request.dispose();
  }

  @Test
  void shouldNotDoubleStringifyTypeBodyWhenContentTypeIsApplicationJson() throws ExecutionException, InterruptedException {
    APIRequestContext request = playwright.request().newContext();
    for (Object value : values) {
      String stringifiedValue = new Gson().toJson(value);
      Future<Server.Request> req = server.futureRequest("/empty.html");
      request.post(server.EMPTY_PAGE, RequestOptions.create()
        .setHeader("content-type", "application/json")
        .setData(stringifiedValue));
      byte[] body = req.get().postBody;
      assertEquals(stringifiedValue, new String(body));
    }
    request.dispose();
  }

  @Test
  void shouldReturnBodyForFailingRequests() {
    APIRequestContext request = playwright.request().newContext();
    for (String method : new String[] {"head", "put", "trace"}) {
      server.setRoute("/empty.html", exchange -> {
        exchange.getResponseHeaders().set("Content-type", "text/plain");
        exchange.sendResponseHeaders(404, 10);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write("Not found.");
        }
      });
      APIResponse response = request.fetch(server.EMPTY_PAGE, RequestOptions.create().setMethod(method));
      assertEquals(404, response.status());
      // HEAD response returns empty body in node http module.
      assertEquals("head".equals(method) ? "" : "Not found.", response.text());
    }
    request.dispose();
  }

  @Test
  void shouldThrowAnErrorWhenMaxRedirectsIsExceeded() {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/b/c/redirect3");
    server.setRedirect("/b/c/redirect3", "/b/c/redirect4");
    server.setRedirect("/b/c/redirect4", "/simple.json");

    APIRequestContext request = playwright.request().newContext();
    for (String method : new String[] {"GET", "PUT", "POST", "OPTIONS", "HEAD", "PATCH"}) {
      for (int maxRedirects = 1; maxRedirects < 4; maxRedirects++) {
        int currMaxRedirects = maxRedirects;
        PlaywrightException exception = assertThrows(PlaywrightException.class,
          () -> request.fetch(server.PREFIX + "/a/redirect1",
          RequestOptions.create().setMethod(method).setMaxRedirects(currMaxRedirects)));
        assertTrue(exception.getMessage().contains("Max redirect count exceeded"), exception.getMessage());
      }
    }
    request.dispose();
  }

  @Test
  void shouldNotFollowRedirectsWhenMaxRedirectsIsSetTo0() {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");

    APIRequestContext request = playwright.request().newContext();
    for (String method : new String[] {"GET", "PUT", "POST", "OPTIONS", "HEAD", "PATCH"}) {
      APIResponse response = request.fetch(server.PREFIX + "/a/redirect1",
        RequestOptions.create().setMethod(method).setMaxRedirects(0));
      assertEquals("/b/c/redirect2", response.headers().get("location"));
      assertEquals(302, response.status());
    }
    request.dispose();
  }

  @Test
  void shouldThrowAnErrorWhenMaxRedirectsIsLessThan0() {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");

    APIRequestContext request = playwright.request().newContext();
    for (String method : new String[] {"GET", "PUT", "POST", "OPTIONS", "HEAD", "PATCH"}) {
      PlaywrightException exception = assertThrows(PlaywrightException.class,
        () -> request.fetch(server.PREFIX + "/a/redirect1",
          RequestOptions.create().setMethod(method).setMaxRedirects(-1)));
      assertTrue(exception.getMessage().contains("'maxRedirects' should be greater than or equal to '0'"), exception.getMessage());
    }
    request.dispose();
  }

  @Test
  void shouldNotModifyRequestMethodInOptions() {
    APIRequestContext request = playwright.request().newContext();
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().set("Content-type", "text/plain");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write(exchange.getRequestMethod());
      }
    });
    RequestOptions options = RequestOptions.create();
    options.setTimeout(10000);
    {
      APIResponse response = request.fetch(server.EMPTY_PAGE, options);
      assertTrue(response.ok());
      assertEquals("GET", response.text());
    }
    {
      APIResponse response = request.delete(server.EMPTY_PAGE, options);
      assertTrue(response.ok());
      assertEquals("DELETE", response.text());
    }
    {
      APIResponse response = request.put(server.EMPTY_PAGE, options);
      assertTrue(response.ok());
      assertEquals("PUT", response.text());
    }
    request.dispose();
  }

  @Test
  void shouldSupportGlobalHttpCredentialsOptionAndMatchingOrigin() {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request1 = playwright.request().newContext();
    APIResponse response1 = request1.get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());
    request1.dispose();

    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(server.PREFIX);
    APIRequestContext request2 = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials(httpCredentials));
    APIResponse response2 = request2.get(server.EMPTY_PAGE);
    assertEquals(200, response2.status());
    request2.dispose();
  }

  @Test
  void shouldSupportGlobalHttpCredentialsOptionAndMatchingOriginCaseInsensitive() {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request1 = playwright.request().newContext();
    APIResponse response1 = request1.get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());
    request1.dispose();

    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(server.PREFIX.toUpperCase());
    APIRequestContext request2 = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials(httpCredentials));
    APIResponse response2 = request2.get(server.EMPTY_PAGE);
    assertEquals(200, response2.status());
    request2.dispose();
  }

  @Test
  void shouldReturnErrorWithCorrectCredentialsAndWrongOriginScheme() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginScheme(server));
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials(httpCredentials));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
  }

  @Test
  void shouldReturnErrorWithCorrectCredentialsAndWrongOriginHostname() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginHostname(server));
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials(httpCredentials));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
  }

  @Test
  void shouldReturnErrorWithCorrectCredentialsAndWrongOriginPort() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginPort(server));
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials(httpCredentials));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
  }

}
