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

public class Cookie {
  public String name;
  public String value;
  /**
   * either url or domain / path are required. Optional.
   */
  public String url;
  /**
   * either url or domain / path are required Optional.
   */
  public String domain;
  /**
   * either url or domain / path are required Optional.
   */
  public String path;
  /**
   * Unix time in seconds. Optional.
   */
  public Double expires;
  /**
   * Optional.
   */
  public Boolean httpOnly;
  /**
   * Optional.
   */
  public Boolean secure;
  /**
   * Optional.
   */
  public SameSiteAttribute sameSite;

  public Cookie(String name, String value) {
    this.name = name;
    this.value = value;
  }
  /**
   * either url or domain / path are required. Optional.
   */
  public Cookie setUrl(String url) {
    this.url = url;
    return this;
  }
  /**
   * either url or domain / path are required Optional.
   */
  public Cookie setDomain(String domain) {
    this.domain = domain;
    return this;
  }
  /**
   * either url or domain / path are required Optional.
   */
  public Cookie setPath(String path) {
    this.path = path;
    return this;
  }
  /**
   * Unix time in seconds. Optional.
   */
  public Cookie setExpires(double expires) {
    this.expires = expires;
    return this;
  }
  /**
   * Optional.
   */
  public Cookie setHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
    return this;
  }
  /**
   * Optional.
   */
  public Cookie setSecure(boolean secure) {
    this.secure = secure;
    return this;
  }
  /**
   * Optional.
   */
  public Cookie setSameSite(SameSiteAttribute sameSite) {
    this.sameSite = sameSite;
    return this;
  }
}