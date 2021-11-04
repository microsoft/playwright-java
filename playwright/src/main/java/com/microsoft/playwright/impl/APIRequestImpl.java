package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.microsoft.playwright.impl.Serialization.gson;

class APIRequestImpl implements APIRequest {
  private final PlaywrightImpl playwright;

  APIRequestImpl(PlaywrightImpl playwright) {
    this.playwright = playwright;
  }

  @Override
  public APIRequestContextImpl newContext(NewContextOptions options) {
    return playwright.withLogging("APIRequest.newContext", () -> newContextImpl(options));
  }

  private APIRequestContextImpl newContextImpl(NewContextOptions options) {
    if (options == null) {
      options = new NewContextOptions();
    }
    if (options.storageStatePath != null) {
      try {
        byte[] bytes = Files.readAllBytes(options.storageStatePath);
        options.storageState = new String(bytes, StandardCharsets.UTF_8);
        options.storageStatePath = null;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read storage state from file", e);
      }
    }
    JsonObject storageState = null;
    if (options.storageState != null) {
      storageState = new Gson().fromJson(options.storageState, JsonObject.class);
      options.storageState = null;
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (storageState != null) {
      params.add("storageState", storageState);
    }

    JsonObject result = playwright.sendMessage("newRequest", params).getAsJsonObject();
    APIRequestContextImpl context = playwright.connection.getExistingObject(result.getAsJsonObject("request").get("guid").getAsString());
    return context;
  }
}
