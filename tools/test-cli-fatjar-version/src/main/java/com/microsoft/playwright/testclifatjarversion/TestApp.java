package com.microsoft.playwright.testclifatjarversion;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

public class TestApp {
  public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
    URI uri = new URI("jar:file:/home/user/.m2/repository/com/microsoft/playwright/driver-bundle/1.28.0/driver-bundle-1.28.0.jar!/driver/linux");
    FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    assert (fs != null);
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch();
      Page page = browser.newPage();
    }
  }
}
