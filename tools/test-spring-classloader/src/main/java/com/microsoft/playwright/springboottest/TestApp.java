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
    System.out.println("Starting original Playwright test...");
    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
      try (Playwright playwright = Playwright.create()) {
        System.out.println("original Playwright test started, waiting for completion...");
        BrowserType browserType = getBrowserTypeFromEnv(playwright);
        System.out.println("Running original test with " + browserType.name());
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

    System.out.println("original Playwright test is running asynchronously, main thread will wait for it to complete.");

    voidCompletableFuture.join();

    System.out.println("original Playwright test completed.");


    System.out.println("Starting new Playwright test...");

    // Set the new driver implementation to use the DriverJar class
    System.setProperty( "playwright.driver.impl", "com.microsoft.playwright.springboottest.DriverJar" );

    CompletableFuture<Void> voidCompletableFuture2 = CompletableFuture.runAsync(() -> {
      try (Playwright playwright = Playwright.create()) {
        System.out.println("new Playwright test started, waiting for completion...");
        BrowserType browserType = getBrowserTypeFromEnv(playwright);
        System.out.println("Running new test with " + browserType.name());
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

    System.out.println("new Playwright test is running asynchronously, main thread will wait for it to complete.");

    voidCompletableFuture2.join();

    System.out.println("new Playwright test completed.");

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
