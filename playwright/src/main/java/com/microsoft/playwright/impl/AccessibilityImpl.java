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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Accessibility;

import static com.microsoft.playwright.impl.Serialization.gson;

class AccessibilityImpl implements Accessibility {
  private final PageImpl page;

  AccessibilityImpl(PageImpl page) {
    this.page = page;
  }

  @Override
  public String snapshot(SnapshotOptions options) {
    return page.withLogging("Accessibility.snapshot", () -> snapshotImpl(options));
  }

  private String snapshotImpl(SnapshotOptions options) {
    if (options == null) {
      options = new SnapshotOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = page.sendMessage("accessibilitySnapshot", params).getAsJsonObject();
    if (!json.has("rootAXNode")) {
      return null;
    }
    return json.getAsJsonObject("rootAXNode").toString();
  }
}
