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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BindingCallback;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.*;

class BindingCall extends ChannelOwner {
  private static class SourceImpl implements BindingCallback.Source {
    private final Frame frame;

    public SourceImpl(Frame frame) {
      this.frame = frame;
    }

    @Override
    public BrowserContext context() {
      return page().context();
    }

    @Override
    public Page page() {
      return frame.page();
    }

    @Override
    public Frame frame() {
      return frame;
    }
  }

  BindingCall(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  String name() {
    return initializer.get("name").getAsString();
  }

  void call(BindingCallback binding) {
    String method;
    JsonObject params = new JsonObject();
    try {
      params.add("result", gson().toJsonTree(serializeArgument(invoke(binding))));
      method = "resolve";
    } catch (RuntimeException exception) {
      params.add("error", gson().toJsonTree(serializeError(exception)));
      method = "reject";
    }
    // Like the Node client, do not fail when the page was closed before the result arrived.
    sendMessageAsync(method, params);
  }

  private Object invoke(BindingCallback binding) {
    Frame frame = connection.getExistingObject(initializer.getAsJsonObject("frame").get("guid").getAsString());
    BindingCallback.Source source = new SourceImpl(frame);
    List<Object> args = new ArrayList<>();
    if (initializer.has("handle")) {
      JSHandle handle = connection.getExistingObject(initializer.getAsJsonObject("handle").get("guid").getAsString());
      args.add(handle);
    } else {
      for (JsonElement arg : initializer.getAsJsonArray("args")) {
        args.add(deserialize(gson().fromJson(arg, SerializedValue.class)));
      }
    }
    return binding.call(source, args.toArray());
  }
}
