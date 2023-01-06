package com.microsoft.playwright.testclifatjar;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.impl.driver.jar.DriverJar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

public class TestApp {
  public static void main(String[] args) throws IOException, URISyntaxException {
    URI uri = DriverJar.getDriverResourceURI();
    FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    if (fs == null) {
      throw new RuntimeException();
    }
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch();
      Page page = browser.newPage();
    }
  }
}
