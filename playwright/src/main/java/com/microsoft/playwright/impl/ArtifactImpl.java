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

// Port of artifact.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static com.microsoft.playwright.impl.Utils.writeToFile;

public class ArtifactImpl extends ChannelOwner {
  final ArtifactChannel channel = new ArtifactChannel(this);

  // java: from is not ported, generated results already hold the impl object.

  public ArtifactImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  public Path pathAfterFinished() {
    if (connection.isRemote()) {
      throw new PlaywrightException("Path is not available when connecting remotely. Use saveAs() to save a local copy.");
    }
    return FileSystems.getDefault().getPath(channel.pathAfterFinished(NO_TIMEOUT).value);
  }

  public void saveAs(Path path) {
    if (!connection.isRemote()) {
      ArtifactSaveAsParams params = new ArtifactSaveAsParams();
      params.path = path.toString();
      channel.saveAs(params, NO_TIMEOUT);
      return;
    }

    Stream stream = channel.saveAsStream(NO_TIMEOUT).stream;
    // java: writeToFile creates the parent directories and pipes the stream, as mkdirIfNeeded and pipe do upstream.
    writeToFile(stream.stream(), path);
  }

  public String failure() {
    return channel.failure(NO_TIMEOUT).error;
  }

  public InputStream createReadStream() {
    return channel.stream(NO_TIMEOUT).stream.stream();
  }

  // java: the 'data' chunks of the stream are read in a loop.
  byte[] readIntoBuffer() {
    final int bufLen = 1024 * 1024;
    byte[] buf = new byte[bufLen];
    int readLen;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); InputStream stream = createReadStream()) {
      while ((readLen = stream.read(buf, 0, bufLen)) != -1) {
        outputStream.write(buf, 0, readLen);
      }
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read artifact",  e);
    }
  }

  public void cancel() {
    channel.cancel(NO_TIMEOUT);
  }

  public void delete() {
    channel.delete(NO_TIMEOUT);
  }

  // java: the old name of readIntoBuffer, BrowserImpl calls it.
  byte[] readAllBytes() {
    return readIntoBuffer();
  }
}
