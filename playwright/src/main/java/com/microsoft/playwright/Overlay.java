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


/**
 * Interface for managing page overlays that display persistent visual indicators on top of the page.
 */
public interface Overlay {
  class ShowOptions {
    /**
     * Duration in milliseconds after which the overlay is automatically removed. Overlay stays until dismissed if not
     * provided.
     */
    public Double duration;

    /**
     * Duration in milliseconds after which the overlay is automatically removed. Overlay stays until dismissed if not
     * provided.
     */
    public ShowOptions setDuration(double duration) {
      this.duration = duration;
      return this;
    }
  }
  class ChapterOptions {
    /**
     * Optional description text displayed below the title.
     */
    public String description;
    /**
     * Duration in milliseconds after which the overlay is automatically removed. Defaults to {@code 2000}.
     */
    public Double duration;

    /**
     * Optional description text displayed below the title.
     */
    public ChapterOptions setDescription(String description) {
      this.description = description;
      return this;
    }
    /**
     * Duration in milliseconds after which the overlay is automatically removed. Defaults to {@code 2000}.
     */
    public ChapterOptions setDuration(double duration) {
      this.duration = duration;
      return this;
    }
  }
  /**
   * Adds an overlay with the given HTML content. The overlay is displayed on top of the page until removed. Returns a
   * disposable that removes the overlay when disposed.
   *
   * @param html HTML content for the overlay.
   * @since v1.59
   */
  default AutoCloseable show(String html) {
    return show(html, null);
  }
  /**
   * Adds an overlay with the given HTML content. The overlay is displayed on top of the page until removed. Returns a
   * disposable that removes the overlay when disposed.
   *
   * @param html HTML content for the overlay.
   * @since v1.59
   */
  AutoCloseable show(String html, ShowOptions options);
  /**
   * Shows a chapter overlay with a title and optional description, centered on the page with a blurred backdrop. Useful for
   * narrating video recordings. The overlay is removed after the specified duration, or 2000ms.
   *
   * @param title Title text displayed prominently in the overlay.
   * @since v1.59
   */
  default void chapter(String title) {
    chapter(title, null);
  }
  /**
   * Shows a chapter overlay with a title and optional description, centered on the page with a blurred backdrop. Useful for
   * narrating video recordings. The overlay is removed after the specified duration, or 2000ms.
   *
   * @param title Title text displayed prominently in the overlay.
   * @since v1.59
   */
  void chapter(String title, ChapterOptions options);
  /**
   * Sets visibility of all overlays without removing them.
   *
   * @param visible Whether overlays should be visible.
   * @since v1.59
   */
  void setVisible(boolean visible);
}

