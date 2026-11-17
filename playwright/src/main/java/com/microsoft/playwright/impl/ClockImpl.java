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
