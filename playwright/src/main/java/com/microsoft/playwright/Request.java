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
 * Whenever the page sends a request for a network resource the following sequence of events are emitted by Page:
 * <p>
 * {@code 'request'} emitted when the request is issued by the page.
 * <p>
 * {@code 'response'} emitted when/if the response status and headers are received for the request.
 * <p>
 * {@code 'requestfinished'} emitted when the response body is downloaded and the request is complete.
 * <p>
 * If request fails at some point, then instead of {@code 'requestfinished'} event (and possibly instead of 'response' event), the  {@code 'requestfailed'} event is emitted.
 * <p>
 * <strong>NOTE</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete with {@code 'requestfinished'} event.
 * <p>
 * If request gets a 'redirect' response, the request is successfully finished with the 'requestfinished' event, and a new request is  issued to a redirected url.
 */
public interface Request {
  class RequestFailure {
    /**
     * Human-readable error message, e.g. {@code 'net::ERR_FAILED'}.
     */
    private String errorText;

    public RequestFailure(String errorText) {
      this.errorText = errorText;
    }
    public String errorText() {
      return this.errorText;
    }
  }
  class RequestTiming {
    /**
     * Request start time in milliseconds elapsed since January 1, 1970 00:00:00 UTC
     */
    private int startTime;
    /**
     * Time immediately before the browser starts the domain name lookup for the resource. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int domainLookupStart;
    /**
     * Time immediately after the browser starts the domain name lookup for the resource. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int domainLookupEnd;
    /**
     * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int connectStart;
    /**
     * Time immediately before the browser starts the handshake process to secure the current connection. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int secureConnectionStart;
    /**
     * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int connectEnd;
    /**
     * Time immediately before the browser starts requesting the resource from the server, cache, or local resource. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int requestStart;
    /**
     * Time immediately after the browser starts requesting the resource from the server, cache, or local resource. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    private int responseStart;
    /**
     * Time immediately after the browser receives the last byte of the resource or immediately before the transport connection is closed, whichever comes first. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     * };
     */
    private int responseEnd;

    public int startTime() {
      return this.startTime;
    }
    public int domainLookupStart() {
      return this.domainLookupStart;
    }
    public int domainLookupEnd() {
      return this.domainLookupEnd;
    }
    public int connectStart() {
      return this.connectStart;
    }
    public int secureConnectionStart() {
      return this.secureConnectionStart;
    }
    public int connectEnd() {
      return this.connectEnd;
    }
    public int requestStart() {
      return this.requestStart;
    }
    public int responseStart() {
      return this.responseStart;
    }
    public int responseEnd() {
      return this.responseEnd;
    }
  }
  /**
   * The method returns {@code null} unless this request has failed, as reported by
   * <p>
   * {@code requestfailed} event.
   * <p>
   * Example of logging of all the failed requests:
   * <p>
   * 
   * @return Object describing request failure, if any
   */
  RequestFailure failure();
  /**
   * 
   * @return A Frame that initiated this request.
   */
  Frame frame();
  /**
   * 
   * @return An object with HTTP headers associated with the request. All header names are lower-case.
   */
  Map<String, String> headers();
  /**
   * Whether this request is driving frame's navigation.
   */
  boolean isNavigationRequest();
  /**
   * 
   * @return Request's method (GET, POST, etc.)
   */
  String method();
  /**
   * 
   * @return Request's post body, if any.
   */
  String postData();
  /**
   * 
   * @return Request's post body in a binary form, if any.
   */
  byte[] postDataBuffer();
  /**
   * When the server responds with a redirect, Playwright creates a new Request object. The two requests are connected by {@code redirectedFrom()} and {@code redirectedTo()} methods. When multiple server redirects has happened, it is possible to construct the whole redirect chain by repeatedly calling {@code redirectedFrom()}.
   * <p>
   * For example, if the website {@code http://example.com} redirects to {@code https://example.com}:
   * <p>
   * If the website {@code https://google.com} has no redirects:
   * <p>
   * 
   * @return Request that was redirected by the server to this one, if any.
   */
  Request redirectedFrom();
  /**
   * This method is the opposite of request.redirectedFrom():
   * <p>
   * 
   * @return New request issued by the browser if the server responded with redirect.
   */
  Request redirectedTo();
  /**
   * Contains the request's resource type as it was perceived by the rendering engine.
   * <p>
   * ResourceType will be one of the following: {@code document}, {@code stylesheet}, {@code image}, {@code media}, {@code font}, {@code script}, {@code texttrack}, {@code xhr}, {@code fetch}, {@code eventsource}, {@code websocket}, {@code manifest}, {@code other}.
   */
  String resourceType();
  /**
   * 
   * @return A matching Response object, or {@code null} if the response was not received due to error.
   */
  Response response();
  /**
   * Returns resource timing information for given request. Most of the timing values become available upon the response, {@code responseEnd} becomes available when request finishes. Find more information at Resource Timing API.
   * <p>
   */
  RequestTiming timing();
  /**
   * 
   * @return URL of the request.
   */
  String url();
}

