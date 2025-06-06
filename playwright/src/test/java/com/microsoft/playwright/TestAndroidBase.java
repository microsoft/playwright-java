package com.microsoft.playwright;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.microsoft.playwright.Utils.nextFreePort;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestAndroidBase {
  Playwright playwright;
  Android android;
  AndroidDevice androidDevice;
  Server server;
  Server httpsServer;

  @BeforeAll
  void setupAndroid() {
    playwright = Playwright.create();
    List<AndroidDevice> devices = playwright.android().devices();
    assertFalse(devices.isEmpty());
    androidDevice = devices.get(0);
  }

  @AfterAll
  void teardownAndroid() {
    if (androidDevice != null) {
      androidDevice.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

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

  @AfterEach
  void cleanupPage() {
    server.reset();
    httpsServer.reset();
  }
}
