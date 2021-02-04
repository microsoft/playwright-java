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

import java.util.*;

/**
 * Whenever the page sends a request for a network resource the following sequence of events are emitted by {@code Page}:
 * - [{@code event: Page.request}] emitted when the request is issued by the page.
 * - [{@code event: Page.response}] emitted when/if the response status and headers are received for the request.
 * - [{@code event: Page.requestFinished}] emitted when the response body is downloaded and the request is complete.
 *
 * <p> If request fails at some point, then instead of {@code 'requestfinished'} event (and possibly instead of 'response' event),
 * the  [{@code event: Page.requestFailed}] event is emitted.
 *
 * <p> <strong>NOTE:</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will
 * complete with {@code 'requestfinished'} event.
 *
 * <p> If request gets a 'redirect' response, the request is successfully finished with the 'requestfinished' event, and a new
 * request is  issued to a redirected url.
 */
public interface Request {
  class RequestTiming {
    /**
     * Request start time in milliseconds elapsed since January 1, 1970 00:00:00 UTC
     */
    private double startTime;
    /**
     * Time immediately before the browser starts the domain name lookup for the resource. The value is given in milliseconds
     * relative to {@code startTime}, -1 if not available.
     */
    private double domainLookupStart;
    /**
     * Time immediately after the browser starts the domain name lookup for the resource. The value is given in milliseconds
     * relative to {@code startTime}, -1 if not available.
     */
    private double domainLookupEnd;
    /**
     * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The
     * value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private double connectStart;
    /**
     * Time immediately before the browser starts the handshake process to secure the current connection. The value is given in
     * milliseconds relative to {@code startTime}, -1 if not available.
     */
    private double secureConnectionStart;
    /**
     * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The
     * value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private double connectEnd;
    /**
     * Time immediately before the browser starts requesting the resource from the server, cache, or local resource. The value
     * is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private double requestStart;
    /**
     * Time immediately after the browser starts requesting the resource from the server, cache, or local resource. The value
     * is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private double responseStart;
    /**
     * Time immediately after the browser receives the last byte of the resource or immediately before the transport connection
     * is closed, whichever comes first. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private double responseEnd;

    public double startTime() {
      return this.startTime;
    }
    public double domainLookupStart() {
      return this.domainLookupStart;
    }
    public double domainLookupEnd() {
      return this.domainLookupEnd;
    }
    public double connectStart() {
      return this.connectStart;
    }
    public double secureConnectionStart() {
      return this.secureConnectionStart;
    }
    public double connectEnd() {
      return this.connectEnd;
    }
    public double requestStart() {
      return this.requestStart;
    }
    public double responseStart() {
      return this.responseStart;
    }
    public double responseEnd() {
      return this.responseEnd;
    }
  }
  /**
   * The method returns {@code null} unless this request has failed, as reported by {@code requestfailed} event.
   *
   * <p> Example of logging of all the failed requests:
   */
  String failure();
  /**
   * Returns the {@code Frame} that initiated this request.
   */
  Frame frame();
  /**
   * An object with HTTP headers associated with the request. All header names are lower-case.
   */
  Map<String, String> headers();
  /**
   * Whether this request is driving frame's navigation.
   */
  boolean isNavigationRequest();
  /**
   * Request's method (GET, POST, etc.)
   */
  String method();
  /**
   * Request's post body, if any.
   */
  String postData();
  /**
   * Request's post body in a binary form, if any.
   */
  byte[] postDataBuffer();
  /**
   * Request that was redirected by the server to this one, if any.
   *
   * <p> When the server responds with a redirect, Playwright creates a new {@code Request} object. The two requests are connected by
   * {@code redirectedFrom()} and {@code redirectedTo()} methods. When multiple server redirects has happened, it is possible to
   * construct the whole redirect chain by repeatedly calling {@code redirectedFrom()}.
   *
   * <p> For example, if the website {@code http://example.com} redirects to {@code https://example.com}:
   *
   * <p> If the website {@code https://google.com} has no redirects:
   */
  Request redirectedFrom();
  /**
   * New request issued by the browser if the server responded with redirect.
   *
   * <p> This method is the opposite of [{@code method: Request.redirectedFrom}]:
   */
  Request redirectedTo();
  /**
   * Contains the request's resource type as it was perceived by the rendering engine. ResourceType will be one of the
   * following: {@code document}, {@code stylesheet}, {@code image}, {@code media}, {@code font}, {@code script}, {@code texttrack}, {@code xhr}, {@code fetch}, {@code eventsource},
   * {@code websocket}, {@code manifest}, {@code other}.
   */
  String resourceType();
  /**
   * Returns the matching {@code Response} object, or {@code null} if the response was not received due to error.
   */
  Response response();
  /**
   * Returns resource timing information for given request. Most of the timing values become available upon the response,
   * {@code responseEnd} becomes available when request finishes. Find more information at
   * [Resource Timing API](https://developer.mozilla.org/en-US/docs/Web/API/PerformanceResourceTiming).
   */
  RequestTiming timing();
  /**
   * URL of the request.
   */
  String url();
}

