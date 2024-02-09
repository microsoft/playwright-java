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

  public BrowserType.LaunchOptions getLaunchOptions() {
    return launchOptions;
  }

  public Options setLaunchOptions(BrowserType.LaunchOptions launchOptions) {
    this.launchOptions = launchOptions;
    return this;
  }

  public Browser.NewContextOptions getContextOptions() {
    return contextOptions;
  }

  public Options setContextOptions(Browser.NewContextOptions contextOptions) {
    this.contextOptions = contextOptions;
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

  public String getBrowserName() {
    return browserName;
  }

  public Options setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public Options setDeviceName(String deviceName) {
    this.deviceName = deviceName;
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
}
