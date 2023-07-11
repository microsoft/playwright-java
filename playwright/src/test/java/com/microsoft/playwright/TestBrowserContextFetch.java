package com.microsoft.playwright;

import com.google.gson.Gson;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.Utils.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextFetch extends TestBase {
  @Test
  void getShouldWork() {
    APIResponse response = context.request().get(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertNotNull(response.ok());
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals("application/json", response.headers().get("content-type"));
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void fetchShouldWork() {
    APIResponse response = context.request().fetch(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertNotNull(response.ok());
    assertEquals(server.PREFIX + "/simple.json", response.url());
    Optional<HttpHeader> contentType = response.headersArray().stream().filter(h -> "content-type".equals(h.name.toLowerCase())).findFirst();
    assertTrue(contentType.isPresent());
    assertEquals("application/json", contentType.get().value);
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  //     server.setRoute("/one-style.css", exchange -> exchange.getResponseBody().close());
  @Test
  void shouldThrowOnNetworkError() {
    server.setRoute("/test", exchange -> exchange.getResponseBody().close());
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/test"));
    assertTrue(e.getMessage().contains("socket hang up"), e.getMessage());
  }

  @Test
  void shouldThrowOnNetworkErrorAfterRedirect() {
    server.setRedirect("/redirect", "/test");
    server.setRoute("/test", exchange -> exchange.getResponseBody().close());
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/redirect"));
    assertTrue(e.getMessage().contains("socket hang up"), e.getMessage());
  }

  @Test
  void shouldThrowOnNetworkErrorWhenSendingBody() {
    server.setRoute("/test", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 4096);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<title>A");
      }
    });
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/test"));
    assertTrue(e.getMessage().contains("aborted"), e.getMessage());
  }

  @Test
  void shouldThrowOnNetworkErrorWhenSendingBodyAfterRedirect() {
    server.setRedirect("/redirect", "/test");
    server.setRoute("/test", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 4096);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<title>A");
      }
    });
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/redirect"));
    assertTrue(e.getMessage().contains("aborted"), e.getMessage());
  }

  @Test
  void shouldAddSessionCookiesToRequest() throws ExecutionException, InterruptedException {
    Cookie cookie = new Cookie("username", "John Doe");
    cookie.domain = "localhost";
    cookie.path = "/";
    cookie.expires = -1.0;
    cookie.httpOnly = false;
    cookie.secure = false;
    cookie.sameSite = SameSiteAttribute.LAX;
    context.addCookies(asList(cookie));
    Future<Server.Request> req = server.futureRequest("/simple.json");
    context.request().get(server.PREFIX + "/simple.json");
    assertEquals(asList("username=John Doe"), req.get().headers.get("cookie"));
  }

  @Test
  void getShouldSupportQueryParams() throws ExecutionException, InterruptedException {
    Future<Server.Request> req = server.futureRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE + "?p1=foo",
      RequestOptions.create().setQueryParam("p1", "v1").setQueryParam("парам2", "знач2"));
    assertNotNull(req.get());
    assertEquals("/empty.html?p1=v1&%D0%BF%D0%B0%D1%80%D0%B0%D0%BC2=%D0%B7%D0%BD%D0%B0%D1%872", req.get().url);
  }

  ;

  @Test
  void getShouldSupportFailOnStatusCode() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      context.request().get(server.PREFIX + "/does-not-exist.html", RequestOptions.create().setFailOnStatusCode(true));
    });
    assertTrue(e.getMessage().contains("404 Not Found"), e.getMessage());
  }

  @Test
  @Disabled("Error: socket hang up")
  void getShouldSupportIgnoreHTTPSErrorsOption() {
    APIResponse response = context.request().get(httpsServer.EMPTY_PAGE, RequestOptions.create().setIgnoreHTTPSErrors(true));
    assertEquals(200, response.status());
  }

  @Test
  void shouldNotAddContextCookieIfCookieHeaderPassedAsAParameter() throws ExecutionException, InterruptedException {
    Cookie cookie = new Cookie("username", "John Doe");
    cookie.domain = "localhost";
    cookie.path = "/";
    cookie.expires = -1.0;
    cookie.httpOnly = false;
    cookie.secure = false;
    cookie.sameSite = SameSiteAttribute.LAX;
    context.addCookies(asList(cookie));
    Future<Server.Request> req = server.futureRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE, RequestOptions.create().setHeader("Cookie", "foo=bar"));
    assertEquals(asList("foo=bar"), req.get().headers.get("cookie"));
  }

  @Test
  void shouldFollowRedirects() throws ExecutionException, InterruptedException {
    server.setRedirect("/redirect1", "/redirect2");
    server.setRedirect("/redirect2", "/simple.json");
    Cookie cookie = new Cookie("username", "John Doe");
    cookie.domain = "localhost";
    cookie.path = "/";
    cookie.expires = -1.0;
    cookie.httpOnly = false;
    cookie.secure = false;
    cookie.sameSite = SameSiteAttribute.LAX;
    context.addCookies(asList(cookie));

    Future<Server.Request> req = server.futureRequest("/simple.json");
    APIResponse response = context.request().get(server.PREFIX + "/redirect1");
    assertEquals(asList("username=John Doe"), req.get().headers.get("cookie"));
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldAddCookiesFromSetCookieHeader() {
    server.setRoute("/setcookie.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "session=value");
      exchange.getResponseHeaders().add("Set-Cookie", "foo=bar; max-age=3600");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });

    context.request().get(server.PREFIX + "/setcookie.html");
    List<Cookie> cookies = context.cookies();
    assertEquals(2, cookies.size());
    cookies.sort(Comparator.comparing(a -> a.name));
    assertEquals("foo", cookies.get(0).name);
    assertEquals("bar", cookies.get(0).value);
    assertEquals("session", cookies.get(1).name);
    assertEquals("value", cookies.get(1).value);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList("foo=bar", "session=value"), page.evaluate("() => document.cookie.split(';').map(s => s.trim()).sort()"));
  }

  @Test
  @Disabled("Default Java's HTTP server throws on 'CONNECT non-existent.com:80 HTTP/1.1' because path is null.")
  void shouldWorkWithContextLevelProxy() throws ExecutionException, InterruptedException {
    server.setRoute("/target.html", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<title>Served by the proxy</title>");
      }
    });
    try (Browser browser = browserType.launch(new BrowserType.LaunchOptions().setProxy("http://per-context"))) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy("localhost:" + server.PORT));
      Future<Server.Request> request = server.futureRequest("/target.html");
      APIResponse response = context.request().get("http://non-existent.com/target.html");

      assertEquals(200, response.status());
      assertEquals("/target.html", request.get().url);
    }
  }


  @Test
  void shouldWorkWithHttpCredentials() throws ExecutionException, InterruptedException {
    server.setAuth("/empty.html", "user", "pass");

    String base64 = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));;
    Future<Server.Request> request = server.futureRequest("/empty.html");
    APIResponse response = context.request().get(server.EMPTY_PAGE, RequestOptions.create()
      .setHeader("authorization", "Basic " + base64));
    assertEquals(200, response.status());
    assertEquals("/empty.html", request.get().url);
  }

  @Test
  void shouldWorkWithSetHTTPCredentials() {
    server.setAuth("/empty.html", "user", "pass");
    APIResponse response1 = context.request().get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());

    try (BrowserContext context2 = browser.newContext(
      new Browser.NewContextOptions().setHttpCredentials("user", "pass"))) {
      APIResponse response2 = context2.request().get(server.EMPTY_PAGE);
      assertEquals(200, response2.status());
    }
  }

  @Test
  void shouldReturnErrorWithWrongCredentials() {
    server.setAuth("/empty.html", "user", "pass");
    try (BrowserContext context = browser.newContext(
      new Browser.NewContextOptions().setHttpCredentials("user", "wrong"))) {
      APIResponse response = context.request().get(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }

  @Test
  void postShouldSupportPostData() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/simple.json");
    APIResponse response = context.request().post(server.PREFIX + "/simple.json",
      RequestOptions.create().setData("My request"));
    assertEquals("POST", request.get().method);
    assertEquals("My request", new String(request.get().postBody));
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.get().url);
  }

  @Test
  void deleteShouldSupportPostData() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/simple.json");
    APIResponse response = context.request().delete(server.PREFIX + "/simple.json",
      RequestOptions.create().setData("My request"));
    assertEquals("DELETE", request.get().method);
    assertEquals("My request", new String(request.get().postBody));
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.get().url);
  }

  @Test
  void patchShouldSupportPostData() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/simple.json");
    APIResponse response = context.request().patch(server.PREFIX + "/simple.json",
      RequestOptions.create().setData("My request"));
    assertEquals("PATCH", request.get().method);
    assertEquals("My request", new String(request.get().postBody));
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.get().url);
  }

  @Test
  void putShouldSupportPostData() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/simple.json");
    APIResponse response = context.request().put(server.PREFIX + "/simple.json",
      RequestOptions.create().setData("My request"));
    assertEquals("PUT", request.get().method);
    assertEquals("My request", new String(request.get().postBody));
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.get().url);
  }

  @Test
  void getShouldSupportPostData() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/simple.json");
    APIResponse response = context.request().get(server.PREFIX + "/simple.json",
      RequestOptions.create().setData("My request"));
    assertEquals("GET", request.get().method);
    assertEquals("My request", new String(request.get().postBody));
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.get().url);
  }

  @Test
  void headShouldSupportPostData() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/simple.json");
    APIResponse response = context.request().head(server.PREFIX + "/simple.json",
      RequestOptions.create().setData("My request"));
    assertEquals("HEAD", request.get().method);
    assertEquals("My request", new String(request.get().postBody));
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.get().url);
  }

  @Test
  void shouldAddDefaultHeaders() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE);

    assertEquals(asList("*/*"), request.get().headers.get("accept"));
    Object userAgent = page.evaluate("() => navigator.userAgent");
    assertEquals(asList(userAgent), request.get().headers.get("user-agent"));
    assertEquals(asList("gzip,deflate,br"), request.get().headers.get("accept-encoding"));
  }

  @Test
  void shouldSendContentLength() throws ExecutionException, InterruptedException {
    byte[] bytes = new byte[256];
    for (int i = 0; i < 256; i++) {
      bytes[i] = (byte) i;
    }
    Future<Server.Request> request = server.futureRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setData(bytes));
    assertEquals(asList("256"), request.get().headers.get("content-length"));
    assertEquals(asList("application/octet-stream"), request.get().headers.get("content-type"));
  }

  @Test
  void shouldAddDefaultHeadersToRedirects() throws ExecutionException, InterruptedException {
    server.setRedirect("/redirect", "/empty.html");
    Future<Server.Request> request = server.futureRequest("/empty.html");
    context.request().get(server.PREFIX + "/redirect");

    assertEquals(asList("*/*"), request.get().headers.get("accept"));
    Object userAgent = page.evaluate("() => navigator.userAgent");
    assertEquals(asList(userAgent), request.get().headers.get("user-agent"));
    assertEquals(asList("gzip,deflate,br"), request.get().headers.get("accept-encoding"));
  }

  @Test
  void shouldAllowToOverrideDefaultHeaders() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE, RequestOptions.create()
        .setHeader(
          "User-Agent", "Playwright")
        .setHeader("Accept", "text/html")
        .setHeader("Accept-Encoding", "br"));
    assertEquals(asList("text/html"), request.get().headers.get("accept"));
    assertEquals(asList("Playwright"), request.get().headers.get("user-agent"));
    assertEquals(asList("br"), request.get().headers.get("accept-encoding"));
  }

  @Test
  void shouldPropagateCustomHeadersWithRedirects() throws ExecutionException, InterruptedException {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");
    Future<Server.Request> req1 = server.futureRequest("/a/redirect1");
    Future<Server.Request> req2 = server.futureRequest("/b/c/redirect2");
    Future<Server.Request> req3 = server.futureRequest("/simple.json");
    context.request().get(server.PREFIX + "/a/redirect1",
      RequestOptions.create().setHeader("foo", "bar"));
    assertEquals(asList("bar"), req1.get().headers.get("foo"));
    assertEquals(asList("bar"), req2.get().headers.get("foo"));
    assertEquals(asList("bar"), req3.get().headers.get("foo"));
  }

  @Test
  void shouldPropagateExtraHttpHeadersWithRedirects() throws ExecutionException, InterruptedException {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");
    context.setExtraHTTPHeaders(mapOf("My-Secret", "Value"));
    Future<Server.Request> req1 = server.futureRequest("/a/redirect1");
    Future<Server.Request> req2 = server.futureRequest("/b/c/redirect2");
    Future<Server.Request> req3 = server.futureRequest("/simple.json");
    context.request().get(server.PREFIX + "/a/redirect1");

    assertEquals(asList("Value"), req1.get().headers.get("my-secret"));
    assertEquals(asList("Value"), req2.get().headers.get("my-secret"));
    assertEquals(asList("Value"), req3.get().headers.get("my-secret"));
  }

  @Test
  void shouldThrowOnInvalidHeaderValue() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      context.request().get(server.EMPTY_PAGE, RequestOptions.create()
        .setHeader("foo", "недопустимое значение"));
    });
    assertTrue(e.getMessage().contains("Invalid character in header content"), e.getMessage());
  }

  @Test
  void shouldThrowOnNonHttpSProtocol() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get("data:text/plain,test"));
    assertTrue(e.getMessage().contains("Protocol \"data:\" not supported"), e.getMessage());

    e = assertThrows(PlaywrightException.class, () -> context.request().get("file:///tmp/foo"));
    assertTrue(e.getMessage().contains("Protocol \"file:\" not supported"), e.getMessage());
  }

  @Test
  void shouldSupportTimeoutOption() {
    server.setRoute("/slow", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 4096);
    });

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      context.request().get(server.PREFIX + "/slow", RequestOptions.create().setTimeout(100));
    });
    assertTrue(e.getMessage().contains("Request timed out after 100ms"), e.getMessage());
  }

  @Test
  void shouldSupportATimeoutOf0() {
    server.setRoute("/slow", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 4);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("done");
      }
    });
    APIResponse response = context.request().get(server.PREFIX + "/slow",
      RequestOptions.create().setTimeout(0));
    assertEquals("done", response.text());
  }

  @Test
  void shouldRespectTimeoutAfterRedirects() {
    server.setRedirect("/redirect", "/slow");
    server.setRoute("/slow", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 4096);
    });

    context.setDefaultTimeout(100);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/redirect"));
    assertTrue(e.getMessage().contains("Request timed out after 100ms"), e.getMessage());
  }

  @Test
  void shouldDispose() {
    APIResponse response = context.request().get(server.PREFIX + "/simple.json");
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
    response.dispose();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(e.getMessage().contains("Response has been disposed"), e.getMessage());
  }

  @Test
  void shouldDisposeWhenContextCloses() {
    APIResponse response = context.request().get(server.PREFIX + "/simple.json");
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
    context.close();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(e.getMessage().contains("Response has been disposed") ||
        e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
  }
  @Test
  void shouldOverrideRequestParameters() throws ExecutionException, InterruptedException {
    Request pageReq = page.waitForRequest("**/*", () -> page.navigate(server.EMPTY_PAGE));
    Future<Server.Request> req = server.futureRequest("/empty.html");
    context.request().fetch(pageReq, RequestOptions.create().setMethod("POST")
        .setHeader("foo", "bar")
        .setData("data"));
    assertEquals("POST", req.get().method);
    assertEquals(asList("bar"), req.get().headers.get("foo"));
    assertEquals("data", new String(req.get().postBody));
  }

  public static class TestData {
    public String name;
    public LocalDateTime localDateTime;
    public Date date;
    public LocalDateTime nullLocalDateTime;
    public Date nullDate;
  }

  @Test
  void shouldSerializeDateAndLocalDateTime() throws ExecutionException, InterruptedException, ParseException {
    Request pageReq = page.waitForRequest("**/*", () -> page.navigate(server.EMPTY_PAGE));
    Future<Server.Request> req = server.futureRequest("/empty.html");
    TestData testData = new TestData();
    testData.name = "foo";
    long currentMillis = 1671776098818L;
    testData.date = new Date(currentMillis);
    testData.localDateTime = testData.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    context.request().fetch(pageReq, RequestOptions.create().setMethod("POST").setData(testData));
    assertEquals("{\"name\":\"foo\",\"localDateTime\":\"2022-12-23T06:14:58.818Z\",\"date\":\"2022-12-23T06:14:58.818Z\"}",
      new String(req.get().postBody));
  }

  @Test
  void shouldSupportApplicationXWwwFormUrlencoded() throws ExecutionException, InterruptedException {
    Future<Server.Request> req = server.futureRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setForm(
      FormData.create()
        .set("firstName", "John")
        .set("lastName", "Doe")
        .set("age", 30)
        .set("isMale", true)
        .set("file", "f.js")));

    assertEquals("POST", req.get().method);
    assertEquals(asList("application/x-www-form-urlencoded"), req.get().headers.get("content-type"));
    String body = new String(req.get().postBody);
    assertTrue(body.contains("firstName=John"), body);
    assertTrue(body.contains("lastName=Doe"), body);
    assertTrue(body.contains("age=30"), body);
    assertTrue(body.contains("isMale=true"), body);
    assertTrue(body.contains("file=f.js"), body);
  }

  @Test
  void shouldEncodeToApplicationJsonByDefault() throws ExecutionException, InterruptedException {
    Map<String, Object> data = mapOf(
      "firstName", "John",
      "lastName", "Doe",
      "age", 30,
      "isMale", true,
      "file", mapOf("name", "f.js")
    );
    Future<Server.Request> req = server.futureRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setData(data));
    assertEquals("POST", req.get().method);
    assertEquals(asList("application/json"), req.get().headers.get("content-type"));
    String body = new String(req.get().postBody);
    assertEquals(new Gson().toJson(data), body);
  }

  @Test
  void shouldSupportMultipartFormData() throws ExecutionException, InterruptedException {
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");

    FilePayload file = new FilePayload("f.js", "text/javascript",
      "var x = 10;\r\n;console.log(x);".getBytes(StandardCharsets.UTF_8));
    APIResponse response = context.request().post(server.EMPTY_PAGE, RequestOptions.create().setMultipart(
      FormData.create()
        .set("firstName", "John")
        .set("lastName", "Doe")
        .set("file", file)));

    assertEquals("POST", serverRequest.get().method);
    List<String> contentType = serverRequest.get().headers.get("content-type");
    assertNotNull(contentType);
    assertEquals(1, contentType.size());
    assertTrue(contentType.get(0).contains("multipart/form-data"), contentType.get(0));

    String body = new String(serverRequest.get().postBody);
    assertTrue(body.contains("content-disposition: form-data; name=\"firstName\"\r\n" +
      "\r\n" +
      "John"), body);
    assertTrue(body.contains("content-disposition: form-data; name=\"lastName\"\r\n" +
      "\r\n" +
      "Doe"), body);
    assertTrue(body.contains("content-disposition: form-data; name=\"file\"; filename=\"f.js\"\r\n" +
      "content-type: text/javascript\r\n" +
      "\r\n" +
      "var x = 10;\r\n" +
      ";console.log(x);"), body);
    assertEquals(200, response.status());
  }

  @Test
  void shouldSupportMultipartFormDataWithPathValues(@TempDir Path tmp) throws ExecutionException, InterruptedException, IOException {
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");

    Path path = tmp.resolve("simplezip.json");
    try (FileOutputStream output = new FileOutputStream(path.toFile())) {
      output.write("{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8));
    }
    APIResponse response = context.request().post(server.EMPTY_PAGE, RequestOptions.create().setMultipart(
      FormData.create()
        .set("firstName", "John")
        .set("lastName", "Doe")
        .set("file", path)));

    assertEquals("POST", serverRequest.get().method);
    List<String> contentType = serverRequest.get().headers.get("content-type");
    assertNotNull(contentType);
    assertEquals(1, contentType.size());
    assertTrue(contentType.get(0).contains("multipart/form-data"), contentType.get(0));

    String body = new String(serverRequest.get().postBody);
    assertTrue(body.contains("content-disposition: form-data; name=\"firstName\"\r\n" +
      "\r\n" +
      "John"), body);
    assertTrue(body.contains("content-disposition: form-data; name=\"lastName\"\r\n" +
      "\r\n" +
      "Doe"), body);
    assertTrue(body.contains("content-disposition: form-data; name=\"file\"; filename=\"simplezip.json\"\r\n" +
      "content-type: application/json\r\n" +
      "\r\n" +
      "{\"foo\":\"bar\"}"), body);
    assertEquals(200, response.status());
  }

  @Test
  void shouldSerializeDataToJsonRegardlessOfContentType() throws ExecutionException, InterruptedException {
    Map<String, Object> data = mapOf(
      "firstName", "John",
      "lastName", "Doe");
    Future<Server.Request> req = server.futureRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create()
      .setHeader("content-type", "unknown")
      .setData(data));
    assertEquals("POST", req.get().method);
    assertEquals(asList("unknown"), req.get().headers.get("content-type"));
    String body = new String(req.get().postBody);
    assertEquals(new Gson().toJson(data), body);
  }

  @Test
  void shouldNotThrowWhenDataPassedForUnsupportedRequest() {
    context.request().fetch(server.EMPTY_PAGE, RequestOptions.create()
      .setMethod("GET").setData("bar"));
  }

  @Test
  void contextRequestShouldExportSameStorageStateAsContext() {
    server.setRoute("/setcookie.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "a=b");
      exchange.getResponseHeaders().add("Set-Cookie", "c=d");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    context.request().get(server.PREFIX + "/setcookie.html");
    String contextState = context.storageState();
    assertEquals(2, context.cookies().size());
    String requestState = context.request().storageState();
    assertEquals(contextState, requestState);
    String pageState = page.request().storageState();
    assertEquals(contextState, pageState);
  }

  @Test
  void shouldAcceptBoolAndNumericParams() throws ExecutionException, InterruptedException {
    Future<Server.Request> req = server.futureRequest("/empty.html");
    page.request().get(server.EMPTY_PAGE, RequestOptions.create()
      .setQueryParam("str", "s")
      .setQueryParam("num", 10)
      .setQueryParam("bool", true)
      .setQueryParam("bool2", false));
    assertEquals("/empty.html?str=s&num=10&bool=true&bool2=false", req.get().url);
  }

  @Test
  void shouldAbortRequestsWhenBrowserContextCloses() {
    server.setRoute("/empty.html", exchange -> {
    });

    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    page.exposeFunction("closeContext", (Object... args) -> {
      context.close();
      return null;
    });
    page.evaluate("() => setTimeout(closeContext, 1000);");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.request().get(server.EMPTY_PAGE));
    assertTrue(e.getMessage().contains("Request context disposed"), e.getMessage());

    e = assertThrows(PlaywrightException.class, () ->  context.request().post(server.EMPTY_PAGE));
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
  }

  @Test
  void shouldWorkWithSetHTTPCredentialsAndMatchingOrigin() throws ExecutionException, InterruptedException {
    server.setAuth("/empty.html", "user", "pass");
    APIResponse response1 = context.request().get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());

    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(server.PREFIX);
    try (BrowserContext context2 = browser.newContext(
      new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      APIResponse response2 = context2.request().get(server.EMPTY_PAGE);
      assertEquals(200, response2.status());
    }
  }

  @Test
  void shouldWorkWithSetHTTPCredentialsAndMatchingOriginCaseInsensitive() throws ExecutionException, InterruptedException {
    server.setAuth("/empty.html", "user", "pass");
    APIResponse response1 = context.request().get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());

    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(server.PREFIX.toUpperCase());
    try (BrowserContext context2 = browser.newContext(
      new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      APIResponse response2 = context2.request().get(server.EMPTY_PAGE);
      assertEquals(200, response2.status());
    }
  }

  @Test
  void shouldReturnErrorWithCorrectCredentialsAndWrongOriginScheme() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginScheme(server));
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      APIResponse response = context.request().get(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }

  @Test
  void shouldReturnErrorWithCorrectCredentialsAndWrongOriginHostname() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginHostname(server));
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      APIResponse response = context.request().get(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }

  @Test
  void shouldReturnErrorWithCorrectCredentialsAndWrongOriginPort() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginPort(server));
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      APIResponse response = context.request().get(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }
}
