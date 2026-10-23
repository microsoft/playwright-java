/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.WaitUntilState;

import static com.microsoft.playwright.options.WaitUntilState.COMMIT;
import static com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageSetContent {
  private static final String expectedOutput = "<html><head></head><body><div>hello</div></body></html>";

  @PlaywrightTest
  void shouldWork(Page page) {
    page.setContent("<div>hello</div>");
    Object result = page.content();
    assertEquals(expectedOutput, result);
  }

  @PlaywrightTest
  void shouldIncludeShadowRoots(Page page) {
    String html = "<!DOCTYPE html><html lang=\"en\"><head></head><body><div id=\"host\"><template shadowrootmode=\"open\"><div id=\"nested\"><template shadowrootmode=\"open\"><span>nested</span></template><slot></slot></div></template><span>light</span></div><div id=\"closed\"></div></body></html>";
    // Closed shadow roots are not accessible from script and are never serialized.
    page.setContent(html.replace("<div id=\"closed\">", "<div id=\"closed\"><template shadowrootmode=\"closed\"><span>closed</span></template>"));
    assertEquals("<!DOCTYPE html><html lang=\"en\"><head></head><body><div id=\"host\"><span>light</span></div><div id=\"closed\"></div></body></html>", page.content());
    assertEquals(html, page.content(new Page.ContentOptions().setIncludeShadow(true)));
  }

  @PlaywrightTest
  void shouldWorkWithDomcontentloaded(Page page) {
    page.setContent("<div>hello</div>", new Page.SetContentOptions().setWaitUntil(DOMCONTENTLOADED));
    Object result = page.content();
    assertEquals(expectedOutput, result);
  }

  @PlaywrightTest
  void shouldWorkWithCommit(Page page) {
    page.setContent("<div>hello</div>", new Page.SetContentOptions().setWaitUntil(COMMIT));
    Object result = page.content();
    assertEquals(expectedOutput, result);
  }


  @PlaywrightTest
  void shouldWorkWithDoctype(Page page) {
    String doctype = "<!DOCTYPE html>";
    page.setContent(doctype + "<div>hello</div>");
    Object result = page.content();
    assertEquals(doctype + expectedOutput, result);
  }

  @PlaywrightTest
  void shouldWorkWithHTML4Doctype(Page page) {
    String doctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
      "\"http://www.w3.org/TR/html4/strict.dtd\">";
    page.setContent(doctype + "<div>hello</div>");
    Object result = page.content();
    assertEquals(doctype + expectedOutput, result);
  }

  @PlaywrightTest
  void shouldRespectTimeout(Page page, Server server) {
    String imgPath = "/img.png";
    // stall for image
    server.setRoute(imgPath, exchange -> {});
    assertThrows(PlaywrightException.class, () -> {
      page.setContent("<img src='" + server.PREFIX + imgPath + "'></img>", new Page.SetContentOptions().setTimeout(100));
    });
  }

  @PlaywrightTest
  void shouldRespectDefaultNavigationTimeout(Page page, Server server) {
    page.setDefaultNavigationTimeout(100);
     String imgPath = "/img.png";
    // stall for image
    server.setRoute(imgPath, exchange -> {});
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.setContent("<img src='" + server.PREFIX + imgPath + "'></img>");
    });
    assertTrue(e.getMessage().contains("Timeout 100ms exceeded."), e.getMessage());
  }

  @PlaywrightTest
  void shouldWorkFastEnough(Page page) {
    for (int i = 0; i < 20; ++i) {
      page.setContent("<div>yo</div>");
    }
  }

  @PlaywrightTest
  void shouldWorkWithTrickyContent(Page page) {
    page.setContent("<div>hello world</div>" + "\\x7F");
    assertEquals("hello world", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  void shouldWorkWithAccents(Page page) {
    page.setContent("<div>aberración</div>");
    assertEquals("aberración", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  void shouldWorkWithEmojis(Page page) {
    page.setContent("<div>🐥</div>");
    assertEquals("🐥", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  void shouldWorkWithNewline(Page page) {
    page.setContent("<div>\n</div>");
    assertEquals("\n", page.evalOnSelector("div", "div => div.textContent"));
  }
}
