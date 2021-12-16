package com.microsoft.playwright.testcliversion;

import com.microsoft.playwright.Playwright;

import java.io.IOException;

public class TestApp {
  public static void main(String[] args) throws IOException, InterruptedException {
    String version = Playwright.class.getPackage().getImplementationVersion();
    if (version == null) {
      throw new RuntimeException("FAIL: Version is null");
    }
    System.out.println("ImplementationVersion " + version);
  }
}
