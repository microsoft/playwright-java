package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static org.junit.jupiter.api.Assertions.*;

@FixtureTest
@UsePlaywright
public class TestPageClock {
  private ArrayList<Object> calls;

  @BeforeEach
  void exposeStubFunction(Page page) {
    calls = new ArrayList();
    page.exposeFunction("stub", (Object... params) -> {
      calls.add(params);
      return null;
    });
  }

  private void setupRunForTest(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.clock().pauseAt(1000);
  }

  @Test
  void runForTriggersImmediatelyWithoutSpecifiedDelay(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setTimeout(window.stub)");
    page.clock().runFor(0);
    assertEquals(1, calls.size());
  }

  @Test
  void runForDoesNotTriggerWithoutSufficientDelay(Page page) {
    setupRunForTest(page);
    // Trigger the stub with a delay
    page.evaluate("() => setTimeout(window.stub, 100)");
    page.clock().runFor(11);
    assertEquals(0, calls.size());
  }

  @Test
  void  runForTriggersAfterSufficientDelay(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setTimeout(window.stub, 100)");
    page.clock().runFor(100);
    assertEquals(1, calls.size());
  }

  @Test
  void  runForTriggersSimultaneousTimers(Page page) {
    setupRunForTest(page);
    page.evaluate("() => { setTimeout(window.stub, 100);" +
      "setTimeout(window.stub, 100); }");
    page.clock().runFor(100);
    assertEquals(2, calls.size());
  }

  @Test
  void  runForTriggersMultipleSimultaneousTimers(Page page) {
    setupRunForTest(page);
    page.evaluate("() => { setTimeout(window.stub, 100);" +
      "setTimeout(window.stub, 100);" +
      "setTimeout(window.stub, 99);" +
      "setTimeout(window.stub, 100); }");
    page.clock().runFor(100);
    assertEquals(4, calls.size());
  }

  @Test
  void  runForWaitsAfterSetTimeoutWasCalled(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setTimeout(window.stub, 150)");
    page.clock().runFor(50);
    assertEquals(0, calls.size());
    page.clock().runFor(100);
    assertEquals(1, calls.size());
  }

  @Test
  void runForTriggersEventWhenSomeThrow(Page page) {
    setupRunForTest(page);
    page.evaluate("() => {\n" +
      "  setTimeout(() => {throw new Error(); }, 100);\n" +
      "  setTimeout(window.stub, 120);\n" +
      "}");
    assertThrows(PlaywrightException.class, () -> page.clock().runFor(120));
    assertEquals(1, calls.size());
  }

  @Test
  void runForCreatesUpdatedDateWhileTicking(Page page) {
    setupRunForTest(page);
    page.clock().setSystemTime(0);
    page.evaluate("() => setInterval(() => { window.stub(new Date().getTime()); }, 10)");
    page.clock().runFor(100);
    assertJsonEquals("[[10],[20],[30],[40],[50],[60],[70],[80],[90],[100]]", calls);
  }

  @Test
  void runForPasses8Seconds(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setInterval(window.stub, 4000)");
    page.clock().runFor("08");
    assertEquals(2, calls.size());
  }

  @Test
  void runForPasses1Minute(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setInterval(window.stub, 6000)");
    page.clock().runFor("01:00");
    assertEquals(10, calls.size());
  }

  @Test
  void runForPasses2Hours34MinutesAnd10Seconds(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setInterval(window.stub, 10000)");
    page.clock().runFor("02:34:10");
    assertEquals(925, calls.size());
  }

  @Test
  void runForThrowsForInvalidFormat(Page page) {
    setupRunForTest(page);
    page.evaluate("() => setInterval(window.stub, 10000)");
    assertThrows(PlaywrightException.class, () -> page.clock().runFor("12:02:34:10"));
    assertEquals(0, calls.size());
  }

  @Test
  void runForReturnsTheCurrentNowValue(Page page) {
    setupRunForTest(page);
    page.clock().setSystemTime(0);
    final int value = 200;
    page.clock().runFor(value);
    assertEquals(value, page.evaluate("() => Date.now()"));
  }

  private void setupFastForwardTest(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.clock().pauseAt(1000);
  }

  @Test
  void fastForwardIgnoresTimersWhichWouldntBeRun(Page page) {
    setupFastForwardTest(page);
    page.evaluate("() => setTimeout(() => { window.stub('should not be logged'); }, 1000)");
    page.clock().fastForward(500);
    assertEquals(0, calls.size());
  }

  @Test
  void fastForwardPushesBackExecutionTimeForSkippedTimers(Page page) {
    setupFastForwardTest(page);
    page.evaluate("() => setTimeout(() => { window.stub(Date.now()); }, 1000)");
    page.clock().fastForward(2000);
    assertEquals(1, calls.size());
    assertEquals(1000 + 2000, ((Object[])calls.get(0))[0]);
  }

