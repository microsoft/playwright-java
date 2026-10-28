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

import java.util.Map;

/** {@code RemoteServerOptions} of {@code tests/config/remoteServer.ts}. */
public class RemoteServerOptions {
  public boolean stallOnClose;
  public boolean disconnectOnSIGHUP;
  public String exitOnFile;
  public boolean exitOnWarning;
  public boolean inCluster;
  public String url;
  public boolean startStopAndRunHttp;
  public boolean sharedBrowser;
  public String artifactsDir;
  /** Only supported by the {@code run-server} kind. */
  public boolean unsafe;
  /** Only supported by the {@code run-server} kind; a null value removes the variable. */
  public Map<String, String> env;

  public RemoteServerOptions setStallOnClose(boolean stallOnClose) {
    this.stallOnClose = stallOnClose;
    return this;
  }

  public RemoteServerOptions setDisconnectOnSIGHUP(boolean disconnectOnSIGHUP) {
    this.disconnectOnSIGHUP = disconnectOnSIGHUP;
    return this;
  }

  public RemoteServerOptions setExitOnFile(String exitOnFile) {
    this.exitOnFile = exitOnFile;
    return this;
  }

  public RemoteServerOptions setExitOnWarning(boolean exitOnWarning) {
    this.exitOnWarning = exitOnWarning;
    return this;
  }

  public RemoteServerOptions setInCluster(boolean inCluster) {
    this.inCluster = inCluster;
    return this;
  }

  public RemoteServerOptions setUrl(String url) {
    this.url = url;
    return this;
  }

  public RemoteServerOptions setStartStopAndRunHttp(boolean startStopAndRunHttp) {
    this.startStopAndRunHttp = startStopAndRunHttp;
    return this;
  }

  public RemoteServerOptions setSharedBrowser(boolean sharedBrowser) {
    this.sharedBrowser = sharedBrowser;
    return this;
  }

  public RemoteServerOptions setArtifactsDir(String artifactsDir) {
    this.artifactsDir = artifactsDir;
    return this;
  }

  public RemoteServerOptions setUnsafe(boolean unsafe) {
    this.unsafe = unsafe;
    return this;
  }

  public RemoteServerOptions setEnv(Map<String, String> env) {
    this.env = env;
    return this;
  }
}
