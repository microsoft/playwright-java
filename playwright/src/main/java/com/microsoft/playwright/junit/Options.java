package com.microsoft.playwright.junit;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class Options {
  public String baseUrl;
  public String channel;
  public Boolean headless;
  public String browserName;
  public String deviceName;
  public BrowserType.LaunchOptions launchOptions;
  public Browser.NewContextOptions contextOptions;
  public APIRequest.NewContextOptions apiRequestOptions;
  public Playwright.CreateOptions playwrightCreateOptions;

  public Playwright.CreateOptions getPlaywrightCreateOptions() {
    return playwrightCreateOptions;
  }

  public Options setPlaywrightCreateOptions(Playwright.CreateOptions playwrightCreateOptions) {
    this.playwrightCreateOptions = playwrightCreateOptions;
    return this;
  }

  public Options setLaunchOptions(BrowserType.LaunchOptions launchOptions) {
    this.launchOptions = launchOptions;
    return this;
  }

  public Options setContextOptions(Browser.NewContextOptions contextOptions) {
    this.contextOptions = contextOptions;
    return this;
  }

  public Options setApiRequestOptions(APIRequest.NewContextOptions apiRequestOptions) {
    this.apiRequestOptions = apiRequestOptions;
    return this;
  }

  public Options setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Options setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }

  public Options setDeviceName(String deviceName) {
    this.deviceName = deviceName;
    return this;
  }

  public Options setChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public Options setHeadless(Boolean headless) {
    this.headless = headless;
    return this;
  }
}
