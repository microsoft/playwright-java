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

public class Timing {
  /**
   * Request start time in milliseconds elapsed since January 1, 1970 00:00:00 UTC
   */
  public double startTime;
  /**
   * Time immediately before the browser starts the domain name lookup for the resource. The value is given in milliseconds
   * relative to {@code startTime}, -1 if not available.
   */
  public double domainLookupStart;
  /**
   * Time immediately after the browser starts the domain name lookup for the resource. The value is given in milliseconds
   * relative to {@code startTime}, -1 if not available.
   */
  public double domainLookupEnd;
  /**
   * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The
   * value is given in milliseconds relative to {@code startTime}, -1 if not available.
   */
  public double connectStart;
  /**
   * Time immediately before the browser starts the handshake process to secure the current connection. The value is given in
   * milliseconds relative to {@code startTime}, -1 if not available.
   */
  public double secureConnectionStart;
  /**
   * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The
   * value is given in milliseconds relative to {@code startTime}, -1 if not available.
   */
  public double connectEnd;
  /**
   * Time immediately before the browser starts requesting the resource from the server, cache, or local resource. The value
   * is given in milliseconds relative to {@code startTime}, -1 if not available.
   */
  public double requestStart;
  /**
   * Time immediately after the browser receives the first byte of the response from the server, cache, or local resource.
   * The value is given in milliseconds relative to {@code startTime}, -1 if not available.
   */
  public double responseStart;
  /**
   * Time immediately after the browser receives the last byte of the resource or immediately before the transport connection
   * is closed, whichever comes first. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
   */
  public double responseEnd;

}