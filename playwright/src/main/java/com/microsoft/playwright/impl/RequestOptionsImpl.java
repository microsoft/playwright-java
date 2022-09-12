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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestOptionsImpl implements RequestOptions {
  Map<String, Object> params;
  String method;
  Map<String, String> headers;
  Object data;
  FormDataImpl form;
  FormDataImpl multipart;
  Boolean failOnStatusCode;
  Boolean ignoreHTTPSErrors;
  Double timeout;
  Integer maxRedirects;

  @Override
  public RequestOptions setHeader(String name, String value) {
    if (headers == null) {
      headers = new LinkedHashMap<>();
    }
    headers.put(name, value);
    return this;
  }

  @Override
  public RequestOptions setData(String data) {
    this.data = data;
    return this;
  }

  @Override
  public RequestOptions setData(byte[] data) {
    this.data = data;
    return this;
  }

  @Override
  public RequestOptions setData(Object data) {
    this.data = data;
    return this;
  }

  @Override
  public RequestOptions setForm(FormData form) {
    this.form = (FormDataImpl) form;
    return this;
  }

  @Override
  public RequestOptions setMethod(String method) {
    this.method = method;
    return this;
  }

  @Override
  public RequestOptions setMultipart(FormData form) {
    this.multipart = (FormDataImpl) form;
    return this;
  }

  @Override
  public RequestOptions setQueryParam(String name, String value) {
    return setQueryParamImpl(name, value);
  }

  @Override
  public RequestOptions setQueryParam(String name, boolean value) {
    return setQueryParamImpl(name, value);
  }

  @Override
  public RequestOptions setQueryParam(String name, int value) {
    return setQueryParamImpl(name, value);
  }

  private RequestOptions setQueryParamImpl(String name, Object value) {
    if (params == null) {
      params = new LinkedHashMap<>();
    }
    params.put(name, value);
    return this;
  }

  @Override
  public RequestOptions setTimeout(double timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public RequestOptions setFailOnStatusCode(boolean failOnStatusCode) {
    this.failOnStatusCode = failOnStatusCode;
    return this;
  }

  @Override
  public RequestOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
    this.ignoreHTTPSErrors = ignoreHTTPSErrors;
    return this;
  }

  @Override
  public RequestOptions setMaxRedirects(int maxRedirects) {
    this.maxRedirects = maxRedirects;
    return this;
  }
}
