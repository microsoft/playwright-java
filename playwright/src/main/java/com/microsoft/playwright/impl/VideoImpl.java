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

// Port of video.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Video;

import java.nio.file.Path;
import java.nio.file.Paths;

class VideoImpl extends EventEmitter implements Video {
  private ArtifactImpl artifact;
  private boolean isRemote = false;

  VideoImpl(Connection connection, ArtifactImpl artifact) {
    this.isRemote = connection.isRemote();
    this.artifact = artifact;
  }

  @Override
  public Path path() {
    if (isRemote)
      throw new PlaywrightException("Path is not available when connecting remotely. Use saveAs() to save a local copy.");
    if (artifact == null)
      throw new PlaywrightException("Video recording has not been started.");
    return Paths.get(artifact.initializer.get("absolutePath").getAsString());
  }

  @Override
  public void saveAs(Path path) {
    if (artifact == null)
      throw new PlaywrightException("Video recording has not been started.");
    artifact.saveAs(path);
  }

  @Override
  public void delete() {
    if (artifact != null)
      artifact.delete();
  }

  // java: the constructor PageImpl calls.
  VideoImpl(PageImpl page, ArtifactImpl artifact) {
    this(page.connection, artifact);
  }
}