  @Test
  void fastForwardSupportsStringTimeArguments(Page page) {
    setupFastForwardTest(page);
    page.evaluate("() => setTimeout(() => { window.stub(Date.now()); }, 100000)");
    page.clock().fastForward("01:50");
    assertEquals(1, calls.size());
    assertEquals(1000 + 110000, ((Object[])calls.get(0))[0]);
  }

  private void setupStubTimers(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.clock().pauseAt(1000);
  }

  @Test
  void setsInitialTimestamp(Page page) {
    setupStubTimers(page);
    page.clock().setSystemTime(1400);
    assertEquals(1400, page.evaluate("() => Date.now()"));
  }

  @Test
  void replacesGlobalSetTimeout(Page page) {
    setupStubTimers(page);
    page.evaluate("() => setTimeout(window.stub, 1000)");
    page.clock().runFor(1000);
    assertEquals(1, calls.size());
  }

  @Test
  void globalFakeSetTimeoutShouldReturnId(Page page) {
    setupStubTimers(page);
    Double to = (Double) page.evaluate("() => setTimeout(window.stub, 1000)");
    assertTrue(to > 0);
  }

  @Test
  void replacesGlobalClearTimeout(Page page) {
    setupStubTimers(page);
    page.evaluate("() => { const to = setTimeout(window.stub, 1000); clearTimeout(to); }");
    page.clock().runFor(1000);
    assertEquals(0, calls.size());
  }

  @Test
  void replacesGlobalSetInterval(Page page) {
    setupStubTimers(page);
    page.evaluate("() => setInterval(window.stub, 500)");
    page.clock().runFor(1000);
    assertEquals(2, calls.size());
  }

  @Test
  void replacesGlobalClearInterval(Page page) {
    setupStubTimers(page);
    page.evaluate("() => { const to = setInterval(window.stub, 500); clearInterval(to); }");
    page.clock().runFor(1000);
    assertEquals(0, calls.size());
  }

  @Test
  void replacesGlobalPerformanceNow(Page page) {
  }

  @Test
  void fakesDateConstructor(Page page) {
    setupStubTimers(page);
    Integer now = (Integer) page.evaluate("() => new Date().getTime()");
    assertEquals(1000, now);
  }

  @Test
  void replacesGlobalPerformanceTimeOrigin(Page page) {
  }


