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

import com.microsoft.playwright.BrowserType;

/**
 * The {@code startRemoteServer} fixture of {@code tests/config/browserTest.ts}: starts at most one
 * remote server per test, of the kind {@code launchServer} ({@link RemoteServer}) or
 * {@code run-server} ({@link RunServer}), and closes it when the test ends.
 */
public final class StartRemoteServer implements AutoCloseable {
  private final BrowserType browserType;
  private final ChildProcessFactory childProcess;
  private final BrowserType.LaunchOptions launchOptions;
  private PlaywrightServer server;

  StartRemoteServer(BrowserType browserType, ChildProcessFactory childProcess, BrowserType.LaunchOptions launchOptions) {
    this.browserType = browserType;
    this.childProcess = childProcess;
    this.launchOptions = launchOptions;
  }

  public PlaywrightServer start(String kind) {
    return start(kind, new RemoteServerOptions());
  }

  /**
   * @param kind {@code "launchServer"} or {@code "run-server"}; the result is a {@link RemoteServer} or a {@link RunServer}
   */
  public synchronized PlaywrightServer start(String kind, RemoteServerOptions options) {
    if (server != null) {
      throw new IllegalStateException("can only start one remote server");
    }
    if ("launchServer".equals(kind)) {
      RemoteServer remoteServer = new RemoteServer();
      // The launch options are read now, so a test can change them before it starts the server.
      remoteServer.start(childProcess, browserType, Env.channel(), launchOptions, options);
      server = remoteServer;
    } else if ("run-server".equals(kind)) {
      RunServer runServer = new RunServer();
      runServer.start(childProcess, new RunServer.Options()
        .setArtifactsDir(options.artifactsDir).setUnsafe(options.unsafe).setEnv(options.env));
      server = runServer;
    } else {
      throw new IllegalArgumentException("Unknown kind " + kind);
    }
    return server;
  }

  @Override
  public synchronized void close() throws InterruptedException {
    if (server != null) {
      PlaywrightServer toClose = server;
      server = null;
      toClose.close();
      // Give any connected browsers a chance to disconnect to avoid
      // poisoning next test with quasy-alive browsers.
      Thread.sleep(1000);
    }
  }
}
