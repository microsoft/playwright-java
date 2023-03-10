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

package com.microsoft.playwright.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class StackTraceCollector {
  static final String PLAYWRIGHT_JAVA_SRC = "PLAYWRIGHT_JAVA_SRC";
  private final List<Path> srcDirs;
  private final Map<Path, String> classToSourceCache = new HashMap<>();

  static StackTraceCollector createFromEnv(Map<String, String> env) {
    String srcRoots = null;
    if (env != null) {
      srcRoots = env.get(PLAYWRIGHT_JAVA_SRC);
    }
    if (srcRoots == null) {
      srcRoots = System.getenv(PLAYWRIGHT_JAVA_SRC);
    }
    if (srcRoots == null) {
      return null;
    }
    List<Path> srcDirs = Arrays.stream(srcRoots.split(File.pathSeparator)).map(p -> Paths.get(p)).collect(Collectors.toList());
    for (Path srcDir: srcDirs) {
      if (!Files.exists(srcDir.toAbsolutePath())) {
        throw new PlaywrightException("Source location specified in " + PLAYWRIGHT_JAVA_SRC + " doesn't exist: '" + srcDir.toAbsolutePath() + "'");
      }
    }
    return new StackTraceCollector(srcDirs);
  }

  private StackTraceCollector(List<Path> srcDirs) {
    this.srcDirs = srcDirs;
  }

  private String sourceFile(StackTraceElement frame) {
    String pkg = frame.getClassName();
    int lastDot = pkg.lastIndexOf('.');
    if (lastDot == -1) {
      pkg = "";
    } else {
      pkg = frame.getClassName().substring(0, lastDot + 1);
    }
    pkg = pkg.replace('.', File.separatorChar);
    String file = frame.getFileName();
    if (file == null) {
      return "";
    }
    try {
      // The file name can contain an arbitrary string which may cause Path implementation
      // to throw. See https://github.com/microsoft/playwright-java/issues/1115
      return resolveSourcePath(Paths.get(pkg).resolve(file));
    } catch (RuntimeException e) {
      return "";
    }
  }

  private String resolveSourcePath(Path relativePath) {
    String path = classToSourceCache.get(relativePath);
    if (path == null) {
      for (Path dir : srcDirs) {
        Path absolutePath = dir.resolve(relativePath);
        if (Files.exists(absolutePath)) {
          path = absolutePath.toString();
          classToSourceCache.put(relativePath, path);
          break;
        }
      }
      if (path == null) {
        path = "";
        classToSourceCache.put(relativePath, path);
      }
    }
    return path;
  }

  JsonArray currentStackTrace() {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();

    int index = 0;
    while (index < stack.length && !stack[index].getClassName().equals(getClass().getName())) {
      index++;
    };
    // Find Playwright API call
    while (index < stack.length && stack[index].getClassName().startsWith("com.microsoft.playwright.")) {
      // hack for tests
      if (stack[index].getClassName().startsWith("com.microsoft.playwright.Test")) {
        break;
      }
      index++;
    }
    JsonArray jsonStack = new JsonArray();
    for (; index < stack.length; index++) {
      StackTraceElement frame = stack[index];
      JsonObject jsonFrame = new JsonObject();
      jsonFrame.addProperty("file", sourceFile(frame));
      jsonFrame.addProperty("line", frame.getLineNumber());
      jsonFrame.addProperty("column", 0);
      jsonFrame.addProperty("function", frame.getClassName() + "." + frame.getMethodName());
      jsonStack.add(jsonFrame);
    }
    return jsonStack;
  }


}
