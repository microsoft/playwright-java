/**
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

package com.microsoft.playwright.tests.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.parseZip;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/tracing.spec.ts. */
public class TestTracing {
  // Java: the client sends the stack frames of the calls, and so the sources of the trace, only when
  // PLAYWRIGHT_JAVA_SRC is set; the Node.js client always sends them.
  @Fixture(scope = Scope.WORKER)
  static Playwright.CreateOptions createOptions(Playwright.CreateOptions createOptions) {
    Map<String, String> env = new HashMap<>(createOptions.env);
    env.put("PLAYWRIGHT_JAVA_SRC", Env.srcTestDir().resolve("java").toString());
    return new Playwright.CreateOptions().setEnv(env);
  }

  // Java: the events and the resources of parseTraceRaw() of upstream's tests/config/utils.ts; the
  // titles of the actions (renderFullTitleForCall) and the stacks of the trace model are not available.
  private static final class Trace {
    final List<JsonObject> events = new ArrayList<>();
    final Map<String, byte[]> resources;

    Trace(Map<String, byte[]> resources) {
      this.resources = resources;
    }
  }

  private static Trace parseTraceRaw(Path file) {
    Trace trace = new Trace(parseZip(file));
    for (String name : trace.resources.keySet()) {
      if (name.endsWith(".trace")) {
        addLines(trace, name);
      }
    }
    for (String name : trace.resources.keySet()) {
      if (name.endsWith(".network")) {
        addLines(trace, name);
      }
    }
    return trace;
  }

  private static void addLines(Trace trace, String name) {
    for (String line : new String(trace.resources.get(name), StandardCharsets.UTF_8).split("\n")) {
      if (!line.isEmpty()) {
        trace.events.add(JsonParser.parseString(line).getAsJsonObject());
      }
    }
  }

  // Java: e.type, e.method, e.callId: a missing property is null, like undefined.
  private static String str(JsonObject e, String key) {
    return e.has(key) && !e.get(key).isJsonNull() ? e.get(key).getAsString() : null;
  }

  private static boolean isType(JsonObject e, String type) {
    return type.equals(str(e, "type"));
  }

  private static JsonObject snapshot(JsonObject e) {
    return e.getAsJsonObject("snapshot");
  }

  private static boolean isResourceSnapshotEndingWith(JsonObject e, String suffix) {
    return isType(e, "resource-snapshot") && str(snapshot(e).getAsJsonObject("request"), "url").endsWith(suffix);
  }

  private static boolean some(List<JsonObject> events, String type) {
    for (JsonObject e : events) {
      if (isType(e, type)) {
        return true;
      }
    }
    return false;
  }

