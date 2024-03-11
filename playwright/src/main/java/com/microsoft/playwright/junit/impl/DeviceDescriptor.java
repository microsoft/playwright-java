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
