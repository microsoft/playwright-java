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
 * The Accessibility class provides methods for inspecting Chromium's accessibility tree. The accessibility tree is used by
 * assistive technology such as [screen readers](https://en.wikipedia.org/wiki/Screen_reader) or
 * [switches](https://en.wikipedia.org/wiki/Switch_access).
 *
 * <p> Accessibility is a very platform-specific thing. On different platforms, there are different screen readers that might
 * have wildly different output.
 *
 * <p> Rendering engines of Chromium, Firefox and Webkit have a concept of "accessibility tree", which is then translated into
 * different platform-specific APIs. Accessibility namespace gives access to this Accessibility Tree.
 *
 * <p> Most of the accessibility tree gets filtered out when converting from internal browser AX Tree to Platform-specific
 * AX-Tree or by assistive technologies themselves. By default, Playwright tries to approximate this filtering, exposing
 * only the "interesting" nodes of the tree.
 */
public interface Accessibility {
  class SnapshotOptions {
    /**
     * Prune uninteresting nodes from the tree. Defaults to {@code true}.
     */
    public Boolean interestingOnly;
    /**
     * The root DOM element for the snapshot. Defaults to the whole page.
     */
    public ElementHandle root;

    public SnapshotOptions withInterestingOnly(boolean interestingOnly) {
      this.interestingOnly = interestingOnly;
      return this;
    }
    public SnapshotOptions withRoot(ElementHandle root) {
      this.root = root;
      return this;
    }
  }
  default String snapshot() {
    return snapshot(null);
  }
  /**
   * Captures the current state of the accessibility tree. The returned object represents the root accessible node of the
   * page.
   *
   * <p> <strong>NOTE:</strong> The Chromium accessibility tree contains nodes that go unused on most platforms and by most screen readers.
   * Playwright will discard them as well for an easier to process tree, unless {@code interestingOnly} is set to {@code false}.
   */
  String snapshot(SnapshotOptions options);
}

