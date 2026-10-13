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

package com.microsoft.playwright.options;

import org.jspecify.annotations.Nullable;

public class WebMCPToolAnnotations {
  /**
   * The tool does not modify any state.
   */
  public @Nullable Boolean readOnly;
  /**
   * The tool output may contain third-party content.
   */
  public @Nullable Boolean untrustedContent;
  /**
   * The tool takes a consequential action, such as placing an order.
   */
  public @Nullable Boolean consequential;

  /**
   * The tool does not modify any state.
   */
  public WebMCPToolAnnotations setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }
  /**
   * The tool output may contain third-party content.
   */
  public WebMCPToolAnnotations setUntrustedContent(boolean untrustedContent) {
    this.untrustedContent = untrustedContent;
    return this;
  }
  /**
   * The tool takes a consequential action, such as placing an order.
   */
  public WebMCPToolAnnotations setConsequential(boolean consequential) {
    this.consequential = consequential;
    return this;
  }
}