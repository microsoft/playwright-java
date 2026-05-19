package com.microsoft.playwright.impl.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.isParameterSupported;

public class BrowserNewContextOptionsExtension implements ParameterResolver {
  private static final ThreadLocal<Browser.NewContextOptions> threadLocalBrowserNewContextOptions = new ThreadLocal<>();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, Browser.NewContextOptions.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    return getBrowserContextOptions(playwright, options);
  }

  static Browser.NewContextOptions getBrowserContextOptions(Playwright playwright, Options options) {
    Browser.NewContextOptions contextOptions = threadLocalBrowserNewContextOptions.get();
    if (contextOptions != null) {
      return contextOptions;
    }

    contextOptions = Utils.clone(options.contextOptions);

    if (contextOptions == null) {
      contextOptions = new Browser.NewContextOptions();
    }

    if (options.baseUrl != null) {
      contextOptions.setBaseURL(options.baseUrl);
    }

    if (options.deviceName != null) {
      DeviceDescriptor deviceDescriptor = DeviceDescriptor.findByName(playwright, options.deviceName);
      if (deviceDescriptor == null) {
        throw new PlaywrightException("Unknown device name: " + options.deviceName);
      }
      contextOptions.userAgent = deviceDescriptor.userAgent;
      if (deviceDescriptor.viewport != null) {
        contextOptions.setViewportSize(deviceDescriptor.viewport.width, deviceDescriptor.viewport.height);
      }
      contextOptions.deviceScaleFactor = deviceDescriptor.deviceScaleFactor;
      contextOptions.isMobile = deviceDescriptor.isMobile;
      contextOptions.hasTouch = deviceDescriptor.hasTouch;
    }

    if (options.ignoreHTTPSErrors != null) {
      contextOptions.setIgnoreHTTPSErrors(options.ignoreHTTPSErrors);
    }

    threadLocalBrowserNewContextOptions.set(contextOptions);
    return contextOptions;
  }
}
