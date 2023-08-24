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
 * <p> If request fails at some point, then instead of {@code "requestfinished"} event (and possibly instead of 'response'
 * event), the  {@link Page#onRequestFailed Page.onRequestFailed()} event is emitted.
 *
 * <p> <strong>NOTE:</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete
 * with {@code "requestfinished"} event.
 *
 * <p> If request gets a 'redirect' response, the request is successfully finished with the {@code requestfinished} event, and
 * a new request is  issued to a redirected url.
 */
public interface Request {
  /**
   * An object with all the request HTTP headers associated with this request. The header names are lower-cased.
   *
   * @since v1.15
   */
  Map<String, String> allHeaders();
  /**
   * The method returns {@code null} unless this request has failed, as reported by {@code requestfailed} event.
   *
   * <p> **Usage**
   *
   * <p> Example of logging of all the failed requests:
   * <pre>{@code
   * page.onRequestFailed(request -> {
   *   System.out.println(request.url() + " " + request.failure());
   * });
   * }</pre>
   *
   * @since v1.8
   */
  String failure();
  /**
   * Returns the {@code Frame} that initiated this request.
   *
   * <p> **Usage**
   * <pre>{@code
   * String frameUrl = request.frame().url();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Note that in some cases the frame is not available, and this method will throw.
   * <ul>
   * <li> When request originates in the Service Worker. You can use {@code request.serviceWorker()} to check that.</li>
   * <li> When navigation request is issued before the corresponding frame is created. You can use {@link
   * Request#isNavigationRequest Request.isNavigationRequest()} to check that.</li>
   * </ul>
   *
   * <p> Here is an example that handles all the cases:
   *
   * @since v1.8
   */
  Frame frame();
  /**
   * An object with the request HTTP headers. The header names are lower-cased. Note that this method does not return
   * security-related headers, including cookie-related ones. You can use {@link Request#allHeaders Request.allHeaders()} for
   * complete list of headers that include {@code cookie} information.
   *
   * @since v1.8
   */
  Map<String, String> headers();
  /**
   * An array with all the request HTTP headers associated with this request. Unlike {@link Request#allHeaders
   * Request.allHeaders()}, header names are NOT lower-cased. Headers with multiple entries, such as {@code Set-Cookie},
   * appear in the array multiple times.
   *
   * @since v1.15
   */
  List<HttpHeader> headersArray();
  /**
   * Returns the value of the header matching the name. The name is case insensitive.
   *
   * @param name Name of the header.
   * @since v1.15
   */
  String headerValue(String name);
  /**
   * Whether this request is driving frame's navigation.
   *
   * <p> Some navigation requests are issued before the corresponding frame is created, and therefore do not have {@link
   * Request#frame Request.frame()} available.
   *
   * @since v1.8
   */
  boolean isNavigationRequest();
  /**
   * Request's method (GET, POST, etc.)
   *
   * @since v1.8
   */
  String method();
  /**
   * Request's post body, if any.
   *
   * @since v1.8
   */
  String postData();
  /**
   * Request's post body in a binary form, if any.
   *
   * @since v1.8
   */
  byte[] postDataBuffer();
  /**
   * Request that was redirected by the server to this one, if any.
   *
   * <p> When the server responds with a redirect, Playwright creates a new {@code Request} object. The two requests are
   * connected by {@code redirectedFrom()} and {@code redirectedTo()} methods. When multiple server redirects has happened,
   * it is possible to construct the whole redirect chain by repeatedly calling {@code redirectedFrom()}.
   *
   * <p> **Usage**
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
   *
   * @since v1.8
   */
  Request redirectedFrom();
  /**
   * New request issued by the browser if the server responded with redirect.
   *
   * <p> **Usage**
   *
   * <p> This method is the opposite of {@link Request#redirectedFrom Request.redirectedFrom()}:
   * <pre>{@code
   * System.out.println(request.redirectedFrom().redirectedTo() == request); // true
   * }</pre>
   *
   * @since v1.8
   */
  Request redirectedTo();
  /**
   * Contains the request's resource type as it was perceived by the rendering engine. ResourceType will be one of the
   * following: {@code document}, {@code stylesheet}, {@code image}, {@code media}, {@code font}, {@code script}, {@code
   * texttrack}, {@code xhr}, {@code fetch}, {@code eventsource}, {@code websocket}, {@code manifest}, {@code other}.
   *
   * @since v1.8
   */
  String resourceType();
  /**
   * Returns the matching {@code Response} object, or {@code null} if the response was not received due to error.
   *
   * @since v1.8
   */
  Response response();
  /**
   * Returns resource size information for given request.
   *
   * @since v1.15
   */
  Sizes sizes();
  /**
   * Returns resource timing information for given request. Most of the timing values become available upon the response,
   * {@code responseEnd} becomes available when request finishes. Find more information at <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/PerformanceResourceTiming">Resource Timing API</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.onRequestFinished(request -> {
   *   Timing timing = request.timing();
   *   System.out.println(timing.responseEnd - timing.startTime);
   * });
   * page.navigate("http://example.com");
   * }</pre>
   *
   * @since v1.8
   */
  Timing timing();
  /**
   * URL of the request.
   *
   * @since v1.8
   */
  String url();
}

