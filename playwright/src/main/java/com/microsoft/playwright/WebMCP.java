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

import org.jspecify.annotations.Nullable;
import com.microsoft.playwright.options.*;
import java.util.*;

/**
 * {@code WebMCP} exposes the tools that a frame registers through the experimental {@code WebMCP} browser API, {@code
 * navigator.modelContext}. It lists the tools and calls them.
 *
 * <p> Instances are accessed through {@link com.microsoft.playwright.Frame#webmcp Frame.webmcp()}. {@link
 * com.microsoft.playwright.Page#webmcp Page.webmcp()} is the instance of the main frame.
 *
 * <p> <strong>NOTE:</strong> WebMCP is an experimental browser feature. Chromium enables it with the {@code --enable-features=WebMCP} launch
 * argument, Firefox with the {@code dom.modelcontext.enabled} and {@code dom.modelcontext.testing.enabled} preferences.
 * WebKit does not implement it.
 *
 * <p> Tool names, descriptions, input schemas and results are provided by the page, so treat them as untrusted input.
 */
public interface WebMCP {
  class CallToolOptions {
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link com.microsoft.playwright.BrowserContext#setDefaultTimeout
     * BrowserContext.setDefaultTimeout()} or {@link com.microsoft.playwright.Page#setDefaultTimeout Page.setDefaultTimeout()}
     * methods.
     */
    public @Nullable Double timeout;

    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link com.microsoft.playwright.BrowserContext#setDefaultTimeout
     * BrowserContext.setDefaultTimeout()} or {@link com.microsoft.playwright.Page#setDefaultTimeout Page.setDefaultTimeout()}
     * methods.
     */
    public CallToolOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ToolsOptions {
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link com.microsoft.playwright.BrowserContext#setDefaultTimeout
     * BrowserContext.setDefaultTimeout()} or {@link com.microsoft.playwright.Page#setDefaultTimeout Page.setDefaultTimeout()}
     * methods.
     */
    public @Nullable Double timeout;

    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link com.microsoft.playwright.BrowserContext#setDefaultTimeout
     * BrowserContext.setDefaultTimeout()} or {@link com.microsoft.playwright.Page#setDefaultTimeout Page.setDefaultTimeout()}
     * methods.
     */
    public ToolsOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Calls a tool registered by the frame and returns its result. The result is whatever the tool's {@code execute} function
   * resolved to, typically an object with a {@code content} array. A result with {@code isError: true} is returned as is.
   * The method throws when the tool is not registered or its {@code execute} function throws.
   *
   * @param name Name of the tool, as reported by {@link com.microsoft.playwright.WebMCP#tools WebMCP.tools()}.
   * @param input Input for the tool, matching its {@code inputSchema}. Defaults to an empty object.
   * @since v1.64
   */
  default @Nullable Object callTool(String name, @Nullable Object input) {
    return callTool(name, input, null);
  }
  /**
   * Calls a tool registered by the frame and returns its result. The result is whatever the tool's {@code execute} function
   * resolved to, typically an object with a {@code content} array. A result with {@code isError: true} is returned as is.
   * The method throws when the tool is not registered or its {@code execute} function throws.
   *
   * @param name Name of the tool, as reported by {@link com.microsoft.playwright.WebMCP#tools WebMCP.tools()}.
   * @since v1.64
   */
  default @Nullable Object callTool(String name) {
    return callTool(name, null);
  }
  /**
   * Calls a tool registered by the frame and returns its result. The result is whatever the tool's {@code execute} function
   * resolved to, typically an object with a {@code content} array. A result with {@code isError: true} is returned as is.
   * The method throws when the tool is not registered or its {@code execute} function throws.
   *
   * @param name Name of the tool, as reported by {@link com.microsoft.playwright.WebMCP#tools WebMCP.tools()}.
   * @param input Input for the tool, matching its {@code inputSchema}. Defaults to an empty object.
   * @since v1.64
   */
  @Nullable Object callTool(String name, @Nullable Object input, @Nullable CallToolOptions options);
  /**
   * Returns the tools currently registered by the frame. Throws if the browser was launched without WebMCP support, see the
   * note above for the launch options that enable it.
   *
   * <p> {@link com.microsoft.playwright.Page#webmcp Page.webmcp()} covers the main frame only, child frames list their tools
   * through their own {@link com.microsoft.playwright.Frame#webmcp Frame.webmcp()}.
   *
   * @since v1.64
   */
  default List<WebMCPTool> tools() {
    return tools(null);
  }
  /**
   * Returns the tools currently registered by the frame. Throws if the browser was launched without WebMCP support, see the
   * note above for the launch options that enable it.
   *
   * <p> {@link com.microsoft.playwright.Page#webmcp Page.webmcp()} covers the main frame only, child frames list their tools
   * through their own {@link com.microsoft.playwright.Frame#webmcp Frame.webmcp()}.
   *
   * @since v1.64
   */
  List<WebMCPTool> tools(@Nullable ToolsOptions options);
}

