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
 * {@code FileChooser} objects are dispatched by the page in the {@link Page#onFileChooser Page.onFileChooser()} event.
 * <pre>{@code
 * FileChooser fileChooser = page.waitForFileChooser(() -> page.getByText("Upload file").click());
 * fileChooser.setFiles(Paths.get("myfile.pdf"));
 * }</pre>
 */
public interface FileChooser {
  class SetFilesOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SetFilesOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetFilesOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Returns input element associated with this file chooser.
   *
   * @since v1.8
   */
  ElementHandle element();
  /**
   * Returns whether this file chooser accepts multiple files.
   *
   * @since v1.8
   */
  boolean isMultiple();
  /**
   * Returns page this file chooser belongs to.
   *
   * @since v1.8
   */
  Page page();
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  default void setFiles(Path files) {
    setFiles(files, null);
  }
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  void setFiles(Path files, SetFilesOptions options);
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  default void setFiles(Path[] files) {
    setFiles(files, null);
  }
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  void setFiles(Path[] files, SetFilesOptions options);
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  default void setFiles(FilePayload files) {
    setFiles(files, null);
  }
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  void setFiles(FilePayload files, SetFilesOptions options);
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  default void setFiles(FilePayload[] files) {
    setFiles(files, null);
  }
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths,
   * then they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * @since v1.8
   */
  void setFiles(FilePayload[] files, SetFilesOptions options);
}

