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

package com.microsoft.playwright.fixtures;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * The {@code createUserDataDir} fixture: fresh user data directories for persistent contexts,
 * removed when the test ends.
 */
public final class CreateUserDataDir implements AutoCloseable {
  private final List<Path> dirs = new ArrayList<>();

  CreateUserDataDir() {
  }

  public Path create() {
    try {
      Path dir = Files.createTempDirectory("playwright-test-");
      synchronized (dirs) {
        dirs.add(dir);
      }
      return dir;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    for (Path dir : dirs) {
      try (Stream<Path> paths = Files.walk(dir)) {
        paths.sorted(Comparator.reverseOrder()).forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            // Best effort: Firefox removes its lock file later, from another process.
          }
        });
      } catch (IOException e) {
        // The directory is already gone.
      }
    }
  }
}
