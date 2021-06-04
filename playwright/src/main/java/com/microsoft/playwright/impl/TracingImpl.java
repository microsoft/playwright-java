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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Tracing;

import java.nio.file.Path;

import static com.microsoft.playwright.impl.Serialization.gson;

class TracingImpl implements Tracing {
  private final BrowserContextImpl context;

  TracingImpl(BrowserContextImpl context) {
    this.context = context;
  }

  private void export(Path path) {
    JsonObject json = context.sendMessage("tracingExport").getAsJsonObject();
    ArtifactImpl artifact = context.connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
    if (context.browser().isRemote) {
      artifact.isRemote = true;
    }
    artifact.saveAs(path);
    artifact.delete();
  }

  @Override
  public void start(StartOptions options) {
    context.withLogging("Tracing.start", () -> startImpl(options));
  }

  private void startImpl(StartOptions options) {
    if (options == null) {
      options = new StartOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    context.sendMessage("tracingStart", params);
  }

  @Override
  public void stop(StopOptions options) {
    context.withLogging("Tracing.stop", () -> {
      context.sendMessage("tracingStop");
      if (options != null && options.path != null) {
        export(options.path);
      }
    });
  }
}
