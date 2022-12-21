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

import java.nio.file.Path;

/**
 * When browser context is created with the {@code recordVideo} option, each page has a video object associated with it.
 * <pre>{@code
 * System.out.println(page.video().path());
 * }</pre>
 */
public interface Video {
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
}

