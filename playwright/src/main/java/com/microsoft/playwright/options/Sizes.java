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

public class Sizes {
  /**
   * Size of the request body (POST data payload) in bytes. Set to 0 if there was no body.
   */
  public int requestBodySize;
  /**
   * Total number of bytes from the start of the HTTP request message until (and including) the double CRLF before the body.
   */
  public int requestHeadersSize;
  /**
   * Size of the received response body (encoded) in bytes.
   */
  public int responseBodySize;
  /**
   * Total number of bytes from the start of the HTTP response message until (and including) the double CRLF before the body.
   */
  public int responseHeadersSize;

}