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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestLaunch extends TestBase {

  @Override
  @BeforeAll
  // Hide base class method to not launch browser.
  void launchBrowser() {
  }

  @Override
  void createContextAndPage() {
    // Do nothing
  }

  @Test
  void passEnvVar() {
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    options.setEnv(mapOf("DEBUG", "pw:protocol"));
    launchBrowser(options);
  }

  public static boolean canRunHeaded() {
    // On linux headed browser requires xvfb.
    return isHeadful() || isMac || isWindows;
  }

  public static boolean canRunExtensionTest() {
    return canRunHeaded() && isChromium();
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestLaunch#canRunExtensionTest", disabledReason="Only Chromium Headed")
  void shouldReturnBackgroundPages(@TempDir Path tmpDir) throws IOException {
    Path profileDir = tmpDir.resolve("profile");
    Files.createDirectories(profileDir);
    String extensionPath = Paths.get("src/test/resources/simple-extension").toAbsolutePath().toString();
    initBrowserType();
    BrowserContext context = browserType.launchPersistentContext(profileDir, new BrowserType.LaunchPersistentContextOptions()
      .setHeadless(false)
      .setArgs(asList(
        "--disable-extensions-except=" + extensionPath,
        "--load-extension=" + extensionPath
      )));
    List<Page> backgroundPages = context.backgroundPages();
    context.onBackgroundPage(page1 -> backgroundPages.add(page1));
    context.waitForCondition(() -> !backgroundPages.isEmpty(),
      new BrowserContext.WaitForConditionOptions().setTimeout(10_000));
    Page backgroundPage = backgroundPages.get(0);
    assertNotNull(backgroundPage);
    assertTrue(context.backgroundPages().contains(backgroundPage));
    assertFalse(context.pages().contains(backgroundPage));
    context.close();
    assertEquals(0, context.pages().size());
    assertEquals(0, context.backgroundPages().size());
  }
}
