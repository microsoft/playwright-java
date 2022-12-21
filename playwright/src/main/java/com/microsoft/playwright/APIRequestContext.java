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

import com.microsoft.playwright.options.*;
import java.nio.file.Path;

/**
 * This API is used for the Web API testing. You can use it to trigger API endpoints, configure micro-services, prepare
 * environment or the service to your e2e test.
 *
 * <p> Each Playwright browser context has associated with it {@code APIRequestContext} instance which shares cookie storage
 * with the browser context and can be accessed via {@link BrowserContext#request BrowserContext.request()} or {@link
 * Page#request Page.request()}. It is also possible to create a new APIRequestContext instance manually by calling {@link
 * APIRequest#newContext APIRequest.newContext()}.
 *
 * <p> **Cookie management**
 *
 * <p> {@code APIRequestContext} returned by {@link BrowserContext#request BrowserContext.request()} and {@link Page#request
 * Page.request()} shares cookie storage with the corresponding {@code BrowserContext}. Each API request will have {@code
 * Cookie} header populated with the values from the browser context. If the API response contains {@code Set-Cookie}
 * header it will automatically update {@code BrowserContext} cookies and requests made from the page will pick them up.
 * This means that if you log in using this API, your e2e test will be logged in and vice versa.
 *
 * <p> If you want API requests to not interfere with the browser cookies you should create a new {@code APIRequestContext} by
 * calling {@link APIRequest#newContext APIRequest.newContext()}. Such {@code APIRequestContext} object will have its own
 * isolated cookie storage.
 */
