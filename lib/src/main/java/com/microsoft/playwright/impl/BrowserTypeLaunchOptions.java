/**
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

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;

public class BrowserTypeLaunchOptions {
  public String executablePath;
  public String[] args;
  public Boolean ignoreAllDefaultArgs;
  public String[] ignoreDefaultArgs;
  public Boolean handleSIGINT;
  public Boolean handleSIGTERM;
  public Boolean handleSIGHUP;
  public Integer timeout;
  public LinkedHashMap<String, String> env;
  public Boolean headless;
  public Boolean devtools;
  public static class Proxy {
    String server;
    String bypass;
    String username;
    String password;
  }
  public Proxy proxy;
  public String downloadsPath;
  public String _videosPath;
  public JsonObject firefoxUserPrefs;
  public Boolean chromiumSandbox;
  public Integer slowMo;
}
