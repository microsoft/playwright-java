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
  <version>0.170.0</version>
</dependency>
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>driver-bundle</artifactId>
  <version>0.170.0</version>
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
      Browser browser = browserType.launch();
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

#### Mobile and geolocation

This snippet emulates Mobile Chromium on a device at a given geolocation, navigates to openstreetmap.org, performs action and takes a screenshot.

```java
import com.microsoft.playwright.*;
import java.nio.file.Paths;
import static java.util.Arrays.asList;

public class MobileAndGeolocation {
  public static void main(String[] args) throws Exception {
    Playwright playwright = Playwright.create();
    BrowserType browserType = playwright.chromium();
    Browser browser = browserType.launch();
    DeviceDescriptor pixel2 = playwright.devices().get("Pixel 2");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .withViewport(pixel2.viewport().width(), pixel2.viewport().height())
        .withUserAgent(pixel2.userAgent())
        .withDeviceScaleFactor(pixel2.deviceScaleFactor())
        .withIsMobile(pixel2.isMobile())
        .withHasTouch(pixel2.hasTouch())
        .withLocale("en-US")
        .withGeolocation(new Geolocation(41.889938, 12.492507))
        .withPermissions(asList("geolocation")));
    Page page = context.newPage();
    page.navigate("https://www.openstreetmap.org/");
    page.click("a[data-original-title=\"Show My Location\"]");
    page.screenshot(new Page.ScreenshotOptions().withPath(Paths.get("colosseum-pixel2.png")));
    browser.close();
    playwright.close();
  }
}
```

#### Evaluate in browser context

This code snippet navigates to example.com in Firefox, and executes a script in the page context.

```java
import com.microsoft.playwright.*;

public class EvaluateInBrowserContext {
  public static void main(String[] args) throws Exception {
    Playwright playwright = Playwright.create();
    BrowserType browserType = playwright.firefox();
    Browser browser = browserType.launch(new BrowserType.LaunchOptions().withHeadless(false));
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate("https://www.example.com/");
    Object dimensions = page.evaluate("() => {\n" +
        "  return {\n" +
        "      width: document.documentElement.clientWidth,\n" +
        "      height: document.documentElement.clientHeight,\n" +
        "      deviceScaleFactor: window.devicePixelRatio\n" +
        "  }\n" +
        "}");
    System.out.println(dimensions);
    browser.close();
    playwright.close();
  }
}
```

#### Intercept network requests

This code snippet sets up request routing for a WebKit page to log all network requests.

```java
import com.microsoft.playwright.*;

public class InterceptNetworkRequests {
  public static void main(String[] args) throws Exception {
    Playwright playwright = Playwright.create();
    BrowserType browserType = playwright.webkit();
    Browser browser = browserType.launch();
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.route("**", route -> {
      System.out.println(route.request().url());
      route.continue_();
    });
    page.navigate("http://todomvc.com");
    browser.close();
    playwright.close();
  }
}
```

## Notes

Follow [the instructions](https://github.com/microsoft/playwright-java/blob/master/CONTRIBUTING.md#getting-code) to build the project from source and install driver.

Original Playwright [documentation](https://playwright.dev/). We are converting it to javadoc.

