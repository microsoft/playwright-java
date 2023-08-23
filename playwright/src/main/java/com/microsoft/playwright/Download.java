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

/**
 * {@code Download} objects are dispatched by page via the {@link Page#onDownload Page.onDownload()} event.
 *
 * <p> All the downloaded files belonging to the browser context are deleted when the browser context is closed.
 *
 * <p> Download event is emitted once the download starts. Download path becomes available once download completes.
 * <pre>{@code
 * // Wait for the download to start
 * Download download = page.waitForDownload(() -> {
 *     // Perform the action that initiates download
 *     page.getByText("Download file").click();
 * });
 *
 * // Wait for the download process to complete and save the downloaded file somewhere
 * download.saveAs(Paths.get("/path/to/save/at/", download.suggestedFilename()));
 * }</pre>
 */
public interface Download {
  /**
   * Cancels a download. Will not fail if the download is already finished or canceled. Upon successful cancellations, {@code
   * download.failure()} would resolve to {@code "canceled"}.
   *
   * @since v1.13
   */
  void cancel();
  /**
   * Returns readable stream for current download or {@code null} if download failed.
   *
   * @since v1.8
   */
  InputStream createReadStream();
  /**
   * Deletes the downloaded file. Will wait for the download to finish if necessary.
   *
   * @since v1.8
   */
  void delete();
  /**
   * Returns download error if any. Will wait for the download to finish if necessary.
   *
   * @since v1.8
   */
  String failure();
  /**
   * Get the page that the download belongs to.
   *
   * @since v1.12
   */
  Page page();
  /**
   * Returns path to the downloaded file in case of successful download. The method will wait for the download to finish if
   * necessary. The method throws when connected remotely.
   *
   * <p> Note that the download's file name is a random GUID, use {@link Download#suggestedFilename Download.suggestedFilename()}
   * to get suggested file name.
   *
   * @since v1.8
   */
  Path path();
  /**
   * Copy the download to a user-specified path. It is safe to call this method while the download is still in progress. Will
   * wait for the download to finish if necessary.
   *
   * <p> **Usage**
   * <pre>{@code
   * download.saveAs(Paths.get("/path/to/save/at/", download.suggestedFilename()));
   * }</pre>
   *
   * @param path Path where the download should be copied.
   * @since v1.8
   */
  void saveAs(Path path);
  /**
   * Returns suggested filename for this download. It is typically computed by the browser from the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition">{@code Content-Disposition}</a>
   * response header or the {@code download} attribute. See the spec on <a
   * href="https://html.spec.whatwg.org/#downloading-resources">whatwg</a>. Different browsers can use different logic for
   * computing it.
   *
   * @since v1.8
   */
  String suggestedFilename();
  /**
   * Returns downloaded url.
   *
   * @since v1.8
   */
  String url();
}

