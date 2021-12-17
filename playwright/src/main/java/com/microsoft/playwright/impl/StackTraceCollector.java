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

class StackTraceCollector {
  private final Path srcDir;

  StackTraceCollector(Path srcDir) {
    if (!Files.exists(srcDir.toAbsolutePath())) {
      throw new PlaywrightException("Source location doesn't exist: '" + srcDir.toAbsolutePath() + "'");
    }
    this.srcDir = srcDir;
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
    return srcDir.resolve(pkg).resolve(file).toString();
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
      jsonFrame.addProperty("function", frame.getClassName() + "." + frame.getMethodName());
      jsonStack.add(jsonFrame);
    }
    return jsonStack;
  }


}
