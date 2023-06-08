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
import java.util.*;

/**
 * Exposes API that can be used for the Web API testing. This class is used for creating {@code APIRequestContext} instance
 * which in turn can be used for sending web requests. An instance of this class can be obtained via {@link
 * Playwright#request Playwright.request()}. For more information see {@code APIRequestContext}.
 */
public interface APIRequest {
  class NewContextOptions {
    /**
     * Methods like {@link APIRequestContext#get APIRequestContext.get()} take the base URL into consideration by using the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a> constructor for building the
     * corresponding URL. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and sending request to {@code /bar.html} results in {@code
     * http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and sending request to {@code ./bar.html} results in {@code
     * http://localhost:3000/foo/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo} (without trailing slash) and navigating to {@code ./bar.html} results in
     * {@code http://localhost:3000/bar.html}</li>
     * </ul>
     */
    public String baseURL;
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public Map<String, String> extraHTTPHeaders;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public HttpCredentials httpCredentials;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Network proxy settings.
     */
    public Proxy proxy;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()} or {@link APIRequestContext#storageState
     * APIRequestContext.storageState()}. Either a path to the file with saved storage, or the value returned by one of {@link
     * BrowserContext#storageState BrowserContext.storageState()} or {@link APIRequestContext#storageState
     * APIRequestContext.storageState()} methods.
     */
    public String storageState;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    public Path storageStatePath;
    /**
     * Maximum time in milliseconds to wait for the response. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable
     * timeout.
     */
    public Double timeout;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;

    /**
     * Methods like {@link APIRequestContext#get APIRequestContext.get()} take the base URL into consideration by using the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a> constructor for building the
     * corresponding URL. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and sending request to {@code /bar.html} results in {@code
     * http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and sending request to {@code ./bar.html} results in {@code
     * http://localhost:3000/foo/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo} (without trailing slash) and navigating to {@code ./bar.html} results in
     * {@code http://localhost:3000/bar.html}</li>
     * </ul>
     */
    public NewContextOptions setBaseURL(String baseURL) {
      this.baseURL = baseURL;
      return this;
    }
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public NewContextOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public NewContextOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public NewContextOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public NewContextOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Network proxy settings.
     */
    public NewContextOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    /**
     * Network proxy settings.
     */
    public NewContextOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()} or {@link APIRequestContext#storageState
     * APIRequestContext.storageState()}. Either a path to the file with saved storage, or the value returned by one of {@link
     * BrowserContext#storageState BrowserContext.storageState()} or {@link APIRequestContext#storageState
     * APIRequestContext.storageState()} methods.
     */
    public NewContextOptions setStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    public NewContextOptions setStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    /**
     * Maximum time in milliseconds to wait for the response. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable
     * timeout.
     */
    public NewContextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Specific user agent to use in this context.
     */
    public NewContextOptions setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
  }
  /**
   * Creates new instances of {@code APIRequestContext}.
   *
   * @since v1.16
   */
  default APIRequestContext newContext() {
    return newContext(null);
  }
  /**
   * Creates new instances of {@code APIRequestContext}.
   *
   * @since v1.16
   */
  APIRequestContext newContext(NewContextOptions options);
}

