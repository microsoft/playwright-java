package com.microsoft.playwright;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static com.microsoft.playwright.Utils.nextFreePort;

// Created this temporarily in order to update tests slowly without relying on TestBase and its browser lifecycle methods
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class __TestBaseNew {
  Server server;
  Server httpsServer;

  @BeforeAll
  void startServer() throws IOException {
    server = Server.createHttp(nextFreePort());
    httpsServer = Server.createHttps(nextFreePort());
  }

  @AfterAll
  void stopServer() {
    if (server != null) {
      server.stop();
      server = null;
    }
    if (httpsServer != null) {
      httpsServer.stop();
      httpsServer = null;
    }
  }
}
