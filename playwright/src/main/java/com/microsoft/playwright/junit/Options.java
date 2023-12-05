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

  public Options setPlaywrightCreateOptions(Playwright.CreateOptions playwrightCreateOptions) {
    this.playwrightCreateOptions = playwrightCreateOptions;
    return this;
  }

  public BrowserType.LaunchOptions getLaunchOptions() {
    return launchOptions;
  }

  public Options setLaunchOptions(BrowserType.LaunchOptions launchOptions) {
    this.launchOptions = launchOptions;
    return this;
  }

  public Browser.NewContextOptions getContextOption() {
    return contextOption;
  }

  public Options setContextOption(Browser.NewContextOptions contextOption) {
    this.contextOption = contextOption;
    return this;
  }

  public APIRequest.NewContextOptions getApiRequestOptions() {
    return apiRequestOptions;
  }

  public Options setApiRequestOptions(APIRequest.NewContextOptions apiRequestOptions) {
    this.apiRequestOptions = apiRequestOptions;
    return this;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public Options setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Path getStorageStatePath() {
    return storageStatePath;
  }

  public Options setStorageStatePath(Path storageStatePath) {
    this.storageStatePath = storageStatePath;
    return this;
  }

  public String getBrowserName() {
    return browserName;
  }

  public Options setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }

  public String getChannel() {
    return channel;
  }

  public Options setChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public Boolean isHeadless() {
    return headless;
  }

  public Options setHeadless(Boolean headless) {
    this.headless = headless;
    return this;
  }

  public ViewportSize getViewportSize() {
    return viewportSize;
  }

  public Options setViewportSize(ViewportSize viewportSize) {
    this.viewportSize = viewportSize;
    return this;
  }
}
