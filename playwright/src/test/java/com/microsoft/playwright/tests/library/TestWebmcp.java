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

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebMCP;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WebMCPTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/webmcp.spec.ts. */
@DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "WebKit does not implement WebMCP")
public class TestWebmcp {
  @Fixture(scope = Scope.WORKER)
  static BrowserType.LaunchOptions launchOptions(BrowserType.LaunchOptions launchOptions, BrowserName browserName) {
    if (browserName == BrowserName.FIREFOX) {
      Map<String, Object> firefoxUserPrefs = new HashMap<>();
      if (launchOptions.firefoxUserPrefs != null) {
        firefoxUserPrefs.putAll(launchOptions.firefoxUserPrefs);
      }
      firefoxUserPrefs.put("dom.modelcontext.enabled", true);
      firefoxUserPrefs.put("dom.modelcontext.testing.enabled", true);
      return launchOptions.setFirefoxUserPrefs(firefoxUserPrefs);
    }
    List<String> args = new ArrayList<>();
    if (launchOptions.args != null) {
      args.addAll(launchOptions.args);
    }
    args.add("--enable-features=WebMCP");
    return launchOptions.setArgs(args);
  }

  @BeforeEach
  void skipOnBidi() {
    assumeFalse(Env.isBidi(), "WebMCP is not implemented over BiDi");
  }

  private static String registerScript(String tools) {
    return "<script>\n" +
      "    const modelContext = document.modelContext || navigator.modelContext;\n" +
      "    " + tools + "\n" +
      "  </script>";
  }

  private static final String kAdd = "\n" +
    "  modelContext.registerTool({\n" +
    "    name: 'add',\n" +
    "    description: 'Adds two numbers',\n" +
    "    inputSchema: { type: 'object', properties: { a: { type: 'number' }, b: { type: 'number' } }, required: ['a', 'b'] },\n" +
    "    annotations: { readOnlyHint: true },\n" +
    "    async execute(input) { return { content: [{ type: 'text', text: String(input.a + input.b) }] }; },\n" +
    "  });\n";

  private static final Map<String, Object> kAddSchema = mapOf(
    "type", "object",
    "properties", mapOf("a", mapOf("type", "number"), "b", mapOf("type", "number")),
    "required", asList("a", "b"));

  private static void serve(TestServer server, String path, String body) {
    server.setRoute(path, (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<title>WebMCP</title>" + body);
    });
  }

  // Java: toEqual() of a tool without inputSchema and annotations.
  private static void assertPlainTool(String name, String description, WebMCPTool tool) {
    assertEquals(name, tool.name);
    assertEquals(description, tool.description);
    assertNull(tool.inputSchema);
    assertNull(tool.annotations);
  }

  // Java: sort((a, b) => a.name.localeCompare(b.name))
  private static List<WebMCPTool> sortedByName(List<WebMCPTool> tools) {
    List<WebMCPTool> sorted = new ArrayList<>(tools);
    sorted.sort((a, b) -> a.name.compareTo(b.name));
    return sorted;
  }

  private static Map<String, Object> textResult(String text) {
    return mapOf("content", asList(mapOf("type", "text", "text", text)));
  }

  @PlaywrightTest
  @DisplayName("WM-001 should list tools registered by the page")
  void shouldListToolsRegisteredByThePage(Page page, TestServer server) {
    serve(server, "/", registerScript(kAdd + "\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'noop',\n" +
      "      description: 'Does nothing',\n" +
      "      async execute() {},\n" +
      "    });\n" +
      "  "));
    page.navigate(server.PREFIX + "/");

    // Browsers list tools in their own order.
    List<WebMCPTool> tools = sortedByName(page.webmcp().tools());
    assertEquals(2, tools.size());
    assertEquals("add", tools.get(0).name);
    assertEquals("Adds two numbers", tools.get(0).description);
    assertEquals(kAddSchema, tools.get(0).inputSchema);
    assertEquals(true, tools.get(0).annotations.readOnly);
    assertNull(tools.get(0).annotations.untrustedContent);
    assertNull(tools.get(0).annotations.consequential);
    assertPlainTool("noop", "Does nothing", tools.get(1));
  }

  @PlaywrightTest
  @DisplayName("WM-002 should report no tools when the page registers none")
  void shouldReportNoToolsWhenThePageRegistersNone(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(new ArrayList<>(), page.webmcp().tools());
  }

  @PlaywrightTest
  @DisplayName("WM-003 should call a tool")
  void shouldCallATool(Page page, TestServer server) {
    serve(server, "/", registerScript(kAdd));
    page.navigate(server.PREFIX + "/");

    assertEquals(textResult("42"), page.webmcp().callTool("add", mapOf("a", 2, "b", 40)));
  }

