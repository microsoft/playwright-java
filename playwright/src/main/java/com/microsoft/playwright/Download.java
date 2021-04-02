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
 * {@code Download} objects are dispatched by page via the {@link Page#onDownload Page.onDownload()} event.
 *
 * <p> All the downloaded files belonging to the browser context are deleted when the browser context is closed. All downloaded
 * files are deleted when the browser closes.
 *
 * <p> Download event is emitted once the download starts. Download path becomes available once download completes:
 * <pre>{@code
 * // wait for download to start
 * Download download  = page.waitForDownload(() -> page.click("a"));
 * // wait for download to complete
 * Path path = download.path();
 * }</pre>
 * <pre>{@code
 * // wait for download to start
 * Download download = page.waitForDownload(() -> {
 *   page.click("a");
 * });
 * // wait for download to complete
 * Path path = download.path();
 * }</pre>
 *
 * <p> <strong>NOTE:</strong> Browser context **must** be created with the {@code acceptDownloads} set to {@code true} when user needs access to the downloaded
 * content. If {@code acceptDownloads} is not set, download events are emitted, but the actual download is not performed and user
 * has no access to the downloaded files.
 */
public interface Download {
  /**
   * Returns readable stream for current download or {@code null} if download failed.
   */
  InputStream createReadStream();
  /**
   * Deletes the downloaded file. Will wait for the download to finish if necessary.
   */
  void delete();
  /**
   * Returns download error if any. Will wait for the download to finish if necessary.
   */
  String failure();
  /**
   * Returns path to the downloaded file in case of successful download. The method will wait for the download to finish if
   * necessary. The method throws when connected remotely via {@link BrowserType#connect BrowserType.connect()}.
   */
  Path path();
  /**
   * Saves the download to a user-specified path. It is safe to call this method while the download is still in progress.
   *
   * @param path Path where the download should be saved.
   */
  void saveAs(Path path);
  /**
   * Returns suggested filename for this download. It is typically computed by the browser from the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition">{@code Content-Disposition}</a> response
   * header or the {@code download} attribute. See the spec on <a
   * href="https://html.spec.whatwg.org/#downloading-resources">whatwg</a>. Different browsers can use different logic for
   * computing it.
   */
  String suggestedFilename();
  /**
   * Returns downloaded url.
   */
  String url();
}

