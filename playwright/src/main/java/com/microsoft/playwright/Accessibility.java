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

import java.util.*;

/**
 * The Accessibility class provides methods for inspecting Chromium's accessibility tree. The accessibility tree is used by assistive technology such as screen readers or switches.
 * <p>
 * Accessibility is a very platform-specific thing. On different platforms, there are different screen readers that might have wildly different output.
 * <p>
 * Blink - Chromium's rendering engine - has a concept of "accessibility tree", which is then translated into different platform-specific APIs. Accessibility namespace gives users
 * <p>
 * access to the Blink Accessibility Tree.
 * <p>
 * Most of the accessibility tree gets filtered out when converting from Blink AX Tree to Platform-specific AX-Tree or by assistive technologies themselves. By default, Playwright tries to approximate this filtering, exposing only the "interesting" nodes of the tree.
 */
public interface Accessibility {
  class SnapshotOptions {
    public Boolean interestingOnly;
    public ElementHandle root;

    public SnapshotOptions withInterestingOnly(Boolean interestingOnly) {
      this.interestingOnly = interestingOnly;
      return this;
    }
    public SnapshotOptions withRoot(ElementHandle root) {
      this.root = root;
      return this;
    }
  }
  default AccessibilityNode snapshot() {
    return snapshot(null);
  }
  /**
   * Captures the current state of the accessibility tree. The returned object represents the root accessible node of the page.
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> The Chromium accessibility tree contains nodes that go unused on most platforms and by
   * <p>
   * most screen readers. Playwright will discard them as well for an easier to process tree,
   * <p>
   * unless {@code interestingOnly} is set to {@code false}.
   * <p>
   * 
   * <p>
   * An example of dumping the entire accessibility tree:
   * <p>
   * 
   * <p>
   * An example of logging the focused node's name:
   * <p>
   * 
   * @return An AXNode object with the following properties:
   */
  AccessibilityNode snapshot(SnapshotOptions options);
}

