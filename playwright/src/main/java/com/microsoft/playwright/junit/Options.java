package com.microsoft.playwright.junit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Options {
  private String baseUrl;
  private Path storageState;
  private ColorScheme colorScheme;
  private GeoLocation geoLocation;
  private String locale;
  private List<String> permissions;
  private String timezoneId;
  private Viewport viewport;
  private boolean acceptDownloads;
  private Map<String, String> extraHTTPHeaders;
  private HTTPCredentials httpCredentials;
  private boolean ignoreHTTPSErrors;
  private boolean offline;
  private double actionTimeout;
  private String browserName;
  private boolean bypassCSP;
  private Channel channel;
  private boolean headless;
  private String testIdAttribute;

  public String getBaseUrl() {
    return baseUrl;
  }

  public Options setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Path getStorageState() {
    return storageState;
  }

  public Options setStorageState(Path storageState) {
    this.storageState = storageState;
    return this;
  }

  public ColorScheme getColorScheme() {
    return colorScheme;
  }

  public Options setColorScheme(ColorScheme colorScheme) {
    this.colorScheme = colorScheme;
    return this;
  }

  public GeoLocation getGeoLocation() {
    return geoLocation;
  }

  public Options setGeoLocation(GeoLocation geoLocation) {
    this.geoLocation = geoLocation;
    return this;
  }

  public String getLocale() {
    return locale;
  }

  public Options setLocale(String locale) {
    this.locale = locale;
    return this;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public Options setPermissions(List<String> permissions) {
    this.permissions = permissions;
    return this;
  }

  public String getTimezoneId() {
    return timezoneId;
  }

  public Options setTimezoneId(String timezoneId) {
    this.timezoneId = timezoneId;
    return this;
  }

  public Viewport getViewport() {
    return viewport;
  }

  public Options setViewport(Viewport viewport) {
    this.viewport = viewport;
    return this;
  }

  public boolean isAcceptDownloads() {
    return acceptDownloads;
  }

  public Options setAcceptDownloads(boolean acceptDownloads) {
    this.acceptDownloads = acceptDownloads;
    return this;
  }

  public Map<String, String> getExtraHTTPHeaders() {
    return extraHTTPHeaders;
  }

  public Options setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
    this.extraHTTPHeaders = extraHTTPHeaders;
    return this;
  }

  public HTTPCredentials getHttpCredentials() {
    return httpCredentials;
  }

  public Options setHttpCredentials(HTTPCredentials httpCredentials) {
    this.httpCredentials = httpCredentials;
    return this;
  }

  public boolean isIgnoreHTTPSErrors() {
    return ignoreHTTPSErrors;
  }

  public Options setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
    this.ignoreHTTPSErrors = ignoreHTTPSErrors;
    return this;
  }

  public boolean isOffline() {
    return offline;
  }

  public Options setOffline(boolean offline) {
    this.offline = offline;
    return this;
  }

  public double getActionTimeout() {
    return actionTimeout;
  }

  public Options setActionTimeout(double actionTimeout) {
    this.actionTimeout = actionTimeout;
    return this;
  }

  public String getBrowserName() {
    return browserName;
  }

  public Options setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }

  public boolean isBypassCSP() {
    return bypassCSP;
  }

  public Options setBypassCSP(boolean bypassCSP) {
    this.bypassCSP = bypassCSP;
    return this;
  }

  public Channel getChannel() {
    return channel;
  }

  public Options setChannel(Channel channel) {
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

  public String getTestIdAttribute() {
    return testIdAttribute;
  }

  public Options setTestIdAttribute(String testIdAttribute) {
    this.testIdAttribute = testIdAttribute;
    return this;
  }

  public enum ColorScheme {
    LIGHT,
    DARK
  }

  public static class GeoLocation {
    private String latitude;
    private String longitude;

    public String getLatitude() {
      return latitude;
    }

    public GeoLocation setLatitude(String latitude) {
      this.latitude = latitude;
      return this;
    }

    public String getLongitude() {
      return longitude;
    }

    public GeoLocation setLongitude(String longitude) {
      this.longitude = longitude;
      return this;
    }
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

  public static class HTTPCredentials {
    private String username;
    private String password;

    public String getUsername() {
      return username;
    }

    public HTTPCredentials setUsername(String username) {
      this.username = username;
      return this;
    }

    public String getPassword() {
      return password;
    }

    public HTTPCredentials setPassword(String password) {
      this.password = password;
      return this;
    }
  }

  public static class Proxy {
    private String server;
    private String bypass;

    public String getServer() {
      return server;
    }

    public Proxy setServer(String server) {
      this.server = server;
      return this;
    }

    public String getBypass() {
      return bypass;
    }

    public Proxy setBypass(String bypass) {
      this.bypass = bypass;
      return this;
    }
  }

  public enum Channel {
    CHROME,
    CHROME_BETA,
    CHROME_DEV,
    CHROME_CANARY,
    MSEDGE,
    MSEDGE_BETA,
    MSEDGE_DEV,
    MSEDGE_CANARY
  }

}
