package com.microsoft.playwright.springboottest;

import com.microsoft.playwright.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class TestApp implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(TestApp.class, args);
  }

  @Override
  public void run(String... args) {
    if (args.length == 0) {
      runSync();
    } else {
      if ("--async".equals(args[0])) {
        runAsync();
      }
      else {
        runSync();
      }
    }
  }

  private void runAsync() {
    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(this::runSync);
    voidCompletableFuture.join();
  }

  private void runSync() {
    try (Playwright playwright = Playwright.create()) {
      BrowserType browserType = getBrowserTypeFromEnv(playwright);
      System.out.println("Running test with " + browserType.name());
      Browser browser = browserType.launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      System.out.println(page.evaluate("'SUCCESS: did evaluate in page'"));
    }
  }

  static BrowserType getBrowserTypeFromEnv(Playwright playwright) {
    String browserName = System.getenv("BROWSER");

    if (browserName == null) {
      browserName = "chromium";
    }

    switch (browserName) {
      case "webkit":
        return playwright.webkit();
      case "firefox":
        return playwright.firefox();
      case "chromium":
        return playwright.chromium();
      default:
        throw new IllegalArgumentException("Unknown browser: " + browserName);
    }
  }

}