  @Test
  void shouldTickAfterPopup(Page page) throws ParseException {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    Date now = new SimpleDateFormat("yyyy-MM-dd").parse("2015-09-25");
    page.clock().pauseAt(now.getTime());
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank')");
    });
    Double popupTime = (Double) popup.evaluate("() => Date.now()");
    assertEquals(now.getTime(), popupTime);
    page.clock().runFor(1000);
    Double popupTimeAfter = (Double) popup.evaluate("() => Date.now()");
    assertEquals(now.getTime() + 1000, popupTimeAfter);
  }

  @Test
  void shouldTickBeforePopup(Page page) throws ParseException {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    Date now = new SimpleDateFormat("yyyy-MM-dd").parse("2015-09-25");
    page.clock().pauseAt(now.getTime());
    page.clock().runFor(1000);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank')");
    });
    Double popupTime = (Double) popup.evaluate("() => Date.now()");
    assertEquals(now.getTime() + 1000, popupTime);
  }

  @Test
  void shouldRunTimeBeforePopup(Page page, Server server) {
    server.setRoute("/popup.html", exchange -> {
      exchange.getResponseHeaders().set("Content-type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<script>window.time = Date.now()</script>");
      }
    });
    page.navigate(server.EMPTY_PAGE);
    page.waitForTimeout(2000);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url)", server.PREFIX + "/popup.html");
    });
    popup.waitForURL(server.PREFIX + "/popup.html");
    Double popupTime = (Double) popup.evaluate("time");
    assertTrue(popupTime >= 2000, "popupTime = " + popupTime);
  }

  @Test
  void shouldNotRunTimeBeforePopupOnPause(Page page, Server server) {
    server.setRoute("/popup.html", exchange -> {
      exchange.getResponseHeaders().set("Content-type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<script>window.time = Date.now()</script>");
      }
    });
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.clock().pauseAt(1000);
    page.navigate(server.EMPTY_PAGE);
    page.waitForTimeout(2000);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url)", server.PREFIX + "/popup.html");
    });
    popup.waitForURL(server.PREFIX + "/popup.html");
    Object popupTime = popup.evaluate("time");
    assertEquals(1000, popupTime);
  }

  @Test
  void setFixedTimeDoesNotFakeMethods(Page page) {
    page.clock().setFixedTime(0);
    // Should not stall.
    page.evaluate("() => new Promise(f => setTimeout(f, 1))");
  }

  @Test
  void setFixedTimeAllowsSettingTimeMultipleTimes(Page page) {
    page.clock().setFixedTime(100);
    assertEquals(100, page.evaluate("() => Date.now()"));
    page.clock().setFixedTime(200);
    assertEquals(200, page.evaluate("() => Date.now()"));
  }

  @Test
  void setFixedTimeFixedTimeIsNotAffectedByClockManipulation(Page page) {
    page.clock().setFixedTime(100);
    assertEquals(100, page.evaluate("() => Date.now()"));
    page.clock().fastForward(20);
    assertEquals(100, page.evaluate("() => Date.now()"));
  }

  @Test
  void setFixedTimeAllowsInstallingFakeTimersAfterSettingTime(Page page) {
    page.clock().setFixedTime(100);
    assertEquals(100, page.evaluate("() => Date.now()"));
    page.clock().setFixedTime(200);
    page.evaluate("async () => { setTimeout(() => window.stub(Date.now()), 0); }");
    page.clock().runFor(0);
    assertEquals(1, calls.size());
    assertEquals(200, ((Object[]) calls.get(0))[0]);
  }

  @Test
  void whileRunningShouldProgressTime(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.waitForTimeout(1000);
    int now = (int) page.evaluate("() => Date.now()");
    assertTrue(now >= 1000 && now <= 2000);
  }

  @Test
  void whileRunningShouldRunFor(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().runFor(10000);
    int now = (int) page.evaluate("() => Date.now()");
    assertTrue(now >= 10000 && now <= 11000);
  }

  @Test
  void whileRunningShouldFastForward(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().fastForward(10000);
    int now = (int) page.evaluate("() => Date.now()");
    assertTrue(now >= 10000 && now <= 11000);
  }

  @Test
  void whileRunningShouldFastForwardTo(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().fastForward(10000);
    int now = (int) page.evaluate("() => Date.now()");
    assertTrue(now >= 10000 && now <= 11000);
  }

  @Test
  void whileRunningShouldPause(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().pauseAt(1000);
    // Internally wait to make sure the clock is paused and not running.
    page.waitForTimeout(1111);
    int now = (int) page.evaluate("() => Date.now()");
    assertTrue(now >= 0 && now <= 1000);
  }

  @Test
  void whileRunningShouldPauseAndFastForward(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().pauseAt(1000);
    page.clock().fastForward(1000);
    int now = (int) page.evaluate("() => Date.now()");
    assertEquals(2000, now);
  }

  @Test
  void whileRunningShouldSetSystemTimeOnPause(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().pauseAt(1000);
    int now = (int) page.evaluate("() => Date.now()");
    assertEquals(1000, now);
  }

  @Test
  void whileOnPauseFastForwardShouldNotRunNestedImmediate(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().pauseAt(1000);
    page.evaluate("() => { setTimeout(() => { window.stub('outer'); setTimeout(() => window.stub('inner'), 0); }, 1000); }");
    page.clock().fastForward(1000);
    assertEquals(1, calls.size());
    assertEquals("outer", ((Object[]) calls.get(0))[0]);
    page.clock().fastForward(1);
    assertEquals(2, calls.size());
    assertEquals("inner", ((Object[]) calls.get(1))[0]);
  }

  @Test
  void whileOnPauseRunForShouldNotRunNestedImmediate(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().pauseAt(1000);
    page.evaluate("() => { setTimeout(() => { window.stub('outer'); setTimeout(() => window.stub('inner'), 0); }, 1000); }");
    page.clock().runFor(1000);
    assertEquals(1, calls.size());
    assertEquals("outer", ((Object[]) calls.get(0))[0]);
    page.clock().runFor(1);
    assertEquals(2, calls.size());
    assertEquals("inner", ((Object[]) calls.get(1))[0]);
  }

  @Test
  void whileOnPauseRunForShouldNotRunNestedImmediateFromMicrotask(Page page) {
    page.clock().install(new Clock.InstallOptions().setTime(0));
    page.navigate("data:text/html,");
    page.clock().pauseAt(1000);
    page.evaluate("() => { setTimeout(() => { window.stub('outer'); void Promise.resolve().then(() => setTimeout(() => window.stub('inner'), 0)); }, 1000); }");
    page.clock().runFor(1000);
    assertEquals(1, calls.size());
    assertEquals("outer", ((Object[]) calls.get(0))[0]);
    page.clock().runFor(1);
    assertEquals(2, calls.size());
    assertEquals("inner", ((Object[]) calls.get(1))[0]);
  }

  @Test
  void shouldThrowForInvalidDate(Page page) {
    Exception exception1 = assertThrows(PlaywrightException.class, () -> page.clock().setSystemTime("invalid"));
    assertTrue(exception1.getMessage().contains("Invalid date: invalid"), exception1.getMessage());
  }
}
