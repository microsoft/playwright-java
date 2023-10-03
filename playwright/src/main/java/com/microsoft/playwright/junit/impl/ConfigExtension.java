package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.*;
import com.microsoft.playwright.junit.*;
import org.junit.jupiter.api.extension.*;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.getUsePlaywrightAnnotation;

public class ConfigExtension implements AfterAllCallback {
  private static final ThreadLocal<Config> threadLocalConfig = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalConfig.remove();
  }

  static Config getConfig(ExtensionContext extensionContext) {
    Config config = threadLocalConfig.get();
    if (config != null) {
      return config;
    }
    UsePlaywright usePlaywrightAnnotation = getUsePlaywrightAnnotation(extensionContext);
    if (!DefaultConfigFactory.class.equals(usePlaywrightAnnotation.configFactory())) {
      try {
        ConfigFactory configFactory = usePlaywrightAnnotation.configFactory().newInstance();
        config = configFactory.newConfig();
        threadLocalConfig.set(config);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new PlaywrightException("Failed to create config", e);
      }
    }
    return config;
  }

  static Path outputDirForTest(Config config, ExtensionContext extensionContext) {
    Path dir = config.outputDir();
    if (dir == null) {
      // CWD
      dir = FileSystems.getDefault().getPath("");
    }
    //          String className = extensionContext.getTestClass().get().getName();
    String methodName = extensionContext.getTestMethod().get().getName();
    return dir.resolve(methodName);
  }
}
