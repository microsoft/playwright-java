# 🎭 [Playwright](https://playwright.dev) for Java

[![maven version](https://img.shields.io/maven-central/v/com.microsoft.playwright/playwright)](https://search.maven.org/search?q=com.microsoft.playwright)  [![Join Slack](https://img.shields.io/badge/join-slack-infomational)](https://join.slack.com/t/playwright/shared_invite/enQtOTEyMTUxMzgxMjIwLThjMDUxZmIyNTRiMTJjNjIyMzdmZDA3MTQxZWUwZTFjZjQwNGYxZGM5MzRmNzZlMWI5ZWUyOTkzMjE5Njg1NDg)

#### [Website](https://playwright.dev/) | [API reference](https://www.javadoc.io/doc/com.microsoft.playwright/playwright/latest/index.html)

Playwright is a Java library to automate [Chromium](https://www.chromium.org/Home), [Firefox](https://www.mozilla.org/en-US/firefox/new/) and [WebKit](https://webkit.org/) with a single API. Playwright is built to enable cross-browser web automation that is **ever-green**, **capable**, **reliable** and **fast**.

|          | Linux | macOS | Windows |
|   :---   | :---: | :---: | :---:   |
| Chromium <!-- GEN:chromium-version -->89.0.4344.0<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| WebKit <!-- GEN:webkit-version -->14.1<!-- GEN:stop --> | ✅ | ✅ | ✅ |
| Firefox <!-- GEN:firefox-version -->84.0b9<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |

Headless execution is supported for all the browsers on all platforms. Check out [system requirements](https://playwright.dev/#?path=docs/intro.md&q=system-requirements) for details.

## Usage

Playwright supports Java 8 and above.

#### Add Maven dependency

Playwright is distributed as a set of [Maven](https://maven.apache.org/what-is-maven.html) modules. The easiest way to use it is to add a couple of dependencies to your Maven `pom.xml` file as described below. If you're not familiar with Maven please refer to its [documentation](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).

To run Playwright simply add following dependency to your Maven project:

```xml
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>0.171.0</version>
</dependency>
```

## Examples

You can find Maven project with the examples [here](./examples).

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
    Browser browser = browserType.launch(new BrowserType.LaunchOptions().withHeadless(false));
    DeviceDescriptor pixel2 = playwright.devices().get("Pixel 2");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .withDevice(pixel2)
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

## Documentation

We are in the process of converting our documentation from the Node.js form to [Javadocs](https://www.javadoc.io/doc/com.microsoft.playwright/playwright/latest/index.html). You can go ahead and use the Node.js [documentation](https://playwright.dev/) since the API is pretty much the same.

## Contributing

Follow [the instructions](https://github.com/microsoft/playwright-java/blob/master/CONTRIBUTING.md#getting-code) to build the project from source and install the driver.

## Is Playwright for Java ready?

Yes, Playwright for Java is ready. We are still not at the version v1.0, so breaking API changes could potentially happen. But a) this is unlikely and b) we will only do that if we know it improves your experience with the new library. We'd like to collect your feedback before we freeze the API for v1.0.

> Note: We don't yet support some of the edge-cases of the vendor-specific APIs such as collecting Chromium trace, coverage report, etc.
