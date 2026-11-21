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

// Port of download.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;

import java.io.InputStream;
import java.nio.file.Path;

class DownloadImpl implements Download {
  private final PageImpl page;
  private final String url;
  private final String suggestedFilename;
  private final ArtifactImpl artifact;

  DownloadImpl(PageImpl page, String url, String suggestedFilename, ArtifactImpl artifact) {
    this.page = page;
    this.url = url;
    this.suggestedFilename = suggestedFilename;
    this.artifact = artifact;
  }

  @Override
  public Page page() {
    return page;
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public String suggestedFilename() {
    return suggestedFilename;
  }

  @Override
  public Path path() {
    return artifact.pathAfterFinished();
  }

  @Override
  public void saveAs(Path path) {
    artifact.saveAs(path);
  }

  @Override
  public String failure() {
    return artifact.failure();
  }

  @Override
  public InputStream createReadStream() {
    return artifact.createReadStream();
  }

  @Override
  public void cancel() {
    artifact.cancel();
  }

  @Override
  public void delete() {
    artifact.delete();
  }

  // java: the constructor PageImpl calls, it reads url and suggestedFilename from the download event.
  DownloadImpl(PageImpl page, ArtifactImpl artifact, JsonObject initializer) {
    this(page, initializer.get("url").getAsString(), initializer.get("suggestedFilename").getAsString(), artifact);
  }
}
