package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Clock;

import java.util.Date;

class ClockImpl implements Clock {
  private final ChannelOwner browserContext;

  ClockImpl(BrowserContextImpl browserContext) {
    this.browserContext = browserContext;
  }

  @Override
  public void fastForward(long ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksNumber", ticks);
    browserContext.sendMessage("clockFastForward", params);
  }

  @Override
  public void fastForward(String ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksString", ticks);
    browserContext.sendMessage("clockFastForward", params);
  }

  @Override
  public void install(InstallOptions options) {
    JsonObject params = new JsonObject();
    if (options != null) {
      parseTime(options.time, params);
    }
    browserContext.sendMessage("clockInstall", params);
  }

  @Override
  public void runFor(long ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksNumber", ticks);
    browserContext.sendMessage("clockRunFor", params);
  }

  @Override
  public void runFor(String ticks) {
    JsonObject params = new JsonObject();
    params.addProperty("ticksString", ticks);
    browserContext.sendMessage("clockRunFor", params);
  }

  @Override
  public void pauseAt(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    browserContext.sendMessage("clockPauseAt", params);
  }

  @Override
  public void pauseAt(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    browserContext.sendMessage("clockPauseAt", params);
  }

  @Override
  public void pauseAt(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    browserContext.sendMessage("clockPauseAt", params);
  }

  @Override
  public void resume() {
    browserContext.sendMessage("clockResume");
  }

  @Override
  public void setFixedTime(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    browserContext.sendMessage("clockSetFixedTime", params);
  }

  @Override
  public void setFixedTime(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    browserContext.sendMessage("clockSetFixedTime", params);
  }

  @Override
  public void setFixedTime(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    browserContext.sendMessage("clockSetFixedTime", params);
  }

  @Override
  public void setSystemTime(long time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time);
    browserContext.sendMessage("clockSetSystemTime", params);
  }

  @Override
  public void setSystemTime(String time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeString", time);
    browserContext.sendMessage("clockSetSystemTime", params);
  }

  @Override
  public void setSystemTime(Date time) {
    JsonObject params = new JsonObject();
    params.addProperty("timeNumber", time.getTime());
    browserContext.sendMessage("clockSetSystemTime", params);
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
