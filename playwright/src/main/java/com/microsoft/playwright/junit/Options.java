package com.microsoft.playwright.junit;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ViewportSize;

import java.nio.file.Path;

public class Options {
  public String baseUrl;
  public Path storageStatePath;
  public ViewportSize viewportSize;
  public String channel;
  public Boolean headless;
  public String browserName = "chromium";
  public BrowserType.LaunchOptions launchOptions;
  public Browser.NewContextOptions contextOption;
  public APIRequest.NewContextOptions apiRequestOptions;
  public Playwright.CreateOptions playwrightCreateOptions;

  public Playwright.CreateOptions getPlaywrightCreateOptions() {
    return playwrightCreateOptions;
  }

  public BrowserType.LaunchOptions getLaunchOptions() {
    return launchOptions;
  }

  public Browser.NewContextOptions getContextOption() {
    return contextOption;
  }

  public APIRequest.NewContextOptions getApiRequestOptions() {
    return apiRequestOptions;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public Path getStorageStatePath() {
    return storageStatePath;
  }

  public String getBrowserName() {
    return browserName;
  }

  public String getChannel() {
    return channel;
  }

  public Boolean isHeadless() {
    return headless;
  }

  public ViewportSize getViewportSize() {
    return viewportSize;
  }
}
