package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.microsoft.playwright.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextViewport extends TestBase {

  @Test
  void shouldGetTheProperDefaultViewPortSize() {
    verifyViewport(page, 1280, 720);
  }

  @Test
  void shouldSetTheProperViewportSize() {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(123, 456);
    verifyViewport(page,123, 456);
  }

  @Test
  void shouldReturnCorrectOuterWidthAndOuterHeight() {
    Map<String, Integer> size = (Map<String, Integer>) page.evaluate("() => {\n" +
      "  return {\n" +
      "    innerWidth: window.innerWidth,\n" +
      "    innerHeight: window.innerHeight,\n" +
      "    outerWidth: window.outerWidth,\n" +
      "    outerHeight: window.outerHeight,\n" +
      "  };\n" +
      "}");
    assertEquals(1280, size.get("innerWidth"));
    assertEquals(720, size.get("innerHeight"));
    assertTrue(size.get("outerWidth") >= size.get("innerWidth"));
    assertTrue(size.get("outerHeight") >= size.get("innerHeight"));
  }

  @Test
  void shouldEmulateDeviceWidth() {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(200, 200);
    assertEquals(200, page.evaluate("() => window.screen.width"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 100px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 100px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-width: 500px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-width: 200px)').matches"));
    page.setViewportSize(500, 500);
    assertEquals(500, page.evaluate("() => window.screen.width"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 400px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-width: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-width: 500px)').matches"));
  }

  @Test
  void shouldEmulateDeviceHeight() {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(200, 200);
    assertEquals(200, page.evaluate("() => window.screen.height"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 100px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 100px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-height: 500px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-height: 200px)').matches"));
    page.setViewportSize(500, 500);
    assertEquals(500, page.evaluate("() => window.screen.height"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 400px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-height: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-height: 500px)').matches"));
  }

  @Test
  void shouldEmulateAvailWidthAndAvailHeight() {
    page.setViewportSize(500, 600);
    assertEquals(500, page.evaluate("() => window.screen.availWidth"));
    assertEquals(600, page.evaluate("() => window.screen.availHeight"));
  }

  @Test
  void shouldNotHaveTouchByDefault() {
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(false, page.evaluate("() => 'ontouchstart' in window"));
    page.navigate(server.PREFIX + "/detect-touch.html");
    assertEquals("NO", page.evaluate("() => document.body.textContent.trim()"));
  }

  @Test
  void shouldSupportTouchWithNullViewport() {
    Browser.NewContextOptions options = new Browser.NewContextOptions()
      .setHasTouch(true).setViewportSize(null);
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(true, page.evaluate("() => 'ontouchstart' in window"));
    context.close();
  }

  @Test
  void shouldReportNullViewPortSizeWhenGivenNullViewport() {
    Browser.NewContextOptions options = new Browser.NewContextOptions().setViewportSize(null);
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    assertNull(page.viewportSize());
    context.close();
  }
}
