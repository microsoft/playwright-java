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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * A child process of a test with its output collected, the port of {@code TestChildProcess} in
 * {@code tests/config/commonFixtures.ts}. The environment is the one of the test run without the
 * CI variables ({@code inheritAndCleanEnv}). {@link #kill()} kills the whole process tree.
 *
 * <p>Java 8 can not start a process in a new process group, so {@code kill("SIGINT")} signals
 * the process itself rather than its group, and the tree is found from its parent links.
 */
public final class TestChildProcess {
  /** How a process ended. */
  public static final class Exit {
    public final int exitCode;
    /** The signal name such as {@code SIGKILL} when the process was killed by a signal, else null. */
    public final String signal;

    Exit(int exitCode, String signal) {
      this.exitCode = exitCode;
      this.signal = signal;
    }

    @Override
    public String toString() {
      return "exitCode=" + exitCode + ", signal=" + signal;
    }
  }

  private static final String[] CLEANED_ENV = {
    "CI", "BUILD_URL", "CI_COMMIT_SHA", "CI_JOB_URL", "CI_PROJECT_URL", "GITHUB_ACTIONS",
    "GITHUB_REPOSITORY", "GITHUB_RUN_ID", "GITHUB_SERVER_URL", "GITHUB_SHA", "GITHUB_EVENT_PATH",
    "PW_TEST_HTML_REPORT_OPEN", "PLAYWRIGHT_HTML_OPEN", "PW_TEST_DEBUG_REPORTERS", "PW_TEST_REPORTER",
    "PW_TEST_REPORTER_WS_ENDPOINT", "PW_TEST_SOURCE_TRANSFORM", "PW_TEST_SOURCE_TRANSFORM_SCOPE",
    "PWTEST_BOT_NAME", "PWTEST_SHARD_WEIGHTS", "TEST_WORKER_INDEX", "TEST_PARALLEL_INDEX", "NODE_OPTIONS",
  };
  private static final List<TestChildProcess> live = new CopyOnWriteArrayList<>();

  static {
    // Like process.on('exit', killProcessGroup) upstream: no child outlives the test run.
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      for (TestChildProcess process : live) {
        process.killProcessTree("SIGKILL");
      }
    }));
  }

  private final List<String> command;
  private final Process process;
  private final long pid;
  private final StringBuilder output = new StringBuilder();
  private final StringBuilder stdout = new StringBuilder();
  private final StringBuilder stderr = new StringBuilder();
  private final StringBuilder fullOutput = new StringBuilder();
  private final Object lock = new Object();
  private final CompletableFuture<Exit> exited = new CompletableFuture<>();
  private volatile Consumer<String> onOutput;
  private int openStreams = 2;
  private volatile boolean sentKill;

  /** The environment of the test run without the CI variables, plus {@code env} (a null value removes the variable). */
  public static Map<String, String> inheritAndCleanEnv(Map<String, String> env) {
    Map<String, String> result = new HashMap<>(System.getenv());
    for (String name : CLEANED_ENV) {
      result.remove(name);
    }
    if (env != null) {
      for (Map.Entry<String, String> entry : env.entrySet()) {
        if (entry.getValue() == null) {
          result.remove(entry.getKey());
        } else {
          result.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return result;
  }

  public TestChildProcess(List<String> command) {
    this(command, null, null, null);
  }

  /**
   * @param env variables to set on top of the cleaned environment; a null value removes the variable
   * @param cwd working directory, or null
   * @param onOutput called with every chunk of stdout or stderr
   */
  public TestChildProcess(List<String> command, Map<String, String> env, Path cwd, Consumer<String> onOutput) {
    this.command = new ArrayList<>(command);
    this.onOutput = onOutput;
    ProcessBuilder builder = new ProcessBuilder(Env.isWindows ? quoteForWindows(command) : command);
    builder.environment().clear();
    builder.environment().putAll(inheritAndCleanEnv(env));
    if (cwd != null) {
      builder.directory(cwd.toFile());
    }
    if (System.getenv("PWTEST_DEBUG") != null) {
      System.out.print("\n\nLaunching " + String.join(" ", command) + "\n");
    }
    try {
      process = builder.start();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to start " + command, e);
    }
    pid = pidOf(process);
    live.add(this);
    startReader(process.getInputStream(), false);
    startReader(process.getErrorStream(), true);
    Thread waiter = new Thread(() -> {
      int exitValue;
      try {
        exitValue = process.waitFor();
      } catch (InterruptedException e) {
        return;
      }
      // Let the readers drain what the process wrote before reporting the exit.
      synchronized (lock) {
        long deadline = System.currentTimeMillis() + 5000;
        while (openStreams > 0 && System.currentTimeMillis() < deadline) {
          try {
            lock.wait(100);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
      live.remove(this);
      exited.complete(new Exit(exitValue, signalOf(exitValue)));
    }, "child-process-waiter");
    waiter.setDaemon(true);
    waiter.start();
  }

  private void startReader(InputStream stream, boolean isStderr) {
    Thread thread = new Thread(() -> {
      char[] buffer = new char[8192];
      try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
        int read;
        while ((read = reader.read(buffer)) != -1) {
          appendChunk(new String(buffer, 0, read), isStderr);
        }
      } catch (IOException e) {
        // The stream is closed when the process ends.
      } finally {
        synchronized (lock) {
          openStreams--;
          lock.notifyAll();
        }
      }
    }, "child-process-reader");
    thread.setDaemon(true);
    thread.start();
  }

  private void appendChunk(String chunk, boolean isStderr) {
    synchronized (lock) {
      output.append(chunk);
      (isStderr ? stderr : stdout).append(chunk);
      if (System.getenv("PWTEST_DEBUG") != null) {
        System.out.print(chunk);
      } else {
        fullOutput.append(chunk);
      }
    }
    Consumer<String> callback = onOutput;
    if (callback != null) {
      callback.accept(chunk);
    }
    synchronized (lock) {
      lock.notifyAll();
    }
  }

  public List<String> command() {
    return command;
  }

  /** The process id, or -1 when this JVM can not tell. */
  public long pid() {
    return pid;
  }

  public void onOutput(Consumer<String> onOutput) {
    this.onOutput = onOutput;
  }

  public String output() {
    synchronized (lock) {
      return output.toString();
    }
  }

  public String stdout() {
    synchronized (lock) {
      return stdout.toString();
    }
  }

  public String stderr() {
    synchronized (lock) {
      return stderr.toString();
    }
  }

  public String fullOutput() {
    synchronized (lock) {
      return fullOutput.toString();
    }
  }

  /** The lines of the output that start with {@code %%}, without the prefix. */
  public List<String> outputLines() {
    List<String> lines = new ArrayList<>();
    for (String line : Utils.stripAnsi(output()).split("\n")) {
      if (line.startsWith("%%")) {
        lines.add(line.substring(2).trim());
      }
    }
    return lines;
  }

  public CompletableFuture<Exit> exited() {
    return exited;
  }

  public CompletableFuture<Integer> exitCode() {
    return exited.thenApply(exit -> exit.exitCode);
  }

  /** Kills the process tree with SIGKILL. */
  public CompletableFuture<Exit> kill() {
    return kill("SIGKILL");
  }

  /** {@code SIGKILL} kills the process and all its descendants; {@code SIGINT} is sent to the process. */
  public CompletableFuture<Exit> kill(String signal) {
    killProcessTree(signal);
    return exited;
  }

  private void killProcessTree(String signal) {
    if (!process.isAlive()) {
      return;
    }
    if (Env.isWindows) {
      if (pid <= 0) {
        // Java 8 can not tell the pid on Windows, so only the process itself is killed, not its descendants.
        process.destroyForcibly();
        return;
      }
      run("taskkill", "/pid", String.valueOf(pid), "/T", "/F", "/FI", "MEMUSAGE gt 0");
      return;
    }
    if (pid <= 0) {
      if ("SIGKILL".equals(signal)) {
        sentKill = true;
        process.destroyForcibly();
      }
      return;
    }
    if ("SIGINT".equals(signal)) {
      run("kill", "-INT", String.valueOf(pid));
      return;
    }
    sentKill = true;
    Set<Long> tree = new LinkedHashSet<>();
    collectDescendants(pid, readParents(), tree);
    List<String> kill = new ArrayList<>(Arrays.asList("kill", "-KILL"));
    for (long id : tree) {
      kill.add(String.valueOf(id));
    }
    run(kill.toArray(new String[0]));
    process.destroyForcibly();
  }

  private static void collectDescendants(long root, Map<Long, Long> parents, Set<Long> result) {
    result.add(root);
    for (Map.Entry<Long, Long> entry : parents.entrySet()) {
      if (entry.getValue() == root && !result.contains(entry.getKey())) {
        collectDescendants(entry.getKey(), parents, result);
      }
    }
  }

  /** Process id to parent process id of every process we can see: /proc on Linux, ps elsewhere. */
  private static Map<Long, Long> readParents() {
    Map<Long, Long> parents = new HashMap<>();
    if (Env.isLinux) {
      String[] dirs = new File("/proc").list();
      if (dirs == null) {
        return parents;
      }
      for (String dir : dirs) {
        if (!dir.matches("\\d+")) {
          continue;
        }
        try {
          // Format of /proc/*/stat: pid (comm) state ppid ...; comm may contain spaces and brackets.
          String stat = new String(Files.readAllBytes(Paths.get("/proc", dir, "stat")), StandardCharsets.UTF_8);
          String[] rest = stat.substring(stat.lastIndexOf(')') + 2).split(" ");
          parents.put(Long.parseLong(dir), Long.parseLong(rest[1]));
        } catch (IOException | RuntimeException e) {
          // We don't have access to some /proc/<pid>/stat file.
        }
      }
      return parents;
    }
    try {
      Process ps = new ProcessBuilder("ps", "-eo", "pid,ppid").redirectErrorStream(true).start();
      String text = readAll(ps.getInputStream());
      ps.waitFor();
      for (String line : text.split("\n")) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 2 && tokens[0].matches("\\d+") && tokens[1].matches("\\d+")) {
          parents.put(Long.parseLong(tokens[0]), Long.parseLong(tokens[1]));
        }
      }
    } catch (IOException e) {
      // No ps, nothing to walk.
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return parents;
  }

  private static String readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
      out.write(buffer, 0, read);
    }
    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }

  private static void run(String... command) {
    try {
      Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
      p.getOutputStream().close();
      readAll(p.getInputStream());
      p.waitFor();
    } catch (IOException e) {
      // The process might have already stopped.
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** Waits for the exit and throws unless the process exited with code 0. */
  public void cleanExit() {
    Exit exit;
    try {
      exit = exited.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (ExecutionException e) {
      throw new IllegalStateException(e.getCause());
    }
    if (exit.signal != null) {
      throw new IllegalStateException("Process received signal: " + exit.signal + ". Output:\n" + output());
    }
    if (exit.exitCode != 0) {
      throw new IllegalStateException("Process failed with exit code " + exit.exitCode + ". Output:\n" + output());
    }
  }

  /** Blocks until the output, without ANSI escapes, contains {@code substring}. */
  public void waitForOutput(String substring) {
    waitForOutput(substring, 1);
  }

  public void waitForOutput(String substring, int count) {
    synchronized (lock) {
      while (countTimes(Utils.stripAnsi(output.toString()), substring) < count) {
        if (openStreams == 0) {
          throw new IllegalStateException("The process ended before it printed '" + substring + "' " + count + " times. Output:\n" + output);
        }
        try {
          lock.wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException(e);
        }
      }
    }
  }

  public void clearOutput() {
    synchronized (lock) {
      output.setLength(0);
    }
  }

  public void write(String chars) {
    try {
      OutputStream stdin = process.getOutputStream();
      stdin.write(chars.getBytes(StandardCharsets.UTF_8));
      stdin.flush();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  static int countTimes(String s, String sub) {
    int result = 0;
    for (int index = 0; index != -1;) {
      index = s.indexOf(sub, index);
      if (index != -1) {
        result++;
        index += sub.length();
      }
    }
    return result;
  }

  /**
   * Java does not escape the quotes in an argument on Windows, so the child gets {@code {a:1}} for
   * {@code {"a":1}}. An argument with quotes gets the quoting of the Microsoft C runtime, which Java
   * then passes as it is.
   */
  private static List<String> quoteForWindows(List<String> command) {
    List<String> result = new ArrayList<>();
    for (String arg : command) {
      if (arg.indexOf('"') < 0) {
        result.add(arg);
        continue;
      }
      StringBuilder quoted = new StringBuilder("\"");
      int backslashes = 0;
      for (char c : arg.toCharArray()) {
        if (c == '\\') {
          backslashes++;
          continue;
        }
        // Backslashes before a quote are doubled, and the quote gets one more.
        int count = c == '"' ? backslashes * 2 + 1 : backslashes;
        for (int i = 0; i < count; i++) {
          quoted.append('\\');
        }
        quoted.append(c);
        backslashes = 0;
      }
      for (int i = 0; i < backslashes * 2; i++) {
        quoted.append('\\');
      }
      result.add(quoted.append('"').toString());
    }
    return result;
  }

  private static long pidOf(Process process) {
    try {
      Method method = Process.class.getMethod("pid");
      return (Long) method.invoke(process);
    } catch (ReflectiveOperationException | RuntimeException e) {
      // Java 8: the pid is a field of java.lang.UNIXProcess.
    }
    try {
      Field field = process.getClass().getDeclaredField("pid");
      field.setAccessible(true);
      return field.getLong(process);
    } catch (ReflectiveOperationException | RuntimeException e) {
      return -1;
    }
  }

  /**
   * Java reports a signal death as 128 + signal number, which a process that calls
   * {@code process.exit(130)} reports too, so only the SIGKILL of {@link #kill()} is a signal.
   */
  private String signalOf(int exitValue) {
    return sentKill && !Env.isWindows && exitValue == 128 + 9 ? "SIGKILL" : null;
  }
}
