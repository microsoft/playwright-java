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

import java.io.IOException;
import java.time.Duration;

import static com.microsoft.playwright.impl.LoggingSupport.logWithTimestamp;
import static com.microsoft.playwright.impl.Serialization.gson;

class TransportLogger implements Transport {
  private final Transport transport;

  TransportLogger(Transport transport) {
    this.transport = transport;
  }

  @Override
  public void send(JsonObject message) {
    String messageString = gson().toJson(message);
    logWithTimestamp("SEND ► " + messageString);
    transport.send(message);
  }

  @Override
  public JsonObject poll(Duration timeout) {
    JsonObject message = transport.poll(timeout);
    if (message != null) {
      String messageString = gson().toJson(message);
      logWithTimestamp("◀ RECV " + messageString);
    }
    return message;
  }

  @Override
  public void close() throws IOException {
    transport.close();
  }
}
