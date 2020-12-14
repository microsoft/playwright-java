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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Download objects are dispatched by page via the page.on('download') event.
 * <p>
 * All the downloaded files belonging to the browser context are deleted when the browser context is closed. All downloaded
 * <p>
 * files are deleted when the browser closes.
 * <p>
 * Download event is emitted once the download starts. Download path becomes available once download completes:
 * <p>
 * 
 * <p>
 * <strong>NOTE</strong> Browser context **must** be created with the {@code acceptDownloads} set to {@code true} when user needs access to the
 * <p>
 * downloaded content. If {@code acceptDownloads} is not set or set to {@code false}, download events are emitted, but the actual
 * <p>
 * download is not performed and user has no access to the downloaded files.
 */
public interface Download {
  /**
   * Returns readable stream for current download or {@code null} if download failed.
   */
  InputStream createReadStream();
  /**
   * Deletes the downloaded file.
   */
  void delete();
  /**
   * Returns download error if any.
   */
  String failure();
  /**
   * Returns path to the downloaded file in case of successful download.
   */
  Path path();
  /**
   * Saves the download to a user-specified path.
   * @param path Path where the download should be saved.
   */
  void saveAs(Path path);
  /**
   * Returns suggested filename for this download. It is typically computed by the browser from the
   * <p>
   * {@code Content-Disposition} response header
   * <p>
   * or the {@code download} attribute. See the spec on whatwg. Different
   * <p>
   * browsers can use different logic for computing it.
   */
  String suggestedFilename();
  /**
   * Returns downloaded url.
   */
  String url();
}

