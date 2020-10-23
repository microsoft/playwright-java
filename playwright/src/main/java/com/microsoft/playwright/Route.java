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

import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.*;

public interface Route {
  class ContinueOverrides {
    public String method;
    public byte[] postData;
    public Map<String, String> headers;

    public ContinueOverrides withMethod(String method) {
      this.method = method;
      return this;
    }
    public ContinueOverrides withPostData(String postData) {
      this.postData = postData.getBytes(StandardCharsets.UTF_8);
      return this;
    }
    public ContinueOverrides withPostData(byte[] postData) {
      this.postData = postData;
      return this;
    }
    public ContinueOverrides withHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
  }
  class FulfillResponse {
    public Integer status;
    public Map<String, String> headers;
    public String contentType;
    public String body;
    public File path;

    public FulfillResponse withStatus(Integer status) {
      this.status = status;
      return this;
    }
    public FulfillResponse withHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    public FulfillResponse withContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }
    public FulfillResponse withBody(String body) {
      this.body = body;
      return this;
    }
    public FulfillResponse withPath(File path) {
      this.path = path;
      return this;
    }
  }
  default void abort() {
    abort(null);
  }
  void abort(String errorCode);
  default void continue_() {
    continue_(null);
  }
  void continue_(ContinueOverrides overrides);
  void fulfill(FulfillResponse response);
  Request request();
}

