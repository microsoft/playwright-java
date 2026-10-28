/**
 * Copyright 2018 Google Inc. All rights reserved.
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
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.ImageComparator;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/headful.spec.ts. */
@Execution(ExecutionMode.SAME_THREAD) // Headed windows fail to capture screenshots when several share the display.
public class TestHeadful {
  // Java: upstream runs this file only in its headed project, where every browser, context and page
  // fixture is headed. The class does the same by overriding the launch options of the worker.
  @Fixture(scope = Scope.WORKER)
  static BrowserType.LaunchOptions launchOptions() {
    return Env.launchOptions().setHeadless(false);
  }

  // Java: launchPersistent.launch() takes the options of the headed project.
  private static BrowserType.LaunchPersistentContextOptions headedPersistentOptions() {
    return Env.launchPersistentContextOptions().setHeadless(false);
  }

  @BeforeEach
  void skipHeadlessShell() {
    assumeFalse("chromium-headless-shell".equals(Env.channel()), "shell is never headed");
  }

  @PlaywrightTest
  @DisplayName("HE-001 should have default url when launching browser @smoke")
  void shouldHaveDefaultUrlWhenLaunchingBrowser(LaunchPersistent launchPersistent) {
    BrowserContext context = launchPersistent.launch(headedPersistentOptions()).context;
    List<String> urls = new ArrayList<>();
    for (Page page : context.pages()) {
      urls.add(page.url());
    }
    assertEquals(Arrays.asList("about:blank"), urls);
  }

  @PlaywrightTest
  @DisplayName("HE-002 should close browser with beforeunload page")
  void shouldCloseBrowserWithBeforeunloadPage(LaunchPersistent launchPersistent, TestServer server) {
    // it.slow()
    BrowserContext context = launchPersistent.launch(headedPersistentOptions()).context;
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");
    context.close();
  }

