# 🎭 [Playwright](https://github.com/microsoft/playwright) for Java

### _The project is in early developement phase, some of the APIs are not implemented yet, others may change._


## Usage

Follow [the instructions](https://github.com/microsoft/playwright-java/blob/master/CONTRIBUTING.md#getting-code) to build the project from source and install driver.


Simple example:

```java
package com.microsoft.playwright.example;

import com.microsoft.playwright.*;

import java.io.File;

public class Main {
  public static void main(String[] args) {
    Playwright playwright = Playwright.create();
    Browser browser = playwright.chromium().launch();
    BrowserContext context = browser.newContext(
      new Browser.NewContextOptions().withViewport(800, 600));
    Page page = context.newPage();
    page.navigate("https://webkit.org");
    page.click("text=check feature status");
    page.screenshot(new Page.ScreenshotOptions().withPath(new File("s.png")));
    browser.close();
  }
}
```

Original Playwright [documentation](https://playwright.dev/). We will convert it to Javadoc eventually.
