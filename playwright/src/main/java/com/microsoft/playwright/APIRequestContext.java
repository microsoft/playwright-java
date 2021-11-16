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

import java.nio.file.Path;
import java.util.*;

/**
 * This API is used for the Web API testing. You can use it to trigger API endpoints, configure micro-services, prepare
 * environment or the service to your e2e test. When used on {@code Page} or a {@code BrowserContext}, this API will automatically use
 * the cookies from the corresponding {@code BrowserContext}. This means that if you log in using this API, your e2e test will be
 * logged in and vice versa.
 */
public interface APIRequestContext {
  class DeleteOptions {
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public Object data;
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public Map<String, Object> form;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public Map<String, Object> multipart;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public DeleteOptions setData(String data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public DeleteOptions setData(byte[] data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public DeleteOptions setData(Object data) {
      this.data = data;
      return this;
    }
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public DeleteOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public DeleteOptions setForm(Map<String, Object> form) {
      this.form = form;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public DeleteOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public DeleteOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public DeleteOptions setMultipart(Map<String, Object> multipart) {
      this.multipart = multipart;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public DeleteOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public DeleteOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FetchOptions {
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public Object data;
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public Map<String, Object> form;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * If set changes the fetch method (e.g. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> or
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a>). If not specified, GET method is
     * used.
     */
    public String method;
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public Map<String, Object> multipart;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public FetchOptions setData(String data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public FetchOptions setData(byte[] data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public FetchOptions setData(Object data) {
      this.data = data;
      return this;
    }
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public FetchOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public FetchOptions setForm(Map<String, Object> form) {
      this.form = form;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public FetchOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public FetchOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * If set changes the fetch method (e.g. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> or
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a>). If not specified, GET method is
     * used.
     */
    public FetchOptions setMethod(String method) {
      this.method = method;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public FetchOptions setMultipart(Map<String, Object> multipart) {
      this.multipart = multipart;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public FetchOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public FetchOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetOptions {
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public GetOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public GetOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public GetOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public GetOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public GetOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HeadOptions {
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public HeadOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public HeadOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public HeadOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public HeadOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public HeadOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PatchOptions {
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public Object data;
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public Map<String, Object> form;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public Map<String, Object> multipart;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PatchOptions setData(String data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PatchOptions setData(byte[] data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PatchOptions setData(Object data) {
      this.data = data;
      return this;
    }
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public PatchOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public PatchOptions setForm(Map<String, Object> form) {
      this.form = form;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public PatchOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public PatchOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public PatchOptions setMultipart(Map<String, Object> multipart) {
      this.multipart = multipart;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public PatchOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public PatchOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PostOptions {
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public Object data;
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public Map<String, Object> form;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public Map<String, Object> multipart;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PostOptions setData(String data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PostOptions setData(byte[] data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PostOptions setData(Object data) {
      this.data = data;
      return this;
    }
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public PostOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public PostOptions setForm(Map<String, Object> form) {
      this.form = form;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public PostOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public PostOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public PostOptions setMultipart(Map<String, Object> multipart) {
      this.multipart = multipart;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public PostOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public PostOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PutOptions {
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public Object data;
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public Boolean failOnStatusCode;
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public Map<String, Object> form;
    /**
     * Allows to set HTTP headers.
     */
    public Map<String, String> headers;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public Map<String, Object> multipart;
    /**
     * Query parameters to be sent with the URL.
     */
    public Map<String, Object> params;
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PutOptions setData(String data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PutOptions setData(byte[] data) {
      this.data = data;
      return this;
    }
    /**
     * Allows to set post data of the request. If the data parameter is an object, it will be serialized to json string and
     * {@code content-type} header will be set to {@code application/json} if not explicitly set. Otherwise the {@code content-type} header will
     * be set to {@code application/octet-stream} if not explicitly set.
     */
    public PutOptions setData(Object data) {
      this.data = data;
      return this;
    }
    /**
     * Whether to throw on response codes other than 2xx and 3xx. By default response object is returned for all status codes.
     */
    public PutOptions setFailOnStatusCode(boolean failOnStatusCode) {
      this.failOnStatusCode = failOnStatusCode;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code application/x-www-form-urlencoded} encoding and sent as
     * this request body. If this parameter is specified {@code content-type} header will be set to
     * {@code application/x-www-form-urlencoded} unless explicitly provided.
     */
    public PutOptions setForm(Map<String, Object> form) {
      this.form = form;
      return this;
    }
    /**
     * Allows to set HTTP headers.
     */
    public PutOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public PutOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Provides an object that will be serialized as html form using {@code multipart/form-data} encoding and sent as this request
     * body. If this parameter is specified {@code content-type} header will be set to {@code multipart/form-data} unless explicitly
     * provided. File values can be passed either as [File] or as file-like object [FilePayload] containing file name,
     * mime-type and its content.
     */
    public PutOptions setMultipart(Map<String, Object> multipart) {
      this.multipart = multipart;
      return this;
    }
    /**
     * Query parameters to be sent with the URL.
     */
    public PutOptions setParams(Map<String, Object> params) {
      this.params = params;
      return this;
    }
    /**
     * Request timeout in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public PutOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class StorageStateOptions {
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public Path path;

    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public StorageStateOptions setPath(Path path) {
      this.path = path;
      return this;
    }
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/DELETE">DELETE</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  default APIResponse delete(String url) {
    return delete(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/DELETE">DELETE</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  APIResponse delete(String url, DeleteOptions options);
  /**
   * All responses returned by {@link APIRequestContext#get APIRequestContext.get()} and similar methods are stored in the
   * memory, so that you can later call {@link APIResponse#body APIResponse.body()}. This method discards all stored
   * responses, and makes {@link APIResponse#body APIResponse.body()} throw "Response disposed" error.
   */
  void dispose();
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects.
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   */
  default APIResponse fetch(String urlOrRequest) {
    return fetch(urlOrRequest, null);
  }
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects.
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   */
  APIResponse fetch(String urlOrRequest, FetchOptions options);
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects.
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   */
  default APIResponse fetch(Request urlOrRequest) {
    return fetch(urlOrRequest, null);
  }
  /**
   * Sends HTTP(S) request and returns its response. The method will populate request cookies from the context and update
   * context cookies from the response. The method will automatically follow redirects.
   *
   * @param urlOrRequest Target URL or Request to get all parameters from.
   */
  APIResponse fetch(Request urlOrRequest, FetchOptions options);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">GET</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  default APIResponse get(String url) {
    return get(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">GET</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  APIResponse get(String url, GetOptions options);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/HEAD">HEAD</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  default APIResponse head(String url) {
    return head(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/HEAD">HEAD</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  APIResponse head(String url, HeadOptions options);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PATCH">PATCH</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  default APIResponse patch(String url) {
    return patch(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PATCH">PATCH</a> request and returns
   * its response. The method will populate request cookies from the context and update context cookies from the response.
   * The method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  APIResponse patch(String url, PatchOptions options);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  default APIResponse post(String url) {
    return post(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  APIResponse post(String url, PostOptions options);
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  default APIResponse put(String url) {
    return put(url, null);
  }
  /**
   * Sends HTTP(S) <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a> request and returns its
   * response. The method will populate request cookies from the context and update context cookies from the response. The
   * method will automatically follow redirects.
   *
   * @param url Target URL.
   */
  APIResponse put(String url, PutOptions options);
  /**
   * Returns storage state for this request context, contains current cookies and local storage snapshot if it was passed to
   * the constructor.
   */
  default String storageState() {
    return storageState(null);
  }
  /**
   * Returns storage state for this request context, contains current cookies and local storage snapshot if it was passed to
   * the constructor.
   */
  String storageState(StorageStateOptions options);
}

