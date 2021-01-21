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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Whenever a network route is set up with [{@code method: Page.route}] or [{@code method: BrowserContext.route}], the {@code Route} object
 * allows to handle the route.
 */
public interface Route {
  class ContinueOptions {
    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    public Map<String, String> headers;
    /**
     * If set changes the request method (e.g. GET or POST)
     */
    public String method;
    /**
     * If set changes the post data of request
     */
    public byte[] postData;
    /**
     * If set changes the request URL. New URL must have same protocol as original one.
     */
    public String url;

    public ContinueOptions withHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    public ContinueOptions withMethod(String method) {
      this.method = method;
      return this;
    }
    public ContinueOptions withPostData(String postData) {
      this.postData = postData.getBytes(StandardCharsets.UTF_8);
      return this;
    }
    public ContinueOptions withPostData(byte[] postData) {
      this.postData = postData;
      return this;
    }
    public ContinueOptions withUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class FulfillOptions {
    /**
     * Response body.
     */
    public String body;
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
     * File path to respond with. The content type will be inferred from file extension. If {@code path} is a relative path, then it
     * is resolved relative to the current working directory.
     */
    public Path path;
    /**
     * Response status code, defaults to {@code 200}.
     */
    public int status;

    public FulfillOptions withBody(byte[] body) {
      this.bodyBytes = body;
      return this;
    }
    public FulfillOptions withBody(String body) {
      this.body = body;
      return this;
    }
    public FulfillOptions withContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }
    public FulfillOptions withHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    public FulfillOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public FulfillOptions withStatus(int status) {
      this.status = status;
      return this;
    }
  }
  default void abort() {
    abort(null);
  }
  /**
   * Aborts the route's request.
   *
   * @param errorCode Optional error code. Defaults to {@code failed}, could be one of the following:
   * - {@code 'aborted'} - An operation was aborted (due to user action)
   * - {@code 'accessdenied'} - Permission to access a resource, other than the network, was denied
   * - {@code 'addressunreachable'} - The IP address is unreachable. This usually means that there is no route to the specified
   *   host or network.
   * - {@code 'blockedbyclient'} - The client chose to block the request.
   * - {@code 'blockedbyresponse'} - The request failed because the response was delivered along with requirements which are not
   *   met ('X-Frame-Options' and 'Content-Security-Policy' ancestor checks, for instance).
   * - {@code 'connectionaborted'} - A connection timed out as a result of not receiving an ACK for data sent.
   * - {@code 'connectionclosed'} - A connection was closed (corresponding to a TCP FIN).
   * - {@code 'connectionfailed'} - A connection attempt failed.
   * - {@code 'connectionrefused'} - A connection attempt was refused.
   * - {@code 'connectionreset'} - A connection was reset (corresponding to a TCP RST).
   * - {@code 'internetdisconnected'} - The Internet connection has been lost.
   * - {@code 'namenotresolved'} - The host name could not be resolved.
   * - {@code 'timedout'} - An operation timed out.
   * - {@code 'failed'} - A generic failure occurred.
   */
  void abort(String errorCode);
  default void continue_() {
    continue_(null);
  }
  /**
   * Continues route's request with optional overrides.
   */
  void continue_(ContinueOptions options);
  default void fulfill() {
    fulfill(null);
  }
  /**
   * Fulfills route's request with given response.
   */
  void fulfill(FulfillOptions options);
  /**
   * A request to be routed.
   */
  Request request();
}

