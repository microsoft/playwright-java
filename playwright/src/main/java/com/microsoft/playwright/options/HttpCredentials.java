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

public class HttpCredentials {
  public String username;
  public String password;
  /**
   * Restrain sending http credentials on specific origin (scheme://host:port).
   */
  public String origin;

  public HttpCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }
  /**
   * Restrain sending http credentials on specific origin (scheme://host:port).
   */
  public HttpCredentials setOrigin(String origin) {
    this.origin = origin;
    return this;
  }
}