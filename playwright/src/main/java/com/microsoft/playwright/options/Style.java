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

public class Style {
  /**
   * CSS declarations for the marker at the action point. The marker is positioned at the action point, has zero size and is
   * centered on the point, so its size and look come from this style. Not shown when omitted.
   */
  public @Nullable String point;
  /**
   * CSS declarations for the box that covers the target element. The box is positioned and sized to the element bounds. Not
   * shown when omitted.
   */
  public @Nullable String highlight;
  /**
   * CSS declarations for the action title, for example {@code "font-size: 32px; background: #333"}. The title is placed
   * according to {@code position}.
   */
  public @Nullable String title;

  /**
   * CSS declarations for the marker at the action point. The marker is positioned at the action point, has zero size and is
   * centered on the point, so its size and look come from this style. Not shown when omitted.
   */
  public Style setPoint(String point) {
    this.point = point;
    return this;
  }
  /**
   * CSS declarations for the box that covers the target element. The box is positioned and sized to the element bounds. Not
   * shown when omitted.
   */
  public Style setHighlight(String highlight) {
    this.highlight = highlight;
    return this;
  }
  /**
   * CSS declarations for the action title, for example {@code "font-size: 32px; background: #333"}. The title is placed
   * according to {@code position}.
   */
  public Style setTitle(String title) {
    this.title = title;
    return this;
  }
}