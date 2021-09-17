---
name: Bug Report
about: Something doesn't work like it should? Tell us!
title: "[BUG]"
labels: ''
assignees: ''

---

**Context:**
- Playwright Version: [what Playwright version do you use?]
- Operating System: [e.g. Windows, Linux or Mac]
- Browser: [e.g. All, Chromium, Firefox, WebKit]
- Extra: [any specific details about your environment]

<!-- CLI to auto-capture this info -->
<!-- npx envinfo --preset playwright --markdown -->

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
