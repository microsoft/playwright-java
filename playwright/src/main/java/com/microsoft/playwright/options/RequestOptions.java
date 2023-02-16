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

package com.microsoft.playwright.options;

import com.microsoft.playwright.impl.RequestOptionsImpl;

/**
 * The {@code RequestOptions} allows to create form data to be sent via {@code APIRequestContext}. Playwright will
 * automatically determine content type of the request.
 * <pre>{@code
 * context.request().post(
 *   "https://example.com/submit",
 *   RequestOptions.create()
 *     .setQueryParam("page", 1)
 *     .setData("My data"));
 * }</pre>
 *
 * <p> **Uploading html form data**
 *
 * <p> {@code FormData} class can be used to send a form to the server, by default the request will use {@code
 * application/x-www-form-urlencoded} encoding:
 * <pre>{@code
 * context.request().post("https://example.com/signup", RequestOptions.create().setForm(
 *   FormData.create()
 *     .set("firstName", "John")
 *     .set("lastName", "Doe")));
 * }</pre>
 *
 * <p> You can also send files as fields of an html form. The data will be encoded using <a
 * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">{@code multipart/form-data}</a>:
 * <pre>{@code
 * Path path = Paths.get("members.csv");
 * APIResponse response = context.request().post("https://example.com/upload_members",
 *   RequestOptions.create().setMultipart(FormData.create().set("membersList", path)));
 * }</pre>
 *
 * <p> Alternatively, you can build the file payload manually:
 * <pre>{@code
 * FilePayload filePayload = new FilePayload("members.csv", "text/csv",
 *   "Alice, 33\nJohn, 35\n".getBytes(StandardCharsets.UTF_8));
 * APIResponse response = context.request().post("https://example.com/upload_members",
 *   RequestOptions.create().setMultipart(FormData.create().set("membersList", filePayload)));
 * }</pre>
 */
public interface RequestOptions {
  /**
   * Creates new instance of {@code RequestOptions}.
   *
   * @since v1.18
   */
  static RequestOptions create() {
    return new RequestOptionsImpl();
  }
  /**
   * Sets the request's post data.
   *
   * @param data Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
   * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code
   * content-type} header will be set to {@code application/octet-stream} if not explicitly set.
   * @since v1.18
   */
  RequestOptions setData(String data);
  /**
   * Sets the request's post data.
   *
   * @param data Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
   * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code
   * content-type} header will be set to {@code application/octet-stream} if not explicitly set.
   * @since v1.18
   */
  RequestOptions setData(byte[] data);
  /**
   * Sets the request's post data.
   *
   * @param data Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
   * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code
   * content-type} header will be set to {@code application/octet-stream} if not explicitly set.
   * @since v1.18
   */
  RequestOptions setData(Object data);
  /**
   *
   *
   * @param failOnStatusCode Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
   * @since v1.18
   */
  RequestOptions setFailOnStatusCode(boolean failOnStatusCode);
  /**
   * Provides {@code FormData} object that will be serialized as html form using {@code application/x-www-form-urlencoded}
   * encoding and sent as this request body. If this parameter is specified {@code content-type} header will be set to {@code
   * application/x-www-form-urlencoded} unless explicitly provided.
   *
   * @param form Form data to be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as this
   * request body.
   * @since v1.18
   */
  RequestOptions setForm(FormData form);
  /**
   * Sets an HTTP header to the request. This header will apply to the fetched request as well as any redirects initiated by
   * it.
   *
   * @param name Header name.
   * @param value Header value.
   * @since v1.18
   */
  RequestOptions setHeader(String name, String value);
  /**
   *
   *
   * @param ignoreHTTPSErrors Whether to ignore HTTPS errors when sending network requests.
   * @since v1.18
   */
  RequestOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors);
  /**
   *
   *
   * @param maxRedirects Maximum number of request redirects that will be followed automatically. An error will be thrown if the number is
   * exceeded. Defaults to {@code 20}. Pass {@code 0} to not follow redirects.
   * @since v1.26
   */
  RequestOptions setMaxRedirects(int maxRedirects);
  /**
   * Changes the request method (e.g. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> or <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a>).
   *
   * @param method Request method, e.g. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a>.
   * @since v1.18
   */
  RequestOptions setMethod(String method);
  /**
   * Provides {@code FormData} object that will be serialized as html form using {@code multipart/form-data} encoding and
   * sent as this request body. If this parameter is specified {@code content-type} header will be set to {@code
   * multipart/form-data} unless explicitly provided.
   *
   * @param form Form data to be serialized as html form using {@code multipart/form-data} encoding and sent as this request body.
   * @since v1.18
   */
  RequestOptions setMultipart(FormData form);
  /**
   * Adds a query parameter to the request URL.
   *
   * @param name Parameter name.
   * @param value Parameter value.
   * @since v1.18
   */
  RequestOptions setQueryParam(String name, String value);
  /**
   * Adds a query parameter to the request URL.
   *
   * @param name Parameter name.
   * @param value Parameter value.
   * @since v1.18
   */
  RequestOptions setQueryParam(String name, boolean value);
  /**
   * Adds a query parameter to the request URL.
   *
   * @param name Parameter name.
   * @param value Parameter value.
   * @since v1.18
   */
  RequestOptions setQueryParam(String name, int value);
  /**
   * Sets request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
   *
   * @param timeout Request timeout in milliseconds.
   * @since v1.18
   */
  RequestOptions setTimeout(double timeout);
}

