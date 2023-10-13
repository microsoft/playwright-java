# 🎭 [Playwright](https://playwright.dev) for Java

[![javadoc](https://javadoc.io/badge2/com.microsoft.playwright/playwright/javadoc.svg)](https://javadoc.io/doc/com.microsoft.playwright/playwright)
[![maven version](https://img.shields.io/maven-central/v/com.microsoft.playwright/playwright)](https://search.maven.org/search?q=com.microsoft.playwright)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.microsoft.playwright/playwright.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/microsoft/playwright/playwright/)
[![Join Slack](https://img.shields.io/badge/join-slack-infomational)](https://aka.ms/playwright-slack)

#### [Website](https://playwright.dev/java/) | [API reference](https://www.javadoc.io/doc/com.microsoft.playwright/playwright/latest/index.html)

Playwright is a Java library to automate [Chromium](https://www.chromium.org/Home), [Firefox](https://www.mozilla.org/en-US/firefox/new/) and [WebKit](https://webkit.org/) with a single API. Playwright is built to enable cross-browser web automation that is **ever-green**, **capable**, **reliable** and **fast**.

|          | Linux | macOS | Windows |
|   :---   | :---: | :---: | :---:   |
| Chromium <!-- GEN:chromium-version -->119.0.6045.9<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| WebKit <!-- GEN:webkit-version -->17.4<!-- GEN:stop --> | ✅ | ✅ | ✅ |
| Firefox <!-- GEN:firefox-version -->118.0.1<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |

Headless execution is supported for all the browsers on all platforms. Check out [system requirements](https://playwright.dev/java/docs/intro#system-requirements) for details.

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

Playwright is distributed as a set of [Maven](https://maven.apache.org/what-is-maven.html) modules. The easiest way to use it is to add one dependency to your Maven `pom.xml` file as described below. If you're not familiar with Maven please refer to its [documentation](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).

To run Playwright simply add following dependency to your Maven project:

```xml
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>1.28.1</version>
</dependency>
```

To run Playwright using Gradle add following dependency to your build.gradle file:

```gradle
dependencies {
  implementation group: 'com.microsoft.playwright', name: 'playwright', version: '1.28.1'
}
```

#### Is Playwright thread-safe?

No, Playwright is not thread safe, i.e. all its methods as well as methods on all objects created by it (such as BrowserContext, Browser, Page etc.) are expected to be called on the same thread where Playwright object was created or proper synchronization should be implemented to ensure only one thread calls Playwright methods at any given time. Having said that it's okay to create multiple Playwright instances each on its own thread.

## Examples

You can find Maven project with the examples [here](./examples).

#### Page screenshot

This code snippet navigates to Playwright homepage in Chromium, Firefox and WebKit, and saves 3 screenshots.

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
          page.navigate("https://playwright.dev/");
          page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot-" + browserType.name() + ".png")));
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
        .setUserAgent("Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36")
        .setViewportSize(411, 731)
        .setDeviceScaleFactor(2.625)
        .setIsMobile(true)
        .setHasTouch(true)
        .setLocale("en-US")
        .setGeolocation(41.889938, 12.492507)
        .setPermissions(asList("geolocation")));
      Page page = context.newPage();
      page.navigate("https://www.openstreetmap.org/");
      page.click("a[data-original-title=\"Show My Location\"]");
      page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("colosseum-pixel2.png")));
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

Check out our official [documentation site](https://playwright.dev/java).

You can also browse [javadoc online](https://www.javadoc.io/doc/com.microsoft.playwright/playwright/latest/index.html).

## Contributing

Follow [the instructions](https://github.com/microsoft/playwright-java/blob/main/CONTRIBUTING.md#getting-code) to build the project from source and install the driver.

## Is Playwright for Java ready?

Yes, Playwright for Java is ready. v1.10.0 is the first stable release. Going forward we will adhere to [semantic versioning](https://semver.org/) of the API.
