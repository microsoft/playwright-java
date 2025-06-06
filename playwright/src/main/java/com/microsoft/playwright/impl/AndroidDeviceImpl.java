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

import com.microsoft.playwright.AndroidDevice;
import com.microsoft.playwright.PlaywrightException;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Utils.isSafeCloseError;

public class AndroidDeviceImpl extends ChannelOwner implements AndroidDevice {
  final Set<BrowserContextImpl> contexts = new HashSet<>();
  boolean isConnectedOverWebSocket;
  final TimeoutSettings timeoutSettings;
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();

  enum EventType {
    CLOSE,
  }
  AndroidDeviceImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.timeoutSettings = new TimeoutSettings(((AndroidImpl) parent).timeoutSettings);
  }

  @Override
  public void onClose(Consumer<AndroidDevice> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<AndroidDevice> handler) {
    listeners.remove(EventType.CLOSE, handler);
  }

  @Override
  public void close() {
    withLogging("AndroidDevice.close", () -> closeImpl());
  }

  private void closeImpl() {
    if (isConnectedOverWebSocket) {
      try {
        connection.close();
      } catch (IOException e) {
        throw new PlaywrightException("Failed to close device connection", e);
      }   
    } else {
      try {
        sendMessage("close");
      } catch (PlaywrightException e) {
        if (!isSafeCloseError(e)) {
          throw e;
        }
      }
    }
  }

  void didClose() {
    listeners.notify(EventType.CLOSE, this);
  }

  @Override
  public String model() {
    return initializer.get("model").getAsString();
  }

  void notifyRemoteClosed() {
    for (BrowserContextImpl context : new ArrayList<>(contexts)) {
      for (PageImpl page : new ArrayList<>(context.pages)) {
        page.didClose();
      }
      context.didClose();
    }
    didClose();
  }

  @Override
  public String serial() {
    return initializer.get("serial").getAsString();
  }

  @Override
  void handleEvent(String event, JsonObject parameters) {
    if ("close".equals(event)) {
      didClose();
    } 
  }
}
