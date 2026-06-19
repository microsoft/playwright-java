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

package com.microsoft.playwright.impl.driver.jar;

import com.microsoft.playwright.impl.driver.Driver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DriverJar extends Driver {
  private static final String PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD";
  private static final String SELENIUM_REMOTE_URL = "SELENIUM_REMOTE_URL";
  private final Path driverTempDir;
  private final boolean deleteOnExit;
  private Path preinstalledNodePath;

  public DriverJar() throws IOException {
    this(createTempDriverDir(), true);
    String nodePath = System.getProperty("playwright.nodejs.path");
    if (nodePath != null) {
      preinstalledNodePath = Paths.get(nodePath);
      if (!Files.exists(preinstalledNodePath)) {
        throw new RuntimeException("Invalid Node.js path specified: " + nodePath);
      }
    }
    logMessage("created DriverJar: " + driverTempDir);
  }

  private DriverJar(Path driverDir, boolean deleteOnExit) {
    this.driverTempDir = driverDir;
    this.deleteOnExit = deleteOnExit;
    if (deleteOnExit) {
      driverTempDir.toFile().deleteOnExit();
    }
  }

  private static Path createTempDriverDir() throws IOException {
    // Allow specifying custom path for the driver installation
    // See https://github.com/microsoft/playwright-java/issues/728
    String alternativeTmpdir = System.getProperty("playwright.driver.tmpdir");
    String prefix = "playwright-java-";
    return alternativeTmpdir == null
      ? Files.createTempDirectory(prefix)
      : Files.createTempDirectory(Paths.get(alternativeTmpdir), prefix);
  }

  // Extracts the driver (playwright-core package and the Node.js binary for the current platform)
  // into the given directory, persistently. Point playwright.cli.dir / PLAYWRIGHT_DRIVER_DIR at it
  // to run without extracting to a temp directory on every launch. See issue #1268.
  public static void installDriverTo(Path driverDir) throws IOException, URISyntaxException {
    Files.createDirectories(driverDir);
    new DriverJar(driverDir, false).extractDriverToTempDir();
  }

  @Override
  protected void initialize(Boolean installBrowsers) throws Exception {
    if (preinstalledNodePath == null && env.containsKey(PLAYWRIGHT_NODEJS_PATH)) {
      preinstalledNodePath = Paths.get(env.get(PLAYWRIGHT_NODEJS_PATH));
      if (!Files.exists(preinstalledNodePath)) {
        throw new RuntimeException("Invalid Node.js path specified: " + preinstalledNodePath);
      }
    } else if (preinstalledNodePath != null) {
      // Pass the env variable to the driver process.
      env.put(PLAYWRIGHT_NODEJS_PATH, preinstalledNodePath.toString());
    }
    extractDriverToTempDir();
    logMessage("extracted driver from jar to " + driverDir());
    if (installBrowsers)
      installBrowsers(env);
  }

  private void installBrowsers(Map<String, String> env) throws IOException, InterruptedException {
    String skip = env.get(PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD);
    if (skip == null) {
      skip = System.getenv(PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD);
    }
    if (skip != null && !"0".equals(skip) && !"false".equals(skip)) {
      logMessage("Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set");
      return;
    }
    if (env.get(SELENIUM_REMOTE_URL) != null || System.getenv(SELENIUM_REMOTE_URL) != null) {
      logMessage("Skipping browsers download because `SELENIUM_REMOTE_URL` env variable is set");
      return;
    }
    Path driver = driverDir();
    if (!Files.exists(driver)) {
      throw new RuntimeException("Failed to find driver: " + driver);
    }
    ProcessBuilder pb = createProcessBuilder();
    pb.command().add("install");
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    Process p = pb.start();
    boolean result = p.waitFor(10, TimeUnit.MINUTES);
    if (!result) {
      p.destroy();
      throw new RuntimeException("Timed out waiting for browsers to install");
    }
    if (p.exitValue() != 0) {
      throw new RuntimeException("Failed to install browsers, exit code: " + p.exitValue());
    }
  }

  private static boolean isExecutable(Path filePath) {
    String name = filePath.getFileName().toString();
    return name.endsWith(".sh") || name.endsWith(".exe") || !name.contains(".");
  }

  private FileSystem initFileSystem(URI uri) throws IOException {
    try {
      return FileSystems.newFileSystem(uri, Collections.emptyMap());
    } catch (FileSystemAlreadyExistsException e) {
      return null;
    }
  }

  public static URI getDriverResourceURI() throws URISyntaxException {
    ClassLoader classloader = DriverJar.class.getClassLoader();
    return classloader.getResource("driver/" + platformDir()).toURI();
  }

  void extractDriverToTempDir() throws URISyntaxException, IOException {
    extractResourceToDir("driver/package", driverTempDir.resolve("package"));
    if (preinstalledNodePath == null) {
      String platformResource = "driver/" + platformDir();
      if (DriverJar.class.getClassLoader().getResource(platformResource) == null) {
        throw new RuntimeException("Failed to find the bundled Node.js for platform '" + platformDir()
          + "'. Add the com.microsoft.playwright:driver-bundle dependency, or set the "
          + PLAYWRIGHT_NODEJS_PATH + " environment variable (or the playwright.nodejs.path system "
          + "property) to point at a preinstalled Node.js.");
      }
      extractResourceToDir(platformResource, driverTempDir);
    }
  }

  private void extractResourceToDir(String resourcePath, Path destDir) throws URISyntaxException, IOException {
    URI originalUri = DriverJar.class.getClassLoader().getResource(resourcePath).toURI();
    URI uri = maybeExtractNestedJar(originalUri);

    // Create zip filesystem if loading from jar.
    try (FileSystem fileSystem = "jar".equals(uri.getScheme()) ? initFileSystem(uri) : null) {
      Path srcRoot = Paths.get(uri);
      // jar file system's .relativize gives wrong results when used with
      // spring-boot-maven-plugin, convert to the default filesystem to
      // have predictable results.
      // See https://github.com/microsoft/playwright-java/issues/306
      Path srcRootDefaultFs = Paths.get(srcRoot.toString());
      Files.walk(srcRoot).forEach(fromPath -> {
        Path relative = srcRootDefaultFs.relativize(Paths.get(fromPath.toString()));
        Path toPath = destDir.resolve(relative.toString());
        try {
          if (Files.isDirectory(fromPath)) {
            Files.createDirectories(toPath);
          } else {
            Files.copy(fromPath, toPath);
            if (isExecutable(toPath)) {
              toPath.toFile().setExecutable(true, true);
            }
          }
          if (deleteOnExit) {
            toPath.toFile().deleteOnExit();
          }
        } catch (IOException e) {
          throw new RuntimeException("Failed to extract driver from " + uri + ", full uri: " + originalUri, e);
        }
      });
    }
  }

  private URI maybeExtractNestedJar(final URI uri) throws URISyntaxException {
    if (!"jar".equals(uri.getScheme())) {
      return uri;
    }
    final String JAR_URL_SEPARATOR = "!/";
    String[] parts = uri.toString().split("!/");
    if (parts.length != 3) {
      return uri;
    }
    String innerJar = String.join(JAR_URL_SEPARATOR, parts[0], parts[1]);
    URI jarUri = new URI(innerJar);
    try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
      Path fromPath = Paths.get(jarUri);
      Path toPath = driverTempDir.resolve(fromPath.getFileName().toString());
      Files.copy(fromPath, toPath);
      if (deleteOnExit) {
        toPath.toFile().deleteOnExit();
      }
      return new URI("jar:" + toPath.toUri() + JAR_URL_SEPARATOR + parts[2]);
    } catch (IOException e) {
      throw new RuntimeException("Failed to extract driver's nested .jar from " + jarUri + "; full uri: " + uri, e);
    }
  }

  private static String platformDir() {
    String name = System.getProperty("os.name").toLowerCase();
    String arch = System.getProperty("os.arch").toLowerCase();

    if (name.contains("windows")) {
      return "win32_x64";
    }
    if (name.contains("linux")) {
      if (arch.equals("aarch64")) {
        return "linux-arm64";
      } else {
        return "linux";
      }
    }
    if (name.contains("mac os x")) {
      if (arch.equals("aarch64")) {
        return "mac-arm64";
      } else {
        return "mac";
      }
    }
    throw new RuntimeException("Unexpected os.name value: " + name);
  }

  @Override
  public Path driverDir() {
    return driverTempDir;
  }
}
