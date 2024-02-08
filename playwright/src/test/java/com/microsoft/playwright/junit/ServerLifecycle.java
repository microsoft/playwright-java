package com.microsoft.playwright.junit;

import com.microsoft.playwright.Server;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.Utils.nextFreePort;

public class ServerLifecycle implements BeforeAllCallback, AfterAllCallback {
  // This is a public map so that objects outside test scope can access the server.
  // For example, nested classes inside test classes that define custom options and need the server.
  public static Map<Class<?>, Server> serverMap;

  static {
    serverMap = new HashMap<>();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    Server server = serverMap.get(extensionContext.getRequiredTestClass());
    if (server != null) {
      server.stop();
    }
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    Server server = Server.createHttp(nextFreePort());
    serverMap.put(extensionContext.getRequiredTestClass(), server);
  }
}
