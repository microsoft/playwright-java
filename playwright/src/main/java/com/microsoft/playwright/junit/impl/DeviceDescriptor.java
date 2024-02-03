package com.microsoft.playwright.junit.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.options.ViewportSize;

class DeviceDescriptor {
  public String userAgent;
  public ViewportSize viewport;
  public Double deviceScaleFactor;
  public Boolean isMobile;
  public Boolean hasTouch;
  public String defaultBrowserType;

  static DeviceDescriptor findByName(Playwright playwright, String name) {
    JsonArray devices = ((PlaywrightImpl) playwright).deviceDescriptors();
    JsonObject descriptor = null;
    for (JsonElement item : devices) {
      if (name.equals(item.getAsJsonObject().get("name").getAsString())) {
        descriptor = item.getAsJsonObject().getAsJsonObject("descriptor");
        break;
      }
    }
    if (descriptor == null) {
      return null;
    }
    return new Gson().fromJson(descriptor, DeviceDescriptor.class);
  }

}
