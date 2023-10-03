package com.microsoft.playwright.junit;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
  private BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
  private Browser.NewContextOptions contextOption = new Browser.NewContextOptions();
  private APIRequest.NewContextOptions apiRequestOptions = new APIRequest.NewContextOptions();
  private Map<String, Object> clientValues = new HashMap<>();

  private String browserName = "chromium";

  public String browserName() {
    return browserName;
  }

  public void setBrowserName(String browserName) {
    this.browserName = browserName;
  }

  public void setHeadless(boolean headless) {
    launchOptions.setHeadless(headless);
  }

  public void setBaseURL(String url) {
    contextOption.setBaseURL(url);
  }

  public void setStorageStatePath(Path path) {
    contextOption.setStorageStatePath(path);
  }

  public BrowserType.LaunchOptions launchOptions() {
    return launchOptions;
  }

  public Browser.NewContextOptions contextOptions() {
    return contextOption;
  }

  public APIRequest.NewContextOptions apiRequestOptions() {
    return apiRequestOptions;
  }

  public <T> void setClientValue(String name, T value) {
    clientValues.put(name, value);
  }

  public <T> T getClientValue(String name) {
    return (T) clientValues.get(name);
  }

}
