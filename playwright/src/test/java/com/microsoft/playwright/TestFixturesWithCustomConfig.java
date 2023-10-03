package com.microsoft.playwright;

import com.microsoft.playwright.junit.Config;
import com.microsoft.playwright.junit.ConfigFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@UsePlaywright(configFactory = TestFixturesWithCustomConfig.CustomConfigFactory.class)
public class TestFixturesWithCustomConfig {
  public static class CustomConfigFactory implements ConfigFactory {
    @Override
    public Config newConfig() {
      Config config = new Config();
      config.setOutputDir(Paths.get("/tmp/tracing"));
      config.setBrowserName("webkit");
      config.setTrace(true);
      config.contextOptions().setViewportSize(1280, 960);
      return config;
    }
  }

  @Test
  void recordTraceInWebKit(Page page) {
    page.navigate("https://playwright.dev");
  }
}
