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

/**
 * When browser context is created with the {@code recordVideo} option, each page has a video object associated with it.
 * <pre>{@code
 * System.out.println(page.video().path());
 * }</pre>
 *
 * <p> Alternatively, you can use {@link com.microsoft.playwright.Video#start Video.start()} and {@link
 * com.microsoft.playwright.Video#stop Video.stop()} to record video manually. This approach is mutually exclusive with the
 * {@code recordVideo} option.
 * <pre>{@code
 * page.video().start(new Video.StartOptions().setPath(Paths.get("video.webm")));
 * // ... perform actions ...
 * page.video().stop();
 * }</pre>
 */
public interface Video {
  class StartOptions {
    /**
     * If specified, enables visual annotations on interacted elements during video recording. Interacted elements are
     * highlighted with a semi-transparent blue box and click points are shown as red circles.
     */
    public Annotate annotate;
    /**
     * Path where the video should be saved when the recording is stopped.
     */
    public Path path;
    /**
     * Optional dimensions of the recorded video. If not specified the size will be equal to page viewport scaled down to fit
     * into 800x800. Actual picture of the page will be scaled down if necessary to fit the specified size.
     */
    public Size size;

    /**
     * If specified, enables visual annotations on interacted elements during video recording. Interacted elements are
     * highlighted with a semi-transparent blue box and click points are shown as red circles.
     */
    public StartOptions setAnnotate(Annotate annotate) {
      this.annotate = annotate;
      return this;
    }
    /**
     * Path where the video should be saved when the recording is stopped.
     */
    public StartOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * Optional dimensions of the recorded video. If not specified the size will be equal to page viewport scaled down to fit
     * into 800x800. Actual picture of the page will be scaled down if necessary to fit the specified size.
     */
    public StartOptions setSize(int width, int height) {
      return setSize(new Size(width, height));
    }
    /**
     * Optional dimensions of the recorded video. If not specified the size will be equal to page viewport scaled down to fit
     * into 800x800. Actual picture of the page will be scaled down if necessary to fit the specified size.
     */
    public StartOptions setSize(Size size) {
      this.size = size;
      return this;
    }
  }
  /**
   * Deletes the video file. Will wait for the video to finish if necessary.
   *
   * @since v1.11
   */
  void delete();
  /**
   * Returns the file system path this video will be recorded to. The video is guaranteed to be written to the filesystem
   * upon closing the browser context. This method throws when connected remotely.
   *
   * @since v1.8
   */
  Path path();
  /**
   * Saves the video to a user-specified path. It is safe to call this method while the video is still in progress, or after
   * the page has closed. This method waits until the page is closed and the video is fully saved.
   *
   * @param path Path where the video should be saved.
   * @since v1.11
   */
  void saveAs(Path path);
  /**
   * Starts video recording. This method is mutually exclusive with the {@code recordVideo} context option.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.video().start(new Video.StartOptions().setPath(Paths.get("video.webm")));
   * // ... perform actions ...
   * page.video().stop();
   * }</pre>
   *
   * @since v1.59
   */
  default AutoCloseable start() {
    return start(null);
  }
  /**
   * Starts video recording. This method is mutually exclusive with the {@code recordVideo} context option.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * page.video().start(new Video.StartOptions().setPath(Paths.get("video.webm")));
   * // ... perform actions ...
   * page.video().stop();
   * }</pre>
   *
   * @since v1.59
   */
  AutoCloseable start(StartOptions options);
  /**
   * Stops video recording started with {@link com.microsoft.playwright.Video#start Video.start()}.
   *
   * @since v1.59
   */
  void stop();
}

