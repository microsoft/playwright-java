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

import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.Clock;

import java.util.Date;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;

class ClockImpl implements Clock {
  private final BrowserContextImpl browserContext;

  ClockImpl(BrowserContextImpl browserContext) {
    this.browserContext = browserContext;
  }

  // java: parseTime/parseTicks are inlined into the typed overloads (long, String, Date) of the generated Clock interface.

  @Override
  public void install(InstallOptions options) {
    BrowserContextClockInstallParams params = new BrowserContextClockInstallParams();
    if (options != null) {
      Object time = options.time;
      if (time instanceof Long) {
        params.timeNumber = (double) (Long) time;
      } else if (time instanceof Date) {
        params.timeNumber = (double) ((Date) time).getTime();
      } else if (time instanceof String) {
        params.timeString = (String) time;
      }
    }
    browserContext.channel.clockInstall(params, NO_TIMEOUT);
  }

  @Override
  public void fastForward(long ticks) {
    BrowserContextClockFastForwardParams params = new BrowserContextClockFastForwardParams();
    params.ticksNumber = (double) ticks;
    browserContext.channel.clockFastForward(params, NO_TIMEOUT);
  }

  @Override
  public void fastForward(String ticks) {
    BrowserContextClockFastForwardParams params = new BrowserContextClockFastForwardParams();
    params.ticksString = ticks;
    browserContext.channel.clockFastForward(params, NO_TIMEOUT);
  }

  @Override
  public void pauseAt(long time) {
    BrowserContextClockPauseAtParams params = new BrowserContextClockPauseAtParams();
    params.timeNumber = (double) time;
    browserContext.channel.clockPauseAt(params, NO_TIMEOUT);
  }

  @Override
  public void pauseAt(String time) {
    BrowserContextClockPauseAtParams params = new BrowserContextClockPauseAtParams();
    params.timeString = time;
    browserContext.channel.clockPauseAt(params, NO_TIMEOUT);
  }

  @Override
  public void pauseAt(Date time) {
    BrowserContextClockPauseAtParams params = new BrowserContextClockPauseAtParams();
    params.timeNumber = (double) time.getTime();
    browserContext.channel.clockPauseAt(params, NO_TIMEOUT);
  }

  @Override
  public void resume() {
    browserContext.channel.clockResume(NO_TIMEOUT);
  }

  @Override
  public void runFor(long ticks) {
    BrowserContextClockRunForParams params = new BrowserContextClockRunForParams();
    params.ticksNumber = (double) ticks;
    browserContext.channel.clockRunFor(params, NO_TIMEOUT);
  }

  @Override
  public void runFor(String ticks) {
    BrowserContextClockRunForParams params = new BrowserContextClockRunForParams();
    params.ticksString = ticks;
    browserContext.channel.clockRunFor(params, NO_TIMEOUT);
  }

  @Override
  public void setFixedTime(long time) {
    BrowserContextClockSetFixedTimeParams params = new BrowserContextClockSetFixedTimeParams();
    params.timeNumber = (double) time;
    browserContext.channel.clockSetFixedTime(params, NO_TIMEOUT);
  }

  @Override
  public void setFixedTime(String time) {
    BrowserContextClockSetFixedTimeParams params = new BrowserContextClockSetFixedTimeParams();
    params.timeString = time;
    browserContext.channel.clockSetFixedTime(params, NO_TIMEOUT);
  }

  @Override
  public void setFixedTime(Date time) {
    BrowserContextClockSetFixedTimeParams params = new BrowserContextClockSetFixedTimeParams();
    params.timeNumber = (double) time.getTime();
    browserContext.channel.clockSetFixedTime(params, NO_TIMEOUT);
  }

  @Override
  public void setSystemTime(long time) {
    BrowserContextClockSetSystemTimeParams params = new BrowserContextClockSetSystemTimeParams();
    params.timeNumber = (double) time;
    browserContext.channel.clockSetSystemTime(params, NO_TIMEOUT);
  }

  @Override
  public void setSystemTime(String time) {
    BrowserContextClockSetSystemTimeParams params = new BrowserContextClockSetSystemTimeParams();
    params.timeString = time;
    browserContext.channel.clockSetSystemTime(params, NO_TIMEOUT);
  }

  @Override
  public void setSystemTime(Date time) {
    BrowserContextClockSetSystemTimeParams params = new BrowserContextClockSetSystemTimeParams();
    params.timeNumber = (double) time.getTime();
    browserContext.channel.clockSetSystemTime(params, NO_TIMEOUT);
  }
}
