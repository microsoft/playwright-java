package com.microsoft.playwright.springboottest;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApp implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(TestApp.class, args);
  }

  @Override
  public void run(String... args) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      System.out.println(page.evaluate("'SUCCESS: did evaluate in page'"));
    }
  }
}
