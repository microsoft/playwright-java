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

public class Proxy {
  /**
   * Proxy to be used for all requests. HTTP and SOCKS proxies are supported, for example {@code http://myproxy.com:3128} or
   * {@code socks5://myproxy.com:3128}. Short form {@code myproxy.com:3128} is considered an HTTP proxy.
   */
  public String server;
  /**
   * Optional comma-separated domains to bypass proxy, for example {@code ".com, chromium.org, .domain.com"}.
   */
  public String bypass;
  /**
   * Optional username to use if HTTP proxy requires authentication.
   */
  public String username;
  /**
   * Optional password to use if HTTP proxy requires authentication.
   */
  public String password;

  public Proxy(String server) {
    this.server = server;
  }
  /**
   * Optional comma-separated domains to bypass proxy, for example {@code ".com, chromium.org, .domain.com"}.
   */
  public Proxy setBypass(String bypass) {
    this.bypass = bypass;
    return this;
  }
  /**
   * Optional username to use if HTTP proxy requires authentication.
   */
  public Proxy setUsername(String username) {
    this.username = username;
    return this;
  }
  /**
   * Optional password to use if HTTP proxy requires authentication.
   */
  public Proxy setPassword(String password) {
    this.password = password;
    return this;
  }
}