  @PlaywrightTest
  @DisplayName("HE-003 should close browsercontext with pending beforeunload dialog")
  void shouldCloseBrowsercontextWithPendingBeforeunloadDialog(TestServer server, BrowserContext context) {
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");
    // Java: there is no waitForDialog, the handler completes a future.
    CompletableFuture<Dialog> dialog = new CompletableFuture<>();
    page.onDialog(dialog::complete);
    page.close(new Page.CloseOptions().setRunBeforeUnload(true));
    page.waitForCondition(dialog::isDone);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("HE-004 should not crash when creating second context")
  void shouldNotCrashWhenCreatingSecondContext(Browser browser) {
    {
      BrowserContext browserContext = browser.newContext();
      browserContext.newPage();
      browserContext.close();
    }
    {
      BrowserContext browserContext = browser.newContext();
      browserContext.newPage();
      browserContext.close();
    }
  }

  @PlaywrightTest
  @DisplayName("HE-005 should click when viewport size is larger than screen")
  void shouldClickWhenViewportSizeIsLargerThanScreen(Page page) {
    // issue: https://github.com/microsoft/playwright/issues/22082
    page.setViewportSize(3000, 3000);
    page.setContent("\n" +
      "    <style>\n" +
      "      html, body { position: absolute; left: 0; top: 0; right: 0; bottom: 0; }\n" +
      "    </style>\n" +
      "    <button style=\"position: absolute; right: 0; bottom: 0;\">Button in the bottom-right corner</button>\n" +
      "  ");
    page.locator("button").click();
  }

  @PlaywrightTest
  @DisplayName("HE-006 should dispatch click events to oversized viewports")
  void shouldDispatchClickEventsToOversizedViewports(Page page) {
    // issue: https://github.com/microsoft/playwright/issues/22082
    // Some prime numbers for width/height.
    int width = 2971;
    int height = 3067;
    page.setViewportSize(width, height);
    page.evaluate("() => {\n" +
      "  window['events'] = [];\n" +
      "  window.addEventListener('click', event => window['events'].push({ x: event.clientX, y: event.clientY }), false);\n" +
      "}");
    List<Object> expectedEvents = new ArrayList<>();
    // Allow a little padding from the edges of viewport.
    for (int i = 3; i < 23; ++i) {
      int x = width - i;
      int y = height - i;
      expectedEvents.add(mapOf("x", x, "y", y));
      page.mouse().move(x, y);
      page.mouse().down();
      page.mouse().up();
    }
    Object actualEvents = page.evaluate("() => window['events']");
    assertEquals(expectedEvents, actualEvents);
  }

  @PlaywrightTest
  @DisplayName("HE-007 should click background tab")
  void shouldClickBackgroundTab(Page page, TestServer server) {
    page.setContent("<button>Hello</button><a target=_blank href=\"" + server.EMPTY_PAGE + "\">empty.html</a>");
    page.click("a");
    page.click("button");
  }

  @PlaywrightTest
  @DisplayName("HE-008 should close browser after context menu was triggered")
  void shouldCloseBrowserAfterContextMenuWasTriggered(BrowserType browserType, TestServer server) {
    Browser browser = browserType.launch(Env.launchOptions().setHeadless(false));
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/grid.html");
    page.click("body", new Page.ClickOptions().setButton(MouseButton.RIGHT));
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("HE-009 should(not) block third party cookies")
  void shouldNotBlockThirdPartyCookies(Page page, TestServer server, BrowserName browserName) {
    // allowsThirdParty and defaultSameSiteCookieValue of tests/config/browserTest.ts
    boolean allowsThirdParty = browserName == BrowserName.FIREFOX;
    SameSiteAttribute defaultSameSiteCookieValue;
    if (browserName == BrowserName.CHROMIUM || Env.isBidi())
      defaultSameSiteCookieValue = SameSiteAttribute.LAX;
    else if (browserName == BrowserName.WEBKIT && (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel())))
      defaultSameSiteCookieValue = SameSiteAttribute.LAX;
    else
      defaultSameSiteCookieValue = SameSiteAttribute.NONE; // Windows + older macOS, firefox

    page.navigate(server.EMPTY_PAGE);
    page.evaluate("src => {\n" +
      "  let fulfill;\n" +
      "  const promise = new Promise(x => fulfill = x);\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  document.body.appendChild(iframe);\n" +
      "  iframe.onload = fulfill;\n" +
      "  iframe.src = src;\n" +
      "  return promise;\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/grid.html");
    Object documentCookie = page.frames().get(1).evaluate("() => {\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    page.waitForTimeout(2000);
    assertEquals(allowsThirdParty ? "username=John Doe" : "", documentCookie);
    List<Cookie> cookies = page.context().cookies(server.CROSS_PROCESS_PREFIX + "/grid.html");
    if (allowsThirdParty) {
      assertEquals(1, cookies.size());
      // Java: Cookie has no equals(), the fields are compared one by one.
      Cookie cookie = cookies.get(0);
      assertEquals("127.0.0.1", cookie.domain);
      assertEquals(-1.0, cookie.expires);
      assertEquals(false, cookie.httpOnly);
      assertEquals("username", cookie.name);
      assertEquals("/", cookie.path);
      assertEquals(defaultSameSiteCookieValue, cookie.sameSite);
      assertEquals(false, cookie.secure);
      assertEquals("John Doe", cookie.value);
    } else {
      assertEquals(Collections.emptyList(), cookies);
    }
  }

  @PlaywrightTest
  @DisplayName("HE-010 should not block third party SameSite=None cookies")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "No third party cookies in WebKit")
  void shouldNotBlockThirdPartySameSiteNoneCookies(HttpsTestServer httpsServer, BrowserName browserName, Browser browser) {
    // it.skip(process.env.PW_CLOCK === 'frozen') is always false here.
    Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true));

