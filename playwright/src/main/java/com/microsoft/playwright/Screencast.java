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

import com.microsoft.playwright.options.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Interface for capturing screencast frames from a page.
 */
public interface Screencast {
  class StartOptions {
    /**
     * Callback that receives JPEG-encoded frame data.
     */
    public Consumer<ScreencastFrame> onFrame;
    /**
     * Path where the video should be saved when the screencast is stopped. When provided, video recording is started.
     */
    public Path path;
    /**
     * The quality of the image, between 0-100.
     */
    public Integer quality;

    /**
     * Callback that receives JPEG-encoded frame data.
     */
    public StartOptions setOnFrame(Consumer<ScreencastFrame> onFrame) {
      this.onFrame = onFrame;
      return this;
    }
    /**
     * Path where the video should be saved when the screencast is stopped. When provided, video recording is started.
     */
    public StartOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * The quality of the image, between 0-100.
     */
    public StartOptions setQuality(int quality) {
      this.quality = quality;
      return this;
    }
  }
  class ShowOverlayOptions {
    /**
     * Duration in milliseconds after which the overlay is automatically removed. Overlay stays until dismissed if not
     * provided.
     */
    public Double duration;

    /**
     * Duration in milliseconds after which the overlay is automatically removed. Overlay stays until dismissed if not
     * provided.
     */
    public ShowOverlayOptions setDuration(double duration) {
      this.duration = duration;
      return this;
    }
  }
  class ShowChapterOptions {
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
    public ShowChapterOptions setDescription(String description) {
      this.description = description;
      return this;
    }
    /**
     * Duration in milliseconds after which the overlay is automatically removed. Defaults to {@code 2000}.
     */
    public ShowChapterOptions setDuration(double duration) {
      this.duration = duration;
      return this;
    }
  }
  class ShowActionsOptions {
    /**
     * How long each annotation is displayed in milliseconds. Defaults to {@code 500}.
     */
    public Double duration;
    /**
     * Font size of the action title in pixels. Defaults to {@code 24}.
     */
    public Integer fontSize;
    /**
     * Position of the action title overlay. Defaults to {@code "top-right"}.
     */
    public AnnotatePosition position;

    /**
     * How long each annotation is displayed in milliseconds. Defaults to {@code 500}.
     */
    public ShowActionsOptions setDuration(double duration) {
      this.duration = duration;
      return this;
    }
    /**
     * Font size of the action title in pixels. Defaults to {@code 24}.
     */
    public ShowActionsOptions setFontSize(int fontSize) {
      this.fontSize = fontSize;
      return this;
    }
    /**
     * Position of the action title overlay. Defaults to {@code "top-right"}.
     */
    public ShowActionsOptions setPosition(AnnotatePosition position) {
      this.position = position;
      return this;
    }
  }
  /**
   * Starts the screencast. When {@code path} is provided, it saves video recording to the specified file. When {@code
   * onFrame} is provided, delivers JPEG-encoded frames to the callback. Both can be used together.
   *
   * <p> <strong>Usage</strong>
   *
   * @since v1.59
   */
  default AutoCloseable start() {
    return start(null);
  }
  /**
   * Starts the screencast. When {@code path} is provided, it saves video recording to the specified file. When {@code
   * onFrame} is provided, delivers JPEG-encoded frames to the callback. Both can be used together.
   *
   * <p> <strong>Usage</strong>
   *
   * @since v1.59
   */
  AutoCloseable start(StartOptions options);
  /**
   * Stops the screencast and video recording if active. If a video was being recorded, saves it to the path specified in
   * {@link com.microsoft.playwright.Screencast#start Screencast.start()}.
   *
   * @since v1.59
   */
  void stop();
  /**
   * Adds an overlay with the given HTML content. The overlay is displayed on top of the page until removed. Returns a
   * disposable that removes the overlay when disposed.
   *
   * @param html HTML content for the overlay.
   * @since v1.59
   */
  default AutoCloseable showOverlay(String html) {
    return showOverlay(html, null);
  }
  /**
   * Adds an overlay with the given HTML content. The overlay is displayed on top of the page until removed. Returns a
   * disposable that removes the overlay when disposed.
   *
   * @param html HTML content for the overlay.
   * @since v1.59
   */
  AutoCloseable showOverlay(String html, ShowOverlayOptions options);
  /**
   * Shows a chapter overlay with a title and optional description, centered on the page with a blurred backdrop. Useful for
   * narrating video recordings. The overlay is removed after the specified duration, or 2000ms.
   *
   * @param title Title text displayed prominently in the overlay.
   * @since v1.59
   */
  default void showChapter(String title) {
    showChapter(title, null);
  }
  /**
   * Shows a chapter overlay with a title and optional description, centered on the page with a blurred backdrop. Useful for
   * narrating video recordings. The overlay is removed after the specified duration, or 2000ms.
   *
   * @param title Title text displayed prominently in the overlay.
   * @since v1.59
   */
  void showChapter(String title, ShowChapterOptions options);
  /**
   * Enables visual annotations on interacted elements. Returns a disposable that stops showing actions when disposed.
   *
   * @since v1.59
   */
  default AutoCloseable showActions() {
    return showActions(null);
  }
  /**
   * Enables visual annotations on interacted elements. Returns a disposable that stops showing actions when disposed.
   *
   * @since v1.59
   */
  AutoCloseable showActions(ShowActionsOptions options);
  /**
   * Shows overlays.
   *
   * @since v1.59
   */
  void showOverlays();
  /**
   * Removes action decorations.
   *
   * @since v1.59
   */
  void hideActions();
  /**
   * Hides overlays without removing them.
   *
   * @since v1.59
   */
  void hideOverlays();
}

