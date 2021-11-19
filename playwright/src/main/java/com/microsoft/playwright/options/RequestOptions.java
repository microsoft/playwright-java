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
import java.util.*;

/**
 * The {@code RequestOptions} allows to create form data to be sent via {@code APIRequestContext}.
 * <pre>{@code
 * context.request().post(
 *   "https://example.com/submit",
 *   RequestOptions.create()
 *     .setQueryParam("page", 1)
 *     .setData("My data"));
 * }</pre>
 */
public interface RequestOptions {
  /**
   * Creates new instance of {@code RequestOptions}.
   */
  static RequestOptions create() {
    return new RequestOptionsImpl();
  }
  /**
   * Sets the request's post data.
   *
   * @param data Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
   * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
   * be set to {@code application/octet-stream} if not explicitly set.
   */
  RequestOptions setData(String data);
  /**
   * Sets the request's post data.
   *
   * @param data Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
   * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
   * be set to {@code application/octet-stream} if not explicitly set.
   */
  RequestOptions setData(byte[] data);
  /**
   * Sets the request's post data.
   *
   * @param data Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
   * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
   * be set to {@code application/octet-stream} if not explicitly set.
   */
  RequestOptions setData(Object data);
  /**
   *
   *
   * @param failOnStatusCode Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
   */
  RequestOptions setFailOnStatusCode(boolean failOnStatusCode);
  /**
   * Provides {@code FormData} object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and
   * sent as this request body. If this parameter is specified {@code content-type} header will be set to
   * {@code application/x-www-form-urlencoded} unless explicitly provided.
   *
   * @param form Form data to be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as this request
   * body.
   */
  RequestOptions setForm(FormData form);
  /**
   * Sets an HTTP header to the request.
   *
   * @param name Header name.
   * @param value Header value.
   */
  RequestOptions setHeader(String name, String value);
  /**
   *
   *
   * @param ignoreHTTPSErrors Whether to ignore HTTPS errors when sending network requests.
   */
  RequestOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors);
  /**
   * Changes the request method (e.g. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> or <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a>).
   *
   * @param method Request method, e.g. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a>.
   */
  RequestOptions setMethod(String method);
  /**
   * Provides {@code FormData} object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this
   * request body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless
   * explicitly provided.
   *
   * @param form Form data to be serialized as html form using {@code multipart/form-data} encoding and sent as this request body.
   */
  RequestOptions setMultipart(FormData form);
  /**
   * Adds a query parameter to the request URL.
   *
   * @param name Parameter name.
   * @param value Parameter value.
   */
  RequestOptions setQueryParam(String name, String value);
  /**
   * Adds a query parameter to the request URL.
   *
   * @param name Parameter name.
   * @param value Parameter value.
   */
  RequestOptions setQueryParam(String name, boolean value);
  /**
   * Adds a query parameter to the request URL.
   *
   * @param name Parameter name.
   * @param value Parameter value.
   */
  RequestOptions setQueryParam(String name, int value);
  /**
   * Sets request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
   *
   * @param timeout Request timeout in milliseconds.
   */
  RequestOptions setTimeout(double timeout);
}

