package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Clock;

import java.util.Date;

class ClockImpl implements Clock {
  private final ChannelOwner browserContext;

  ClockImpl(BrowserContextImpl browserContext) {
    this.browserContext = browserContext;
  }

  private void sendMessageWithLogging(String method, JsonObject params) {
    String capitalizedMethod = method.substring(0, 1).toUpperCase() + method.substring(1);
    browserContext.sendMessage("clock" + capitalizedMethod, params);
  }

  @Override
  public void fastForward(long ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksNumber", ticks);
    sendMessageWithLogging("fastForward", params);
  }

  @Override
  public void fastForward(String ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksString", ticks);
    sendMessageWithLogging("fastForward", params);
  }

  @Override
  public void install(InstallOptions options) {
    JsonObject params = new JsonObject();
    if (options != null) {
      parseTime(options.time, params);
    }
    sendMessageWithLogging("install", params);
  }

  @Override
  public void runFor(long ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksNumber", ticks);
    sendMessageWithLogging("runFor", params);
  }

  @Override
  public void runFor(String ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksString", ticks);
    sendMessageWithLogging("runFor", params);
  }

  @Override
  public void pauseAt(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    sendMessageWithLogging("pauseAt", params);
  }

  @Override
  public void pauseAt(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    sendMessageWithLogging("pauseAt", params);
  }

  @Override
  public void pauseAt(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    sendMessageWithLogging("pauseAt", params);
  }

  @Override
  public void resume() {
    sendMessageWithLogging("resume", new JsonObject());
  }

  @Override
  public void setFixedTime(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    sendMessageWithLogging("setFixedTime", params);
  }

  @Override
  public void setFixedTime(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    sendMessageWithLogging("setFixedTime", params);
  }

  @Override
  public void setFixedTime(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    sendMessageWithLogging("setFixedTime", params);
  }

  @Override
  public void setSystemTime(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    sendMessageWithLogging("setSystemTime", params);
  }

  @Override
  public void setSystemTime(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    sendMessageWithLogging("setSystemTime", params);
  }

  @Override
  public void setSystemTime(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    sendMessageWithLogging("setSystemTime", params);
  }

  private static void parseTime(Object time, JsonObject params) {
    if (time instanceof Long) {
      params.addProperty("timeNumber", (Long) time);
    } else if (time instanceof Date) {
      params.addProperty("timeNumber", ((Date) time).getTime());
    } else if (time instanceof String) {
      params.addProperty("timeString", (String) time);
    }
  }
}
