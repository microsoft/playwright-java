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

import com.microsoft.playwright.options.WebMCPTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

@DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="WebKit does not implement WebMCP")
public class TestWebMCP extends TestBase {
  private static final String ADD_TOOL = "modelContext.registerTool({\n" +
    "  name: 'add',\n" +
    "  description: 'Adds two numbers',\n" +
    "  inputSchema: { type: 'object', properties: { a: { type: 'number' }, b: { type: 'number' } }, required: ['a', 'b'] },\n" +
    "  annotations: { readOnlyHint: true },\n" +
    "  async execute(input) { return { content: [{ type: 'text', text: String(input.a + input.b) }] }; },\n" +
    "});\n";

  @Override
  @BeforeAll
  void launchBrowser() {
    BrowserType.LaunchOptions options = createLaunchOptions();
    if (isFirefox()) {
      options.setFirefoxUserPrefs(mapOf(
        "dom.modelcontext.enabled", true,
        "dom.modelcontext.testing.enabled", true));
    } else {
      options.setArgs(singletonList("--enable-features=WebMCP"));
    }
    launchBrowser(options);
  }

  private static String registerScript(String tools) {
    return "<script>\n" +
      "  const modelContext = document.modelContext || navigator.modelContext;\n" +
      tools +
      "</script>";
  }

  private void serve(String path, String body) {
    server.setRoute(path, exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<title>WebMCP</title>" + body);
      }
    });
  }

  @Test
  void shouldListToolsRegisteredByThePage() {
    serve("/", registerScript(ADD_TOOL +
      "modelContext.registerTool({\n" +
      "  name: 'noop',\n" +
      "  description: 'Does nothing',\n" +
      "  async execute() {},\n" +
      "});\n"));
    page.navigate(server.PREFIX + "/");

    List<WebMCPTool> tools = page.webmcp().tools();
    // Browsers list tools in their own order.
    tools.sort(Comparator.comparing(tool -> tool.name));
    assertJsonEquals("[{\n" +
      "  \"name\": \"add\",\n" +
      "  \"description\": \"Adds two numbers\",\n" +
      "  \"inputSchema\": { \"type\": \"object\", \"properties\": { \"a\": { \"type\": \"number\" }, \"b\": { \"type\": \"number\" } }, \"required\": [\"a\", \"b\"] },\n" +
      "  \"annotations\": { \"readOnly\": true }\n" +
      "}, {\n" +
      "  \"name\": \"noop\",\n" +
      "  \"description\": \"Does nothing\"\n" +
      "}]", tools);
    assertTrue(tools.get(0).annotations.readOnly);
  }

  @Test
  void shouldReportNoToolsWhenThePageRegistersNone() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.webmcp().tools().size());
  }

  @Test
  void shouldCallATool() {
    serve("/", registerScript(ADD_TOOL));
    page.navigate(server.PREFIX + "/");

    Object result = page.webmcp().callTool("add", mapOf("a", 2, "b", 40));
    assertEquals(mapOf("content", asList(mapOf("type", "text", "text", "42"))), result);
  }

  @Test
  void shouldReturnIsErrorResultsAsIs() {
    serve("/", registerScript(
      "modelContext.registerTool({\n" +
      "  name: 'broken',\n" +
      "  description: 'Always fails',\n" +
      "  async execute() { return { content: [{ type: 'text', text: 'nope' }], isError: true }; },\n" +
      "});\n"));
    page.navigate(server.PREFIX + "/");

    Object result = page.webmcp().callTool("broken");
    assertEquals(mapOf("content", asList(mapOf("type", "text", "text", "nope")), "isError", true), result);
  }

  @Test
  void shouldThrowForAnUnknownTool() {
    serve("/", registerScript(ADD_TOOL));
    page.navigate(server.PREFIX + "/");

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.webmcp().callTool("missing"));
    assertTrue(e.getMessage().contains("No WebMCP tool named \"missing\""), e.getMessage());
    assertTrue(e.getMessage().contains("Available tools: add"), e.getMessage());
  }

  @Test
  void shouldTimeOutWhenTheToolDoesNotSettle() {
    serve("/", registerScript(
      "modelContext.registerTool({\n" +
      "  name: 'stuck',\n" +
      "  description: 'Never resolves',\n" +
      "  execute() { return new Promise(() => {}); },\n" +
      "});\n"));
    page.navigate(server.PREFIX + "/");

    TimeoutError e = assertThrows(TimeoutError.class, () -> page.webmcp().callTool("stuck", mapOf(), new WebMCP.CallToolOptions().setTimeout(500)));
    assertTrue(e.getMessage().contains("Timeout 500ms exceeded"), e.getMessage());
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="Firefox does not support registering WebMCP tools in iframes yet")
  void shouldScopeToolsToTheirFrame() {
    serve("/", registerScript(ADD_TOOL) + "<iframe src='/frame.html'></iframe>");
    serve("/frame.html", registerScript(
      "modelContext.registerTool({\n" +
      "  name: 'subscribe',\n" +
      "  description: 'Subscribes to the newsletter',\n" +
      "  async execute() { return { content: [{ type: 'text', text: 'subscribed' }] }; },\n" +
      "});\n"));
    page.navigate(server.PREFIX + "/");

    Frame childFrame = page.frames().get(1);
    List<WebMCPTool> mainTools = page.webmcp().tools();
    assertEquals(1, mainTools.size());
    assertEquals("add", mainTools.get(0).name);
    List<WebMCPTool> frameTools = childFrame.webmcp().tools();
    assertEquals(1, frameTools.size());
    assertEquals("subscribe", frameTools.get(0).name);
    assertEquals(mapOf("content", asList(mapOf("type", "text", "text", "subscribed"))), childFrame.webmcp().callTool("subscribe"));

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.webmcp().callTool("subscribe"));
    assertTrue(e.getMessage().contains("No WebMCP tool named \"subscribe\""), e.getMessage());
    assertTrue(e.getMessage().contains("Available tools: add"), e.getMessage());
  }
}
