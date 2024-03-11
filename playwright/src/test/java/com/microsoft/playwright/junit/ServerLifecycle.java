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

package com.microsoft.playwright.junit;

import com.microsoft.playwright.Server;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.Utils.nextFreePort;

public class ServerLifecycle implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
  // This is a public map so that objects outside test scope can access the server.
  // For example, nested classes inside test classes that define custom options and need the server.
  public static Map<Class<?>, Server> serverMap;

  static {
    serverMap = new HashMap<>();
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    getOrCreateServer(extensionContext);
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    Server server = serverMap.get(extensionContext.getRequiredTestClass());
    if (server != null) {
      server.stop();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return Server.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateServer(extensionContext);
  }

  private Server getOrCreateServer(ExtensionContext extensionContext) {
    Server server = serverMap.get(extensionContext.getRequiredTestClass());
    if(server == null) {
      try {
        server = Server.createHttp(nextFreePort());
        serverMap.put(extensionContext.getRequiredTestClass(), server);
      } catch (IOException e) {
        throw new ParameterResolutionException(e.getMessage());
      }
    }
    return server;
  }
}
