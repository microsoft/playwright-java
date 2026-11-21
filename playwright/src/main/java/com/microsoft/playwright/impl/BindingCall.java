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

// Port of page.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.BindingCallback;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.*;

public class BindingCall extends ChannelOwner {
  final BindingCallChannel channel = new BindingCallChannel(this);
  // java: the initializer parsed once, upstream reads this._initializer.
  final BindingCallInitializer bindingCallInitializer;

  // java: from is not ported, generated results already hold the impl object.

  BindingCall(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    bindingCallInitializer = connection.protocolGson().fromJson(initializer, BindingCallInitializer.class);
  }

  void call(BindingCallback func) {
    try {
      FrameImpl frame = bindingCallInitializer.frame;
      BindingCallback.Source source = new Source(frame.page.context(), frame.page, frame);
      List<Object> args = new ArrayList<>();
      for (SerializedValue arg : bindingCallInitializer.args) {
        args.add(deserialize(arg));
      }
      Object result = func.call(source, args.toArray());
      BindingCallResolveParams params = new BindingCallResolveParams();
      params.result = serializeArgument(result);
      // java: channel.resolve(...).catch(() => {}) is not awaited upstream; sendMessageAsync does not pump.
      try {
        sendMessageAsync("resolve", params);
      } catch (PlaywrightException e) {
      }
    } catch (RuntimeException e) {
      // java: an Error thrown by the callback, such as a failed assertion, reaches the caller instead.
      BindingCallRejectParams params = new BindingCallRejectParams();
      params.error = serializeError(e);
      try {
        sendMessageAsync("reject", params);
      } catch (PlaywrightException e2) {
      }
    }
  }

  // java: the { context, page, frame } object literal of call().
  private static class Source implements BindingCallback.Source {
    private final BrowserContext context;
    private final Page page;
    private final Frame frame;

    Source(BrowserContext context, Page page, Frame frame) {
      this.context = context;
      this.page = page;
      this.frame = frame;
    }

    @Override
    public BrowserContext context() {
      return context;
    }

    @Override
    public Page page() {
      return page;
    }

    @Override
    public Frame frame() {
      return frame;
    }
  }
}
