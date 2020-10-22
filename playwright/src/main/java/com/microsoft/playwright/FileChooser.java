/**
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

import java.io.File;
import java.util.*;

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
    public Boolean noWaitAfter;
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
  ElementHandle element();
  boolean isMultiple();
  Page page();
  default void setFiles(File file) { setFiles(file, null); }
  default void setFiles(File file, SetFilesOptions options) { setFiles(new File[]{ file }, options); }
  default void setFiles(File[] files) { setFiles(files, null); }
  void setFiles(File[] files, SetFilesOptions options);
  default void setFiles(FileChooser.FilePayload file) { setFiles(file, null); }
  default void setFiles(FileChooser.FilePayload file, SetFilesOptions options)  { setFiles(new FileChooser.FilePayload[]{ file }, options); }
  default void setFiles(FileChooser.FilePayload[] files) { setFiles(files, null); }
  void setFiles(FileChooser.FilePayload[] files, SetFilesOptions options);
}

