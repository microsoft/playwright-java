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

package com.microsoft.playwright;

import java.util.Date;

/**
 * Accurately simulating time-dependent behavior is essential for verifying the correctness of applications. Learn more
 * about <a href="https://playwright.dev/java/docs/clock">clock emulation</a>.
 *
 * <p> Note that clock is installed for the entire {@code BrowserContext}, so the time in all the pages and iframes is
 * controlled by the same clock.
 */
public interface Clock {
  class InstallOptions {
    /**
     * Time to initialize with, current system time by default.
     */
    public Object time;

    /**
     * Time to initialize with, current system time by default.
     */
    public InstallOptions setTime(long time) {
      this.time = time;
      return this;
    }
    /**
     * Time to initialize with, current system time by default.
     */
    public InstallOptions setTime(String time) {
      this.time = time;
      return this;
    }
    /**
     * Time to initialize with, current system time by default.
     */
    public InstallOptions setTime(Date time) {
      this.time = time;
      return this;
    }
  }
  /**
   * Advance the clock by jumping forward in time. Only fires due timers at most once. This is equivalent to user closing the
   * laptop lid for a while and reopening it later, after given time.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().fastForward(1000);
   * page.clock().fastForward("30:00");
   * }</pre>
   *
   * @param ticks Time may be the number of milliseconds to advance the clock by or a human-readable string. Valid string formats are "08"
   * for eight seconds, "01:00" for one minute and "02:34:10" for two hours, 34 minutes and ten seconds.
   * @since v1.45
   */
  void fastForward(long ticks);
  /**
   * Advance the clock by jumping forward in time. Only fires due timers at most once. This is equivalent to user closing the
   * laptop lid for a while and reopening it later, after given time.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().fastForward(1000);
   * page.clock().fastForward("30:00");
   * }</pre>
   *
   * @param ticks Time may be the number of milliseconds to advance the clock by or a human-readable string. Valid string formats are "08"
   * for eight seconds, "01:00" for one minute and "02:34:10" for two hours, 34 minutes and ten seconds.
   * @since v1.45
   */
  void fastForward(String ticks);
  /**
   * Install fake implementations for the following time-related functions:
   * <ul>
   * <li> {@code Date}</li>
   * <li> {@code setTimeout}</li>
   * <li> {@code clearTimeout}</li>
   * <li> {@code setInterval}</li>
   * <li> {@code clearInterval}</li>
   * <li> {@code requestAnimationFrame}</li>
   * <li> {@code cancelAnimationFrame}</li>
   * <li> {@code requestIdleCallback}</li>
   * <li> {@code cancelIdleCallback}</li>
   * <li> {@code performance}</li>
   * </ul>
   *
   * <p> Fake timers are used to manually control the flow of time in tests. They allow you to advance time, fire timers, and
   * control the behavior of time-dependent functions. See {@link com.microsoft.playwright.Clock#runFor Clock.runFor()} and
   * {@link com.microsoft.playwright.Clock#fastForward Clock.fastForward()} for more information.
   *
   * @since v1.45
   */
  default void install() {
    install(null);
  }
  /**
   * Install fake implementations for the following time-related functions:
   * <ul>
   * <li> {@code Date}</li>
   * <li> {@code setTimeout}</li>
   * <li> {@code clearTimeout}</li>
   * <li> {@code setInterval}</li>
   * <li> {@code clearInterval}</li>
   * <li> {@code requestAnimationFrame}</li>
   * <li> {@code cancelAnimationFrame}</li>
   * <li> {@code requestIdleCallback}</li>
   * <li> {@code cancelIdleCallback}</li>
   * <li> {@code performance}</li>
   * </ul>
   *
   * <p> Fake timers are used to manually control the flow of time in tests. They allow you to advance time, fire timers, and
   * control the behavior of time-dependent functions. See {@link com.microsoft.playwright.Clock#runFor Clock.runFor()} and
   * {@link com.microsoft.playwright.Clock#fastForward Clock.fastForward()} for more information.
   *
   * @since v1.45
   */
  void install(InstallOptions options);
  /**
   * Advance the clock, firing all the time-related callbacks.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().runFor(1000);
   * page.clock().runFor("30:00");
   * }</pre>
   *
   * @param ticks Time may be the number of milliseconds to advance the clock by or a human-readable string. Valid string formats are "08"
   * for eight seconds, "01:00" for one minute and "02:34:10" for two hours, 34 minutes and ten seconds.
   * @since v1.45
   */
  void runFor(long ticks);
  /**
   * Advance the clock, firing all the time-related callbacks.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().runFor(1000);
   * page.clock().runFor("30:00");
   * }</pre>
   *
   * @param ticks Time may be the number of milliseconds to advance the clock by or a human-readable string. Valid string formats are "08"
   * for eight seconds, "01:00" for one minute and "02:34:10" for two hours, 34 minutes and ten seconds.
   * @since v1.45
   */
  void runFor(String ticks);
  /**
   * Advance the clock by jumping forward in time and pause the time. Once this method is called, no timers are fired unless
   * {@link com.microsoft.playwright.Clock#runFor Clock.runFor()}, {@link com.microsoft.playwright.Clock#fastForward
   * Clock.fastForward()}, {@link com.microsoft.playwright.Clock#pauseAt Clock.pauseAt()} or {@link
   * com.microsoft.playwright.Clock#resume Clock.resume()} is called.
   *
   * <p> Only fires due timers at most once. This is equivalent to user closing the laptop lid for a while and reopening it at
   * the specified time and pausing.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");
   * page.clock().pauseAt(format.parse("2020-02-02"));
   * page.clock().pauseAt("2020-02-02");
   * }</pre>
   *
   * @param time Time to pause at.
   * @since v1.45
   */
  void pauseAt(long time);
  /**
   * Advance the clock by jumping forward in time and pause the time. Once this method is called, no timers are fired unless
   * {@link com.microsoft.playwright.Clock#runFor Clock.runFor()}, {@link com.microsoft.playwright.Clock#fastForward
   * Clock.fastForward()}, {@link com.microsoft.playwright.Clock#pauseAt Clock.pauseAt()} or {@link
   * com.microsoft.playwright.Clock#resume Clock.resume()} is called.
   *
   * <p> Only fires due timers at most once. This is equivalent to user closing the laptop lid for a while and reopening it at
   * the specified time and pausing.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");
   * page.clock().pauseAt(format.parse("2020-02-02"));
   * page.clock().pauseAt("2020-02-02");
   * }</pre>
   *
   * @param time Time to pause at.
   * @since v1.45
   */
  void pauseAt(String time);
  /**
   * Advance the clock by jumping forward in time and pause the time. Once this method is called, no timers are fired unless
   * {@link com.microsoft.playwright.Clock#runFor Clock.runFor()}, {@link com.microsoft.playwright.Clock#fastForward
   * Clock.fastForward()}, {@link com.microsoft.playwright.Clock#pauseAt Clock.pauseAt()} or {@link
   * com.microsoft.playwright.Clock#resume Clock.resume()} is called.
   *
   * <p> Only fires due timers at most once. This is equivalent to user closing the laptop lid for a while and reopening it at
   * the specified time and pausing.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");
   * page.clock().pauseAt(format.parse("2020-02-02"));
   * page.clock().pauseAt("2020-02-02");
   * }</pre>
   *
   * @param time Time to pause at.
   * @since v1.45
   */
  void pauseAt(Date time);
  /**
   * Resumes timers. Once this method is called, time resumes flowing, timers are fired as usual.
   *
   * @since v1.45
   */
  void resume();
  /**
   * Makes {@code Date.now} and {@code new Date()} return fixed fake time at all times, keeps all the timers running.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().setFixedTime(new Date());
   * page.clock().setFixedTime(new SimpleDateFormat("yyy-MM-dd").parse("2020-02-02"));
   * page.clock().setFixedTime("2020-02-02");
   * }</pre>
   *
   * @param time Time to be set in milliseconds.
   * @since v1.45
   */
  void setFixedTime(long time);
  /**
   * Makes {@code Date.now} and {@code new Date()} return fixed fake time at all times, keeps all the timers running.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().setFixedTime(new Date());
   * page.clock().setFixedTime(new SimpleDateFormat("yyy-MM-dd").parse("2020-02-02"));
   * page.clock().setFixedTime("2020-02-02");
   * }</pre>
   *
   * @param time Time to be set in milliseconds.
   * @since v1.45
   */
  void setFixedTime(String time);
  /**
   * Makes {@code Date.now} and {@code new Date()} return fixed fake time at all times, keeps all the timers running.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().setFixedTime(new Date());
   * page.clock().setFixedTime(new SimpleDateFormat("yyy-MM-dd").parse("2020-02-02"));
   * page.clock().setFixedTime("2020-02-02");
   * }</pre>
   *
   * @param time Time to be set in milliseconds.
   * @since v1.45
   */
  void setFixedTime(Date time);
  /**
   * Sets current system time but does not trigger any timers.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().setSystemTime(new Date());
   * page.clock().setSystemTime(new SimpleDateFormat("yyy-MM-dd").parse("2020-02-02"));
   * page.clock().setSystemTime("2020-02-02");
   * }</pre>
   *
   * @param time Time to be set in milliseconds.
   * @since v1.45
   */
  void setSystemTime(long time);
  /**
   * Sets current system time but does not trigger any timers.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().setSystemTime(new Date());
   * page.clock().setSystemTime(new SimpleDateFormat("yyy-MM-dd").parse("2020-02-02"));
   * page.clock().setSystemTime("2020-02-02");
   * }</pre>
   *
   * @param time Time to be set in milliseconds.
   * @since v1.45
   */
  void setSystemTime(String time);
  /**
   * Sets current system time but does not trigger any timers.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.clock().setSystemTime(new Date());
   * page.clock().setSystemTime(new SimpleDateFormat("yyy-MM-dd").parse("2020-02-02"));
   * page.clock().setSystemTime("2020-02-02");
   * }</pre>
   *
   * @param time Time to be set in milliseconds.
   * @since v1.45
   */
  void setSystemTime(Date time);
}

