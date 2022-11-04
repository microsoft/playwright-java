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

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Video;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

class VideoImpl implements Video {
  private final PageImpl page;
  private final WaitableResult<ArtifactImpl> waitableArtifact = new WaitableResult<>();

  VideoImpl(PageImpl page) {
    this.page = page;
    BrowserImpl browser = page.context().browser();
  }

  void setArtifact(ArtifactImpl artifact) {
    waitableArtifact.complete(artifact);
  }

  private ArtifactImpl waitForArtifact() {
    Waitable<ArtifactImpl> waitable = new WaitableRace<>(asList(waitableArtifact, (Waitable<ArtifactImpl>) page.waitableClosedOrCrashed));
    return page.runUntil(() -> {}, waitable);
  }

  @Override
  public void delete() {
    page.withLogging("Video.delete", () -> {
      try {
        waitForArtifact().delete();
      } catch (PlaywrightException e) {
      }
    });
  }

  @Override
  public Path path() {
    return page.withLogging("Video.path", () -> {
      if (page.connection.isRemote) {
        throw new PlaywrightException("Path is not available when using browserType.connect(). Use saveAs() to save a local copy.");
      }
      try {
        return Paths.get(waitForArtifact().initializer.get("absolutePath").getAsString());
      } catch (PlaywrightException e) {
        throw new PlaywrightException("Page did not produce any video frames", e);
      }
    });
  }

  @Override
  public void saveAs(Path path) {
    page.withLogging("Video.saveAs", () -> {
      if (!page.isClosed()) {
        throw new PlaywrightException("Page is not yet closed. Close the page prior to calling saveAs");
      }
      try {
        waitForArtifact().saveAs(path);
      } catch (PlaywrightException e) {
        throw new PlaywrightException("Page did not produce any video frames", e);
      }
    });
  }
}