  private static BufferedImage decodeImage(byte[] buffer) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(buffer));
  }

  // TR-001 and TR-002 need the titles of the actions of the trace: TODO harness.

  @PlaywrightTest
  @DisplayName("TR-003 should not store secrets in context options")
  @Fixme("driver: the context-options event of driver 1.64.1 contains extraHTTPHeaders and httpCredentials (was: {viewport, userAgent, extraHTTPHeaders:[{name:x-secret,value:secret-header}], httpCredentials:[...], ...}); upstream fix(tracing): do not store secrets in trace context options (#43220) is not in the driver")
  void shouldNotStoreSecretsInContextOptions(Browser browser, TestServer server, TestInfo testInfo) throws Exception {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setBaseURL(server.PREFIX)
      .setViewportSize(500, 600)
      .setDeviceScaleFactor(2)
      .setIsMobile(false)
      .setUserAgent("custom-ua")
      .setHttpCredentials("user", "secret-password")
      .setExtraHTTPHeaders(java.util.Collections.singletonMap("x-secret", "secret-header"))
      .setStorageState("{\"cookies\": [{\"name\": \"session\", \"value\": \"secret-cookie\", \"domain\": \"localhost\", \"path\": \"/\", \"expires\": -1, \"httpOnly\": false, \"secure\": false, \"sameSite\": \"Lax\"}], \"origins\": []}"));
    context.tracing().start();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));
    context.close();

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    assertEquals("context-options", str(trace.events.get(0), "type"));
    JsonObject expectedOptions = new JsonObject();
    expectedOptions.addProperty("baseURL", server.PREFIX);
    JsonObject viewport = new JsonObject();
    viewport.addProperty("width", 500);
    viewport.addProperty("height", 600);
    expectedOptions.add("viewport", viewport);
    expectedOptions.addProperty("deviceScaleFactor", 2);
    expectedOptions.addProperty("isMobile", false);
    expectedOptions.addProperty("userAgent", "custom-ua");
    assertEquals(expectedOptions, trace.events.get(0).get("options"));
    String traceText = new String(Files.readAllBytes(testInfo.outputPath("trace.zip")), StandardCharsets.ISO_8859_1);
    assertFalse(traceText.contains("secret-password"));
    assertFalse(traceText.contains("secret-header"));
    assertFalse(traceText.contains("secret-cookie"));
  }

  @PlaywrightTest
  @DisplayName("TR-004 should not collect snapshots by default")
  void shouldNotCollectSnapshotsByDefault(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().start();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("\"Click\"");
    page.close();
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    assertFalse(some(trace.events, "frame-snapshot"));
    assertFalse(some(trace.events, "resource-snapshot"));
  }

  @PlaywrightTest
  @DisplayName("TR-005 should not collect action screenshots and aria snapshots by default")
  void shouldNotCollectActionScreenshotsAndAriaSnapshotsByDefault(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    assertFalse(some(trace.events, "screenshot"));
    assertFalse(some(trace.events, "aria-snapshot"));
  }

  @PlaywrightTest
  @DisplayName("TR-006 should collect action screenshots")
  void shouldCollectActionScreenshots(BrowserContext context, Page page, TestServer server, TestInfo testInfo) throws Exception {
    // Java: snapshots: { screen: true } is the screenSnapshots option.
    context.tracing().start(new Tracing.StartOptions().setScreenSnapshots(true));
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    String clickCallId = null;
    for (JsonObject e : trace.events) {
      if (isType(e, "before") && "click".equals(str(e, "method"))) {
        clickCallId = str(e, "callId");
        break;
      }
    }
    List<JsonObject> screenshots = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if (isType(e, "screenshot") && clickCallId.equals(str(e, "callId"))) {
        screenshots.add(e);
      }
    }
    List<String> phases = new ArrayList<>();
    for (JsonObject e : screenshots) {
      phases.add(str(e, "phase"));
    }
    assertEquals(Arrays.asList("before", "action", "after"), phases);
    for (JsonObject screenshot : screenshots) {
      assertEquals("screenshots/" + clickCallId + "-" + str(screenshot, "phase") + ".png", str(screenshot, "file"));
      byte[] buffer = trace.resources.get(str(screenshot, "file"));
      assertTrue(decodeImage(buffer).getWidth() > 0);
    }
  }

  // Java: const hasButton = nodes => nodes.some(node => typeof node === 'object' && (node.role === 'button' && node.name === 'Click target' || hasButton(node.children ?? [])));
  private static boolean hasButton(JsonArray nodes) {
    for (JsonElement element : nodes) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject node = element.getAsJsonObject();
      if ("button".equals(str(node, "role")) && "Click target".equals(str(node, "name"))) {
        return true;
      }
      if (hasButton(node.has("children") ? node.getAsJsonArray("children") : new JsonArray())) {
        return true;
      }
    }
    return false;
  }

  @PlaywrightTest
  @DisplayName("TR-007 should collect aria snapshots")
  void shouldCollectAriaSnapshots(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().start(new Tracing.StartOptions().setAriaSnapshots(true));
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    String clickCallId = null;
    for (JsonObject e : trace.events) {
      if (isType(e, "before") && "click".equals(str(e, "method"))) {
        clickCallId = str(e, "callId");
        break;
      }
    }
    List<JsonObject> ariaSnapshots = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if (isType(e, "aria-snapshot") && clickCallId.equals(str(e, "callId"))) {
        ariaSnapshots.add(e);
      }
    }
    List<String> phases = new ArrayList<>();
    for (JsonObject e : ariaSnapshots) {
      phases.add(str(e, "phase"));
    }
    assertEquals(Arrays.asList("before", "action", "after"), phases);
    for (JsonObject ariaSnapshot : ariaSnapshots) {
      assertEquals("aria/" + clickCallId + "-" + str(ariaSnapshot, "phase") + ".json", str(ariaSnapshot, "file"));
      JsonArray snapshot = JsonParser.parseString(new String(trace.resources.get(str(ariaSnapshot, "file")), StandardCharsets.UTF_8)).getAsJsonArray();
      assertEquals(true, hasButton(snapshot));
    }
  }

  @PlaywrightTest
  @DisplayName("TR-008 can call tracing.group/groupEnd at any time and auto-close")
  void canCallTracingGroupGroupEndAtAnyTimeAndAutoClose(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().group("ignored");
    context.tracing().groupEnd();
    context.tracing().group("ignored2");

    context.tracing().start();
    context.tracing().group("actual");
    page.navigate(server.EMPTY_PAGE);
    context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(testInfo.outputPath("trace.zip")));

    context.tracing().group("ignored3");
    context.tracing().groupEnd();
    context.tracing().groupEnd();
    context.tracing().groupEnd();

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    List<JsonObject> groups = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if ("tracingGroup".equals(str(e, "method"))) {
        groups.add(e);
      }
    }
    assertEquals(1, groups.size());
    assertEquals("actual", str(groups.get(0), "title"));
    boolean hasAfter = false;
    for (JsonObject e : trace.events) {
      if (isType(e, "after") && str(groups.get(0), "callId").equals(str(e, "callId"))) {
        hasAfter = true;
      }
    }
    assertEquals(true, hasAfter);
  }

  @PlaywrightTest
  @DisplayName("TR-009 should not include buffers in the trace")
  void shouldNotIncludeBuffersInTheTrace(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    page.navigate(server.PREFIX + "/empty.html");
    page.screenshot();
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));
    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    // Java: the screenshot action is the "before" event with its "after" event (actionObjects of parseTraceRaw).
    JsonObject screenshotEvent = null;
    for (JsonObject e : trace.events) {
      if (isType(e, "before") && "screenshot".equals(str(e, "method"))) {
        screenshotEvent = e;
        break;
      }
    }
    JsonObject after = null;
    for (JsonObject e : trace.events) {
      if (isType(e, "after") && str(screenshotEvent, "callId").equals(str(e, "callId"))) {
        after = e;
      }
    }
    List<String> phases = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if (isType(e, "frame-snapshot") && str(screenshotEvent, "callId").equals(str(snapshot(e), "callId"))) {
        phases.add(str(snapshot(e), "phase"));
      }
    }
    assertTrue(phases.contains("before"));
    assertTrue(phases.contains("after"));
    assertEquals(JsonParser.parseString("{\"binary\": \"<Buffer>\"}"), after.get("result"));
  }

  @PlaywrightTest
  @DisplayName("TR-010 should exclude internal pages")
  void shouldExcludeInternalPages(BrowserName browserName, BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    page.navigate(server.EMPTY_PAGE);

    context.tracing().start();
    context.storageState();
    page.close();
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
    Set<String> pageIds = new HashSet<>();
    for (JsonObject e : trace.events) {
      // Java: e.pageId ?? e.params?.pageId
      String pageId = str(e, "pageId");
      if (pageId == null && e.has("params") && e.get("params").isJsonObject()) {
        pageId = str(e.getAsJsonObject("params"), "pageId");
      }
      if (pageId != null && !pageId.isEmpty()) {
        pageIds.add(pageId);
      }
    }
    assertEquals(1, pageIds.size());
  }

  // TR-011, TR-012 and TR-013 need the titles of the actions of the trace: TODO harness.

  @PlaywrightTest
  @DisplayName("TR-014 should not include trace resources from the previous chunks")
  void shouldNotIncludeTraceResourcesFromThePreviousChunks(BrowserContext context, Page page, TestServer server, BrowserName browserName, TestInfo testInfo) {
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));

    context.tracing().startChunk();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <style>\n" +
      "      @keyframes move {\n" +
      "        from { marign-left: 0; }\n" +
      "        to   { margin-left: 1000px; }\n" +
      "      }\n" +
      "      button {\n" +
      "        animation: 20s linear move;\n" +
      "        animation-iteration-count: infinite;\n" +
      "      }\n" +
      "    </style>\n" +
      "    <button>Click</button>\n" +
      "  ");
    page.click("\"Click\"", new Page.ClickOptions().setForce(true));
    // Give it enough time for both screenshots to get into the trace.
    page.waitForTimeout(3000);
    context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(testInfo.outputPath("trace1.zip")));

    context.tracing().startChunk();
    context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(testInfo.outputPath("trace2.zip")));

    List<String> jpegs = new ArrayList<>();
    {
      Trace trace = parseTraceRaw(testInfo.outputPath("trace1.zip"));
      List<String> names = new ArrayList<>(trace.resources.keySet());
      assertEquals(1, count(names, n -> n.endsWith(".html")));
      for (String n : names) {
        if (n.endsWith(".jpeg")) {
          jpegs.add(n);
        }
      }
      // 1 source file for the test.
      assertEquals(1, count(names, n -> n.startsWith("src/")));
    }

    {
      Trace trace = parseTraceRaw(testInfo.outputPath("trace2.zip"));
      List<String> names = new ArrayList<>(trace.resources.keySet());
      // 1 network resource should be preserved.
      assertEquals(1, count(names, n -> n.endsWith(".html")));
      // screenshots from the previous chunk should not be preserved.
      assertEquals(0, count(names, n -> jpegs.contains(n)));
      // 0 source files for the second test.
      assertEquals(0, count(names, n -> n.startsWith("src/")));
    }
  }

  // Java: names.filter(predicate).length
  private static int count(List<String> names, java.util.function.Predicate<String> predicate) {
    int result = 0;
    for (String name : names) {
      if (predicate.test(name)) {
        result++;
      }
    }
    return result;
  }

  @PlaywrightTest
  @DisplayName("TR-015 should overwrite existing file")
  void shouldOverwriteExistingFile(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("\"Click\"");
    Path path = testInfo.outputPath("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(path));
    {
      Trace trace = parseTraceRaw(path);
      List<String> names = new ArrayList<>(trace.resources.keySet());
      assertEquals(1, count(names, n -> n.endsWith(".html")));
    }

    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
    context.tracing().stop(new Tracing.StopOptions().setPath(path));

    {
      Trace trace = parseTraceRaw(path);
      List<String> names = new ArrayList<>(trace.resources.keySet());
      assertEquals(0, count(names, n -> n.endsWith(".html")));
    }
  }

  @PlaywrightTest
  @DisplayName("TR-016 should collect sources")
  void shouldCollectSources(BrowserContext context, Page page, TestServer server, TestInfo testInfo) throws Exception {
    context.tracing().start(new Tracing.StartOptions().setSources(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("\"Click\"");
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace1.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace1.zip"));
    List<String> sourceNames = new ArrayList<>();
    for (String name : trace.resources.keySet()) {
      if (name.startsWith("src/")) {
        sourceNames.add(name);
      }
    }
    assertEquals(1, sourceNames.size());
    // Java: the source file is this Java file instead of tracing.spec.ts.
    assertTrue(Pattern.compile("^src/[0-9a-f]{40}\\.java$").matcher(sourceNames.get(0)).find());
    byte[] sourceFile = trace.resources.get(sourceNames.get(0));
    byte[] thisFile = Files.readAllBytes(Env.srcTestDir().resolve("java/com/microsoft/playwright/tests/library/TestTracing.java"));
    assertTrue(Arrays.equals(thisFile, sourceFile));
  }

  @PlaywrightTest
  @DisplayName("TR-017 should record network failures")
  void shouldRecordNetworkFailures(BrowserContext context, Page page, TestServer server, TestInfo testInfo) {
    context.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    page.route("**/*", route -> route.abort("connectionaborted"));
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
    }
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace1.zip")));

    Trace trace = parseTraceRaw(testInfo.outputPath("trace1.zip"));
    JsonObject requestEvent = null;
    for (JsonObject e : trace.events) {
      if (isType(e, "resource-snapshot") && snapshot(e).getAsJsonObject("response").has("_failureText")
        && !snapshot(e).getAsJsonObject("response").get("_failureText").getAsString().isEmpty()) {
        requestEvent = e;
        break;
      }
    }
    assertNotNull(requestEvent);
    assertTrue(snapshot(requestEvent).get("_monotonicTime").getAsDouble() > 0);
    assertTrue(snapshot(requestEvent).get("time").getAsDouble() >= 0);
  }

  // TR-018 needs the titles of the actions of the trace: TODO harness.
  // TR-019 reads the _stacksId of the Node.js tracing client: not ported.

  @PlaywrightTest
  @DisplayName("TR-020 should not crash when browser closes mid-trace")
  void shouldNotCrashWhenBrowserClosesMidTrace(BrowserType browserType, TestServer server, TestInfo testInfo) throws Exception {
    Browser browser = browserType.launch(Env.launchOptions());
    Page page = browser.newPage();
    page.context().tracing().start(new Tracing.StartOptions().setSnapshots(true).setScreenshots(true));
    page.navigate(server.EMPTY_PAGE);
    browser.close();
    Thread.sleep(1000);  // Give it some time to throw errors
  }

  // TR-021 sets the _defaultTracesDir of the Node.js client: not ported.

  @PlaywrightTest
  @DisplayName("TR-022 should not stall on dialogs")
  void shouldNotStallOnDialogs(Page page, BrowserContext context, TestServer server) {
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
    page.navigate(server.EMPTY_PAGE);

    page.onDialog(dialog -> {
      dialog.accept();
    });

    page.evaluate("() => {\n" +
      "    confirm('are you sure');\n" +
      "  }");
    context.tracing().stop();
  }

  @Nested
  @ParameterizedClass
  @CsvSource({
    "fit, 500, 500",
    "crop, 400, 800",
    "scale, 1024, 768"
  })
  class ShouldProduceScreencastFrames {
    @Parameter(0)
    String id;
    @Parameter(1)
    int width;
    @Parameter(2)
    int height;

    @PlaywrightTest
    @DisplayName("TR-023 should produce screencast frames ${params.id}")
    void shouldProduceScreencastFramesParamsid(ContextFactory contextFactory, BrowserName browserName, TestInfo testInfo) throws Exception {
      assumeFalse(browserName == BrowserName.CHROMIUM && !Env.isHeadlessShell(browserName), "fixme: Chromium (but not headless-shell) screencast has a min width issue");
      assumeFalse(id.equals("fit") && browserName == BrowserName.CHROMIUM && Env.platform().equals("darwin"), "fixme: High DPI maxes image at 600x600");
      assumeFalse(id.equals("fit") && browserName == BrowserName.WEBKIT && Env.platform().equals("linux"), "fixme: Image size is flaky");
      assumeFalse(browserName == BrowserName.FIREFOX && Env.headed(), "fixme: Image size is different");

      double scale = Math.min(Math.min(800.0 / width, 800.0 / height), 1);
      double previewWidth = width * scale;
      double previewHeight = height * scale;

      BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setViewportSize(width, height));
      context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
      Page page = context.newPage();
      // Make sure we have a chance to paint.
      for (int i = 0; i < 100; ++i) {
        String percentage = (i + 1) + "%";
        page.setContent("<body style=\"box-sizing: border-box; width: 100%; height: 100%; margin:0; background: linear-gradient(to bottom, red " + percentage + ", transparent " + percentage + "); border: 50px solid blue\"></body>");
        rafraf(page);
      }
      for (int i = 0; i < 10; ++i)
        rafraf(page);
      context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip")));

      Trace trace = parseTraceRaw(testInfo.outputPath("trace.zip"));
      List<JsonObject> frames = new ArrayList<>();
      for (JsonObject e : trace.events) {
        if (isType(e, "screencast-frame")) {
          frames.add(e);
        }
      }

      // Check all frame sizes.
      for (JsonObject frame : frames) {
        assertEquals(width, frame.get("width").getAsInt());
        assertEquals(height, frame.get("height").getAsInt());
        byte[] buffer = trace.resources.get(str(frame, "file"));
        BufferedImage image = decodeImage(buffer);
        assertEquals(previewWidth, (double) image.getWidth());
        assertEquals(previewHeight, (double) image.getHeight());
      }

      JsonObject frame = frames.get(frames.size() - 1); // pick last frame.
      byte[] buffer = trace.resources.get(str(frame, "file"));
      BufferedImage image = decodeImage(buffer);
      // Java: image.data of jpegjs, the RGBA pixels.
      byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          int argb = image.getRGB(x, y);
          int offset = (y * image.getWidth() + x) * 4;
          data[offset] = (byte) (argb >> 16);
          data[offset + 1] = (byte) (argb >> 8);
          data[offset + 2] = (byte) argb;
          data[offset + 3] = (byte) 255;
        }
      }
      assertEquals(previewWidth * previewHeight * 4, (double) data.length);
      expectRed(data, (int) (previewWidth * previewHeight * 4 / 2 + previewWidth * 4 / 2)); // center is red
      expectBlue(data, (int) (previewWidth * 5 * 4 + previewWidth * 4 / 2)); // top
      expectBlue(data, (int) (previewWidth * (previewHeight - 5) * 4 + previewWidth * 4 / 2)); // bottom
      expectBlue(data, (int) (previewWidth * previewHeight * 4 / 2 + 5 * 4)); // left
      expectBlue(data, (int) (previewWidth * previewHeight * 4 / 2 + (previewWidth - 5) * 4)); // right
    }
  }

  // TR-024 needs the titles of the actions of the trace: TODO harness.

  @PlaywrightTest
  @DisplayName("TR-025 should throw when starting with different options")
  void shouldThrowWhenStartingWithDifferentOptions(BrowserContext context) {
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> context.tracing().start(new Tracing.StartOptions().setScreenshots(false).setSnapshots(false)));
    assertTrue(error.getMessage().contains("Tracing has been already started"));
  }

  @PlaywrightTest
  @DisplayName("TR-026 should throw when stopping without start")
  void shouldThrowWhenStoppingWithoutStart(BrowserContext context, TestInfo testInfo) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip"))));
    assertTrue(error.getMessage().contains("Must start tracing before stopping"));
  }

  @PlaywrightTest
  @DisplayName("TR-027 should not throw when stopping without start but not exporting")
  void shouldNotThrowWhenStoppingWithoutStartButNotExporting(BrowserContext context, TestInfo testInfo) {
    context.tracing().stop();
  }

  @PlaywrightTest
  @DisplayName("TR-028 start should return a disposable that discards the trace")
  void startShouldReturnADisposableThatDiscardsTheTrace(BrowserContext context, Page page, TestServer server, TestInfo testInfo) throws Exception {
    AutoCloseable disposable = context.tracing().start();
    page.navigate(server.EMPTY_PAGE);
    disposable.close();
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace.zip"))));
    assertTrue(error.getMessage().contains("Must start tracing before stopping"));
  }

  @PlaywrightTest
  @DisplayName("TR-029 startChunk should return a disposable that discards the chunk")
  void startChunkShouldReturnADisposableThatDiscardsTheChunk(BrowserContext context, Page page, TestServer server, TestInfo testInfo) throws Exception {
    context.tracing().start();
    AutoCloseable disposable = context.tracing().startChunk();
    page.navigate(server.EMPTY_PAGE);
    disposable.close();
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(testInfo.outputPath("trace.zip"))));
    assertTrue(error.getMessage().contains("Must start tracing before stopping"));
  }

  // TR-030 needs the titles of the actions of the trace: TODO harness.

  @PlaywrightTest
  @DisplayName("TR-032 should not hang for clicks that open dialogs")
  void shouldNotHangForClicksThatOpenDialogs(BrowserContext context, Page page) {
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
    // Java: page.waitForEvent('dialog') is a handler that keeps the dialog.
    CompletableFuture<Dialog> dialogPromise = new CompletableFuture<>();
    page.onDialog(dialog -> dialogPromise.complete(dialog));
    page.setContent("<div onclick='window.alert(123)'>Click me</div>");
    try {
      page.click("div", new Page.ClickOptions().setTimeout(3500));
    } catch (PlaywrightException e) {
    }
    page.waitForCondition(dialogPromise::isDone);
    Dialog dialog = dialogPromise.join();
    dialog.dismiss();
    context.tracing().stop();
  }

  // TR-033, TR-034 and TR-035 need the titles of the actions and the stacks of the trace: TODO harness.

  @PlaywrightTest
  @DisplayName("TR-036 should record global request trace")
  void shouldRecordGlobalRequestTrace(Playwright playwright, TestServer server, TestInfo testInfo) {
    // Java: the request fixture.
    APIRequestContext request = playwright.request().newContext();
    request.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    String url = server.PREFIX + "/simple.json";
    request.get(url);
    Path tracePath = testInfo.outputPath("trace.zip");
    request.tracing().stop(new Tracing.StopOptions().setPath(tracePath));

    Trace trace = parseTraceRaw(tracePath);
    List<JsonObject> actions = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if (isType(e, "resource-snapshot")) {
        actions.add(e);
      }
    }
    assertEquals(1, actions.size());
    assertEquals("GET", str(snapshot(actions.get(0)).getAsJsonObject("request"), "method"));
    assertEquals(url, str(snapshot(actions.get(0)).getAsJsonObject("request"), "url"));
    JsonObject response = snapshot(actions.get(0)).getAsJsonObject("response");
    assertEquals(200, response.get("status").getAsInt());
    assertEquals("OK", str(response, "statusText"));
    boolean hasContentLength = false;
    for (JsonElement header : response.getAsJsonArray("headers")) {
      if ("Content-Length".equals(str(header.getAsJsonObject(), "name")) && "15".equals(str(header.getAsJsonObject(), "value"))) {
        hasContentLength = true;
      }
    }
    assertTrue(hasContentLength);
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("TR-037 should store global request traces separately")
  void shouldStoreGlobalRequestTracesSeparately(Playwright playwright, TestServer server, BrowserName browserName, TestInfo testInfo) {
    // Java: the request fixture.
    APIRequestContext request = playwright.request().newContext();
    APIRequestContext request2 = playwright.request().newContext();
    request.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    request2.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    String url = server.PREFIX + "/simple.json";
    request.get(url);
    request2.post(url);
    Path tracePath = testInfo.outputPath("trace.zip");
    Path trace2Path = testInfo.outputPath("trace2.zip");
    request.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
    request2.tracing().stop(new Tracing.StopOptions().setPath(trace2Path));
    {
      Trace trace = parseTraceRaw(tracePath);
      List<JsonObject> actions = new ArrayList<>();
      for (JsonObject e : trace.events) {
        if (isType(e, "resource-snapshot")) {
          actions.add(e);
        }
      }
      assertEquals(1, actions.size());
      assertEquals("GET", str(snapshot(actions.get(0)).getAsJsonObject("request"), "method"));
      assertEquals(url, str(snapshot(actions.get(0)).getAsJsonObject("request"), "url"));
    }
    {
      Trace trace = parseTraceRaw(trace2Path);
      List<JsonObject> actions = new ArrayList<>();
      for (JsonObject e : trace.events) {
        if (isType(e, "resource-snapshot")) {
          actions.add(e);
        }
      }
      assertEquals(1, actions.size());
      assertEquals("POST", str(snapshot(actions.get(0)).getAsJsonObject("request"), "method"));
      assertEquals(url, str(snapshot(actions.get(0)).getAsJsonObject("request"), "url"));
    }
    request.dispose();
    request2.dispose();
  }

  @PlaywrightTest
  @DisplayName("TR-038 should store postData for global request")
  void shouldStorePostDataForGlobalRequest(Playwright playwright, TestServer server, TestInfo testInfo) {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/15031' }
    // Java: the request fixture.
    APIRequestContext request = playwright.request().newContext();
    request.tracing().start(new Tracing.StartOptions().setSnapshots(true));
    String url = server.PREFIX + "/simple.json";
    request.post(url, RequestOptions.create().setData("test"));
    Path tracePath = testInfo.outputPath("trace.zip");
    request.tracing().stop(new Tracing.StopOptions().setPath(tracePath));

    Trace trace = parseTraceRaw(tracePath);
    List<JsonObject> actions = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if (isType(e, "resource-snapshot")) {
        actions.add(e);
      }
    }
    assertEquals(1, actions.size());
    JsonObject req = snapshot(actions.get(0)).getAsJsonObject("request");
    assertTrue(req.has("postData") && req.getAsJsonObject("postData").has("_file")
      && !req.getAsJsonObject("postData").get("_file").getAsString().isEmpty());
    assertEquals("POST", str(req, "method"));
    assertEquals(url, str(req, "url"));
    request.dispose();
  }

  // TR-039 reads the trace files of the test runner's artifacts folder: TODO harness.

  @PlaywrightTest
  @DisplayName("TR-040 should flush console events on tracing stop")
  void shouldFlushConsoleEventsOnTracingStop(BrowserContext context, Page page, TestInfo testInfo) {
    context.tracing().start();
    CompletableFuture<Void> promise = new CompletableFuture<>();
    AtomicInteger counter = new AtomicInteger();
    page.onConsoleMessage(message -> {
      if (counter.incrementAndGet() == 100)
        promise.complete(null);
    });
    page.evaluate("() => {\n" +
      "    window.builtins.setTimeout(() => {\n" +
      "      for (let i = 0; i < 100; ++i)\n" +
      "        console.log('hello ' + i);\n" +
      "    });\n" +
      "  }");
    page.waitForCondition(promise::isDone);
    Path tracePath = testInfo.outputPath("trace.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
    Trace trace = parseTraceRaw(tracePath);
    List<JsonObject> events = new ArrayList<>();
    for (JsonObject e : trace.events) {
      if (isType(e, "console")) {
        events.add(e);
      }
    }
    assertEquals(100, events.size());
  }

  // Java: the sanitize() function of the test, minCallId is the smallest call id seen so far.
  private static JsonObject sanitize(JsonObject e, AtomicInteger minCallId) {
    if (isType(e, "after") || isType(e, "before")) {
      minCallId.set(Math.min(minCallId.get(), Integer.parseInt(str(e, "callId").split("@")[1])));
      JsonObject result = new JsonObject();
      result.addProperty("type", str(e, "type"));
      result.addProperty("callId", Integer.parseInt(str(e, "callId").split("@")[1]) - minCallId.get());
      if (str(e, "title") != null)
        result.addProperty("title", str(e, "title"));
      return result;
    }
    return null;
  }

  private static List<JsonObject> sanitizeAll(List<JsonObject> events, AtomicInteger minCallId) {
    List<JsonObject> sanitized = new ArrayList<>();
    for (JsonObject e : events) {
      JsonObject s = sanitize(e, minCallId);
      if (s != null)
        sanitized.add(s);
    }
    return sanitized;
  }

  private static List<String> types(List<JsonObject> events) {
    List<String> types = new ArrayList<>();
    for (JsonObject e : events)
      types.add(str(e, "type"));
    return types;
  }

  private static int callId(JsonObject e) {
    return e.get("callId").getAsInt();
  }

  @PlaywrightTest
  @DisplayName("TR-041 should not emit after w/o before")
  void shouldNotEmitAfterWOBefore(BrowserType browserType, TestInfo testInfo) {
    Path tracesDir = testInfo.outputPath("traces");
    Browser browser = browserType.launch(Env.launchOptions().setTracesDir(tracesDir));
    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    context.tracing().start(new Tracing.StartOptions().setName("name1").setSnapshots(true));
    // The wait spans the chunks: its "before" is in the first chunk, and its "after" must not get into the second one.
    // Java: the calls that the wait spans go in its callback.
    page.waitForConsoleMessage(() -> {
      page.evaluate("() => 1");
      context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(testInfo.outputPath("trace1.zip")));
      assertTrue(Files.exists(tracesDir.resolve("name1.trace")));

      context.tracing().startChunk(new Tracing.StartChunkOptions().setName("name2"));
      page.evaluate("() => console.log('started')");
    });
    context.tracing().stop(new Tracing.StopOptions().setPath(testInfo.outputPath("trace2.zip")));
    assertTrue(Files.exists(tracesDir.resolve("name2.trace")));

    browser.close();
    AtomicInteger minCallId = new AtomicInteger(100000);

    int call1;
    {
      Trace trace = parseTraceRaw(testInfo.outputPath("trace1.zip"));
      List<JsonObject> sanitized = sanitizeAll(trace.events, minCallId);
      assertEquals(Arrays.asList("before", "before", "after"), types(sanitized));
      assertEquals("Wait for event \"console\"", str(sanitized.get(0), "title"));
      assertNull(str(sanitized.get(1), "title"));
      assertNull(str(sanitized.get(2), "title"));
      assertTrue(callId(sanitized.get(1)) > callId(sanitized.get(0)));
      assertEquals(callId(sanitized.get(1)), callId(sanitized.get(2)));
      call1 = callId(sanitized.get(1));
    }

    int call2before;
    int call2after;
    {
      Trace trace = parseTraceRaw(testInfo.outputPath("trace2.zip"));
      List<JsonObject> sanitized = sanitizeAll(trace.events, minCallId);
      assertEquals(Arrays.asList("before", "after"), types(sanitized));
      assertNull(str(sanitized.get(0), "title"));
      assertNull(str(sanitized.get(1), "title"));
      call2before = callId(sanitized.get(0));
      call2after = callId(sanitized.get(1));
    }
    assertTrue(call2before > call1);
    assertEquals(call2before, call2after);
  }

  @PlaywrightTest
  @DisplayName("TR-042 should save trace while a WebSocket keeps streaming frames")
  void shouldSaveTraceWhileAWebSocketKeepsStreamingFrames(BrowserContext context, Page page, TestServer server, TestInfo testInfo) throws Exception {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41351' }
    AtomicBoolean streaming = new AtomicBoolean(true);
    ScheduledExecutorService timers = Executors.newSingleThreadScheduledExecutor();
    char[] payloadChars = new char[16 * 1024];
    Arrays.fill(payloadChars, 'x');
    String payload = new String(payloadChars);
    server.onceWebSocketConnection(ws -> {
      AtomicBoolean open = new AtomicBoolean(true);
      timers.scheduleAtFixedRate(() -> {
        if (streaming.get() && open.get())
          ws.send(payload);
      }, 1, 1, TimeUnit.MILLISECONDS);
      ws.onClose((code, reason) -> open.set(false));
    });

    try {
      context.tracing().start(new Tracing.StartOptions().setSnapshots(true));

      context.tracing().startChunk();
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("url => {\n" +
        "    window.ws = new WebSocket(url);\n" +
        "    return new Promise(resolve => window.ws.addEventListener('open', () => resolve()));\n" +
        "  }", "ws://" + server.HOST + "/ws");
      page.waitForTimeout(100);
      Path tracePath1 = testInfo.outputPath("trace1.zip");
      context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(tracePath1));

      streaming.set(false);
      context.tracing().startChunk();
      page.waitForTimeout(100);
      Path tracePath2 = testInfo.outputPath("trace2.zip");
      context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(tracePath2));

      page.evaluate("() => new Promise(resolve => {\n" +
        "    const ws = window.ws;\n" +
        "    if (ws.readyState === WebSocket.CLOSED) {\n" +
        "      resolve();\n" +
        "      return;\n" +
        "    }\n" +
        "    ws.addEventListener('close', () => resolve(), { once: true });\n" +
        "    ws.close();\n" +
        "  })");

      List<List<String>> webSocketLines = new ArrayList<>();
      for (Path path : Arrays.asList(tracePath1, tracePath2)) {
        Trace trace = parseTraceRaw(path);
        byte[] websocketResource = null;
        for (Map.Entry<String, byte[]> entry : trace.resources.entrySet()) {
          if (entry.getKey().endsWith(".jsonl")) {
            websocketResource = entry.getValue();
            break;
          }
        }
        List<String> lines = new ArrayList<>();
        for (String line : new String(websocketResource, StandardCharsets.UTF_8).split("\n")) {
          if (!line.isEmpty())
            lines.add(line);
        }
        assertTrue(lines.size() > 0);
        for (String line : lines)
          JsonParser.parseString(line);
        webSocketLines.add(lines);
      }
      assertNotEquals(tracePath1, tracePath2);
      assertEquals(webSocketLines.get(1), webSocketLines.get(1));
    } finally {
      timers.shutdownNow();
    }
  }

  private static void expectRed(byte[] pixels, int offset) {
    int r = pixels[offset] & 0xff;
    int g = pixels[offset + 1] & 0xff;
    int b = pixels[offset + 2] & 0xff;
    int a = pixels[offset + 3] & 0xff;
    assertTrue(r > 200);
    assertTrue(g < 70);
    assertTrue(b < 70);
    assertEquals(255, a);
  }

  private static void expectBlue(byte[] pixels, int offset) {
    int r = pixels[offset] & 0xff;
    int g = pixels[offset + 1] & 0xff;
    int b = pixels[offset + 2] & 0xff;
    int a = pixels[offset + 3] & 0xff;
    assertTrue(r < 70);
    assertTrue(g < 70);
    assertTrue(b > 200);
    assertEquals(255, a);
  }
}
