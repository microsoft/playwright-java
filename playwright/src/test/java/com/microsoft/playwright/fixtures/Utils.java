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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helpers of the upstream tests: {@code tests/config/utils.ts}, {@code tests/config/errors.ts}
 * and the {@code asset} fixture. Meant to be statically imported.
 */
public final class Utils {
  /** {@code kTargetClosedErrorMessage} of {@code tests/config/errors.ts}. */
  public static final String kTargetClosedErrorMessage = "Target page, context or browser has been closed";

  private Utils() {
  }

  /** {@code asset('file-to-upload.txt')}: a file of {@code src/test/assets}. */
  public static Path asset(String path) {
    Path result = Env.srcTestDir().resolve("assets");
    for (String segment : path.split("/")) {
      result = result.resolve(segment);
    }
    return result;
  }

  /** {@code path.relative(process.cwd(), path)}: like Node.js, the absolute path when it is on another drive. */
  public static Path relativeToCwd(Path path) {
    Path cwd = Paths.get("").toAbsolutePath();
    Path absolute = path.toAbsolutePath();
    return cwd.getRoot().equals(absolute.getRoot()) ? cwd.relativize(absolute) : absolute;
  }

  public static Frame attachFrame(Page page, String frameId, String url) {
    JSHandle handle = page.evaluateHandle("async ({ frameId, url }) => {\n" +
      "  const frame = document.createElement('iframe');\n" +
      "  frame.src = url;\n" +
      "  frame.id = frameId;\n" +
      "  document.body.appendChild(frame);\n" +
      "  await new Promise(x => frame.onload = x);\n" +
      "  return frame;\n" +
      "}", mapOf("frameId", frameId, "url", url));
    return handle.asElement().contentFrame();
  }

  public static void detachFrame(Page page, String frameId) {
    page.evaluate("frameId => {\n" +
      "  document.getElementById(frameId).remove();\n" +
      "}", frameId);
  }

  public static void verifyViewport(Page page, int width, int height) {
    assertEquals(width, page.viewportSize().width);
    assertEquals(height, page.viewportSize().height);
    assertEquals(width, page.evaluate("window.innerWidth"));
    assertEquals(height, page.evaluate("window.innerHeight"));
  }

  /** The error a browser reports for the self-signed certificate of the https server. */
  public static Pattern expectedSSLError(BrowserName browserName) {
    if (browserName == BrowserName.CHROMIUM) {
      return Pattern.compile("net::(ERR_CERT_AUTHORITY_INVALID|ERR_CERT_INVALID)");
    }
    if (browserName == BrowserName.WEBKIT) {
      if (Env.isMac) {
        return Pattern.compile("The certificate for this server is invalid");
      } else if (Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
        return Pattern.compile("SSL peer certificate or SSH remote key was not OK");
      } else {
        return Pattern.compile("Unacceptable TLS certificate|Operation was cancelled");
      }
    }
    if (browserName == BrowserName.FIREFOX && Env.isBidi()) {
      return Pattern.compile("MOZILLA_PKIX_ERROR_SELF_SIGNED_CERT");
    }
    return Pattern.compile("SSL_ERROR_UNKNOWN");
  }

  public static boolean chromiumVersionLessThan(String a, String b) {
    String[] left = a.split("\\.");
    String[] right = b.split("\\.");
    for (int i = 0; i < 4; i++) {
      int l = Integer.parseInt(left[i]);
      int r = Integer.parseInt(right[i]);
      if (l > r) {
        return false;
      }
      if (l < r) {
        return true;
      }
    }
    return false;
  }

  /** The {@code browserMajorVersion} fixture. */
  public static int browserMajorVersion(Browser browser) {
    return Integer.parseInt(browser.version().split("\\.")[0]);
  }

  /** Waits for two animation frames in the page or frame, the {@code rafraf} helper. */
  public static void rafraf(Page target) {
    rafraf(target, 1);
  }

  public static void rafraf(Page target, int count) {
    for (int i = 0; i < count; i++) {
      target.evaluate("async () => {\n" +
        "  await new Promise(f => window.builtins.requestAnimationFrame(() => window.builtins.requestAnimationFrame(f)));\n" +
        "}");
    }
  }

  public static void rafraf(Frame target) {
    rafraf(target, 1);
  }

  public static void rafraf(Frame target, int count) {
    for (int i = 0; i < count; i++) {
      target.evaluate("async () => {\n" +
        "  await new Promise(f => window.builtins.requestAnimationFrame(() => window.builtins.requestAnimationFrame(f)));\n" +
        "}");
    }
  }

  public static BoundingBox roundBox(BoundingBox box) {
    BoundingBox result = new BoundingBox();
    result.x = Math.round(box.x);
    result.y = Math.round(box.y);
    result.width = Math.round(box.width);
    result.height = Math.round(box.height);
    return result;
  }

  /** Removes the common indentation and the blank lines of a snapshot literal. */
  public static String unshift(String snapshot) {
    String[] lines = snapshot.split("\n");
    int whitespacePrefixLength = 100;
    for (String line : lines) {
      if (line.trim().isEmpty()) {
        continue;
      }
      int indent = line.length() - line.replaceAll("^\\s+", "").length();
      if (indent < whitespacePrefixLength) {
        whitespacePrefixLength = indent;
      }
    }
    StringBuilder result = new StringBuilder();
    for (String line : lines) {
      if (line.trim().isEmpty()) {
        continue;
      }
      if (result.length() > 0) {
        result.append('\n');
      }
      result.append(line.substring(whitespacePrefixLength));
    }
    return result.toString();
  }

  /** The {@code toContainYaml} matcher of {@code tests/page/pageTest.ts}. */
  public static void expectToContainYaml(String received, String expected) {
    assertTrue(received.contains(unshift(expected)), "Expected to contain:\n" + unshift(expected) + "\nReceived:\n" + received);
  }

  public static void expectNotToContainYaml(String received, String expected) {
    assertFalse(received.contains(unshift(expected)), "Expected not to contain:\n" + unshift(expected) + "\nReceived:\n" + received);
  }

  private static final Pattern ANSI = Pattern.compile("[\\u001B\\u009B][\\[\\]()#;?]*(?:(?:(?:[a-zA-Z\\d]*(?:;[-a-zA-Z\\d/#&.:=?%@~_]*)*)?\\u0007)|(?:(?:\\d{0,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-ntqry=><~]))");

  public static String stripAnsi(String str) {
    return ANSI.matcher(str).replaceAll("");
  }

  /** The entries of a zip file such as a HAR or trace archive. */
  public static Map<String, byte[]> parseZip(Path file) {
    Map<String, byte[]> resources = new LinkedHashMap<>();
    try (ZipFile zip = new ZipFile(file.toFile())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream in = zip.getInputStream(entry)) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          byte[] buffer = new byte[8192];
          for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
            out.write(buffer, 0, read);
          }
          resources.put(entry.getName(), out.toByteArray());
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return resources;
  }

  /** A map from alternating keys and values, for {@code page.evaluate} arguments. */
  public static Map<String, Object> mapOf(Object... keysAndValues) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      map.put((String) keysAndValues[i], keysAndValues[i + 1]);
    }
    return map;
  }

  @SafeVarargs
  public static <T> java.util.List<T> listOf(T... values) {
    return Arrays.asList(values);
  }
}
