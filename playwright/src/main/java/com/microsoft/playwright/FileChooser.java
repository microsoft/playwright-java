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
import java.util.*;

/**
 * FileChooser objects are dispatched by the page in the page.on('filechooser') event.
 * <p>
 */
public interface FileChooser {
  class FilePayload {
    public final String name;
    public final String mimeType;
    public final byte[] buffer;

    public FilePayload(String name, String mimeType, byte[] buffer) {
      this.name = name;
      this.mimeType = mimeType;
      this.buffer = buffer;
    }
  }

  class SetFilesOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the {@code browserContext.setDefaultTimeout(timeout)} or {@code page.setDefaultTimeout(timeout)} methods.
     */
    public Integer timeout;

    public SetFilesOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetFilesOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Returns input element associated with this file chooser.
   */
  ElementHandle element();
  /**
   * Returns whether this file chooser accepts multiple files.
   */
  boolean isMultiple();
  /**
   * Returns page this file chooser belongs to.
   */
  Page page();
  default void setFiles(Path file) { setFiles(file, null); }
  default void setFiles(Path file, SetFilesOptions options) { setFiles(new Path[]{ file }, options); }
  default void setFiles(Path[] files) { setFiles(files, null); }
  void setFiles(Path[] files, SetFilesOptions options);
  default void setFiles(FileChooser.FilePayload file) { setFiles(file, null); }
  default void setFiles(FileChooser.FilePayload file, SetFilesOptions options)  { setFiles(new FileChooser.FilePayload[]{ file }, options); }
  default void setFiles(FileChooser.FilePayload[] files) { setFiles(files, null); }
  /**
   * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then they are resolved relative to the the current working directory. For empty array, clears the selected files.
   */
  void setFiles(FileChooser.FilePayload[] files, SetFilesOptions options);
}

