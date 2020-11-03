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

public interface Request {
  class RequestFailure {
    private String errorText;

    public RequestFailure(String errorText) {
      this.errorText = errorText;
    }
    public String errorText() {
      return this.errorText;
    }
  }
  class RequestPostDataJSON {

  }
  class RequestTiming {
    private int startTime;
    private int domainLookupStart;
    private int domainLookupEnd;
    private int connectStart;
    private int secureConnectionStart;
    private int connectEnd;
    private int requestStart;
    private int responseStart;
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
  RequestFailure failure();
  Frame frame();
  Map<String, String> headers();
  boolean isNavigationRequest();
  String method();
  String postData();
  byte[] postDataBuffer();
  Request redirectedFrom();
  Request redirectedTo();
  String resourceType();
  Response response();
  RequestTiming timing();
  String url();
}

