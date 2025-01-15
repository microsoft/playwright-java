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
    browserContext.withLogging("BrowserContext." + method, () -> browserContext.sendMessage(method, params));
  }

  @Override
  public void fastForward(long ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksNumber", ticks);
    sendMessageWithLogging("clockFastForward", params);
  }

  @Override
  public void fastForward(String ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksString", ticks);
    sendMessageWithLogging("clockFastForward", params);
  }

  @Override
  public void install(InstallOptions options) {
    JsonObject params = new JsonObject();
    if (options != null) {
      parseTime(options.time, params);
    }
    sendMessageWithLogging("clockInstall", params);
  }

  @Override
  public void runFor(long ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksNumber", ticks);
    sendMessageWithLogging("clockRunFor", params);
  }

  @Override
  public void runFor(String ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksString", ticks);
    sendMessageWithLogging("clockRunFor", params);
  }

  @Override
  public void pauseAt(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    sendMessageWithLogging("clockPauseAt", params);
  }

  @Override
  public void pauseAt(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    sendMessageWithLogging("clockPauseAt", params);
  }

  @Override
  public void pauseAt(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    sendMessageWithLogging("clockPauseAt", params);
  }

  @Override
  public void resume() {
    sendMessageWithLogging("clockResume", new JsonObject());
  }

  @Override
  public void setFixedTime(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    sendMessageWithLogging("clockSetFixedTime", params);
  }

  @Override
  public void setFixedTime(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    sendMessageWithLogging("clockSetFixedTime", params);
  }

  @Override
  public void setFixedTime(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    sendMessageWithLogging("clockSetFixedTime", params);
  }

  @Override
  public void setSystemTime(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    sendMessageWithLogging("clockSetSystemTime", params);
  }

  @Override
  public void setSystemTime(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    sendMessageWithLogging("clockSetSystemTime", params);
  }

  @Override
  public void setSystemTime(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    sendMessageWithLogging("clockSetSystemTime", params);
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
