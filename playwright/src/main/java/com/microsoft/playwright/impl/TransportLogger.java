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
