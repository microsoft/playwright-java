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

import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.driver.Driver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static com.microsoft.playwright.Utils.getBrowserTypeFromEnv;
import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestPlaywrightCreate {
  @Test
  void shouldSupportEnvSkipBrowserDownload(@TempDir Path browsersDir) throws IOException, NoSuchFieldException, IllegalAccessException {
    Map<String, String> env = mapOf("PLAYWRIGHT_BROWSERS_PATH", browsersDir.toString(),
      "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
    Playwright.CreateOptions options = new Playwright.CreateOptions().setEnv(env);

    try (Playwright playwright = PlaywrightImpl.createImpl(options, true)) {
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> getBrowserTypeFromEnv(playwright).launch());
      assertTrue(e.getMessage().contains("Looks like Playwright Test or Playwright was just installed or updated") ||
        e.getMessage().contains("Looks like Playwright was just installed or updated."), e.getMessage());

      try (DirectoryStream<Path> ds = Files.newDirectoryStream(browsersDir)) {
        for (Path child : ds) {
          fail("Unexpected file: " + child.toString());
        }
      }
    }
  }

  private static class CustomProcessBuilderDriver implements Driver.ThirdPartyDriver, Driver.ExternalProcessDriver {
    private final Driver defaultDriver = Driver.createAndInstall(Collections.emptyMap(), false);

    @Override
    public ProcessBuilder createProcessBuilder() {
      ProcessBuilder pb = defaultDriver.createProcessBuilder();
      pb.command().add("run-driver");
      return pb;
    }
  }

  @Test
  void shouldAcceptThirdPartyExternalProcessDriver() {
    Driver.ThirdPartyDriver customProxyDriver = new CustomProcessBuilderDriver();
    try (Playwright playwright = Playwright.create(null, customProxyDriver)) {
      assertNotNull(playwright.chromium());
    }
  }

  private static class CustomByteChannelDriver implements Driver.ThirdPartyDriver, Driver.ByteChannelDriver {
    private final Driver defaultDriver = Driver.createAndInstall(Collections.emptyMap(), false);

    @Override
    public ByteChannel createByteChannel() {
      ProcessBuilder pb = defaultDriver.createProcessBuilder();
      pb.command().add("run-driver");
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);

      try {
        Process process = pb.start();
        return new ByteChannel() {
          @Override
          public int read(ByteBuffer dst) throws IOException {
            int available = process.getInputStream().available();
            if (available == 0) {
              int b = process.getInputStream().read();
              if (b == -1) {
                return -1;
              }
              dst.put((byte) b);
              return 1;
            }
            int toRead = Math.min(available, dst.remaining());
            byte[] buffer = new byte[toRead];
            int bytesRead = process.getInputStream().read(buffer);
            if (bytesRead > 0) {
              dst.put(buffer, 0, bytesRead);
            }
            return bytesRead;
          }

          @Override
          public int write(ByteBuffer src) throws IOException {
            int bytesToWrite = src.remaining();
            byte[] buffer = new byte[bytesToWrite];
            src.get(buffer);
            process.getOutputStream().write(buffer);
            process.getOutputStream().flush();
            return bytesToWrite;
          }

          @Override
          public boolean isOpen() {
            return process.isAlive();
          }

          @Override
          public void close() throws IOException {
            process.getOutputStream().close();
            process.getInputStream().close();
            process.destroy();
          }
        };
      } catch (IOException e) {
        throw new RuntimeException("Failed to start Playwright driver process", e);
      }
    }
  }

  @Test
  void shouldAcceptThirdPartyByteChannelDriver() {
    Driver.ThirdPartyDriver customProxyDriver = new CustomByteChannelDriver();
    try (Playwright playwright = Playwright.create(null, customProxyDriver)) {
      assertNotNull(playwright.chromium());
    }
  }

  // This test is too slow, so we don't run it.
  void shouldSupportEnvBrowsersPath(@TempDir Path browsersDir) throws IOException {
    Map<String, String> env = mapOf("PLAYWRIGHT_BROWSERS_PATH", browsersDir.toString());
    Playwright.CreateOptions options = new Playwright.CreateOptions().setEnv(env);

    try (Playwright playwright = Playwright.create(options)) {
      try (Browser browser = playwright.chromium().launch()) {
        assertNotNull(browser);
      }

      try (DirectoryStream<Path> ds = Files.newDirectoryStream(browsersDir)) {
        for (Path child : ds) {
          assertTrue(Files.isDirectory(child));
        }
      }
    }
  }
}
