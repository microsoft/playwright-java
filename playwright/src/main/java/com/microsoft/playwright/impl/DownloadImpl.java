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

package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;

import java.io.InputStream;
import java.nio.file.Path;

class DownloadImpl implements Download {
  private final PageImpl page;
  private final ArtifactImpl artifact;
  private final JsonObject initializer;

  DownloadImpl(PageImpl page, ArtifactImpl artifact, JsonObject initializer) {
    this.page = page;
    this.artifact = artifact;
    this.initializer = initializer;
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  @Override
  public String suggestedFilename() {
    return initializer.get("suggestedFilename").getAsString();
  }

  @Override
  public void cancel() {
    page.withLogging("Download.cancel", () -> artifact.cancel());
  }

  @Override
  public InputStream createReadStream() {
    return page.withLogging("Download.createReadStream", () -> artifact.createReadStream());
  }

  @Override
  public void delete() {
    page.withLogging("Download.delete", () -> artifact.delete());
  }

  @Override
  public String failure() {
    return page.withLogging("Download.failure", () -> artifact.failure());
  }

  @Override
  public Page page() {
    return page;
  }

  @Override
  public Path path() {
    return page.withLogging("Download.path", () -> artifact.pathAfterFinished());
  }

  @Override
  public void saveAs(Path path) {
    page.withLogging("Download.saveAs", () -> artifact.saveAs(path));
  }
}
