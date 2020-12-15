# 🎭 [Playwright](https://github.com/microsoft/playwright) for Java

[![maven version](https://img.shields.io/maven-central/v/com.microsoft.playwright/playwright)](https://search.maven.org/search?q=com.microsoft.playwright)  [![Join Slack](https://img.shields.io/badge/join-slack-infomational)](https://join.slack.com/t/playwright/shared_invite/enQtOTEyMTUxMzgxMjIwLThjMDUxZmIyNTRiMTJjNjIyMzdmZDA3MTQxZWUwZTFjZjQwNGYxZGM5MzRmNzZlMWI5ZWUyOTkzMjE5Njg1NDg)

### _The project is in early development phase, the APIs match those in typescript version of Playwright but are subject to change._

## Usage

#### Add Maven dependency

To run Playwright simply add 2 modules to your Maven project:

```xml
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>0.162.3</version>
</dependency>
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>driver-bundle</artifactId>
  <version>0.162.3</version>
</dependency>
```

## Examples

#### Page screenshot

This code snippet navigates to whatsmyuseragent.org in Chromium, Firefox and WebKit, and saves 3 screenshots.

```java
import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PageScreenshot {
  public static void main(String[] args) throws Exception {
    Playwright playwright = Playwright.create();
    List<BrowserType> browserTypes = Arrays.asList(
        playwright.chromium(),
        playwright.webkit(),
        playwright.firefox()
    );
    for (BrowserType browserType : browserTypes) {
      Browser browser = browserType.launch(
          new BrowserType.LaunchOptions().withHeadless(false));
      BrowserContext context = browser.newContext(
          new Browser.NewContextOptions().withViewport(800, 600));
      Page page = context.newPage();
      page.navigate("http://whatsmyuseragent.org/");
      page.screenshot(new Page.ScreenshotOptions().withPath(Paths.get("screenshot-" + browserType.name() + ".png")));
      browser.close();
    }
    playwright.close();
  }
}
```

## Notes

Follow [the instructions](https://github.com/microsoft/playwright-java/blob/master/CONTRIBUTING.md#getting-code) to build the project from source and install driver.

Original Playwright [documentation](https://playwright.dev/). We are converting it to javadoc.

