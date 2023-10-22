package com.microsoft.playwright.junit;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Options {
  private String baseUrl;
  private Path storageStatePath;
  private Viewport viewport;
  private String channel;
  private boolean headless;
  private String browserName = "chromium";
  private BrowserType.LaunchOptions launchOptions;
  private Browser.NewContextOptions contextOption;
  private APIRequest.NewContextOptions apiRequestOptions;
  private Playwright.CreateOptions playwrightCreateOptions;

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

  public Viewport getViewport() {
    return viewport;
  }

  public Options setViewport(Viewport viewport) {
    this.viewport = viewport;
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

  public boolean isHeadless() {
    return headless;
  }

  public Options setHeadless(boolean headless) {
    this.headless = headless;
    return this;
  }

  public static class Viewport {
    private String width;
    private String height;

    public String getWidth() {
      return width;
    }

    public Viewport setWidth(String width) {
      this.width = width;
      return this;
    }

    public String getHeight() {
      return height;
    }

    public Viewport setHeight(String height) {
      this.height = height;
      return this;
    }
  }

}
