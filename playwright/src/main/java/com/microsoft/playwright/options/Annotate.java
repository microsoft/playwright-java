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

public class Annotate {
  /**
   * How long each annotation is displayed in milliseconds. Defaults to {@code 500}.
   */
  public Double duration;
  /**
   * Position of the action title overlay. Defaults to {@code "top-right"}.
   */
  public AnnotatePosition position;
  /**
   * Font size of the action title in pixels. Defaults to {@code 24}.
   */
  public Integer fontSize;

  /**
   * How long each annotation is displayed in milliseconds. Defaults to {@code 500}.
   */
  public Annotate setDuration(double duration) {
    this.duration = duration;
    return this;
  }
  /**
   * Position of the action title overlay. Defaults to {@code "top-right"}.
   */
  public Annotate setPosition(AnnotatePosition position) {
    this.position = position;
    return this;
  }
  /**
   * Font size of the action title in pixels. Defaults to {@code 24}.
   */
  public Annotate setFontSize(int fontSize) {
    this.fontSize = fontSize;
    return this;
  }
}