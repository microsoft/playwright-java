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
import java.util.*;

/**
 * {@code APIResponse} class represents responses returned by {@link APIRequestContext#get APIRequestContext.get()} and
 * similar methods.
 */
public interface APIResponse {
  /**
   * Returns the buffer with response body.
   *
   * @since v1.16
   */
  byte[] body();
  /**
   * Disposes the body of this response. If not called then the body will stay in memory until the context closes.
   *
   * @since v1.16
   */
  void dispose();
  /**
   * An object with all the response HTTP headers associated with this response.
   *
   * @since v1.16
   */
  Map<String, String> headers();
  /**
   * An array with all the request HTTP headers associated with this response. Header names are not lower-cased. Headers with
   * multiple entries, such as {@code Set-Cookie}, appear in the array multiple times.
   *
   * @since v1.16
   */
  List<HttpHeader> headersArray();
  /**
   * Contains a boolean stating whether the response was successful (status in the range 200-299) or not.
   *
   * @since v1.16
   */
  boolean ok();
  /**
   * Contains the status code of the response (e.g., 200 for a success).
   *
   * @since v1.16
   */
  int status();
  /**
   * Contains the status text of the response (e.g. usually an "OK" for a success).
   *
   * @since v1.16
   */
  String statusText();
  /**
   * Returns the text representation of response body.
   *
   * @since v1.16
   */
  String text();
  /**
   * Contains the URL of the response.
   *
   * @since v1.16
   */
  String url();
}

