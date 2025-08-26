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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.options.AriaRole;

class TraceViewerPage {
  private final Page page;

  TraceViewerPage(Page page) {
    this.page = page;
  }

  Page page() {
    return page;
  }

  Locator actionsTree() {
    return page.getByTestId("actions-tree");
  }

  Locator actionTitles() {
    return page.locator(".action-title");
  }

  Locator stackFrames() {
    return this.page.getByRole(AriaRole.LIST, new Page.GetByRoleOptions().setName("stack trace")).getByRole(AriaRole.LISTITEM);
  }

  void selectAction(String title, int ordinal) {
    this.actionsTree().getByTitle(title).nth(ordinal).click();
  }

  void selectAction(String title) {
    selectAction(title, 0);
  }

  void selectSnapshot(String name) {
    this.page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(name)).click();
  }

  FrameLocator snapshotFrame(String actionName, int ordinal, boolean hasSubframe) {
    selectAction(actionName, ordinal);
    while (page.frames().size() < (hasSubframe ? 4 : 3)) {
      page.waitForTimeout(200);
    }
    return page.frameLocator("iframe.snapshot-visible[name=snapshot]");
  }

  FrameLocator snapshotFrame(String actionName, int ordinal) {
    return snapshotFrame(actionName, ordinal, false);
  }

  void showSourceTab() {
    page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Source")).click();
  }

  void expandAction(String title) {
    this.actionsTree().getByRole(AriaRole.TREEITEM, new Locator.GetByRoleOptions().setName(title)).locator(".codicon-chevron-right").click();
  }

  static void showTraceViewer(BrowserType browserType, Path tracePath, TraceViewerConsumer callback) throws Exception {
    Path driverDir = Driver.ensureDriverInstalled(java.util.Collections.emptyMap(), true).driverDir();
    Path traceViewerPath = driverDir.resolve("package").resolve("lib").resolve("vite").resolve("traceViewer");
    Server traceServer = Server.createHttp(Utils.nextFreePort());
    traceServer.setResourceProvider(path -> {
      Path filePath = traceViewerPath.resolve(path.substring(1));
      if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
        try {
          return Files.newInputStream(filePath);
        } catch (IOException e) {
          return null;
        }
      }
      return null;
    });
    traceServer.setRoute("/trace.zip", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/zip");
      exchange.sendResponseHeaders(200, Files.size(tracePath));
      Files.copy(tracePath, exchange.getResponseBody());
      exchange.getResponseBody().close();
    });

    try (Browser browser = browserType.launch(TestBase.createLaunchOptions());
         BrowserContext context = browser.newContext()) {
      Page page = context.newPage();
      page.navigate(traceServer.PREFIX + "/index.html?trace=" + traceServer.PREFIX + "/trace.zip");
      
      TraceViewerPage traceViewer = new TraceViewerPage(page);
      callback.accept(traceViewer);
    } finally {
      traceServer.stop();
    }
  }
  
  @FunctionalInterface
  interface TraceViewerConsumer {
    void accept(TraceViewerPage traceViewer) throws Exception;
  }
}
