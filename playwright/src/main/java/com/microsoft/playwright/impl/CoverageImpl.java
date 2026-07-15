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
import com.microsoft.playwright.Coverage;

import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;

class CoverageImpl implements Coverage {
  private final PageImpl page;

  CoverageImpl(PageImpl page) {
    this.page = page;
  }

  @Override
  public void startJSCoverage(StartJSCoverageOptions options) {
    JsonObject params = gson().toJsonTree(options == null ? new StartJSCoverageOptions() : options).getAsJsonObject();
    page.sendMessage("startJSCoverage", params, NO_TIMEOUT);
  }

  @Override
  public List<ScriptCoverage> stopJSCoverage() {
    JsonObject result = page.sendMessage("stopJSCoverage", new JsonObject(), NO_TIMEOUT).getAsJsonObject();
    return Arrays.asList(gson().fromJson(result.getAsJsonArray("entries"), ScriptCoverage[].class));
  }

  @Override
  public void startCSSCoverage(StartCSSCoverageOptions options) {
    JsonObject params = gson().toJsonTree(options == null ? new StartCSSCoverageOptions() : options).getAsJsonObject();
    page.sendMessage("startCSSCoverage", params, NO_TIMEOUT);
  }

  @Override
  public List<StyleSheetCoverage> stopCSSCoverage() {
    JsonObject result = page.sendMessage("stopCSSCoverage", new JsonObject(), NO_TIMEOUT).getAsJsonObject();
    return Arrays.asList(gson().fromJson(result.getAsJsonArray("entries"), StyleSheetCoverage[].class));
  }
}
