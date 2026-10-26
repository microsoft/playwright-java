/**
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/page-clock.spec.ts. */
public class TestPageClock {
  @BeforeEach
  void skipWithClockEnv() {
    String clock = System.getenv("PW_CLOCK");
    assumeFalse(clock != null && !clock.isEmpty());
  }

  // Java: the calls fixture is a list of the params of each call of window.stub; upstream has a list of { params }.
  public static class Calls extends CopyOnWriteArrayList<List<Object>> {
  }

  @Fixture
  Calls calls(Page page) {
    Calls calls = new Calls();
    page.exposeFunction("stub", params -> {
      calls.add(Arrays.asList(params));
      return null;
    });
    return calls;
  }

  // Java: new Date('2015-09-25') and the like are the UTC time in ISO format.
  private static Date date(String iso) {
    return new Date(Instant.parse(iso).toEpochMilli());
  }

  private static double number(Object value) {
    return ((Number) value).doubleValue();
  }

  @Nested
  class RunFor {
    @BeforeEach
    void beforeEach(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.clock().pauseAt(1000);
    }

    @PlaywrightTest
    @DisplayName("CK-001 triggers immediately without specified delay")
    void triggersImmediatelyWithoutSpecifiedDelay(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub);\n" +
        "    }");

      page.clock().runFor(0);
      assertEquals(1, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-002 does not trigger without sufficient delay")
    void doesNotTriggerWithoutSufficientDelay(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub, 100);\n" +
        "    }");
      page.clock().runFor(10);
      assertEquals(Collections.emptyList(), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-003 triggers after sufficient delay")
    void triggersAfterSufficientDelay(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub, 100);\n" +
        "    }");
      page.clock().runFor(100);
      assertEquals(1, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-004 triggers simultaneous timers")
    void triggersSimultaneousTimers(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub, 100);\n" +
        "      setTimeout(window.stub, 100);\n" +
        "    }");
      page.clock().runFor(100);
      assertEquals(2, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-005 triggers multiple simultaneous timers")
    void triggersMultipleSimultaneousTimers(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub, 100);\n" +
        "      setTimeout(window.stub, 100);\n" +
        "      setTimeout(window.stub, 99);\n" +
        "      setTimeout(window.stub, 100);\n" +
        "    }");
      page.clock().runFor(100);
      assertEquals(4, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-006 waits after setTimeout was called")
    void waitsAfterSetTimeoutWasCalled(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub, 150);\n" +
        "    }");
      page.clock().runFor(50);
      assertEquals(Collections.emptyList(), calls);
      page.clock().runFor(100);
      assertEquals(1, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-007 triggers event when some throw")
    void triggersEventWhenSomeThrow(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(() => { throw new Error(); }, 100);\n" +
        "      setTimeout(window.stub, 120);\n" +
        "    }");
      assertThrows(PlaywrightException.class, () -> page.clock().runFor(120));
      assertEquals(1, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-008 creates updated Date while ticking")
    void createsUpdatedDateWhileTicking(Page page, Calls calls) {
      page.clock().setSystemTime(0);
      page.evaluate("async () => {\n" +
        "      setInterval(() => {\n" +
        "        window.stub(new Date().getTime());\n" +
        "      }, 10);\n" +
        "    }");
      page.clock().runFor(100);
      // Java: each call is the list of its params.
      assertEquals(Arrays.asList(
        Arrays.asList(10),
        Arrays.asList(20),
        Arrays.asList(30),
        Arrays.asList(40),
        Arrays.asList(50),
        Arrays.asList(60),
        Arrays.asList(70),
        Arrays.asList(80),
        Arrays.asList(90),
        Arrays.asList(100)), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-009 passes 8 seconds")
    void passes8Seconds(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setInterval(window.stub, 4000);\n" +
        "    }");

      page.clock().runFor("08");
      assertEquals(2, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-010 passes 1 minute")
    void passes1Minute(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setInterval(window.stub, 6000);\n" +
        "    }");

      page.clock().runFor("01:00");
      assertEquals(10, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-011 passes 2 hours, 34 minutes and 10 seconds")
    void passes2Hours34MinutesAnd10Seconds(Page page, Calls calls) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43452' } }
      page.evaluate("async () => {\n" +
        "      setInterval(window.stub, 10000);\n" +
        "    }");

      page.clock().runFor("02:34:10");
      assertEquals(925, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-012 throws for invalid format")
    void throwsForInvalidFormat(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setInterval(window.stub, 10000);\n" +
        "    }");
      assertThrows(PlaywrightException.class, () -> page.clock().runFor("12:02:34:10"));
      assertEquals(Collections.emptyList(), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-013 returns the current now value")
    void returnsTheCurrentNowValue(Page page) {
      page.clock().setSystemTime(0);
      int value = 200;
      page.clock().runFor(value);
      assertEquals(value, page.evaluate("() => Date.now()"));
    }
  }

  @Nested
  class FastForward {
    @BeforeEach
    void beforeEach(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.clock().pauseAt(1000);
    }

    @PlaywrightTest
    @DisplayName("CK-014 ignores timers which wouldn't be run")
    void ignoresTimersWhichWouldntBeRun(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(() => {\n" +
        "        window.stub('should not be logged');\n" +
        "      }, 1000);\n" +
        "    }");
      page.clock().fastForward(500);
      assertEquals(Collections.emptyList(), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-015 pushes back execution time for skipped timers")
    void pushesBackExecutionTimeForSkippedTimers(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(() => {\n" +
        "        window.stub(Date.now());\n" +
        "      }, 1000);\n" +
        "    }");

      page.clock().fastForward(2000);
      assertEquals(Arrays.asList(Arrays.asList(1000 + 2000)), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-016 supports string time arguments")
    void supportsStringTimeArguments(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(() => {\n" +
        "        window.stub(Date.now());\n" +
        "      }, 100000);  // 100000 = 1:40\n" +
        "    }");
      page.clock().fastForward("01:50");
      assertEquals(Arrays.asList(Arrays.asList(1000 + 110000)), calls);
    }
  }

  @Nested
  class StubTimers {
    @BeforeEach
    void beforeEach(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.clock().pauseAt(1000);
    }

    @PlaywrightTest
    @DisplayName("CK-017 sets initial timestamp")
    void setsInitialTimestamp(Page page, Calls calls) {
      page.clock().setSystemTime(1400);
      assertEquals(1400, page.evaluate("() => Date.now()"));
    }

    @PlaywrightTest
    @DisplayName("CK-018 should throw for invalid date")
    @Fixme("java: the message is 'Invalid date: invalid' without the 'clock.setSystemTime: ' prefix")
    void shouldThrowForInvalidDate(Page page) {
      // Java: an Invalid Date, new Date('invalid'), can not be created in Java.
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.clock().setSystemTime("invalid"));
      assertTrue(error.getMessage().contains("clock.setSystemTime: Invalid date: invalid"));
    }

    @PlaywrightTest
    @DisplayName("CK-019 replaces global setTimeout")
    void replacesGlobalSetTimeout(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setTimeout(window.stub, 1000);\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(1, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-020 global fake setTimeout should return id")
    void globalFakeSetTimeoutShouldReturnId(Page page, Calls calls) {
      Object to = page.evaluate("() => setTimeout(window.stub, 1000)");
      assertTrue(to instanceof Number);
    }

    @PlaywrightTest
    @DisplayName("CK-021 replaces global clearTimeout")
    void replacesGlobalClearTimeout(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      const to = setTimeout(window.stub, 1000);\n" +
        "      clearTimeout(to);\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(Collections.emptyList(), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-022 replaces global setInterval")
    void replacesGlobalSetInterval(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      setInterval(window.stub, 500);\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(2, calls.size());
    }

    @PlaywrightTest
    @DisplayName("CK-023 replaces global clearInterval")
    void replacesGlobalClearInterval(Page page, Calls calls) {
      page.evaluate("async () => {\n" +
        "      const to = setInterval(window.stub, 500);\n" +
        "      clearInterval(to);\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(Collections.emptyList(), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-024 replaces global performance.now")
    void replacesGlobalPerformanceNow(Page page) throws Exception {
      // Java: the promise of the evaluate is kept in window and awaited by a second evaluate, a thread would race with runFor().
      page.evaluate("() => {\n" +
        "      window['promise'] = (async () => {\n" +
        "      const prev = performance.now();\n" +
        "      await new Promise(f => setTimeout(f, 1000));\n" +
        "      const next = performance.now();\n" +
        "      return { prev, next };\n" +
        "    })();\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(mapOf("prev", 1000, "next", 2000), page.evaluate("() => window['promise']"));
    }

    @PlaywrightTest
    @DisplayName("CK-025 fakes Date constructor")
    void fakesDateConstructor(Page page) {
      Object now = page.evaluate("() => new Date().getTime()");
      assertEquals(1000, now);
    }
  }

  @Nested
  class StubTimers2 {
    @PlaywrightTest
    @DisplayName("CK-026 replaces global performance.timeOrigin")
    void replacesGlobalPerformanceTimeOrigin(Page page) throws Exception {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(1000));
      page.clock().pauseAt(2000);
      // Java: the promise of the evaluate is kept in window and awaited by a second evaluate, a thread would race with runFor().
      page.evaluate("() => {\n" +
        "      window['promise'] = (async () => {\n" +
        "      const prev = performance.now();\n" +
        "      await new Promise(f => setTimeout(f, 1000));\n" +
        "      const next = performance.now();\n" +
        "      return { prev, next };\n" +
        "    })();\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(1000, page.evaluate("() => performance.timeOrigin"));
      assertEquals(mapOf("prev", 1000, "next", 2000), page.evaluate("() => window['promise']"));
    }
  }

  @Nested
  class Popup {
    @PlaywrightTest
    @DisplayName("CK-027 should tick after popup")
    void shouldTickAfterPopup(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      Date now = date("2015-09-25T00:00:00Z");
      page.clock().pauseAt(now);
      Page popup = page.waitForPopup(() -> page.evaluate("() => window.open('about:blank')"));
      Object popupTime = popup.evaluate("() => Date.now()");
      assertEquals(now.getTime(), (long) number(popupTime));
      page.clock().runFor(1000);
      Object popupTimeAfter = popup.evaluate("() => Date.now()");
      assertEquals(now.getTime() + 1000, (long) number(popupTimeAfter));
    }

    @PlaywrightTest
    @DisplayName("CK-028 should tick before popup")
    void shouldTickBeforePopup(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      Date now = date("2015-09-25T00:00:00Z");
      page.clock().pauseAt(now);
      page.clock().runFor(1000);

      Page popup = page.waitForPopup(() -> page.evaluate("() => window.open('about:blank')"));
      Object popupTime = popup.evaluate("() => Date.now()");
      assertEquals(now.getTime() + 1000, (long) number(popupTime));
    }

    @PlaywrightTest
    @DisplayName("CK-029 should run time before popup")
    void shouldRunTimeBeforePopup(Page page, TestServer server) {
      server.setRoute("/popup.html", (req, res) -> {
        res.setHeader("Content-Type", "text/html");
        res.end("<script>window.time = Date.now()</script>");
      });
      page.navigate(server.EMPTY_PAGE);
      // Wait for 2 second in real life to check that it is past in popup.
      page.waitForTimeout(2000);
      Page popup = page.waitForPopup(() -> page.evaluate("url => window.open(url)", server.PREFIX + "/popup.html"));
      Object popupTime = popup.evaluate("time");
      assertTrue(number(popupTime) >= 2000);
    }

    @PlaywrightTest
    @DisplayName("CK-030 should not run time before popup on pause")
    void shouldNotRunTimeBeforePopupOnPause(Page page, TestServer server) {
      server.setRoute("/popup.html", (req, res) -> {
        res.setHeader("Content-Type", "text/html");
        res.end("<script>window.time = Date.now()</script>");
      });
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.clock().pauseAt(1000);
      page.navigate(server.EMPTY_PAGE);
      // Wait for 2 second in real life to check that it is past in popup.
      page.waitForTimeout(2000);
      Page popup = page.waitForPopup(() -> page.evaluate("url => window.open(url)", server.PREFIX + "/popup.html"));
      popup.waitForLoadState();
      Object popupTime = popup.evaluate("time");
      assertEquals(1000, popupTime);
    }
  }

  @Nested
  class SetFixedTime {
    @PlaywrightTest
    @DisplayName("CK-031 does not fake methods")
    void doesNotFakeMethods(Page page) {
      page.clock().setFixedTime(0);

      // Should not stall.
      page.evaluate("() => {\n" +
        "      return new Promise(f => setTimeout(f, 1));\n" +
        "    }");
    }

    @PlaywrightTest
    @DisplayName("CK-032 allows setting time multiple times")
    void allowsSettingTimeMultipleTimes(Page page) {
      page.clock().setFixedTime(100);
      assertEquals(100, page.evaluate("() => Date.now()"));
      page.clock().setFixedTime(200);
      assertEquals(200, page.evaluate("() => Date.now()"));
    }

    @PlaywrightTest
    @DisplayName("CK-033 fixed time is not affected by clock manipulation")
    void fixedTimeIsNotAffectedByClockManipulation(Page page) {
      page.clock().setFixedTime(100);
      assertEquals(100, page.evaluate("() => Date.now()"));
      page.clock().fastForward(20);
      assertEquals(100, page.evaluate("() => Date.now()"));
    }

    @PlaywrightTest
    @DisplayName("CK-034 allows installing fake timers after settings time")
    void allowsInstallingFakeTimersAfterSettingsTime(Page page, Calls calls) {
      page.clock().setFixedTime(100);
      assertEquals(100, page.evaluate("() => Date.now()"));
      page.clock().setFixedTime(200);
      page.evaluate("async () => {\n" +
        "      setTimeout(() => window.stub(Date.now()));\n" +
        "    }");
      page.clock().runFor(0);
      assertEquals(Arrays.asList(Arrays.asList(200)), calls);
    }
  }

  @Nested
  class WhileRunning {
    @PlaywrightTest
    @DisplayName("CK-035 should progress time")
    void shouldProgressTime(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      long startRealTime = System.currentTimeMillis();
      page.navigate("data:text/html,");
      page.waitForTimeout(1000);
      double now = number(page.evaluate("() => Date.now()"));
      long realElapsed = System.currentTimeMillis() - startRealTime;
      assertTrue(now >= 1000);
      assertTrue(now <= realElapsed + 1000);
    }

    @PlaywrightTest
    @DisplayName("CK-036 should runFor")
    void shouldRunFor(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      long startRealTime = System.currentTimeMillis();
      page.navigate("data:text/html,");
      page.clock().runFor(10000);
      double now = number(page.evaluate("() => Date.now()"));
      long realElapsed = System.currentTimeMillis() - startRealTime;
      assertTrue(now >= 10000);
      assertTrue(now <= 10000 + realElapsed + 1000);
    }

    @PlaywrightTest
    @DisplayName("CK-037 should fastForward")
    void shouldFastForward(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      long startRealTime = System.currentTimeMillis();
      page.navigate("data:text/html,");
      page.clock().fastForward(10000);
      double now = number(page.evaluate("() => Date.now()"));
      long realElapsed = System.currentTimeMillis() - startRealTime;
      assertTrue(now >= 10000);
      assertTrue(now <= 10000 + realElapsed + 1000);
    }

    @PlaywrightTest
    @DisplayName("CK-038 should pause")
    void shouldPause(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(60000);
      // Internally wait to make sure the clock is paused and not running.
      page.waitForTimeout(1111);
      Object now = page.evaluate("() => Date.now()");
      assertEquals(60000, now);
    }

    @PlaywrightTest
    @DisplayName("CK-039 should reject an invalid target time with an active requestAnimationFrame loop")
    @Fixme("java: the message is 'Invalid date: <time>' without the 'clock.pauseAt: ' prefix")
    void shouldRejectAnInvalidTargetTimeWithAnActiveRequestAnimationFrameLoop(Page page) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright-python/issues/3137' } }
      page.clock().install();
      page.setContent("<script>function tick() { requestAnimationFrame(tick); } requestAnimationFrame(tick);</script>");
      // Java: the number is a double in JavaScript; its string is the shortest representation of the digits.
      double invalidTime = number(page.evaluate("() => Date.now()")) * 1_000_000;
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.clock().pauseAt((long) invalidTime));
      assertTrue(error.getMessage().contains("clock.pauseAt: Invalid date: " + new BigDecimal(Double.toString(invalidTime)).toPlainString()));
    }

    @PlaywrightTest
    @DisplayName("CK-040 should pause and fastForward")
    void shouldPauseAndFastForward(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(60000);
      page.clock().fastForward(1000);
      Object now = page.evaluate("() => Date.now()");
      assertEquals(61000, now);
    }

    @PlaywrightTest
    @DisplayName("CK-041 should set system time on pause")
    void shouldSetSystemTimeOnPause(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(60000);
      Object now = page.evaluate("() => Date.now()");
      assertEquals(60000, now);
    }
  }

  @Nested
  class WhileOnPause {
    @PlaywrightTest
    @DisplayName("CK-042 fastForward should not run nested immediate")
    void fastForwardShouldNotRunNestedImmediate(Page page, Calls calls) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(1000);
      page.evaluate("() => {\n" +
        "      setTimeout(() => {\n" +
        "        window.stub('outer');\n" +
        "        setTimeout(() => window.stub('inner'), 0);\n" +
        "      }, 1000);\n" +
        "    }");
      page.clock().fastForward(1000);
      assertEquals(Arrays.asList(Arrays.asList("outer")), calls);
      page.clock().fastForward(1);
      assertEquals(Arrays.asList(Arrays.asList("outer"), Arrays.asList("inner")), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-043 runFor should not run nested immediate")
    void runForShouldNotRunNestedImmediate(Page page, Calls calls) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(1000);
      page.evaluate("() => {\n" +
        "      setTimeout(() => {\n" +
        "        window.stub('outer');\n" +
        "        setTimeout(() => window.stub('inner'), 0);\n" +
        "      }, 1000);\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(Arrays.asList(Arrays.asList("outer")), calls);
      page.clock().runFor(1);
      assertEquals(Arrays.asList(Arrays.asList("outer"), Arrays.asList("inner")), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-044 runFor should not run nested immediate from microtask")
    void runForShouldNotRunNestedImmediateFromMicrotask(Page page, Calls calls) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(1000);
      page.evaluate("() => {\n" +
        "      setTimeout(() => {\n" +
        "        window.stub('outer');\n" +
        "        void Promise.resolve().then(() => setTimeout(() => window.stub('inner'), 0));\n" +
        "      }, 1000);\n" +
        "    }");
      page.clock().runFor(1000);
      assertEquals(Arrays.asList(Arrays.asList("outer")), calls);
      page.clock().runFor(1);
      assertEquals(Arrays.asList(Arrays.asList("outer"), Arrays.asList("inner")), calls);
    }

    @PlaywrightTest
    @DisplayName("CK-045 should stay paused on the next page when an init script reads the time")
    @Fixme(value = "driver: Date.now() is 2061 instead of 2000 after the second navigation, the fix is upstream #43224, newer than driver 1.64.1", browsers = BrowserName.CHROMIUM)
    void shouldStayPausedOnTheNextPageWhenAnInitScriptReadsTheTime(Page page, TestServer server) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43214' } }
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.clock().pauseAt(1000);
      // Java: addInitScript() takes the script, not a function.
      page.addInitScript("Date.now()");
      page.navigate(server.EMPTY_PAGE);
      page.clock().runFor(1000);
      page.navigate(server.PREFIX + "/title.html");
      assertEquals(2000, page.evaluate("() => Date.now()"));
      page.waitForTimeout(500);
      assertEquals(2000, page.evaluate("() => Date.now()"));
    }

    @PlaywrightTest
    @DisplayName("CK-046 should not advance performance.now() before the first clock call on a new page")
    @Fixme("driver: performance.now() and Date.now() are [503, 1000] instead of [0, 1000], the fix is upstream #43224, newer than driver 1.64.1")
    void shouldNotAdvancePerformanceNowBeforeTheFirstClockCallOnANewPage(Page page, TestServer server) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43214' } }
      page.clock().pauseAt(1000);
      page.navigate(server.EMPTY_PAGE);
      page.waitForTimeout(500);
      assertEquals(Arrays.asList(0, 1000), page.evaluate("() => [performance.now(), Date.now()]"));
    }
  }

  @Nested
  class DateNow {
    @PlaywrightTest
    @DisplayName("CK-047 check Date.now is an integer")
    void checkDateNowIsAnInteger(Page page) {
      page.clock().install();
      page.navigate("data:text/html,");
      page.waitForTimeout(1000);
      Object dateValue = page.evaluate("Date.now()");
      assertTrue(isInteger(dateValue));
      page.waitForTimeout(1000);
      Object dateValue2 = page.evaluate("Date.now()");
      assertTrue(isInteger(dateValue2));
    }

    // Java: Number.isInteger()
    private boolean isInteger(Object value) {
      return value instanceof Number && !Double.isInfinite(number(value)) && number(value) == Math.floor(number(value));
    }
  }

  @Nested
  class TemporalNow {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/42830' } }
    @BeforeEach
    void beforeEach(Page page) {
      assumeFalse(!(Boolean) page.evaluate("() => 'Temporal' in globalThis"), "Temporal is not supported");
    }

    @PlaywrightTest
    @DisplayName("CK-049 should follow fixed time")
    void shouldFollowFixedTime(Page page) {
      page.clock().setFixedTime(date("2020-01-01T00:00:00Z"));
      page.navigate("data:text/html,");
      assertEquals(mapOf(
        "instant", "2020-01-01T00:00:00Z",
        "zonedDateTime", "2020-01-01T00:00:00+00:00[UTC]",
        "plainDateTime", "2020-01-01T00:00:00",
        "plainDate", "2020-01-01",
        "plainTime", "00:00:00"), page.evaluate("() => {\n" +
        "      const Temporal = globalThis.Temporal;\n" +
        "      return {\n" +
        "        instant: Temporal.Now.instant().toString(),\n" +
        "        zonedDateTime: Temporal.Now.zonedDateTimeISO('UTC').toString(),\n" +
        "        plainDateTime: Temporal.Now.plainDateTimeISO('UTC').toString(),\n" +
        "        plainDate: Temporal.Now.plainDateISO('UTC').toString(),\n" +
        "        plainTime: Temporal.Now.plainTimeISO('UTC').toString(),\n" +
        "      };\n" +
        "    }"));
    }

    @PlaywrightTest
    @DisplayName("CK-050 should advance with the clock")
    void shouldAdvanceWithTheClock(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      page.clock().pauseAt(1000);
      page.clock().runFor(2000);
      assertEquals(3000, page.evaluate("() => globalThis.Temporal.Now.instant().epochMilliseconds"));
    }

    @PlaywrightTest
    @DisplayName("CK-051 should use the system time zone by default")
    void shouldUseTheSystemTimeZoneByDefault(Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setTimezoneId("America/Los_Angeles"));
      Page page = context.newPage();
      page.clock().setFixedTime(date("2020-01-01T00:00:00Z"));
      page.navigate("data:text/html,");
      assertEquals(mapOf(
        "timeZoneId", "America/Los_Angeles",
        "zonedDateTime", "2019-12-31T16:00:00-08:00[America/Los_Angeles]",
        "plainDateTime", "2019-12-31T16:00:00"), page.evaluate("() => {\n" +
        "      const Temporal = globalThis.Temporal;\n" +
        "      return {\n" +
        "        timeZoneId: Temporal.Now.timeZoneId(),\n" +
        "        zonedDateTime: Temporal.Now.zonedDateTimeISO().toString(),\n" +
        "        plainDateTime: Temporal.Now.plainDateTimeISO().toString(),\n" +
        "      };\n" +
        "    }"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("CK-052 should keep the rest of Temporal intact")
    void shouldKeepTheRestOfTemporalIntact(Page page) {
      page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
      page.navigate("data:text/html,");
      assertEquals(mapOf(
        "tag", "[object Temporal.Now]",
        "enumerable", new ArrayList<>(),
        "isInstant", true,
        "duration", "PT1H"), page.evaluate("() => {\n" +
        "      const Temporal = globalThis.Temporal;\n" +
        "      return {\n" +
        "        tag: Object.prototype.toString.call(Temporal.Now),\n" +
        "        enumerable: Object.keys(Temporal).concat(Object.keys(Temporal.Now)),\n" +
        "        isInstant: Temporal.Now.instant() instanceof Temporal.Instant,\n" +
        "        duration: Temporal.Duration.from({ hours: 1 }).toString(),\n" +
        "      };\n" +
        "    }"));
    }
  }

  @PlaywrightTest
  @DisplayName("CK-053 AbortSignal.timeout")
  void abortSignalTimeout(Page page, BrowserName browserName) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/39293' } }
    page.clock().install(new com.microsoft.playwright.Clock.InstallOptions().setTime(0));
    JSHandle controller = page.evaluateHandle("() => {\n" +
      "    const signal = AbortSignal.any([\n" +
      "      AbortSignal.timeout(100)\n" +
      "    ]);\n" +
      "    const handle = {\n" +
      "      signal,\n" +
      "      event: false,\n" +
      "      handler: false,\n" +
      "    };\n" +
      "    signal.addEventListener('abort', () => handle.event = true);\n" +
      "    signal.onabort = () => handle.handler = true;\n" +
      "    return handle;\n" +
      "  }");
    assertEquals(mapOf(
      "signal", false,
      "event", false,
      "handler", false), controller.evaluate("handle => ({\n" +
      "    signal: handle.signal.aborted,\n" +
      "    event: handle.event,\n" +
      "    handler: handle.handler,\n" +
      "  })"));
    page.clock().runFor(200);
    assertEquals(mapOf(
      "signal", true,
      "event", true,
      "handler", true,
      "reason", mapOf(
        "name", "TimeoutError",
        "message", browserName == BrowserName.CHROMIUM ? "signal timed out" : "The operation timed out.",
        "code", 23)), controller.evaluate("handle => ({\n" +
      "    signal: handle.signal.aborted,\n" +
      "    event: handle.event,\n" +
      "    handler: handle.handler,\n" +
      "    reason: {\n" +
      "      name: handle.signal.reason.name,\n" +
      "      message: handle.signal.reason.message,\n" +
      "      code: handle.signal.reason.code,\n" +
      "    },\n" +
      "  })"));
    assertEquals(true, page.evaluate("() => AbortSignal.abort().aborted"));
  }

  @PlaywrightTest
  @DisplayName("CK-054 correctly increments Date.now()/performance.now() during blocking execution")
  void correctlyIncrementsDateNowPerformanceNowDuringBlockingExecution(Page page, TestServer server) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/35362' } }
    page.clock().setSystemTime(date("2026-01-01T00:00:00Z"));
    server.setRoute("/repro.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("\n" +
        "      <html>\n" +
        "        <body>\n" +
        "          <script>\n" +
        "          {\n" +
        "            const start = performance.now();\n" +
        "            while (performance.now() - start < 100) { }\n" +
        "          }\n" +
        "          {\n" +
        "            const start = Date.now();\n" +
        "            while (Date.now() - start < 100) { }\n" +
        "          }\n" +
        "          console.log('done');\n" +
        "          </script>\n" +
        "        </body>\n" +
        "      </html>\n" +
        "    ");
    });
    // Java: the console message is awaited around the goto() that triggers it.
    ConsoleMessage waitForDone = page.waitForConsoleMessage(
      new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().equals("done")),
      () -> page.navigate(server.PREFIX + "/repro.html"));
  }
}
