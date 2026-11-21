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

// Port of clock.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.Clock;

import java.util.Date;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;

// java: each number | string | Date parameter is one public overload per type of the generated Clock interface,
// all delegating to one private core that takes an Object.
class ClockImpl implements Clock {
  private final BrowserContextImpl browserContext;

  ClockImpl(BrowserContextImpl browserContext) {
    this.browserContext = browserContext;
  }

  @Override
  public void install(InstallOptions options) {
    if (options == null) {
      options = new InstallOptions();
    }
    BrowserContextClockInstallParams params = gson().fromJson(options.time != null ? parseTime(options.time) : new JsonObject(), BrowserContextClockInstallParams.class);
    browserContext.channel.clockInstall(params, NO_TIMEOUT);
  }

  @Override
  public void fastForward(long ticks) {
    fastForward((Object) ticks);
  }

  @Override
  public void fastForward(String ticks) {
    fastForward((Object) ticks);
  }

  private void fastForward(Object ticks) {
    BrowserContextClockFastForwardParams params = gson().fromJson(parseTicks(ticks), BrowserContextClockFastForwardParams.class);
    browserContext.channel.clockFastForward(params, NO_TIMEOUT);
  }

  @Override
  public void pauseAt(long time) {
    pauseAt((Object) time);
  }

  @Override
  public void pauseAt(String time) {
    pauseAt((Object) time);
  }

  @Override
  public void pauseAt(Date time) {
    pauseAt((Object) time);
  }

  private void pauseAt(Object time) {
    BrowserContextClockPauseAtParams params = gson().fromJson(parseTime(time), BrowserContextClockPauseAtParams.class);
    browserContext.channel.clockPauseAt(params, NO_TIMEOUT);
  }

  @Override
  public void resume() {
    browserContext.channel.clockResume(NO_TIMEOUT);
  }

  @Override
  public void runFor(long ticks) {
    runFor((Object) ticks);
  }

  @Override
  public void runFor(String ticks) {
    runFor((Object) ticks);
  }

  private void runFor(Object ticks) {
    BrowserContextClockRunForParams params = gson().fromJson(parseTicks(ticks), BrowserContextClockRunForParams.class);
    browserContext.channel.clockRunFor(params, NO_TIMEOUT);
  }

  @Override
  public void setFixedTime(long time) {
    setFixedTime((Object) time);
  }

  @Override
  public void setFixedTime(String time) {
    setFixedTime((Object) time);
  }

  @Override
  public void setFixedTime(Date time) {
    setFixedTime((Object) time);
  }

  private void setFixedTime(Object time) {
    BrowserContextClockSetFixedTimeParams params = gson().fromJson(parseTime(time), BrowserContextClockSetFixedTimeParams.class);
    browserContext.channel.clockSetFixedTime(params, NO_TIMEOUT);
  }

  @Override
  public void setSystemTime(long time) {
    setSystemTime((Object) time);
  }

  @Override
  public void setSystemTime(String time) {
    setSystemTime((Object) time);
  }

  @Override
  public void setSystemTime(Date time) {
    setSystemTime((Object) time);
  }

  private void setSystemTime(Object time) {
    BrowserContextClockSetSystemTimeParams params = gson().fromJson(parseTime(time), BrowserContextClockSetSystemTimeParams.class);
    browserContext.channel.clockSetSystemTime(params, NO_TIMEOUT);
  }

  // java: the returned { timeNumber?, timeString? } is a JsonObject, spread into the params by Gson.
  static JsonObject parseTime(Object time) {
    JsonObject result = new JsonObject();
    if (time instanceof Long) {
      result.addProperty("timeNumber", (Long) time);
      return result;
    }
    if (time instanceof String) {
      result.addProperty("timeString", (String) time);
      return result;
    }
    // java: the isFinite(time.getTime()) check is not ported, a Java Date always holds a finite time.
    result.addProperty("timeNumber", ((Date) time).getTime());
    return result;
  }

  static JsonObject parseTicks(Object ticks) {
    JsonObject result = new JsonObject();
    if (ticks instanceof Long) {
      result.addProperty("ticksNumber", (Long) ticks);
    }
    if (ticks instanceof String) {
      result.addProperty("ticksString", (String) ticks);
    }
    return result;
  }
}
