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

import org.jspecify.annotations.Nullable;

public class Cookie {
  public String name;
  public String value;
  /**
   * Either {@code url} or both {@code domain} and {@code path} are required. Optional.
   */
  public @Nullable String url;
  /**
   * For the cookie to apply to all subdomains as well, prefix domain with a dot, like this: ".example.com". Either {@code
   * url} or both {@code domain} and {@code path} are required. Optional.
   */
  public @Nullable String domain;
  /**
   * Either {@code url} or both {@code domain} and {@code path} are required. Optional.
   */
  public @Nullable String path;
  /**
   * Unix time in seconds. Optional.
   */
  public @Nullable Double expires;
  /**
   * Optional.
   */
  public @Nullable Boolean httpOnly;
  /**
   * Optional.
   */
  public @Nullable Boolean secure;
  /**
   * Optional.
   */
  public @Nullable SameSiteAttribute sameSite;
  /**
   * For partitioned third-party cookies (aka <a
   * href="https://developer.mozilla.org/en-US/docs/Web/Privacy/Guides/Privacy_sandbox/Partitioned_cookies">CHIPS</a>), the
   * partition key. Optional.
   */
  public @Nullable String partitionKey;

  public Cookie(String name, String value) {
    this.name = name;
    this.value = value;
  }
  /**
   * Either {@code url} or both {@code domain} and {@code path} are required. Optional.
   */
  public Cookie setUrl(@Nullable String url) {
    this.url = url;
    return this;
  }
  /**
   * For the cookie to apply to all subdomains as well, prefix domain with a dot, like this: ".example.com". Either {@code
   * url} or both {@code domain} and {@code path} are required. Optional.
   */
  public Cookie setDomain(@Nullable String domain) {
    this.domain = domain;
    return this;
  }
  /**
   * Either {@code url} or both {@code domain} and {@code path} are required. Optional.
   */
  public Cookie setPath(@Nullable String path) {
    this.path = path;
    return this;
  }
  /**
   * Unix time in seconds. Optional.
   */
  public Cookie setExpires(@Nullable Double expires) {
    this.expires = expires;
    return this;
  }
  /**
   * Optional.
   */
  public Cookie setHttpOnly(@Nullable Boolean httpOnly) {
    this.httpOnly = httpOnly;
    return this;
  }
  /**
   * Optional.
   */
  public Cookie setSecure(@Nullable Boolean secure) {
    this.secure = secure;
    return this;
  }
  /**
   * Optional.
   */
  public Cookie setSameSite(@Nullable SameSiteAttribute sameSite) {
    this.sameSite = sameSite;
    return this;
  }
  /**
   * For partitioned third-party cookies (aka <a
   * href="https://developer.mozilla.org/en-US/docs/Web/Privacy/Guides/Privacy_sandbox/Partitioned_cookies">CHIPS</a>), the
   * partition key. Optional.
   */
  public Cookie setPartitionKey(@Nullable String partitionKey) {
    this.partitionKey = partitionKey;
    return this;
  }
}