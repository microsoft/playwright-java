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
import com.microsoft.playwright.Android;
import com.microsoft.playwright.AndroidDevice;
import com.microsoft.playwright.PlaywrightException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;

public class AndroidImpl extends ChannelOwner implements Android {
  final TimeoutSettings timeoutSettings = new TimeoutSettings();

  public AndroidImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void setDefaultTimeout(double timeout) {
    setDefaultTimeoutImpl(timeout);
  }

  void setDefaultTimeoutImpl(Double timeout) {
    withLogging("Android.setDefaultTimeout", () -> {
      timeoutSettings.setDefaultTimeout(timeout);
      JsonObject params = new JsonObject();
      params.addProperty("timeout", timeout);
      sendMessage("setDefaultTimeoutNoReply", params);
    });
  }

  @Override
  public List<AndroidDevice> devices(DevicesOptions options) {
    return withLogging("Android.devices", () -> devicesImpl(options));
  }

  private List<AndroidDevice> devicesImpl(DevicesOptions options) {
    if (options == null) {
      options = new DevicesOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("devices", params);
    List<AndroidDevice> devices = new ArrayList<>();
    for (JsonElement deviceElement : result.getAsJsonObject().get("devices").getAsJsonArray()) {
      JsonObject deviceJson = deviceElement.getAsJsonObject();
      AndroidDevice device = connection.getExistingObject(deviceJson.get("guid").getAsString());
      devices.add(device);
    }
    return devices;
  }

  @Override
  public AndroidDevice connect(String wsEndpoint, ConnectOptions options) {
    return withLogging("Android.connect", () -> connectImpl(wsEndpoint, options));
  }

  private AndroidDevice connectImpl(String wsEndpoint, ConnectOptions options) {
    if (options == null) {
        options = new ConnectOptions();
    }

    if (options.headers != null && !options.headers.isEmpty()) {
      options.headers.put("x-playwright-browser", "android");
    }

    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("wsEndpoint", wsEndpoint);
    JsonObject json = connection.localUtils().sendMessage("connect", params).getAsJsonObject();
    JsonPipe pipe = connection.getExistingObject(json.getAsJsonObject("pipe").get("guid").getAsString());
    Connection androidConnection = new Connection(pipe, connection.env, connection.localUtils);
    PlaywrightImpl playwright = androidConnection.initializePlaywright();

    if (!playwright.initializer.has("preConnectedAndroidDevice")) {
        try {
            androidConnection.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        throw new PlaywrightException("Malformed endpoint. Did you use the correct wsEndpoint?");
    }

    JsonObject preConnectedDevice = playwright.initializer.getAsJsonObject("preConnectedAndroidDevice");
    AndroidDeviceImpl androidDevice = androidConnection.getExistingObject(preConnectedDevice.get("guid").getAsString());
    androidDevice.isConnectedOverWebSocket = true;
    Consumer<JsonPipe> connectionCloseListener = t -> androidDevice.notifyRemoteClosed();
    pipe.onClose(connectionCloseListener);
    androidDevice.onClose(b -> {
      pipe.offClose(connectionCloseListener);
      try {
        androidConnection.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    });
    return androidDevice;
  }
}

