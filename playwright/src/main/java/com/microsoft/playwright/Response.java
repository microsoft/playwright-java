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
 * {@code Response} class represents responses which are received by page.
 */
public interface Response {
  /**
   * Returns the buffer with response body.
   */
  byte[] body();
  /**
   * Waits for this response to finish, returns failure error if request failed.
   */
  String finished();
  /**
   * Returns the {@code Frame} that initiated this response.
   */
  Frame frame();
  /**
   * Returns the object with HTTP headers associated with the response. All header names are lower-case.
   */
  Map<String, String> headers();
  /**
   * Contains a boolean stating whether the response was successful (status in the range 200-299) or not.
   */
  boolean ok();
  /**
   * Returns the matching {@code Request} object.
   */
  Request request();
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

