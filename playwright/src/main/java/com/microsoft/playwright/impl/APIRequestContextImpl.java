package com.microsoft.playwright.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.RequestOptions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.*;
import static com.microsoft.playwright.impl.Utils.toFilePayload;

class APIRequestContextImpl extends ChannelOwner implements APIRequestContext {
  private final TracingImpl tracing;

  APIRequestContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.tracing = connection.getExistingObject(initializer.getAsJsonObject("tracing").get("guid").getAsString());
  }

  @Override
  public APIResponse delete(String url, RequestOptions options) {
    return fetch(url, ensureOptions(options, "DELETE"));
  }

  @Override
  public void dispose() {
    withLogging("APIRequestContext.dispose", () -> sendMessage("dispose"));
  }

  @Override
  public APIResponse fetch(String urlOrRequest, RequestOptions options) {
    return withLogging("APIRequestContext.fetch", () -> fetchImpl(urlOrRequest, (RequestOptionsImpl) options));
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
    if (options == null) {
      options = new RequestOptionsImpl();
    }
    JsonObject params = new JsonObject();
    params.addProperty("url", url);
    if (options.params != null) {
      Map<String, String> queryParams = new LinkedHashMap<>();
      for (Map.Entry<String, ?> e : options.params.entrySet()) {
        queryParams.put(e.getKey(), "" + e.getValue());
      }
      params.add("params", toNameValueArray(queryParams));
    }
    if (options.method != null) {
      params.addProperty("method", options.method);
    }
    if (options.headers != null) {
      params.add("headers", toProtocol(options.headers));
    }

    if (options.data != null) {
      byte[] bytes = null;
      if (options.data instanceof byte[]) {
        bytes = (byte[]) options.data;
      } else if (options.data instanceof String && !isJsonContentType(options.headers)) {
        bytes = ((String) options.data).getBytes(StandardCharsets.UTF_8);
      }
      if (bytes == null) {
        params.add("jsonData", gson().toJsonTree(options.data));
      } else {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        params.addProperty("postData", base64);
      }
    }
    if (options.form != null) {
      params.add("formData", toNameValueArray(options.form.fields));
    }
    if (options.multipart != null) {
      params.add("multipartData", serializeMultipartData(options.multipart.fields));
    }
    if (options.timeout != null) {
      params.addProperty("timeout", options.timeout);
    }
    if (options.failOnStatusCode != null) {
      params.addProperty("failOnStatusCode", options.failOnStatusCode);
    }
    if (options.ignoreHTTPSErrors != null) {
      params.addProperty("ignoreHTTPSErrors", options.ignoreHTTPSErrors);
    }
    if (options.maxRedirects != null) {
      if (options.maxRedirects < 0) {
        throw new PlaywrightException("'maxRedirects' should be greater than or equal to '0'");
      }
      params.addProperty("maxRedirects", options.maxRedirects);
    }
    JsonObject json = sendMessage("fetch", params).getAsJsonObject();
    return new APIResponseImpl(this, json.getAsJsonObject("response"));
  }

  private static boolean isJsonContentType(Map<String, String> headers) {
    if (headers == null) {
      return false;
    }
    for (Map.Entry<String, String> e : headers.entrySet()) {
      if ("content-type".equalsIgnoreCase(e.getKey())) {
        return "application/json".equals(e.getValue());
      }
    }
    return false;
  }

  private static JsonArray serializeMultipartData(Map<String, Object> data) {
    JsonArray result = new JsonArray();
    for (Map.Entry<String, Object> e : data.entrySet()) {
      FilePayload filePayload = null;
      if (e.getValue() instanceof FilePayload) {
        filePayload = (FilePayload) e.getValue();
      } else if (e.getValue() instanceof Path) {
        filePayload = toFilePayload((Path) e.getValue());
      } else if (e.getValue() instanceof File) {
        filePayload = toFilePayload(((File) e.getValue()).toPath());
      }
      JsonObject item = new JsonObject();
      item.addProperty("name", e.getKey());
      if (filePayload == null) {
        item.addProperty("value", "" + e.getValue());
      } else {
        item.add("file", toProtocol(filePayload));
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
    return withLogging("APIRequestContext.storageState", () -> {
      JsonElement json = sendMessage("storageState");
      String storageState = json.toString();
      if (options != null && options.path != null) {
        Utils.writeToFile(storageState.getBytes(StandardCharsets.UTF_8), options.path);
      }
      return storageState;
    });
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
}
