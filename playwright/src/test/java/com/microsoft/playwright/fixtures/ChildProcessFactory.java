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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The {@code childProcess} fixture of {@code tests/config/commonFixtures.ts}: starts
 * {@link TestChildProcess}es and kills them all when the test ends, printing their output when
 * the test failed. Also {@link #findFreePort()} and {@link #waitForPort(int)}.
 */
public final class ChildProcessFactory implements AutoCloseable {
  private final ExtensionContext context;
  private final List<TestChildProcess> processes = new ArrayList<>();

  ChildProcessFactory(ExtensionContext context) {
    this.context = context;
  }

  public TestChildProcess start(List<String> command) {
    return start(command, null, null, null);
  }

  /** {@code childProcess({ command, env, cwd, onOutput })}. */
  public TestChildProcess start(List<String> command, Map<String, String> env, Path cwd, Consumer<String> onOutput) {
    TestChildProcess process = new TestChildProcess(command, env, cwd, onOutput);
    synchronized (processes) {
      processes.add(process);
    }
    return process;
  }

  @Override
  public void close() throws Exception {
    List<TestChildProcess> toKill;
    synchronized (processes) {
      toKill = new ArrayList<>(processes);
      processes.clear();
    }
    List<CompletableFuture<TestChildProcess.Exit>> exits = new ArrayList<>();
    for (TestChildProcess process : toKill) {
      exits.add(process.kill());
    }
    for (CompletableFuture<TestChildProcess.Exit> exit : exits) {
      exit.get();
    }
    boolean failed = context.getExecutionException()
      .filter(e -> !(e instanceof TestAbortedException))
      .isPresent();
    if (failed && System.getenv("PWTEST_DEBUG") == null) {
      for (TestChildProcess process : toKill) {
        System.out.println("====== " + String.join(" ", process.command()));
        System.out.println(process.fullOutput().replace("\u001Bc", ""));
        System.out.println("=========================================");
      }
    }
  }

  /** The {@code findFreePort} fixture: a port that was free on 127.0.0.1 a moment ago. */
  public static int findFreePort() {
    try (ServerSocket server = new ServerSocket()) {
      server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0));
      return server.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /** The {@code waitForPort} fixture: blocks until 127.0.0.1:port accepts a connection. */
  public static void waitForPort(int port) {
    while (true) {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress("127.0.0.1", port), 1000);
        return;
      } catch (IOException e) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException(interrupted);
        }
      }
    }
  }
}
