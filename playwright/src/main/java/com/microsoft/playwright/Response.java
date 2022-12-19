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
 * {@code Response} class represents responses which are received by page.
 */
public interface Response {
  /**
   * An object with all the response HTTP headers associated with this response.
   */
  Map<String, String> allHeaders();
  /**
   * Returns the buffer with response body.
   */
  byte[] body();
  /**
   * Waits for this response to finish, returns always {@code null}.
   */
  String finished();
  /**
   * Returns the {@code Frame} that initiated this response.
   */
  Frame frame();
  /**
   * Indicates whether this Response was fulfilled by a Service Worker's Fetch Handler (i.e. via <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/FetchEvent/respondWith">FetchEvent.respondWith</a>).
   */
  boolean fromServiceWorker();
  /**
   * An object with the response HTTP headers. The header names are lower-cased. Note that this method does not return
   * security-related headers, including cookie-related ones. You can use {@link Response#allHeaders Response.allHeaders()}
   * for complete list of headers that include {@code cookie} information.
   */
  Map<String, String> headers();
  /**
   * An array with all the request HTTP headers associated with this response. Unlike {@link Response#allHeaders
   * Response.allHeaders()}, header names are NOT lower-cased. Headers with multiple entries, such as {@code Set-Cookie},
   * appear in the array multiple times.
   */
  List<HttpHeader> headersArray();
  /**
   * Returns the value of the header matching the name. The name is case insensitive. If multiple headers have the same name
   * (except {@code set-cookie}), they are returned as a list separated by {@code , }. For {@code set-cookie}, the {@code \n}
   * separator is used. If no headers are found, {@code null} is returned.
   *
   * @param name Name of the header.
   */
  String headerValue(String name);
  /**
   * Returns all values of the headers matching the name, for example {@code set-cookie}. The name is case insensitive.
   *
   * @param name Name of the header.
   */
  List<String> headerValues(String name);
  /**
   * Contains a boolean stating whether the response was successful (status in the range 200-299) or not.
   */
  boolean ok();
  /**
   * Returns the matching {@code Request} object.
   */
  Request request();
  /**
   * Returns SSL and other security information.
   */
  SecurityDetails securityDetails();
  /**
   * Returns the IP address and port of the server.
   */
  ServerAddr serverAddr();
  /**
   * Contains the status code of the response (e.g., 200 for a success).
   */
  int status();
  /**
   * Contains the status text of the response (e.g. usually an "OK" for a success).
   */
  String statusText();
  /**
   * Returns the text representation of response body.
   */
  String text();
  /**
   * Contains the URL of the response.
   */
  String url();
}

