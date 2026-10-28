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

import com.google.gson.Gson;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code RemoteServer} of {@code tests/config/remoteServer.ts}: the {@code launchServer} kind. Runs
 * {@code tests/config/remote-server-impl.js} (copied to {@code src/test/config}) with the driver's
 * Node.js; the script calls {@code browserType.launchServer()} of the driver's {@code playwright-core}
 * and prints {@code (key=>value)} pairs.
 *
 * <p>The script does {@code require('playwright-core')}. The driver has the package at
 * {@code <driver>/package}, which is not in a {@code node_modules} directory, so the process gets
 * a {@code NODE_PATH} directory with a {@code playwright-core/index.js} that re-exports the
 * driver's package.
 */
public final class RemoteServer implements PlaywrightServer {
  private static final Pattern OUTPUT = Pattern.compile("\\(([^()]+)=>([^()]+)\\)");
  private static Path nodePathDir;

  private TestChildProcess process;
  private final Map<String, String> output = new HashMap<>();
  private final Object outputLock = new Object();
  private int index;
  private BrowserType browserType;
  private Browser browser;
  private String wsEndpoint;

  /**
   * What upstream reads from {@code browserType._playwright._defaultLaunchOptions}: the launch
   * options of the test run as a JSON-ready map (args, headless, channel, executablePath).
   */
  public static Map<String, Object> defaultLaunchOptions(BrowserType.LaunchOptions launchOptions) {
    Map<String, Object> launch = new LinkedHashMap<>();
    launch.put("args", launchOptions.args);
    launch.put("headless", launchOptions.headless);
    launch.put("channel", launchOptions.channel);
    launch.put("executablePath", launchOptions.executablePath == null ? null : launchOptions.executablePath.toString());
    return launch;
  }

  /** A directory with {@code playwright-core/index.js} for {@code NODE_PATH}. */
  private static synchronized Path nodePathDir() {
    if (nodePathDir == null) {
      try {
        Path dir = Files.createTempDirectory("playwright-java-node-path-");
        Path pkg = dir.resolve("playwright-core");
        Files.createDirectories(pkg);
        String target = RunServer.driverPackageDir().toAbsolutePath().toString();
        Files.write(pkg.resolve("index.js"),
          ("module.exports = require(" + new Gson().toJson(target) + ");\n").getBytes(StandardCharsets.UTF_8));
        File root = dir.toFile();
        root.deleteOnExit();
        pkg.toFile().deleteOnExit();
        pkg.resolve("index.js").toFile().deleteOnExit();
        nodePathDir = dir;
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    return nodePathDir;
  }

  /**
   * {@code RemoteServer._start()}.
   *
   * @param launchOptions what upstream reads from {@code _defaultLaunchOptions}
   */
  public void start(ChildProcessFactory childProcess, BrowserType browserType, String channel,
                    BrowserType.LaunchOptions launchOptions, RemoteServerOptions remoteServerOptions) {
    this.browserType = browserType;
    // Copy options to prevent a large JSON string when launching subprocess.
    Map<String, Object> launch = defaultLaunchOptions(launchOptions);
    launch.put("handleSIGINT", true);
    launch.put("handleSIGTERM", true);
    launch.put("handleSIGHUP", true);
    if (remoteServerOptions.sharedBrowser) {
      launch.put("_sharedBrowser", true);
    }
    if (remoteServerOptions.artifactsDir != null) {
      launch.put("artifactsDir", remoteServerOptions.artifactsDir);
    }
    Map<String, Object> options = new LinkedHashMap<>();
    options.put("browserTypeName", browserType.name());
    options.put("channel", channel);
    options.put("launchOptions", launch);
    if (remoteServerOptions.stallOnClose) {
      options.put("stallOnClose", true);
    }
    if (remoteServerOptions.disconnectOnSIGHUP) {
      options.put("disconnectOnSIGHUP", true);
    }
    if (remoteServerOptions.exitOnFile != null) {
      options.put("exitOnFile", remoteServerOptions.exitOnFile);
    }
    if (remoteServerOptions.exitOnWarning) {
      options.put("exitOnWarning", true);
    }
    if (remoteServerOptions.inCluster) {
      options.put("inCluster", true);
    }
    if (remoteServerOptions.url != null) {
      options.put("url", remoteServerOptions.url);
    }
    if (remoteServerOptions.startStopAndRunHttp) {
      options.put("startStopAndRunHttp", true);
    }
    if (remoteServerOptions.sharedBrowser) {
      options.put("sharedBrowser", true);
    }
    if (remoteServerOptions.artifactsDir != null) {
      options.put("artifactsDir", remoteServerOptions.artifactsDir);
    }

    List<String> command = new ArrayList<>(RunServer.driverCommand().subList(0, 1));
    command.add(Env.srcTestDir().resolve("config").resolve("remote-server-impl.js").toString());
    command.add(new Gson().toJson(options));
    Map<String, String> env = new HashMap<>();
    env.put("PWTEST_UNDER_TEST", "1");
    env.put("NODE_OPTIONS", RunServer.nodeOptions());
    String nodePath = System.getenv("NODE_PATH");
    env.put("NODE_PATH", nodePathDir() + (nodePath == null || nodePath.isEmpty() ? "" : File.pathSeparator + nodePath));
    process = childProcess.start(command, env, null, chunk -> parseOutput());

    wsEndpoint = out("wsEndpoint");

    if (remoteServerOptions.url != null) {
      browser = browserType.connect(wsEndpoint);
      Page page = browser.newPage();
      page.navigate(remoteServerOptions.url);
    }
  }

  private void parseOutput() {
    synchronized (outputLock) {
      if (process == null) {
        return;
      }
      Matcher match;
      String text = process.output();
      if (index > text.length()) {
        index = 0;
      }
      while ((match = OUTPUT.matcher(text.substring(index))).find()) {
        output.put(match.group(1), match.group(2));
        index += match.end();
      }
      outputLock.notifyAll();
    }
  }

  /** Waits until the server prints {@code (key=>value)} and returns the value. */
  public String out(String key) {
    synchronized (outputLock) {
      // The callback may have run before the field was assigned.
      if (process != null) {
        parseOutput();
      }
      while (!output.containsKey(key)) {
        if (process.exited().isDone()) {
          throw new IllegalStateException("The server exited before it printed '" + key + "'. Output:\n" + process.output());
        }
        try {
          outputLock.wait(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException(e);
        }
      }
      return output.get(key);
    }
  }

  @Override
  public String wsEndpoint() {
    return wsEndpoint;
  }

  public TestChildProcess child() {
    return process;
  }

  /** The exit code of the server process once it exits. */
  public int childExitCode() {
    return get(process.exitCode());
  }

  /** The signal that ended the server process, or null for a normal exit. */
  public String childSignal() {
    return get(process.exited()).signal;
  }

  private static <T> T get(java.util.concurrent.Future<T> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (ExecutionException e) {
      throw new IllegalStateException(e.getCause());
    }
  }

  @Override
  public void close() {
    if (browser != null) {
      browser.close();
      browser = null;
    }
    process.kill("SIGINT");
    try {
      process.exitCode().get(30, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      // upstream sends SIGINT again after 30 seconds; a server that still does not exit is killed.
      process.kill("SIGINT");
      try {
        process.exitCode().get(30, TimeUnit.SECONDS);
      } catch (TimeoutException timeout) {
        process.kill();
      } catch (InterruptedException | ExecutionException other) {
        throw new IllegalStateException(other);
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }
}