    httpsServer.setRoute("/empty.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<iframe src=\"" + httpsServer.CROSS_PROCESS_PREFIX + "/grid.html\"></iframe>");
    });

    httpsServer.setRoute("/grid.html", (req, res) -> {
      res.writeHead(200, "Set-Cookie", "a=b; Path=/; Max-Age=3600; SameSite=None; Secure", "Content-Type", "text/html");
      res.end("Hello world\n" +
        "    <script>\n" +
        "    setTimeout(() => fetch('/json'), 1000);\n" +
        "    </script>");
    });

    CompletableFuture<String> cookie = new CompletableFuture<>();
    httpsServer.setRoute("/json", (req, res) -> {
      cookie.complete(req.headers.get("cookie"));
      res.end();
    });

    page.navigate(httpsServer.EMPTY_PAGE);
    page.waitForCondition(cookie::isDone);
    assertEquals("a=b", cookie.getNow(null));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("HE-011 should not override viewport size when passed null")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "Our WebKit embedder does not respect window features")
  void shouldNotOverrideViewportSizeWhenPassedNull(BrowserName browserName, TestServer server, Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => {\n" +
        "  const win = window.open(window.location.href, 'Title', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=300,top=0,left=0');\n" +
        "  win.resizeTo(500, 450);\n" +
        "}");
    });
    popup.waitForLoadState();
    popup.waitForFunction("() => window.outerWidth === 500 && window.outerHeight === 450");
    context.close();
  }

  @PlaywrightTest
  @DisplayName("HE-012 Page.bringToFront should work")
  void pageBringToFrontShouldWork(Browser browser) {
    Page page1 = browser.newPage();
    page1.setContent("Page1");
    Page page2 = browser.newPage();
    page2.setContent("Page2");

    page1.bringToFront();
    assertEquals("visible", page1.evaluate("document.visibilityState"));
    assertEquals("visible", page2.evaluate("document.visibilityState"));

    page2.bringToFront();
    assertEquals("visible", page1.evaluate("document.visibilityState"));
    assertEquals("visible", page2.evaluate("document.visibilityState"));

    page1.close();
    page2.close();
  }

  @PlaywrightTest
  @DisplayName("HE-013 should click in OOPIF")
  void shouldClickInOOPIF(BrowserName browserName, LaunchPersistent launchPersistent, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<iframe src=\"" + server.CROSS_PROCESS_PREFIX + "/iframe.html\"></iframe>");
    });
    server.setRoute("/iframe.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<button id=\"button\" onclick=\"console.log('ok')\">Submit</button>\n" +
        "    <script>console.log('frame loaded')</script>");
    });

    Page page = launchPersistent.launch(headedPersistentOptions()).page;
    List<String> consoleLog = new ArrayList<>();
    page.onConsoleMessage(m -> consoleLog.add(m.text()));
    page.navigate(server.EMPTY_PAGE);
    page.frames().get(1).click("text=Submit");
    assertTrue(consoleLog.contains("ok"));
  }

  @PlaywrightTest
  @DisplayName("HE-014 should click bottom row w/ infobar in OOPIF")
  void shouldClickBottomRowWInfobarInOOPIF(BrowserName browserName, LaunchPersistent launchPersistent, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("\n" +
        "      <style>\n" +
        "        html, body { margin: 0; padding: 0; width: 100%; height: 100%; }\n" +
        "        iframe { position: absolute; bottom: 0; }\n" +
        "      </style>\n" +
        "      <iframe src=\"" + server.CROSS_PROCESS_PREFIX + "/iframe.html\"></iframe>\n" +
        "    ");
    });

    server.setRoute("/iframe.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("\n" +
        "      <style>\n" +
        "        html, body { margin: 0; padding: 0; width: 100%; height: 100%; }\n" +
        "        button { position: absolute; bottom: 0; }\n" +
        "      </style>\n" +
        "      <button id=\"button\" onclick=\"window._clicked=true\">Submit</button>");
    });

    Page page = launchPersistent.launch(headedPersistentOptions()).page;
    page.navigate(server.EMPTY_PAGE);
    if (browserName == BrowserName.CHROMIUM) {
      // CHROME BUG:
      //   Unfortunately, on some platforms the automation infobar is shown up late or animates in.
      //   Upon showing, it triggers a resize of WebContentsView and RenderWidgetHostView
      //   through the native view hierarchy. This in turn resizes the compositor to the visible view size
      //   instead of the emulated size specified in Emulation.setDeviceMetricsOverride.
      //   Hit testing for OOPIFs is affected by the new size, and clicks do not reach the iframe.
      //
      //   The workaround is to re-apply the viewport after a delay, in this case after the navigation.
      page.setViewportSize(800, 600);
    }
    page.frames().get(1).click("text=Submit");
    assertEquals(true, page.frames().get(1).evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("HE-015 headless and headful should use same default fonts")
  void headlessAndHeadfulShouldUseSameDefaultFonts(Page page, BrowserName browserName, BrowserType browserType) {
    // issue: https://github.com/microsoft/playwright/issues/11177
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "Text is misaligned in headed vs headless");
    List<String> genericFontFamilies = Arrays.asList(
      "standard",
      "serif",
      "sans-serif",
      "monospace",
      "cursive",
      "fantasy",
      "emoji");
    Browser headlessBrowser = browserType.launch(Env.launchOptions().setHeadless(true));
    Page headlessPage = headlessBrowser.newPage();
    for (String family : genericFontFamilies) {
      String content = "<div style=\"font: 15px bold " + ("standard".equals(family) ? "" : family) + "; max-width: 300px; max-height: 300px; overflow: hidden\">\n" +
        "      Lorem ipsum dolor sit amet consectetur adipiscing elit proin, integer curabitur imperdiet rhoncus cursus tincidunt bibendum, consequat sed magnis laoreet luctus mollis tellus. Nisl parturient mus accumsan feugiat sem laoreet magnis nisi, aptent per sollicitudin gravida orci ac blandit, viverra eros praesent auctor vivamus semper bibendum. Consequat sed habitasse luctus dictumst gravida platea semper phasellus, nascetur ridiculus purus est varius quisque et scelerisque, id vehicula eleifend montes sollicitudin dis velit. Pellentesque ridiculus per natoque et eleifend taciti nunc, laoreet auctor at condimentum imperdiet ante, conubia mi cubilia scelerisque sociosqu sem.</p> <p>Curabitur magna per felis primis mauris non dapibus luctus ultricies eros, quis et egestas condimentum lobortis eget semper montes litora purus, ridiculus elementum sollicitudin imperdiet dictum lacinia parturient cras eu. Risus cum varius rhoncus eros torquent pretium taciti id erat dis egestas, nibh tristique montes convallis metus lacus phasellus blandit ut auctor bibendum semper, facilisis mi integer eget ultrices lobortis odio viverra duis dui. Risus ullamcorper lacinia in venenatis sodales fusce tortor potenti volutpat quis, dictum vulputate suspendisse velit mollis torquent sociis aptent morbi, senectus nascetur justo maecenas conubia magnis viverra gravida fames. Phasellus sed nec gravida nibh class augue lectus, blandit quis turpis orci diam nam pellentesque, ultricies metus imperdiet hendrerit lacinia lacus.</p> <p>Inceptos facilisi montes cum hendrerit, pulvinar ut tellus eget velit, arcu nulla aenean. Phasellus augue urna nostra molestie interdum vehicula, posuere fames cum euismod massa curabitur donec, inceptos cubilia tellus facilisis fermentum. Lacus laoreet facilisis ultrices cursus quisque at ad porta vestibulum massa inceptos, curae class aliquet maecenas cum ullamcorper pulvinar erat mus vitae. Cum in aenean convallis dis quam tincidunt justo sed quisque, imperdiet faucibus hendrerit felis commodo scelerisque magnis vehicula etiam leo, eros varius platea lobortis maecenas condimentum nisi phasellus. Turpis vulputate mus himenaeos sociosqu facilisis dignissim leo quam, ultricies habitasse commodo molestie est tortor vitae et, porttitor risus erat cursus phasellus facilisi litora.</p> <p>Nostra habitasse egestas magnis velit pellentesque parturient cum lectus viverra, vestibulum sociosqu nunc vel urna consequat lacinia phasellus at sapien, aenean pretium dictum sed montes interdum imperdiet iaculis. Leo hac eros arcu senectus maecenas, tortor pulvinar venenatis lacinia volutpat, mattis platea ut facilisi. Aenean condimentum at et donec sociosqu fermentum luctus potenti semper vulputate, sapien justo non est auctor gravida ultricies fames per commodo, sed habitasse facilisi nulla quisque hendrerit aliquet viverra bibendum.</p> <p>Interdum nisl quam etiam montes porttitor laoreet nullam senectus velit, mauris proin tellus imperdiet litora venenatis fames massa quis, sollicitudin justo vivamus curae in sociis suscipit facilisi. Platea inceptos lacus elementum pellentesque quam euismod dictumst sociis tincidunt vulputate porttitor eros, turpis netus ut ad tempor sapien aliquet sodales molestie consequat nostra. Cum augue in quisque primis ut nunc sodales, sem orci tempus posuere cubilia suspendisse lacinia ligula, magna sed ridiculus at maecenas habitant.</p> <p>Natoque magna ac feugiat tellus bibendum diam, metus lobortis nisl ornare varius praesent, dictumst gravida lacus parturient semper. Pellentesque faucibus congue fusce posuere placerat dictum vitae, dui vestibulum eu sociis tempus aliquam ultricies malesuada, potenti laoreet lacus sem gravida nisi. Nostra platea sagittis hendrerit congue conubia senectus bibendum quis sapien pharetra, scelerisque nam imperdiet fermentum feugiat suspendisse viverra luctus at, semper ac consequat vitae mi gravida parturient mollis nascetur. Vel taciti justo consequat primis et blandit convallis sed, felis purus fusce a venenatis etiam aenean scelerisque, fringilla volutpat sagittis egestas rutrum id dis.</p> <p>Feugiat fermentum tortor ante ac iaculis sollicitudin ut interdum, cras orci ullamcorper potenti tristique vehicula. Molestie tortor ullamcorper rutrum turpis malesuada phasellus sem ultricies praesent mattis lobortis porta, senectus venenatis diam nostra laoreet volutpat per aptent justo elementum cum. Urna cursus vel felis cras eleifend arcu enim magnis, duis rutrum nibh nascetur cubilia interdum ultrices curae, id lacus aliquam dictumst diam fringilla lacinia.</p> <p>Luctus diam morbi eget tellus libero taciti faucibus inceptos, natoque facilisis lectus maecenas risus dapibus suscipit nibh, vel curae conubia orci imperdiet metus fusce. Condimentum massa donec luctus pharetra cum, in viverra placerat nisl litora facilisis, neque nascetur sociis dictumst. Suscipit accumsan eget rhoncus pharetra justo malesuada aliquet, suspendisse metus eleifend tincidunt varius ridiculus, convallis primis vitae curabitur quis mus.</p> <p>Gravida donec lacus molestie tortor aenean ultricies blandit per tempor, nostra penatibus orci vestibulum semper lectus vel a, montes potenti cum dapibus natoque eu volutpat nulla. Himenaeos purus nam malesuada habitasse nisl pharetra laoreet feugiat mi non, ultrices ultricies a cras ante eu venenatis ligula. Suscipit ut mus habitasse at aliquet sodales commodo justo, feugiat platea sagittis phasellus eleifend pellentesque interdum iaculis, integer cubilia montes metus hendrerit tincidunt purus.</p> <p>Vel posuere tellus dapibus eget duis cubilia, nec class vehicula libero gravida ligula, tempus urna taciti donec congue. Facilisis ridiculus congue cum dui per augue natoque, molestie hac etiam pellentesque dignissim urna class, feugiat aenean massa himenaeos penatibus ut eu, convallis purus et fusce tempus mattis. At mattis suscipit porta nostra nec facilisis sodales turpis, integer et lectus conubia justo nam congue taciti odio, fermentum semper cubilia fusce nunc purus velit.\n" +
        "      <div/>\n" +
        "    ";
      page.setContent(content);
      headlessPage.setContent(content);
      byte[] image1 = page.screenshot();
      byte[] image2 = headlessPage.screenshot();
      int count = ImageComparator.countDifferentPixels(image1, image2);
      assertEquals(0, count);
    }
    headlessBrowser.close();
  }

  @PlaywrightTest
  @DisplayName("HE-016 should have the same hyphen rendering on headless and headed")
  void shouldHaveTheSameHyphenRenderingOnHeadlessAndHeaded(BrowserType browserType, Page page, TestServer server) {
    // issue: https://github.com/microsoft/playwright/issues/33590
    String content = "\n" +
      "    <!DOCTYPE html>\n" +
      "    <html lang=\"en\">\n" +
      "    <head>\n" +
      "      <style>\n" +
      "        .hyphenated {\n" +
      "          width: 100px;\n" +
      "          hyphens: auto;\n" +
      "          text-align: justify;\n" +
      "          border: 1px solid black;\n" +
      "        }\n" +
      "      </style>\n" +
      "    </head>\n" +
      "    <body>\n" +
      "      <div class=\"hyphenated\">\n" +
      "        supercalifragilisticexpialidocious\n" +
      "      </div>\n" +
      "    </body>\n" +
      "    </html>\n" +
      "  ";
    server.setRoute("/hyphenated.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end(content);
    });
    // headless is false in this file, so the opposite browser is headless.
    Browser oppositeBrowser = browserType.launch(Env.launchOptions().setHeadless(true));
    Page oppositePage = oppositeBrowser.newPage();
    oppositePage.navigate(server.PREFIX + "/hyphenated.html");
    page.navigate(server.PREFIX + "/hyphenated.html");

    Object divHeight1 = page.evaluate("() => document.querySelector('.hyphenated').getBoundingClientRect().height");
    Object divHeight2 = oppositePage.evaluate("() => document.querySelector('.hyphenated').getBoundingClientRect().height");
    assertEquals(divHeight1, divHeight2);
    oppositeBrowser.close();
  }
}
