# 🎭 [Playwright](https://playwright.dev) for Java

[![maven version](https://img.shields.io/maven-central/v/com.microsoft.playwright/playwright)](https://search.maven.org/search?q=com.microsoft.playwright)  [![Join Slack](https://img.shields.io/badge/join-slack-infomational)](https://join.slack.com/t/playwright/shared_invite/enQtOTEyMTUxMzgxMjIwLThjMDUxZmIyNTRiMTJjNjIyMzdmZDA3MTQxZWUwZTFjZjQwNGYxZGM5MzRmNzZlMWI5ZWUyOTkzMjE5Njg1NDg)

#### [Website](https://playwright.dev/) | [API reference](https://www.javadoc.io/doc/com.microsoft.playwright/playwright/latest/index.html)

Playwright is a Java library to automate [Chromium](https://www.chromium.org/Home), [Firefox](https://www.mozilla.org/en-US/firefox/new/) and [WebKit](https://webkit.org/) with a single API. Playwright is built to enable cross-browser web automation that is **ever-green**, **capable**, **reliable** and **fast**.

|          | Linux | macOS | Windows |
|   :---   | :---: | :---: | :---:   |
| Chromium <!-- GEN:chromium-version -->90.0.4392.0<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| WebKit <!-- GEN:webkit-version -->14.1<!-- GEN:stop --> | ✅ | ✅ | ✅ |
| Firefox <!-- GEN:firefox-version -->85.0b5<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |

Headless execution is supported for all the browsers on all platforms. Check out [system requirements](https://playwright.dev/#?path=docs/intro.md&q=system-requirements) for details.

* [Usage](#usage)
  - [Add Maven dependency](#add-maven-dependency)
  - [Is Playwright thread-safe?](#is-playwright-thread-safe)
* [Examples](#examples)
  - [Page screenshot](#page-screenshot)
  - [Mobile and geolocation](#mobile-and-geolocation)
  - [Evaluate JavaScript in browser](#evaluate-javascript-in-browser)
  - [Intercept network requests](#intercept-network-requests)
* [Documentation](#documentation)
* [Contributing](#contributing)
* [Is Playwright for Java ready?](#is-playwright-for-java-ready)

## Usage

Playwright requires **Java 8** or newer.

#### Add Maven dependency

Playwright is distributed as a set of [Maven](https://maven.apache.org/what-is-maven.html) modules. The easiest way to use it is to add a couple of dependencies to your Maven `pom.xml` file as described below. If you're not familiar with Maven please refer to its [documentation](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).

To run Playwright simply add following dependency to your Maven project:

```xml
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>0.180.0</version>
</dependency>
```

#### Is Playwright thread-safe?

No, Playwright is not thread safe, i.e. all its methods as well as methods on all objects created by it (such as BrowserContext, Browser, Page etc.) are expected to be called on the same thread where Playwright object was created or proper synchronization should be implemented to ensure only one thread calls Playwright methods at any given time. Having said that it's okay to create new many Playwright instances each on its own thread.

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
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      List<BrowserType> browserTypes = Arrays.asList(
        playwright.chromium(),
        playwright.webkit(),
        playwright.firefox()
      );
      for (BrowserType browserType : browserTypes) {
        try (Browser browser = browserType.launch()) {
          BrowserContext context = browser.newContext();
          Page page = context.newPage();
          page.navigate("http://whatsmyuseragent.org/");
          page.screenshot(new Page.ScreenshotOptions().withPath(Paths.get("screenshot-" + browserType.name() + ".png")));
        }
      }
    }
  }
}
```

#### Mobile and geolocation

This snippet emulates Mobile Chromium on a device at a given geolocation, navigates to openstreetmap.org, performs action and takes a screenshot.

```java
import com.microsoft.playwright.options.*;
import com.microsoft.playwright.*;

import java.nio.file.Paths;

import static java.util.Arrays.asList;

public class MobileAndGeolocation {
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch();
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .withUserAgent("Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36")
        .withViewportSize(411, 731)
        .withDeviceScaleFactor(2.625)
        .withIsMobile(true)
        .withHasTouch(true)
        .withLocale("en-US")
        .withGeolocation(41.889938, 12.492507)
        .withPermissions(asList("geolocation")));
      Page page = context.newPage();
      page.navigate("https://www.openstreetmap.org/");
      page.click("a[data-original-title=\"Show My Location\"]");
      page.screenshot(new Page.ScreenshotOptions().withPath(Paths.get("colosseum-pixel2.png")));
    }
  }
}
```

#### Evaluate JavaScript in browser

This code snippet navigates to example.com in Firefox, and executes a script in the page context.

```java
import com.microsoft.playwright.*;

public class EvaluateInBrowserContext {
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.firefox().launch();
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
    }
  }
}
```

#### Intercept network requests

This code snippet sets up request routing for a WebKit page to log all network requests.

```java
import com.microsoft.playwright.*;

public class InterceptNetworkRequests {
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.webkit().launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.route("**", route -> {
        System.out.println(route.request().url());
        route.resume();
      });
      page.navigate("http://todomvc.com");
    }
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
