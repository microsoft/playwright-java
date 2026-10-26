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

import com.microsoft.playwright.impl.driver.Driver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** {@code RunServer} of {@code tests/config/remoteServer.ts}: runs {@code playwright run-server} of the driver. */
public final class RunServer implements PlaywrightServer {
  /** What upstream passes to {@code RunServer.start()}. */
  public static final class Options {
    /** {@code "extension"} or null for the default mode. */
    public String mode;
    /** Extra environment variables; a null value removes the variable. */
    public Map<String, String> env;
    public String artifactsDir;
    public boolean unsafe;

    public Options setMode(String mode) {
      this.mode = mode;
      return this;
    }

    public Options setEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }

    public Options setArtifactsDir(String artifactsDir) {
      this.artifactsDir = artifactsDir;
      return this;
    }

    public Options setUnsafe(boolean unsafe) {
      this.unsafe = unsafe;
      return this;
    }
  }

  private static final String PREFIX = "Listening on ";

  private TestChildProcess process;
  private String wsEndpoint;

  /** {@code node, cli.js} of the driver the tests run with. */
  static List<String> driverCommand() {
    List<String> command = Driver.ensureDriverInstalled(Collections.<String, String>emptyMap(), false)
      .createProcessBuilder().command();
    return new ArrayList<>(command.subList(0, 2));
  }

  /** The directory of the driver's {@code playwright-core} package. */
  public static Path driverPackageDir() {
    return Paths.get(driverCommand().get(1)).getParent();
  }

  static String nodeOptions() {
    return System.getenv("NODE_OPTIONS");
  }

  public void start(ChildProcessFactory childProcess) {
    start(childProcess, new Options());
  }

  public void start(ChildProcessFactory childProcess, Options options) {
    List<String> command = driverCommand();
    command.add("run-server");
    if ("extension".equals(options.mode)) {
      command.add("--mode=extension");
    }
    if (options.artifactsDir != null && !options.artifactsDir.isEmpty()) {
      command.add("--artifacts-dir=" + options.artifactsDir);
    }
    if (options.unsafe) {
      command.add("--unsafe");
    }
    Map<String, String> env = new HashMap<>();
    env.put("PWTEST_UNDER_TEST", "1");
    env.put("NODE_OPTIONS", nodeOptions());
    if (options.env != null) {
      env.putAll(options.env);
    }
    CompletableFuture<String> wsEndpointPromise = new CompletableFuture<>();
    process = childProcess.start(command, env, null, chunk -> {
      String output = process == null ? chunk : process.output();
      int index = output.indexOf(PREFIX);
      int end = index == -1 ? -1 : output.indexOf('\n', index);
      if (end != -1) {
        wsEndpointPromise.complete(output.substring(index + PREFIX.length(), end).trim());
      }
    });
    // The first chunk may arrive before the field is assigned.
    String output = process.output();
    int index = output.indexOf(PREFIX);
    int end = index == -1 ? -1 : output.indexOf('\n', index);
    if (end != -1) {
      wsEndpointPromise.complete(output.substring(index + PREFIX.length(), end).trim());
    }
    process.exited().thenRun(() -> wsEndpointPromise.completeExceptionally(
      new IllegalStateException("run-server exited before it listened. Output:\n" + process.output())));
    try {
      wsEndpoint = wsEndpointPromise.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (java.util.concurrent.ExecutionException e) {
      throw new IllegalStateException(e.getCause());
    }
  }

  @Override
  public String wsEndpoint() {
    return wsEndpoint;
  }

  public TestChildProcess child() {
    return process;
  }

  @Override
  public void close() {
    try {
      process.kill("SIGINT").get(30, java.util.concurrent.TimeUnit.SECONDS);
    } catch (java.util.concurrent.TimeoutException e) {
      process.kill();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (java.util.concurrent.ExecutionException e) {
      throw new IllegalStateException(e.getCause());
    }
  }
}
