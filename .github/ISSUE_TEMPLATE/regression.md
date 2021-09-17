---
name: Report regression
about: Functionality that used to work and does not any more
title: "[REGRESSION]: "
labels: ''
assignees: ''

---

**Context:**
- GOOD Playwright Version: [what Playwright version worked nicely?]
- BAD Playwright Version: [what Playwright version doesn't work any more?]
- Operating System: [e.g. Windows, Linux or Mac]
- Extra: [any specific details about your environment]

**Code Snippet**

Help us help you! Put down a short code snippet that illustrates your bug and
that we can run and debug locally. For example:

```java
import com.microsoft.playwright.*;

public class ExampleReproducible {
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      // ...
    }
  }
}
```

**Describe the bug**

Add any other details about the problem here.
