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

import com.microsoft.playwright.impl.Driver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static java.util.Arrays.asList;

/**
 * Use this class to launch playwright cli.
 */
public class CLI {
  public static void main(String[] args) throws IOException, InterruptedException {
    Path driver = Driver.ensureDriverInstalled(Collections.emptyMap());
    ProcessBuilder pb = new ProcessBuilder(driver.toString());
    pb.command().addAll(asList(args));
    if (!pb.environment().containsKey("PW_CLI_TARGET_LANG")) {
      pb.environment().put("PW_CLI_TARGET_LANG", "java");
    }
    String version = Playwright.class.getPackage().getImplementationVersion();
    if (version != null) {
      pb.environment().put("PW_CLI_DISPLAY_VERSION", version);
    }
    pb.inheritIO();
    Process process = pb.start();
    System.exit(process.waitFor());
  }
}
