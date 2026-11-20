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

// Port of fetch.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.RequestOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.Errors.isTargetClosedError;
import static com.microsoft.playwright.impl.RawHeaders.headersObjectToArray;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.jsonDataSerializer;
import static com.microsoft.playwright.impl.Serialization.toClearCookiesParams;

public class APIRequestContextImpl extends ChannelOwner implements APIRequestContext {
  APIRequestImpl request;
  final TracingImpl tracing;
  private String closeReason;
  TimeoutSettings timeoutSettings;

  final APIRequestContextChannel channel = new APIRequestContextChannel(this);

  APIRequestContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.tracing = connection.protocolGson().fromJson(initializer, APIRequestContextInitializer.class).tracing;
    this.timeoutSettings = new TimeoutSettings();
  }

  @Override
  public void dispose(DisposeOptions options) {
    if (options == null) {
      options = new DisposeOptions();
    }
    closeReason = options.reason;
    tracing.exportAllHars();
    try {
      APIRequestContextDisposeParams params = new APIRequestContextDisposeParams();
      params.reason = options.reason;
      channel.dispose(params, NO_TIMEOUT);
    } catch (PlaywrightException e) {
      if (isTargetClosedError(e)) {
        return;
      }
      throw e;
    }
    tracing.resetStackCounter();
    if (request != null) {
      request.contexts.remove(this);
    }
  }

  // java: the string | string[] parameter is two overloads, a null url means all cookies.
  @Override
  public List<Cookie> cookies(String urls) {
    return cookies(urls == null ? Collections.<String>emptyList() : Collections.singletonList(urls));
  }

  @Override
  public List<Cookie> cookies(List<String> urls) {
    if (urls == null) {
      urls = new ArrayList<>();
    }
    APIRequestContextCookiesParams params = new APIRequestContextCookiesParams();
    params.urls = urls;
    List<NetworkCookie> cookies = channel.cookies(params, NO_TIMEOUT).cookies;
    // java: the protocol cookies are converted to the API type.
    return Arrays.asList(gson().fromJson(gson().toJsonTree(cookies), Cookie[].class));
  }

  @Override
  public void addCookies(List<Cookie> cookies) {
    APIRequestContextAddCookiesParams params = new APIRequestContextAddCookiesParams();
    // java: the API cookies are converted to the protocol type.
    params.cookies = new ArrayList<>();
    for (Cookie cookie : cookies) {
      params.cookies.add(Utils.toProtocol(cookie, SetNetworkCookie.class));
    }
    channel.addCookies(params, NO_TIMEOUT);
  }

  @Override
  public void clearCookies(ClearCookiesOptions options) {
    if (options == null) {
      options = new ClearCookiesOptions();
    }
    // java: toClearCookiesParams of network.ts is the seed Serialization.toClearCookiesParams.
    APIRequestContextClearCookiesParams params = connection.protocolGson().fromJson(
      toClearCookiesParams(options.name, options.domain, options.path), APIRequestContextClearCookiesParams.class);
    channel.clearCookies(params, NO_TIMEOUT);
  }

  @Override
  public APIResponse delete(String url, RequestOptions options) {
    return fetch(url, withMethod(options, "DELETE"));
  }

  @Override
  public APIResponse head(String url, RequestOptions options) {
    return fetch(url, withMethod(options, "HEAD"));
  }

  @Override
  public APIResponse get(String url, RequestOptions options) {
    return fetch(url, withMethod(options, "GET"));
  }

  @Override
  public APIResponse patch(String url, RequestOptions options) {
    return fetch(url, withMethod(options, "PATCH"));
  }

  @Override
  public APIResponse post(String url, RequestOptions options) {
    return fetch(url, withMethod(options, "POST"));
  }

  @Override
  public APIResponse put(String url, RequestOptions options) {
    return fetch(url, withMethod(options, "PUT"));
  }

  // java: the string | Request parameter is two overloads.
  @Override
  public APIResponse fetch(String urlOrRequest, RequestOptions options) {
    return wrapApiCall(() -> innerFetch(urlOrRequest, null, (RequestOptionsImpl) options), null, false);
  }

  @Override
  public APIResponse fetch(Request urlOrRequest, RequestOptions options) {
    return wrapApiCall(() -> innerFetch(null, urlOrRequest, (RequestOptionsImpl) options), null, false);
  }

  // java: upstream takes one object { url, request, ...options }.
  APIResponseImpl innerFetch(String url, Request request, RequestOptionsImpl options) {
    if (options == null) {
      options = new RequestOptionsImpl();
    }
    if (closeReason != null) {
      throw new TargetClosedError(closeReason);
    }
    if (request == null && url == null) {
      throw new PlaywrightException("First argument must be either URL string or Request");
    }
    if ((options.data == null ? 0 : 1) + (options.form == null ? 0 : 1) + (options.multipart == null ? 0 : 1) > 1) {
      throw new PlaywrightException("Only one of 'data', 'form' or 'multipart' can be specified");
    }
    if (options.maxRedirects != null && options.maxRedirects < 0) {
      throw new PlaywrightException("'maxRedirects' must be greater than or equal to '0'");
    }
    if (options.maxRetries != null && options.maxRetries < 0) {
      throw new PlaywrightException("'maxRetries' must be greater than or equal to '0'");
    }
    if (url == null) {
      url = request.url();
    }
    String method = options.method != null ? options.method : (request != null ? request.method() : null);
    // java: RequestOptions has no string or URLSearchParams form of params, so there are no encodedParams.
    // Cannot call allHeaders() here as the request may be paused inside route handler.
    Map<String, String> headersObj = options.headers != null ? options.headers : (request != null ? request.headers() : null);
    List<NameValue> headers = headersObj != null ? headersObjectToArray(headersObj) : null;
    String jsonData = null;
    List<NameValue> formData = null;
    List<FormField> multipartData = null;
    byte[] postDataBuffer = null;
    if (options.data != null) {
      if (options.data instanceof String) {
        String data = (String) options.data;
        if (isJsonContentType(headers)) {
          jsonData = isJsonParsable(data) ? data : jsonDataSerializer.toJson(data);
        } else {
          postDataBuffer = data.getBytes(StandardCharsets.UTF_8);
        }
      } else if (options.data instanceof byte[]) {
        postDataBuffer = (byte[]) options.data;
      } else {
        // java: any other object is serialized with Gson, there is no 'Unexpected data type' case.
        jsonData = jsonDataSerializer.toJson(options.data);
      }
    } else if (options.form != null) {
      // java: FormData is the only form type of the Java API.
      formData = new ArrayList<>();
      for (FormDataImpl.Field field : options.form.fields) {
        if (field.value instanceof FilePayload || field.value instanceof Path) {
          throw new PlaywrightException("Expected string for options.form[\"" + field.name + "\"], found File. Please use options.multipart instead.");
        }
        formData.add(nameValue(field.name, String.valueOf(field.value)));
      }
    } else if (options.multipart != null) {
      multipartData = new ArrayList<>();
      // Convert file-like values to ServerFilePayload structs.
      for (FormDataImpl.Field field : options.multipart.fields) {
        multipartData.add(toFormField(field.name, field.value));
      }
    }
    if (postDataBuffer == null && jsonData == null && formData == null && multipartData == null) {
      postDataBuffer = request != null ? request.postDataBuffer() : null;
    }
    APIRequestContextFetchParams params = new APIRequestContextFetchParams();
    params.url = url;
    params.params = objectToArray(options.params);
    params.method = method;
    params.headers = headers;
    params.postData = postDataBuffer;
    params.jsonData = jsonData;
    params.formData = formData;
    params.multipartData = multipartData;
    params.failOnStatusCode = options.failOnStatusCode;
    params.ignoreHTTPSErrors = options.ignoreHTTPSErrors;
    params.maxRedirects = options.maxRedirects;
    params.maxRetries = options.maxRetries;
    APIRequestContextFetchResult result = channel.fetch(params, timeoutSettings.timeout(options.timeout));
    return new APIResponseImpl(this, result.response);
  }

  @Override
  public String storageState(StorageStateOptions options) {
    if (options == null) {
      options = new StorageStateOptions();
    }
    APIRequestContextStorageStateParams params = new APIRequestContextStorageStateParams();
    params.indexedDB = options.indexedDB;
    params.opfs = options.opfs;
    // java: the API returns the raw JSON of the reply; re-serializing the typed result would turn "expires":-1
    // into -1.0 and change the string that is compared with BrowserContext.storageState().
    String state = sendMessage("storageState", connection.protocolGson().toJsonTree(params).getAsJsonObject(), NO_TIMEOUT).toString();
    if (options.path != null) {
      // java: writeToFile creates the parent directories, as mkdirIfNeeded does upstream.
      Utils.writeToFile(Serialization.toPrettyJson(state).getBytes(StandardCharsets.UTF_8), options.path);
    }
    return state;
  }

  // java: FormData values are String, Boolean, Integer, Path (upstream's ReadStream) or FilePayload.
  static FormField toFormField(String name, Object value) {
    FormField field = new FormField();
    field.name = name;
    if (value instanceof FilePayload) {
      field.file = filePayloadToJson((FilePayload) value);
    } else if (value instanceof Path) {
      field.file = readStreamToJson((Path) value);
    } else {
      field.value = String.valueOf(value);
    }
    return field;
  }

  // java: Gson parses some non-JSON text leniently (a bare word is a string), JSON.parse does not.
  static boolean isJsonParsable(String value) {
    if (value.trim().isEmpty()) {
      return false;
    }
    try {
      JsonElement result = JsonParser.parseString(value);
      if (result != null && result.isJsonPrimitive()) {
        JsonPrimitive primitive = result.getAsJsonPrimitive();
        if (primitive.isString() && value.equals(primitive.getAsString())) {
          return false;
        }
      }
      return true;
    } catch (JsonSyntaxException e) {
      return false;
    }
  }

  static FormField.FormFieldFile filePayloadToJson(FilePayload payload) {
    FormField.FormFieldFile file = new FormField.FormFieldFile();
    file.name = payload.name;
    file.mimeType = payload.mimeType;
    file.buffer = payload.buffer;
    return file;
  }

  // java: a file Path stands in for upstream's fs.ReadStream.
  static FormField.FormFieldFile readStreamToJson(Path path) {
    FilePayload payload = Utils.toFilePayload(path);
    FormField.FormFieldFile file = new FormField.FormFieldFile();
    file.name = payload.name;
    file.buffer = payload.buffer;
    return file;
  }

  static boolean isJsonContentType(List<NameValue> headers) {
    if (headers == null) {
      return false;
    }
    for (NameValue header : headers) {
      if ("content-type".equals(header.name.toLowerCase())) {
        return "application/json".equals(header.value.split(";", -1)[0].trim().toLowerCase());
      }
    }
    return false;
  }

  static List<NameValue> objectToArray(Map<String, ?> map) {
    if (map == null) {
      return null;
    }
    List<NameValue> result = new ArrayList<>();
    for (Map.Entry<String, ?> e : map.entrySet()) {
      if (e.getValue() != null) {
        result.add(nameValue(e.getKey(), String.valueOf(e.getValue())));
      }
    }
    return result;
  }

  // java: the accessor of the tracing field, the { ...options, method } of the get/post/... shortcuts and the { name, value } literal.
  @Override
  public TracingImpl tracing() {
    return tracing;
  }

  private static RequestOptionsImpl withMethod(RequestOptions options, String method) {
    RequestOptionsImpl impl = Utils.clone((RequestOptionsImpl) options);
    if (impl == null) {
      impl = new RequestOptionsImpl();
    }
    impl.method = method;
    return impl;
  }

  private static NameValue nameValue(String name, String value) {
    NameValue nv = new NameValue();
    nv.name = name;
    nv.value = value;
    return nv;
  }
}