  @PlaywrightTest
  @DisplayName("WM-004 should return isError results as is")
  void shouldReturnIsErrorResultsAsIs(Page page, TestServer server) {
    serve(server, "/", registerScript("\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'broken',\n" +
      "      description: 'Always fails',\n" +
      "      async execute() { return { content: [{ type: 'text', text: 'nope' }], isError: true }; },\n" +
      "    });\n" +
      "  "));
    page.navigate(server.PREFIX + "/");

    assertEquals(mapOf("content", asList(mapOf("type", "text", "text", "nope")), "isError", true), page.webmcp().callTool("broken"));
  }

  @PlaywrightTest
  @DisplayName("WM-005 should throw when the tool throws")
  void shouldThrowWhenTheToolThrows(Page page, TestServer server, BrowserName browserName) {
    serve(server, "/", registerScript("\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'throwing',\n" +
      "      description: 'Throws',\n" +
      "      async execute() { throw new Error('boom'); },\n" +
      "    });\n" +
      "  "));
    page.navigate(server.PREFIX + "/");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.webmcp().callTool("throwing"));
    // Chromium does not report the error that the tool threw.
    assertTrue(error.getMessage().contains(browserName == BrowserName.CHROMIUM ? "the invocation failed" : "boom"));
  }

  @PlaywrightTest
  @DisplayName("WM-006 should throw for an unknown tool")
  void shouldThrowForAnUnknownTool(Page page, TestServer server) {
    serve(server, "/", registerScript(kAdd));
    page.navigate(server.PREFIX + "/");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.webmcp().callTool("missing"));
    assertTrue(error.getMessage().contains("No WebMCP tool named \"missing\""));
    assertTrue(error.getMessage().contains("Available tools: add"));
  }

  @PlaywrightTest
  @DisplayName("WM-007 should time out when the tool does not settle")
  void shouldTimeOutWhenTheToolDoesNotSettle(Page page, TestServer server) {
    serve(server, "/", registerScript("\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'stuck',\n" +
      "      description: 'Never resolves',\n" +
      "      execute() { return new Promise(() => {}); },\n" +
      "    });\n" +
      "  "));
    page.navigate(server.PREFIX + "/");

    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.webmcp().callTool("stuck", mapOf(), new WebMCP.CallToolOptions().setTimeout(500)));
    assertTrue(error.getMessage().contains("Timeout 500ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("WM-008 should drop tools after navigation")
  void shouldDropToolsAfterNavigation(Page page, TestServer server) {
    serve(server, "/", registerScript(kAdd));
    page.navigate(server.PREFIX + "/");
    assertEquals(1, page.webmcp().tools().size());

    page.navigate(server.EMPTY_PAGE);
    assertEquals(new ArrayList<>(), page.webmcp().tools());
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.webmcp().callTool("add", mapOf("a", 1, "b", 1)));
    assertTrue(error.getMessage().contains("The frame does not register any WebMCP tools"));
  }

  @PlaywrightTest
  @DisplayName("WM-009 should list a tool registered after load")
  void shouldListAToolRegisteredAfterLoad(Page page, TestServer server) {
    serve(server, "/", registerScript(kAdd));
    page.navigate(server.PREFIX + "/");
    assertEquals(1, page.webmcp().tools().size());

    page.evaluate("() => {\n" +
      "    const modelContext = document.modelContext || navigator.modelContext;\n" +
      "    modelContext.registerTool({ name: 'late', description: 'Registered later', async execute() {} });\n" +
      "  }");
    List<WebMCPTool> tools = sortedByName(page.webmcp().tools());
    List<String> names = new ArrayList<>();
    for (WebMCPTool tool : tools) {
      names.add(tool.name);
    }
    assertEquals(asList("add", "late"), names);
  }

  @PlaywrightTest
  @DisplayName("WM-011 should list declarative tools")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "Only Chromium implements declarative tools")
  void shouldListDeclarativeTools(Page page, TestServer server) {
    serve(server, "/", "\n" +
      "    <form toolname=\"subscribe\" tooldescription=\"Subscribes to the newsletter\">\n" +
      "      <input name=\"email\" type=\"email\">\n" +
      "      <button>Subscribe</button>\n" +
      "    </form>\n" +
      "  ");
    page.navigate(server.PREFIX + "/");

    List<WebMCPTool> tools = page.webmcp().tools();
    assertEquals(1, tools.size());
    assertEquals("subscribe", tools.get(0).name);
    assertEquals("Subscribes to the newsletter", tools.get(0).description);
    Map<?, ?> inputSchema = (Map<?, ?>) tools.get(0).inputSchema;
    assertEquals("object", inputSchema.get("type"));
    assertEquals(mapOf("email", mapOf("type", "string")), inputSchema.get("properties"));

    // A declarative tool waits for the user to submit the form.
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.webmcp().callTool("subscribe", mapOf("email", "me@example.com"), new WebMCP.CallToolOptions().setTimeout(500)));
    assertTrue(error.getMessage().contains("Timeout 500ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("WM-012 should scope tools to their frame")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox does not support registering WebMCP tools in iframes yet, https://bugzilla.mozilla.org/show_bug.cgi?id=2019743")
  void shouldScopeToolsToTheirFrame(Page page, TestServer server) {
    serve(server, "/", registerScript(kAdd) + "<iframe src=\"/frame.html\"></iframe>");
    serve(server, "/frame.html", registerScript("\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'subscribe',\n" +
      "      description: 'Subscribes to the newsletter',\n" +
      "      async execute() { return { content: [{ type: 'text', text: 'subscribed' }] }; },\n" +
      "    });\n" +
      "  "));
    page.navigate(server.PREFIX + "/");

    Frame childFrame = page.frames().get(1);
    List<WebMCPTool> pageTools = page.webmcp().tools();
    assertEquals(1, pageTools.size());
    assertEquals("add", pageTools.get(0).name);
    List<WebMCPTool> childTools = childFrame.webmcp().tools();
    assertEquals(1, childTools.size());
    assertEquals("subscribe", childTools.get(0).name);
    assertEquals(textResult("subscribed"), childFrame.webmcp().callTool("subscribe"));

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.webmcp().callTool("subscribe"));
    assertTrue(error.getMessage().contains("No WebMCP tool named \"subscribe\""));
    assertTrue(error.getMessage().contains("Available tools: add"));
  }

  @PlaywrightTest
  @DisplayName("WM-013 should call same-name tools in their own frame")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox does not support registering WebMCP tools in iframes yet, https://bugzilla.mozilla.org/show_bug.cgi?id=2019743")
  void shouldCallSameNameToolsInTheirOwnFrame(Page page, TestServer server) {
    Function<String, String> registerEcho = text -> registerScript("\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'echo',\n" +
      "      description: 'Echoes',\n" +
      "      async execute() { return { content: [{ type: 'text', text: '" + text + "' }] }; },\n" +
      "    });\n" +
      "  ");
    serve(server, "/", registerEcho.apply("main") + "<iframe src=\"/frame.html\"></iframe>");
    serve(server, "/frame.html", registerEcho.apply("frame"));
    page.navigate(server.PREFIX + "/");
    Frame childFrame = page.frames().get(1);

    assertEquals(textResult("main"), page.webmcp().callTool("echo"));
    assertEquals(textResult("frame"), childFrame.webmcp().callTool("echo"));
  }

  @PlaywrightTest
  @DisplayName("WM-014 should list and call tools in cross-origin and nested frames")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox does not support registering WebMCP tools in iframes yet, https://bugzilla.mozilla.org/show_bug.cgi?id=2019743")
  void shouldListAndCallToolsInCrossOriginAndNestedFrames(Page page, TestServer server) {
    Function<String, String> registerEcho = text -> registerScript("\n" +
      "    modelContext.registerTool({\n" +
      "      name: 'echo-" + text + "',\n" +
      "      description: 'Echoes " + text + "',\n" +
      "      async execute() { return { content: [{ type: 'text', text: '" + text + "' }] }; },\n" +
      "    });\n" +
      "  ");
    serve(server, "/", registerEcho.apply("main") + "<iframe allow=\"tools\" src=\"" + server.CROSS_PROCESS_PREFIX + "/frame.html\"></iframe>");
    serve(server, "/frame.html", registerEcho.apply("frame") + "<iframe src=\"/nested.html\"></iframe>");
    serve(server, "/nested.html", registerEcho.apply("nested"));
    page.navigate(server.PREFIX + "/");

    List<Frame> frames = page.frames();
    Frame mainFrame = frames.get(0);
    Frame frame = frames.get(1);
    Frame nested = frames.get(2);
    List<WebMCPTool> mainTools = mainFrame.webmcp().tools();
    assertEquals(1, mainTools.size());
    assertPlainTool("echo-main", "Echoes main", mainTools.get(0));
    List<WebMCPTool> frameTools = frame.webmcp().tools();
    assertEquals(1, frameTools.size());
    assertPlainTool("echo-frame", "Echoes frame", frameTools.get(0));
    List<WebMCPTool> nestedTools = nested.webmcp().tools();
    assertEquals(1, nestedTools.size());
    assertPlainTool("echo-nested", "Echoes nested", nestedTools.get(0));
    assertEquals(textResult("frame"), frame.webmcp().callTool("echo-frame"));
    assertEquals(textResult("nested"), nested.webmcp().callTool("echo-nested"));
  }
}
