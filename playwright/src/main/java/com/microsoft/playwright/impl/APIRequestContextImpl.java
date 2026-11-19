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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.RequestOptions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.*;
import static com.microsoft.playwright.impl.Utils.toFilePayload;
import static com.microsoft.playwright.impl.Utils.toProtocol;
import static java.util.Arrays.asList;

public class APIRequestContextImpl extends ChannelOwner implements APIRequestContext {
  private final TracingImpl tracing;
  private String disposeReason;

  protected TimeoutSettings timeoutSettings = new TimeoutSettings();

  final APIRequestContextChannel channel = new APIRequestContextChannel(this);

  APIRequestContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.tracing = connection.getExistingObject(initializer.getAsJsonObject("tracing").get("guid").getAsString());
  }

  @Override
  public TracingImpl tracing() {
    return tracing;
  }

  @Override
  public List<Cookie> cookies(String url) {
    return cookies(url == null ? new ArrayList<>() : Collections.singletonList(url));
  }

  @Override
  public List<Cookie> cookies(List<String> urls) {
    APIRequestContextCookiesParams params = new APIRequestContextCookiesParams();
    if (urls == null) {
      urls = new ArrayList<>();
    }
    params.urls = urls;
    APIRequestContextCookiesResult result = channel.cookies(params, NO_TIMEOUT);
    Cookie[] cookies = gson().fromJson(gson().toJsonTree(result.cookies), Cookie[].class);
    return asList(cookies);
  }

  @Override
  public void addCookies(List<Cookie> cookies) {
    APIRequestContextAddCookiesParams params = new APIRequestContextAddCookiesParams();
    params.cookies = gson().fromJson(gson().toJsonTree(cookies), new TypeToken<List<SetNetworkCookie>>() {}.getType());
    channel.addCookies(params, NO_TIMEOUT);
  }

  @Override
  public void clearCookies(ClearCookiesOptions options) {
    if (options == null) {
      options = new ClearCookiesOptions();
    }
    APIRequestContextClearCookiesParams params = gson().fromJson(
      toClearCookiesParams(options.name, options.domain, options.path), APIRequestContextClearCookiesParams.class);
    channel.clearCookies(params, NO_TIMEOUT);
  }

  @Override
  public APIResponse delete(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "DELETE"));
  }

  @Override
  public void dispose(DisposeOptions options) {
    if (options == null) {
      options = new DisposeOptions();
    }
    disposeReason = options.reason;
    try {
      channel.dispose(toProtocol(options, APIRequestContextDisposeParams.class), NO_TIMEOUT);
    } catch (TargetClosedError e) {
      return;
    }
    tracing.resetStackCounter();
  }

  @Override
  public APIResponse fetch(String urlOrRequest, RequestOptions options) {
    return fetchImpl(urlOrRequest, (RequestOptionsImpl) options);
  }

  @Override
  public APIResponse fetch(Request request, RequestOptions optionsArg) {
    RequestOptionsImpl options = (RequestOptionsImpl) optionsArg;
    if (options == null) {
      options = new RequestOptionsImpl();
    }
    if (options.method == null) {
      options.method = request.method();
    }
    if (options.headers == null) {
      options.headers = request.headers();
    }
    if (options.data == null && options.form == null && options.multipart == null) {
      options.data = request.postDataBuffer();
    }
    return fetch(request.url(), options);
  }

  private APIResponse fetchImpl(String url, RequestOptionsImpl options) {
    if (disposeReason != null) {
      throw new PlaywrightException(disposeReason);
    }
    if (options == null) {
      options = new RequestOptionsImpl();
    }
    options.timeout = timeoutSettings.timeout(options.timeout);
    APIRequestContextFetchParams params = new APIRequestContextFetchParams();
    params.url = url;
    if (options.params != null) {
      Map<String, String> queryParams = new LinkedHashMap<>();
      for (Map.Entry<String, ?> e : options.params.entrySet()) {
        queryParams.put(e.getKey(), "" + e.getValue());
      }
      params.params = toNameValues(toNameValueArray(queryParams.entrySet()));
    }
    params.method = options.method;
    if (options.headers != null) {
      params.headers = toNameValues(Serialization.toProtocol(options.headers));
    }

    if (options.data != null) {
      byte[] bytes = null;
      if (options.data instanceof byte[]) {
        bytes = (byte[]) options.data;
      } else if (options.data instanceof String) {
        String stringData = (String) options.data;
        if (!isJsonContentType(options.headers) || isJsonParsable(stringData)) {
          bytes = (stringData).getBytes(StandardCharsets.UTF_8);
        }
      }
      if (bytes == null) {
        params.jsonData = jsonDataSerializer.toJson(options.data);
      } else {
        params.postData = bytes;
      }
    }
    if (options.form != null) {
      params.formData = toNameValues(toNameValueArray(options.form.fields));
    }
    if (options.multipart != null) {
      params.multipartData = serializeMultipartData(options.multipart.fields);
    }
    params.failOnStatusCode = options.failOnStatusCode;
    params.ignoreHTTPSErrors = options.ignoreHTTPSErrors;
    if (options.maxRedirects != null) {
      if (options.maxRedirects < 0) {
        throw new PlaywrightException("'maxRedirects' should be greater than or equal to '0'");
      }
      params.maxRedirects = options.maxRedirects;
    }
    if (options.maxRetries != null) {
      if (options.maxRetries < 0) {
        throw new PlaywrightException("'maxRetries' must be greater than or equal to '0'");
      }
      params.maxRetries = options.maxRetries;
    }
    APIRequestContextFetchResult result = channel.fetch(params, timeoutSettings.timeout(options.timeout));
    return new APIResponseImpl(this, result.response);
  }

  private static List<NameValue> toNameValues(JsonArray array) {
    List<NameValue> result = new ArrayList<>();
    for (JsonElement element : array) {
      JsonObject item = element.getAsJsonObject();
      NameValue nv = new NameValue();
      nv.name = item.get("name").getAsString();
      nv.value = item.get("value").getAsString();
      result.add(nv);
    }
    return result;
  }

  private static boolean isJsonContentType(Map<String, String> headers) {
    if (headers == null) {
      return false;
    }
    for (Map.Entry<String, String> e : headers.entrySet()) {
      if ("content-type".equalsIgnoreCase(e.getKey())) {
        return "application/json".equals(e.getValue().split(";", -1)[0].trim().toLowerCase());
      }
    }
    return false;
  }

  private static List<FormField> serializeMultipartData(List<? extends Map.Entry<String, Object>> data) {
    List<FormField> result = new ArrayList<>();
    for (Map.Entry<String, ?> e : data) {
      FilePayload filePayload = null;
      if (e.getValue() instanceof FilePayload) {
        filePayload = (FilePayload) e.getValue();
      } else if (e.getValue() instanceof Path) {
        filePayload = toFilePayload((Path) e.getValue());
      } else if (e.getValue() instanceof File) {
        filePayload = toFilePayload(((File) e.getValue()).toPath());
      }
      FormField item = new FormField();
      item.name = e.getKey();
      if (filePayload == null) {
        item.value = "" + e.getValue();
      } else {
        item.file = new FormField.FormFieldFile();
        item.file.name = filePayload.name;
        item.file.mimeType = filePayload.mimeType;
        item.file.buffer = filePayload.buffer;
      }
      result.add(item);
    }
    return result;
  }

  @Override
  public APIResponse get(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "GET"));
  }

  @Override
  public APIResponse head(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "HEAD"));
  }

  @Override
  public APIResponse patch(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "PATCH"));
  }

  @Override
  public APIResponse post(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "POST"));
  }

  @Override
  public APIResponse put(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "PUT"));
  }

  @Override
  public String storageState(StorageStateOptions options) {
    if (options == null) {
      options = new StorageStateOptions();
    }
    APIRequestContextStorageStateParams params = toProtocol(options, APIRequestContextStorageStateParams.class);
    // Keep the raw reply: re-serializing the typed result would turn "expires":-1 into -1.0 and change the string
    // that is compared with BrowserContext.storageState().
    String storageState = sendMessage("storageState", connection.protocolGson().toJsonTree(params).getAsJsonObject(), NO_TIMEOUT).toString();
    if (options.path != null) {
      Utils.writeToFile(Serialization.toPrettyJson(storageState).getBytes(StandardCharsets.UTF_8), options.path);
    }
    return storageState;
  }

  private static RequestOptionsImpl ensureOptions(RequestOptions options, String method) {
    RequestOptionsImpl impl = Utils.clone((RequestOptionsImpl) options);
    if (impl == null) {
      impl = new RequestOptionsImpl();
    }
    if (impl.method == null) {
      impl.method = method;
    }
    return impl;
  }

  private static boolean isJsonParsable(String value) {
    if (value.trim().isEmpty()) {
      return false;
    }
    try {
      JsonElement result = JsonParser.parseString(value);
      if (result != null && result.isJsonPrimitive()) {
        JsonPrimitive primitive = result.getAsJsonPrimitive();
        if (primitive.isString() && value.equals(primitive.getAsString())) {
          // Gson parses unquoted strings too, but we don't want to treat them
          // as valid JSON.
          return false;
        }
      }
      return true;
    } catch (JsonSyntaxException error) {
      return false;
    }
  }
}
