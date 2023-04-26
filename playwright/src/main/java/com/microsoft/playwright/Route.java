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

package com.microsoft.playwright;

import java.nio.file.Path;
import java.util.*;

/**
 * Whenever a network route is set up with {@link Page#route Page.route()} or {@link BrowserContext#route
 * BrowserContext.route()}, the {@code Route} object allows to handle the route.
 *
 * <p> Learn more about <a href="https://playwright.dev/java/docs/network">networking</a>.
 */
public interface Route {
  class ResumeOptions {
    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public Map<String, String> headers;
    /**
     * If set changes the request method (e.g. GET or POST).
     */
    public String method;
    /**
     * If set changes the post data of request.
     */
    public Object postData;
    /**
     * If set changes the request URL. New URL must have same protocol as original one.
     */
    public String url;

    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public ResumeOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * If set changes the request method (e.g. GET or POST).
     */
    public ResumeOptions setMethod(String method) {
      this.method = method;
      return this;
    }
    /**
     * If set changes the post data of request.
     */
    public ResumeOptions setPostData(String postData) {
      this.postData = postData;
      return this;
    }
    /**
     * If set changes the post data of request.
     */
    public ResumeOptions setPostData(byte[] postData) {
      this.postData = postData;
      return this;
    }
    /**
     * If set changes the request URL. New URL must have same protocol as original one.
     */
    public ResumeOptions setUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class FallbackOptions {
    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public Map<String, String> headers;
    /**
     * If set changes the request method (e.g. GET or POST).
     */
    public String method;
    /**
     * If set changes the post data of request.
     */
    public Object postData;
    /**
     * If set changes the request URL. New URL must have same protocol as original one. Changing the URL won't affect the route
     * matching, all the routes are matched using the original request URL.
     */
    public String url;

    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public FallbackOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * If set changes the request method (e.g. GET or POST).
     */
    public FallbackOptions setMethod(String method) {
      this.method = method;
      return this;
    }
    /**
     * If set changes the post data of request.
     */
    public FallbackOptions setPostData(String postData) {
      this.postData = postData;
      return this;
    }
    /**
     * If set changes the post data of request.
     */
    public FallbackOptions setPostData(byte[] postData) {
      this.postData = postData;
      return this;
    }
    /**
     * If set changes the request URL. New URL must have same protocol as original one. Changing the URL won't affect the route
     * matching, all the routes are matched using the original request URL.
     */
    public FallbackOptions setUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class FetchOptions {
    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public Map<String, String> headers;
    /**
     * Maximum number of request redirects that will be followed automatically. An error will be thrown if the number is
     * exceeded. Defaults to {@code 20}. Pass {@code 0} to not follow redirects.
     */
    public Integer maxRedirects;
    /**
     * If set changes the request method (e.g. GET or POST).
     */
    public String method;
    /**
     * If set changes the post data of request.
     */
    public Object postData;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;
    /**
     * If set changes the request URL. New URL must have same protocol as original one.
     */
    public String url;

    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public FetchOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Maximum number of request redirects that will be followed automatically. An error will be thrown if the number is
     * exceeded. Defaults to {@code 20}. Pass {@code 0} to not follow redirects.
     */
    public FetchOptions setMaxRedirects(int maxRedirects) {
      this.maxRedirects = maxRedirects;
      return this;
    }
    /**
     * If set changes the request method (e.g. GET or POST).
     */
    public FetchOptions setMethod(String method) {
      this.method = method;
      return this;
    }
    /**
     * If set changes the post data of request.
     */
    public FetchOptions setPostData(String postData) {
      this.postData = postData;
      return this;
    }
    /**
     * If set changes the post data of request.
     */
    public FetchOptions setPostData(byte[] postData) {
      this.postData = postData;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public FetchOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * If set changes the request URL. New URL must have same protocol as original one.
     */
    public FetchOptions setUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class FulfillOptions {
    /**
     * Optional response body as text.
     */
    public String body;
    /**
     * Optional response body as raw bytes.
     */
    public byte[] bodyBytes;
    /**
     * If set, equals to setting {@code Content-Type} response header.
     */
    public String contentType;
    /**
     * Response headers. Header values will be converted to a string.
     */
    public Map<String, String> headers;
    /**
     * File path to respond with. The content type will be inferred from file extension. If {@code path} is a relative path,
     * then it is resolved relative to the current working directory.
     */
    public Path path;
    /**
     * {@code APIResponse} to fulfill route's request with. Individual fields of the response (such as headers) can be
     * overridden using fulfill options.
     */
    public APIResponse response;
    /**
     * Response status code, defaults to {@code 200}.
     */
    public Integer status;

    /**
     * Optional response body as text.
     */
    public FulfillOptions setBody(String body) {
      this.body = body;
      return this;
    }
    /**
     * Optional response body as raw bytes.
     */
    public FulfillOptions setBodyBytes(byte[] bodyBytes) {
      this.bodyBytes = bodyBytes;
      return this;
    }
    /**
     * If set, equals to setting {@code Content-Type} response header.
     */
    public FulfillOptions setContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }
    /**
     * Response headers. Header values will be converted to a string.
     */
    public FulfillOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * File path to respond with. The content type will be inferred from file extension. If {@code path} is a relative path,
     * then it is resolved relative to the current working directory.
     */
    public FulfillOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * {@code APIResponse} to fulfill route's request with. Individual fields of the response (such as headers) can be
     * overridden using fulfill options.
     */
    public FulfillOptions setResponse(APIResponse response) {
      this.response = response;
      return this;
    }
    /**
     * Response status code, defaults to {@code 200}.
     */
    public FulfillOptions setStatus(int status) {
      this.status = status;
      return this;
    }
  }
  /**
   * Aborts the route's request.
   *
   * @since v1.8
   */
  default void abort() {
    abort(null);
  }
  /**
   * Aborts the route's request.
   *
   * @param errorCode Optional error code. Defaults to {@code failed}, could be one of the following:
   * <ul>
   * <li> {@code "aborted"} - An operation was aborted (due to user action)</li>
   * <li> {@code "accessdenied"} - Permission to access a resource, other than the network, was denied</li>
   * <li> {@code "addressunreachable"} - The IP address is unreachable. This usually means that there is no route to the specified
   * host or network.</li>
   * <li> {@code "blockedbyclient"} - The client chose to block the request.</li>
   * <li> {@code "blockedbyresponse"} - The request failed because the response was delivered along with requirements which are
   * not met ('X-Frame-Options' and 'Content-Security-Policy' ancestor checks, for instance).</li>
   * <li> {@code "connectionaborted"} - A connection timed out as a result of not receiving an ACK for data sent.</li>
   * <li> {@code "connectionclosed"} - A connection was closed (corresponding to a TCP FIN).</li>
   * <li> {@code "connectionfailed"} - A connection attempt failed.</li>
   * <li> {@code "connectionrefused"} - A connection attempt was refused.</li>
   * <li> {@code "connectionreset"} - A connection was reset (corresponding to a TCP RST).</li>
   * <li> {@code "internetdisconnected"} - The Internet connection has been lost.</li>
   * <li> {@code "namenotresolved"} - The host name could not be resolved.</li>
   * <li> {@code "timedout"} - An operation timed out.</li>
   * <li> {@code "failed"} - A generic failure occurred.</li>
   * </ul>
   * @since v1.8
   */
  void abort(String errorCode);
  /**
   * Continues route's request with optional overrides.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   // Override headers
   *   Map<String, String> headers = new HashMap<>(route.request().headers());
   *   headers.put("foo", "foo-value"); // set "foo" header
   *   headers.remove("bar"); // remove "bar" header
   *   route.resume(new Route.ResumeOptions().setHeaders(headers));
   * });
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Note that any overrides such as {@code url} or {@code headers} only apply to the request being routed. If this request
   * results in a redirect, overrides will not be applied to the new redirected request. If you want to propagate a header
   * through redirects, use the combination of {@link Route#fetch Route.fetch()} and {@link Route#fulfill Route.fulfill()}
   * instead.
   *
   * @since v1.8
   */
  default void resume() {
    resume(null);
  }
  /**
   * Continues route's request with optional overrides.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   // Override headers
   *   Map<String, String> headers = new HashMap<>(route.request().headers());
   *   headers.put("foo", "foo-value"); // set "foo" header
   *   headers.remove("bar"); // remove "bar" header
   *   route.resume(new Route.ResumeOptions().setHeaders(headers));
   * });
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Note that any overrides such as {@code url} or {@code headers} only apply to the request being routed. If this request
   * results in a redirect, overrides will not be applied to the new redirected request. If you want to propagate a header
   * through redirects, use the combination of {@link Route#fetch Route.fetch()} and {@link Route#fulfill Route.fulfill()}
   * instead.
   *
   * @since v1.8
   */
  void resume(ResumeOptions options);
  /**
   * When several routes match the given pattern, they run in the order opposite to their registration. That way the last
   * registered route can always override all the previous ones. In the example below, request will be handled by the
   * bottom-most handler first, then it'll fall back to the previous one and in the end will be aborted by the first
   * registered route.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   // Runs last.
   *   route.abort();
   * });
   *
   * page.route("**\/*", route -> {
   *   // Runs second.
   *   route.fallback();
   * });
   *
   * page.route("**\/*", route -> {
   *   // Runs first.
   *   route.fallback();
   * });
   * }</pre>
   *
   * <p> Registering multiple routes is useful when you want separate handlers to handle different kinds of requests, for example
   * API calls vs page resources or GET requests vs POST requests as in the example below.
   * <pre>{@code
   * // Handle GET requests.
   * page.route("**\/*", route -> {
   *   if (!route.request().method().equals("GET")) {
   *     route.fallback();
   *     return;
   *   }
   *   // Handling GET only.
   *   // ...
   * });
   *
   * // Handle POST requests.
   * page.route("**\/*", route -> {
   *   if (!route.request().method().equals("POST")) {
   *     route.fallback();
   *     return;
   *   }
   *   // Handling POST only.
   *   // ...
   * });
   * }</pre>
   *
   * <p> One can also modify request while falling back to the subsequent handler, that way intermediate route handler can modify
   * url, method, headers and postData of the request.
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   // Override headers
   *   Map<String, String> headers = new HashMap<>(route.request().headers());
   *   headers.put("foo", "foo-value"); // set "foo" header
   *   headers.remove("bar"); // remove "bar" header
   *   route.fallback(new Route.ResumeOptions().setHeaders(headers));
   * });
   * }</pre>
   *
   * @since v1.23
   */
  default void fallback() {
    fallback(null);
  }
  /**
   * When several routes match the given pattern, they run in the order opposite to their registration. That way the last
   * registered route can always override all the previous ones. In the example below, request will be handled by the
   * bottom-most handler first, then it'll fall back to the previous one and in the end will be aborted by the first
   * registered route.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   // Runs last.
   *   route.abort();
   * });
   *
   * page.route("**\/*", route -> {
   *   // Runs second.
   *   route.fallback();
   * });
   *
   * page.route("**\/*", route -> {
   *   // Runs first.
   *   route.fallback();
   * });
   * }</pre>
   *
   * <p> Registering multiple routes is useful when you want separate handlers to handle different kinds of requests, for example
   * API calls vs page resources or GET requests vs POST requests as in the example below.
   * <pre>{@code
   * // Handle GET requests.
   * page.route("**\/*", route -> {
   *   if (!route.request().method().equals("GET")) {
   *     route.fallback();
   *     return;
   *   }
   *   // Handling GET only.
   *   // ...
   * });
   *
   * // Handle POST requests.
   * page.route("**\/*", route -> {
   *   if (!route.request().method().equals("POST")) {
   *     route.fallback();
   *     return;
   *   }
   *   // Handling POST only.
   *   // ...
   * });
   * }</pre>
   *
   * <p> One can also modify request while falling back to the subsequent handler, that way intermediate route handler can modify
   * url, method, headers and postData of the request.
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   // Override headers
   *   Map<String, String> headers = new HashMap<>(route.request().headers());
   *   headers.put("foo", "foo-value"); // set "foo" header
   *   headers.remove("bar"); // remove "bar" header
   *   route.fallback(new Route.ResumeOptions().setHeaders(headers));
   * });
   * }</pre>
   *
   * @since v1.23
   */
  void fallback(FallbackOptions options);
  /**
   * Performs the request and fetches result without fulfilling it, so that the response could be modified and then
   * fulfilled.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.route("https://dog.ceo/api/breeds/list/all", route -> {
   *   APIResponse response = route.fetch();
   *   JsonObject json = new Gson().fromJson(response.text(), JsonObject.class);
   *   JsonObject message = itemObj.get("json").getAsJsonObject();
   *   message.set("big_red_dog", new JsonArray());
   *   route.fulfill(new Route.FulfillOptions()
   *     .setResponse(response)
   *     .setBody(json.toString()));
   * });
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Note that {@code headers} option will apply to the fetched request as well as any redirects initiated by it. If you want
   * to only apply {@code headers} to the original request, but not to redirects, look into {@link Route#resume
   * Route.resume()} instead.
   *
   * @since v1.29
   */
  default APIResponse fetch() {
    return fetch(null);
  }
  /**
   * Performs the request and fetches result without fulfilling it, so that the response could be modified and then
   * fulfilled.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.route("https://dog.ceo/api/breeds/list/all", route -> {
   *   APIResponse response = route.fetch();
   *   JsonObject json = new Gson().fromJson(response.text(), JsonObject.class);
   *   JsonObject message = itemObj.get("json").getAsJsonObject();
   *   message.set("big_red_dog", new JsonArray());
   *   route.fulfill(new Route.FulfillOptions()
   *     .setResponse(response)
   *     .setBody(json.toString()));
   * });
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Note that {@code headers} option will apply to the fetched request as well as any redirects initiated by it. If you want
   * to only apply {@code headers} to the original request, but not to redirects, look into {@link Route#resume
   * Route.resume()} instead.
   *
   * @since v1.29
   */
  APIResponse fetch(FetchOptions options);
  /**
   * Fulfills route's request with given response.
   *
   * <p> **Usage**
   *
   * <p> An example of fulfilling all requests with 404 responses:
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   route.fulfill(new Route.FulfillOptions()
   *     .setStatus(404)
   *     .setContentType("text/plain")
   *     .setBody("Not Found!"));
   * });
   * }</pre>
   *
   * <p> An example of serving static file:
   * <pre>{@code
   * page.route("**\/xhr_endpoint", route -> route.fulfill(
   *   new Route.FulfillOptions().setPath(Paths.get("mock_data.json"))));
   * }</pre>
   *
   * @since v1.8
   */
  default void fulfill() {
    fulfill(null);
  }
  /**
   * Fulfills route's request with given response.
   *
   * <p> **Usage**
   *
   * <p> An example of fulfilling all requests with 404 responses:
   * <pre>{@code
   * page.route("**\/*", route -> {
   *   route.fulfill(new Route.FulfillOptions()
   *     .setStatus(404)
   *     .setContentType("text/plain")
   *     .setBody("Not Found!"));
   * });
   * }</pre>
   *
   * <p> An example of serving static file:
   * <pre>{@code
   * page.route("**\/xhr_endpoint", route -> route.fulfill(
   *   new Route.FulfillOptions().setPath(Paths.get("mock_data.json"))));
   * }</pre>
   *
   * @since v1.8
   */
  void fulfill(FulfillOptions options);
  /**
   * A request to be routed.
   *
   * @since v1.8
   */
  Request request();
}

