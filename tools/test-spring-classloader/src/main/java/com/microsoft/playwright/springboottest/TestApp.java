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

  public void run(String... args) {
    // use CompletableFuture to run Playwright asynchronously
    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
      try (Playwright playwright = Playwright.create()) {
        System.out.println("Playwright classLoader test started, waiting for completion...");
        BrowserType browserType = getBrowserTypeFromEnv(playwright);
        System.out.println("Running test with " + browserType.name());
        Browser browser = browserType.launch();
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        System.out.println(page.evaluate("'SUCCESS: did evaluate in page'"));
      } catch (Exception e) {
        System.out.println("FAILED: " + e.toString());
        for (StackTraceElement ste : e.getStackTrace()) {
          System.out.println("\tat " + ste);
        }
      }
    });

    System.out.println("Playwright classLoader test is running asynchronously, main thread will wait for it to complete.");

    voidCompletableFuture.join();

    System.out.println("Playwright classLoader test completed.");

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