public interface APIRequestContext {
  class StorageStateOptions {
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public Path path;

    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public StorageStateOptions setPath(Path path) {
      this.path = path;
      return this;
    }
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/DELETE">DELETE</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   * @since v1.16
   */
  default APIResponse delete(String url) {
    return delete(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/DELETE">DELETE</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse delete(String url, RequestOptions params);
  /**
   * All responses returned by {@link APIRequestContext#get APIRequestContext.get()} and similar methods are stored in the
   * memory, so that you can later call {@link APIResponse#body APIResponse.body()}. This method discards all stored
   * responses, and makes {@link APIResponse#body APIResponse.body()} throw "Response disposed" error.
   *
   * @since v1.16
   */
  void dispose();
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects. JSON objects can be passed directly
   * to the request.
   *
   * <p> **Usage**
   * <pre>{@code
   * Map<String, Object> data = new HashMap();
   * data.put("title", "Book Title");
   * data.put("body", "John Doe");
   * request.fetch("https://example.com/api/createBook", RequestOptions.create().setMethod("post").setData(data));
   * }</pre>
   *
   * <p> The common way to send file(s) in the body of a request is to encode it as form fields with {@code multipart/form-data}
   * encoding. You can achieve that with Playwright API like this:
   * <pre>{@code
   * // Pass file path to the form data constructor:
   * Path file = Paths.get("team.csv");
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", file)));
   *
   * // Or you can pass the file content directly as FilePayload object:
   * FilePayload filePayload = new FilePayload("f.js", "text/javascript",
   *       "console.log(2022);".getBytes(StandardCharsets.UTF_8));
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", filePayload)));
   * }</pre>
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   * @since v1.16
   */
  default APIResponse fetch(String urlOrRequest) {
    return fetch(urlOrRequest, null);
  }
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects. JSON objects can be passed directly
   * to the request.
   *
   * <p> **Usage**
   * <pre>{@code
   * Map<String, Object> data = new HashMap();
   * data.put("title", "Book Title");
   * data.put("body", "John Doe");
   * request.fetch("https://example.com/api/createBook", RequestOptions.create().setMethod("post").setData(data));
   * }</pre>
   *
   * <p> The common way to send file(s) in the body of a request is to encode it as form fields with {@code multipart/form-data}
   * encoding. You can achieve that with Playwright API like this:
   * <pre>{@code
   * // Pass file path to the form data constructor:
   * Path file = Paths.get("team.csv");
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", file)));
   *
   * // Or you can pass the file content directly as FilePayload object:
   * FilePayload filePayload = new FilePayload("f.js", "text/javascript",
   *       "console.log(2022);".getBytes(StandardCharsets.UTF_8));
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", filePayload)));
   * }</pre>
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse fetch(String urlOrRequest, RequestOptions params);
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects. JSON objects can be passed directly
   * to the request.
   *
   * <p> **Usage**
   * <pre>{@code
   * Map<String, Object> data = new HashMap();
   * data.put("title", "Book Title");
   * data.put("body", "John Doe");
   * request.fetch("https://example.com/api/createBook", RequestOptions.create().setMethod("post").setData(data));
   * }</pre>
   *
   * <p> The common way to send file(s) in the body of a request is to encode it as form fields with {@code multipart/form-data}
   * encoding. You can achieve that with Playwright API like this:
   * <pre>{@code
   * // Pass file path to the form data constructor:
   * Path file = Paths.get("team.csv");
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", file)));
   *
   * // Or you can pass the file content directly as FilePayload object:
   * FilePayload filePayload = new FilePayload("f.js", "text/javascript",
   *       "console.log(2022);".getBytes(StandardCharsets.UTF_8));
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", filePayload)));
   * }</pre>
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   * @since v1.16
   */
  default APIResponse fetch(Request urlOrRequest) {
    return fetch(urlOrRequest, null);
  }
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects. JSON objects can be passed directly
   * to the request.
   *
   * <p> **Usage**
   * <pre>{@code
   * Map<String, Object> data = new HashMap();
   * data.put("title", "Book Title");
   * data.put("body", "John Doe");
   * request.fetch("https://example.com/api/createBook", RequestOptions.create().setMethod("post").setData(data));
   * }</pre>
   *
   * <p> The common way to send file(s) in the body of a request is to encode it as form fields with {@code multipart/form-data}
   * encoding. You can achieve that with Playwright API like this:
   * <pre>{@code
   * // Pass file path to the form data constructor:
   * Path file = Paths.get("team.csv");
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", file)));
   *
   * // Or you can pass the file content directly as FilePayload object:
   * FilePayload filePayload = new FilePayload("f.js", "text/javascript",
   *       "console.log(2022);".getBytes(StandardCharsets.UTF_8));
   * APIResponse response = request.fetch("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMethod("post").setMultipart(
   *     FormData.create().set("fileField", filePayload)));
   * }</pre>
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse fetch(Request urlOrRequest, RequestOptions params);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">GET</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * <p> **Usage**
   *
   * <p> Request parameters can be configured with {@code params} option, they will be serialized into the URL search parameters:
   * <pre>{@code
   * request.get("https://example.com/api/getText", RequestOptions.create()
   *   .setQueryParam("isbn", "1234")
   *   .setQueryParam("page", 23));
   * }</pre>
   *
   * @param url Target URL.
   * @since v1.16
   */
  default APIResponse get(String url) {
    return get(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">GET</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * <p> **Usage**
   *
   * <p> Request parameters can be configured with {@code params} option, they will be serialized into the URL search parameters:
   * <pre>{@code
   * request.get("https://example.com/api/getText", RequestOptions.create()
   *   .setQueryParam("isbn", "1234")
   *   .setQueryParam("page", 23));
   * }</pre>
   *
   * @param url Target URL.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse get(String url, RequestOptions params);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/HEAD">HEAD</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   * @since v1.16
   */
  default APIResponse head(String url) {
    return head(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/HEAD">HEAD</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse head(String url, RequestOptions params);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PATCH">PATCH</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   * @since v1.16
   */
  default APIResponse patch(String url) {
    return patch(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PATCH">PATCH</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse patch(String url, RequestOptions params);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * <p> **Usage**
   *
   * <p> JSON objects can be passed directly to the request:
   * <pre>{@code
   * Map<String, Object> data = new HashMap();
   * data.put("title", "Book Title");
   * data.put("body", "John Doe");
   * request.post("https://example.com/api/createBook", RequestOptions.create().setData(data));
   * }</pre>
   *
   * <p> To send form data to the server use {@code form} option. Its value will be encoded into the request body with {@code
   * application/x-www-form-urlencoded} encoding (see below how to use {@code multipart/form-data} form encoding to send
   * files):
   * <pre>{@code
   * request.post("https://example.com/api/findBook", RequestOptions.create().setForm(
   *     FormData.create().set("title", "Book Title").set("body", "John Doe")
   * ));
   * }</pre>
   *
   * <p> The common way to send file(s) in the body of a request is to upload them as form fields with {@code
   * multipart/form-data} encoding. You can achieve that with Playwright API like this:
   * <pre>{@code
   * // Pass file path to the form data constructor:
   * Path file = Paths.get("team.csv");
   * APIResponse response = request.post("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMultipart(
   *     FormData.create().set("fileField", file)));
   *
   * // Or you can pass the file content directly as FilePayload object:
   * FilePayload filePayload = new FilePayload("f.js", "text/javascript",
   *       "console.log(2022);".getBytes(StandardCharsets.UTF_8));
   * APIResponse response = request.post("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMultipart(
   *     FormData.create().set("fileField", filePayload)));
   * }</pre>
   *
   * @param url Target URL.
   * @since v1.16
   */
  default APIResponse post(String url) {
    return post(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * <p> **Usage**
   *
   * <p> JSON objects can be passed directly to the request:
   * <pre>{@code
   * Map<String, Object> data = new HashMap();
   * data.put("title", "Book Title");
   * data.put("body", "John Doe");
   * request.post("https://example.com/api/createBook", RequestOptions.create().setData(data));
   * }</pre>
   *
   * <p> To send form data to the server use {@code form} option. Its value will be encoded into the request body with {@code
   * application/x-www-form-urlencoded} encoding (see below how to use {@code multipart/form-data} form encoding to send
   * files):
   * <pre>{@code
   * request.post("https://example.com/api/findBook", RequestOptions.create().setForm(
   *     FormData.create().set("title", "Book Title").set("body", "John Doe")
   * ));
   * }</pre>
   *
   * <p> The common way to send file(s) in the body of a request is to upload them as form fields with {@code
   * multipart/form-data} encoding. You can achieve that with Playwright API like this:
   * <pre>{@code
   * // Pass file path to the form data constructor:
   * Path file = Paths.get("team.csv");
   * APIResponse response = request.post("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMultipart(
   *     FormData.create().set("fileField", file)));
   *
   * // Or you can pass the file content directly as FilePayload object:
   * FilePayload filePayload = new FilePayload("f.js", "text/javascript",
   *       "console.log(2022);".getBytes(StandardCharsets.UTF_8));
   * APIResponse response = request.post("https://example.com/api/uploadTeamList",
   *   RequestOptions.create().setMultipart(
   *     FormData.create().set("fileField", filePayload)));
   * }</pre>
   *
   * @param url Target URL.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse post(String url, RequestOptions params);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   * @since v1.16
   */
  default APIResponse put(String url) {
    return put(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   * @param params Optional request parameters.
   * @since v1.16
   */
  APIResponse put(String url, RequestOptions params);
  /**
   * Returns storage state for this request context, contains current cookies and local storage snapshot if it was passed to
   * the constructor.
   *
   * @since v1.16
   */
  default String storageState() {
    return storageState(null);
  }
  /**
   * Returns storage state for this request context, contains current cookies and local storage snapshot if it was passed to
   * the constructor.
   *
   * @since v1.16
   */
  String storageState(StorageStateOptions options);
}

