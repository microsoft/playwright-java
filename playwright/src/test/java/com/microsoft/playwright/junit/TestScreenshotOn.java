package com.microsoft.playwright.junit;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@Transient
@UsePlaywright(TestScreenshotOn.TestOptions.class)
public class TestScreenshotOn {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setScreenshot(Options.Screenshot.ON);
    }
  }

  @Test
  void screenshotShouldExistWhenTestPasses(Page ignored) {
    // force pass
  }

  @Test
  void screenshotShouldExistWhenTestIsAborted(Page ignored) {
    abort();
  }

  @Test
  void screenshotShouldExistWhenTestFails(Page ignored) {
    fail();
  }

  @Test
  void screenshotsShouldExistForAllPagesInTheBrowserContext(BrowserContext browserContext) {
    Page page1 = browserContext.newPage();
    Page page2 = browserContext.newPage();
    // force pass
  }
}
