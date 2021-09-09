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
 * Whenever the page sends a request for a network resource the following sequence of events are emitted by {@code Page}:
 * <ul>
 * <li> {@link Page#onRequest Page.onRequest()} emitted when the request is issued by the page.</li>
 * <li> {@link Page#onResponse Page.onResponse()} emitted when/if the response status and headers are received for the request.</li>
 * <li> {@link Page#onRequestFinished Page.onRequestFinished()} emitted when the response body is downloaded and the request is
 * complete.</li>
 * </ul>
 *
 * <p> If request fails at some point, then instead of {@code "requestfinished"} event (and possibly instead of 'response' event),
 * the  {@link Page#onRequestFailed Page.onRequestFailed()} event is emitted.
 *
 * <p> <strong>NOTE:</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete
 * with {@code "requestfinished"} event.
 *
 * <p> If request gets a 'redirect' response, the request is successfully finished with the 'requestfinished' event, and a new
 * request is  issued to a redirected url.
 */
public interface Request {
  /**
   * An object with all the request HTTP headers associated with this request. The header names are lower-cased.
   */
  Map<String, String> allHeaders();
  /**
   * The method returns {@code null} unless this request has failed, as reported by {@code requestfailed} event.
   *
   * <p> Example of logging of all the failed requests:
   * <pre>{@code
   * page.onRequestFailed(request -> {
   *   System.out.println(request.url() + " " + request.failure());
   * });
   * }</pre>
   */
  String failure();
  /**
   * Returns the {@code Frame} that initiated this request.
   */
  Frame frame();
  /**
   * **DEPRECATED** Incomplete list of headers as seen by the rendering engine. Use {@link Request#allHeaders
   * Request.allHeaders()} instead.
   */
  Map<String, String> headers();
  /**
   * An array with all the request HTTP headers associated with this request. Unlike {@link Request#allHeaders
   * Request.allHeaders()}, header names are not lower-cased. Headers with multiple entries, such as {@code Set-Cookie}, appear in
   * the array multiple times.
   */
  List<HttpHeader> headersArray();
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
   * <pre>{@code
   * Response response = page.navigate("http://example.com");
   * System.out.println(response.request().redirectedFrom().url()); // "http://example.com"
   * }</pre>
   *
   * <p> If the website {@code https://google.com} has no redirects:
   * <pre>{@code
   * Response response = page.navigate("https://google.com");
   * System.out.println(response.request().redirectedFrom()); // null
   * }</pre>
   */
  Request redirectedFrom();
  /**
   * New request issued by the browser if the server responded with redirect.
   *
   * <p> This method is the opposite of {@link Request#redirectedFrom Request.redirectedFrom()}:
   * <pre>{@code
   * System.out.println(request.redirectedFrom().redirectedTo() == request); // true
   * }</pre>
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
   * Returns resource size information for given request.
   */
  Sizes sizes();
  /**
   * Returns resource timing information for given request. Most of the timing values become available upon the response,
   * {@code responseEnd} becomes available when request finishes. Find more information at <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/PerformanceResourceTiming">Resource Timing API</a>.
   * <pre>{@code
   * page.onRequestFinished(request -> {
   *   Timing timing = request.timing();
   *   System.out.println(timing.responseEnd - timing.startTime);
   * });
   * page.navigate("http://example.com");
   * }</pre>
   */
  Timing timing();
  /**
   * URL of the request.
   */
  String url();
}

