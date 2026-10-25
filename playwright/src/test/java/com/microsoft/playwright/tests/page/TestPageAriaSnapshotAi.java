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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaSnapshotMode;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.expectToContainYaml;
import static com.microsoft.playwright.fixtures.Utils.unshift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-aria-snapshot-ai.spec.ts. */
public class TestPageAriaSnapshotAi {
  private static String snapshotForAI(Page page) {
    return snapshotForAI(page, null);
  }

  private static String snapshotForAI(Page page, Page.AriaSnapshotOptions options) {
    if (options == null) {
      options = new Page.AriaSnapshotOptions();
    }
    return page.ariaSnapshot(options.setMode(AriaSnapshotMode.AI));
  }

  @PlaywrightTest
  @DisplayName("AR-048 should generate refs")
  void shouldGenerateRefs(Page page) {
    page.setContent("\n" +
        "    <button>One</button>\n" +
        "    <button>Two</button>\n" +
        "    <button>Three</button>\n" +
        "  ");

    String snapshot1 = snapshotForAI(page);
    expectToContainYaml(snapshot1, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - button \"One\" [ref=e2]\n" +
        "      - button \"Two\" [ref=e3]\n" +
        "      - button \"Three\" [ref=e4]\n" +
        "  ");
    assertThat(page.getByRef("e2")).hasText("One");
    assertThat(page.getByRef("e3")).hasText("Two");
    assertThat(page.getByRef("e4")).hasText("Three");

    page.getByRef("e3").evaluate("e => {\n" +
        "  e.textContent = 'Not Two';\n" +
        "}");

    String snapshot2 = snapshotForAI(page);
    expectToContainYaml(snapshot2, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - button \"One\" [ref=e2]\n" +
        "      - button \"Not Two\" [ref=e5]\n" +
        "      - button \"Three\" [ref=e4]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-049 should list iframes")
  void shouldListIframes(Page page) {
    page.setContent("\n" +
        "    <h1>Hello</h1>\n" +
        "    <iframe name=\"foo\" src=\"data:text/html,<h1>World</h1>\">\n" +
        "  ");

    String snapshot1 = snapshotForAI(page);
    assertTrue(snapshot1.contains("- iframe"));

    String frameSnapshot = page.frameLocator("iframe").locator("body").ariaSnapshot();
    assertEquals("- heading \"World\" [level=1]", frameSnapshot);
  }

  @PlaywrightTest
  @DisplayName("AR-050 should snapshot a locator inside an iframe")
  void shouldSnapshotALocatorInsideAnIframe(Page page) {
    page.setContent("\n" +
        "    <h1>Main Page</h1>\n" +
        "    <iframe srcdoc=\"<ul><li>Item 1</li><li>Item 2</li></ul>\"></iframe>\n" +
        "  ");

    Locator list = page.frames().get(1).locator("ul");
    String snapshot = list.ariaSnapshot(new Locator.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI));
    expectToContainYaml(snapshot, "\n" +
        "    - list [ref=f1e1]:\n" +
        "      - listitem [ref=f1e2]: Item 1\n" +
        "      - listitem [ref=f1e3]: Item 2\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-051 should limit depth across iframe boundary")
  void shouldLimitDepthAcrossIframeBoundary(Page page) {
    page.setContent("\n" +
        "    <nav>\n" +
        "      <iframe srcdoc=\"<ul><li><button>Deep</button></li></ul>\"></iframe>\n" +
        "    </nav>\n" +
        "  ");

    String snapshot = snapshotForAI(page, new Page.AriaSnapshotOptions().setDepth(3));
    expectToContainYaml(snapshot, "\n" +
        "    - navigation [ref=e2]:\n" +
        "      - iframe [ref=e3]:\n" +
        "        - list [ref=f1e2]:\n" +
        "          - listitem [ref=f1e3]\n" +
        "  ");
    assertFalse(snapshot.contains("button"));
  }

  @PlaywrightTest
  @DisplayName("AR-052 should stitch all frame snapshots")
  @Fixme("java: Locator.toString() prints Java code, locator(\"body\"), not the JavaScript locator('body')")
  void shouldStitchAllFrameSnapshots(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - iframe [ref=e2]:\n" +
        "        - generic [ref=f1e1]:\n" +
        "          - iframe [ref=f1e2]:\n" +
        "            - generic [ref=f3e1]: Hi, I'm frame\n" +
        "          - iframe [ref=f1e3]:\n" +
        "            - generic [ref=f4e1]: Hi, I'm frame\n" +
        "      - iframe [ref=e3]:\n" +
        "        - generic [ref=f2e1]: Hi, I'm frame\n" +
        "  ");

    Object href = page.getByRef("e1").evaluate("e => e.ownerDocument.defaultView.location.href");
    assertEquals(server.PREFIX + "/frames/nested-frames.html", href);

    Object href2 = page.getByRef("f1e2").evaluate("e => e.ownerDocument.defaultView.location.href");
    assertEquals(server.PREFIX + "/frames/two-frames.html", href2);

    Object href3 = page.getByRef("f4e2").evaluate("e => e.ownerDocument.defaultView.location.href");
    assertEquals(server.PREFIX + "/frames/frame.html", href3);

    {
      Locator resolved = page.getByRef("e1").normalize();
      assertEquals("locator('body')", resolved.toString());
    }
    {
      Locator resolved = page.getByRef("f4e2").normalize();
      assertEquals("locator('iframe[name=\"2frames\"]').contentFrame().locator('iframe[name=\"dos\"]').contentFrame().getByText('Hi, I\\'m frame')", resolved.toString());
    }
    {
      // Should tolerate .describe().
      Locator resolved = page.getByRef("f3e2").describe("foo bar").normalize();
      assertEquals("locator('iframe[name=\"2frames\"]').contentFrame().locator('iframe[name=\"uno\"]').contentFrame().getByText('Hi, I\\'m frame')", resolved.toString());
    }
    {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.getByRef("e1000").normalize());
      assertTrue(error.getMessage().contains("No element matching aria-ref=e1000"));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-053 should re-number refs across navigations but not same-document navigations")
  void shouldRenumberRefsAcrossNavigationsButNotSamedocumentNavigations(Page page, TestServer server) {
    server.setRoute("/one.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<button>One</button>");
    });
    server.setRoute("/two.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<button>Two</button>");
    });

    // The first committed document keeps the base seq, so the main frame has no prefix.
    page.navigate(server.PREFIX + "/one.html");
    Matcher oneMatch = Pattern.compile("button \"One\" \\[ref=(e\\d+)\\]").matcher(snapshotForAI(page));
    oneMatch.find();
    String oneRef = oneMatch.group(1);
    assertThat(page.getByRef(oneRef)).hasText("One");

    // Cross-document navigation re-numbers the main frame, so its refs gain a frame prefix.
    page.navigate(server.PREFIX + "/two.html");
    Matcher twoMatch = Pattern.compile("button \"Two\" \\[ref=(f\\d+e\\d+)\\]").matcher(snapshotForAI(page));
    twoMatch.find();
    String twoRef = twoMatch.group(1);
    assertThat(page.getByRef(twoRef)).hasText("Two");

    // The stale ref from the previous document must not resolve against the new one.
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.getByRef(oneRef).normalize());
    assertTrue(error.getMessage().contains("No element matching aria-ref=" + oneRef));

    // Same-document navigation keeps refs intact.
    page.evaluate("() => history.pushState({}, '', '/pushed.html')");
    assertTrue(snapshotForAI(page).contains("button \"Two\" [ref=" + twoRef + "]"));
    assertThat(page.getByRef(twoRef)).hasText("Two");
  }

  @PlaywrightTest
  @DisplayName("AR-054 should persist iframe references")
  void shouldPersistIframeReferences(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li><iframe srcdoc=\"<button>button1</button>\"></iframe></li>\n" +
        "      <li><iframe srcdoc=\"<button>button2</button>\"></iframe></li>\n" +
        "    </ul>\n" +
        "  ");
    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - list [ref=e2]:\n" +
        "      - listitem [ref=e3]:\n" +
        "        - iframe [ref=e4]:\n" +
        "          - button \"button1\" [ref=f1e2]\n" +
        "      - listitem [ref=e5]:\n" +
        "        - iframe [ref=e6]:\n" +
        "          - button \"button2\" [ref=f2e2]\n" +
        "  ");

    page.evaluate("() => document.querySelector('iframe').remove()");
    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - list [ref=e2]:\n" +
        "      - listitem [ref=e3]\n" +
        "      - listitem [ref=e5]:\n" +
        "        - iframe [ref=e6]:\n" +
        "          - button \"button2\" [ref=f2e2]\n" +
        "  ");
    assertThat(page.getByRef("f2e2")).hasText("button2");

    page.evaluate("() => {\n" +
        "  const frame = document.createElement('iframe');\n" +
        "  frame.setAttribute('srcdoc', '<button>button1</button>');\n" +
        "  document.querySelector('li').appendChild(frame);\n" +
        "}");
    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - list [ref=e2]:\n" +
        "      - listitem [ref=e3]:\n" +
        "        - iframe [ref=e7]:\n" +
        "          - button \"button1\" [ref=f3e2]\n" +
        "      - listitem [ref=e5]:\n" +
        "        - iframe [ref=e6]:\n" +
        "          - button \"button2\" [ref=f2e2]\n" +
        "  ");
    assertThat(page.getByRef("f3e2")).hasText("button1");
    assertThat(page.getByRef("f2e2")).hasText("button2");
  }

  @PlaywrightTest
  @DisplayName("AR-055 should not generate refs for elements with pointer-events:none")
  void shouldNotGenerateRefsForElementsWithPointereventsnone(Page page) {
    page.setContent("\n" +
        "    <button style=\"pointer-events: none\">no-ref</button>\n" +
        "    <div style=\"pointer-events: none\">\n" +
        "      <button style=\"pointer-events: auto\">with-ref</button>\n" +
        "    </div>\n" +
        "    <div style=\"pointer-events: none\">\n" +
        "      <div style=\"pointer-events: initial\">\n" +
        "        <button>with-ref</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div style=\"pointer-events: none\">\n" +
        "      <div style=\"pointer-events: auto\">\n" +
        "        <button>with-ref</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div style=\"pointer-events: auto\">\n" +
        "      <div style=\"pointer-events: none\">\n" +
        "        <button>no-ref</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - button \"no-ref\"\n" +
        "      - button \"with-ref\" [ref=e2]\n" +
        "      - button \"with-ref\" [ref=e4]\n" +
        "      - button \"with-ref\" [ref=e6]\n" +
        "      - generic [ref=e7]:\n" +
        "        - generic:\n" +
        "          - button \"no-ref\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-056 emit generic roles for nodes w/o roles")
  void emitGenericRolesForNodesWoRoles(Page page) {
    page.setContent("\n" +
        "    <style>\n" +
        "    input {\n" +
        "      width: 0;\n" +
        "      height: 0;\n" +
        "      opacity: 0;\n" +
        "    }\n" +
        "    </style>\n" +
        "    <div>\n" +
        "      <label>\n" +
        "        <span>\n" +
        "          <input type=\"radio\" value=\"Apple\" checked=\"\">\n" +
        "        </span>\n" +
        "        <span>Apple</span>\n" +
        "      </label>\n" +
        "      <label>\n" +
        "        <span>\n" +
        "          <input type=\"radio\" value=\"Pear\">\n" +
        "        </span>\n" +
        "        <span>Pear</span>\n" +
        "      </label>\n" +
        "      <label>\n" +
        "        <span>\n" +
        "          <input type=\"radio\" value=\"Orange\">\n" +
        "        </span>\n" +
        "        <span>Orange</span>\n" +
        "      </label>\n" +
        "    </div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);

    expectToContainYaml(snapshot, "\n" +
        "    - generic [ref=e2]:\n" +
        "      - generic [ref=e3]:\n" +
        "        - generic [ref=e4]:\n" +
        "          - radio \"Apple\" [checked]\n" +
        "        - text: Apple\n" +
        "      - generic [ref=e5]:\n" +
        "        - generic [ref=e6]:\n" +
        "          - radio \"Pear\"\n" +
        "        - text: Pear\n" +
        "      - generic [ref=e7]:\n" +
        "        - generic [ref=e8]:\n" +
        "          - radio \"Orange\"\n" +
        "        - text: Orange\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-057 should collapse generic nodes")
  void shouldCollapseGenericNodes(Page page) {
    page.setContent("\n" +
        "    <div>\n" +
        "      <div>\n" +
        "        <div>\n" +
        "          <button>Button</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - button \"Button\" [ref=e5]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-058 should include cursor pointer hint")
  void shouldIncludeCursorPointerHint(Page page) {
    page.setContent("\n" +
        "    <button style=\"cursor: pointer\">Button</button>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - button \"Button\" [ref=e2] [cursor=pointer]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-059 should not nest cursor pointer hints")
  void shouldNotNestCursorPointerHints(Page page) {
    page.setContent("\n" +
        "    <a style=\"cursor: pointer\" href=\"about:blank\">\n" +
        "      Link with a button\n" +
        "      <button style=\"cursor: pointer\">Button</button>\n" +
        "    </a>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    // The link's name is redundant - "Link with a button" prints as text and "Button" as the button -
    // so it is dropped even though the node is clickable.
    expectToContainYaml(snapshot, "\n" +
        "    - link [ref=e2] [cursor=pointer]:\n" +
        "      - /url: about:blank\n" +
        "      - text: Link with a button\n" +
        "      - button \"Button\" [ref=e3]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-060 should omit names that just repeat printed descendant nodes")
  void shouldOmitNamesThatJustRepeatPrintedDescendantNodes(Page page) {
    page.setContent("\n" +
        "    <h3><a style=\"cursor: pointer\" href=\"/issues/1\">Clipboard API</a></h3>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - heading [level=3] [ref=e2]:\n" +
        "      - link \"Clipboard API\" [ref=e3] [cursor=pointer]:\n" +
        "        - /url: /issues/1\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-061 should omit redundant name when a contributing wrapper is collapsed")
  void shouldOmitRedundantNameWhenAContributingWrapperIsCollapsed(Page page) {
    // The flex span contributes to the heading's name, but is then removed from the tree as a
    // single-child generic wrapper. Its contribution is fully represented by the link, so the
    // heading's name is still redundant.
    page.setContent("\n" +
        "    <h3><span style=\"display: flex\"><a style=\"cursor: pointer\" href=\"/issues/1\">Clipboard API</a></span></h3>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - heading [level=3] [ref=e2]:\n" +
        "      - link \"Clipboard API\" [ref=e4] [cursor=pointer]:\n" +
        "        - /url: /issues/1\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-062 should omit redundant name when a contributor is a skipped leaf generic")
  void shouldOmitRedundantNameWhenAContributorIsASkippedLeafGeneric(Page page) {
    // The outer span is not collapsed (its child is an element, not text), so it becomes a leaf
    // generic node that contributes to both names. The link's rendered name covers it, which in turn
    // makes the heading's name redundant.
    page.setContent("\n" +
        "    <h3><a style=\"cursor: pointer\" href=\"/issues/1\"><span><span>Clipboard API</span></span></a></h3>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - heading [level=3] [ref=e2]:\n" +
        "      - link \"Clipboard API\" [ref=e3] [cursor=pointer]:\n" +
        "        - /url: /issues/1\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-063 should keep the name when the contributing wrapper collapses into repeating text")
  @Fixme("driver: the bundled driver drops the names of the buttons, snapshot has - button [ref=e7] and - button [ref=e10] instead of \"New survey\" and \"Edit survey\"")
  void shouldKeepTheNameWhenTheContributingWrapperCollapsesIntoRepeatingText(Page page) {
    // The outer span is marked as represented while it still wraps the icon. Once the nameless
    // image is dropped, the span collapses into a lone text repeating the button's name and removes
    // itself, so the button must keep the name derived from it.
    // A wrapper that only adds whitespace to the name - a stray text node, or the spacing around an
    // inline-block icon - is not where the name comes from.
    page.setContent("\n" +
        "    <button>\n" +
        "      <span>\n" +
        "        <span>\n" +
        "          <svg focusable=\"false\" tabindex=\"-1\" aria-hidden=\"true\" viewBox=\"0 0 448 512\">\n" +
        "            <path d=\"M416 208H272V64c0-17.67-14.33-32-32-32h-32c-17.67 0-32 14.33-32 32v144H32c-17.67 0-32 14.33-32 32v32c0 17.67 14.33 32 32 32h144v144c0 17.67 14.33 32 32 32h32c17.67 0 32-14.33 32-32V304h144c17.67 0 32-14.33 32-32v-32c0-17.67-14.33-32-32-32z\"/>\n" +
        "          </svg>\n" +
        "        </span>\n" +
        "        <span>Add New Item</span>\n" +
        "      </span>\n" +
        "    </button>\n" +
        "    <button><span>\n  <svg aria-hidden=\"true\"></svg></span>New survey</button>\n" +
        "    <button><span><svg aria-hidden=\"true\" style=\"width: 1em; height: 1em; display: inline-block\"></svg></span>Edit survey</button>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - button \"Add New Item\" [ref=e2]\n" +
        "      - button \"New survey\" [ref=e7]\n" +
        "      - button \"Edit survey\" [ref=e10]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-064 should keep names not derived from printed nodes")
  void shouldKeepNamesNotDerivedFromPrintedNodes(Page page) {
    page.setContent("\n" +
        "    <h3 aria-label=\"Clipboard API issue\"><a style=\"cursor: pointer\" href=\"/issues/1\">Clipboard API</a></h3>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - heading \"Clipboard API issue\" [level=3] [ref=e2]:\n" +
        "      - link \"Clipboard API\" [ref=e3] [cursor=pointer]:\n" +
        "        - /url: /issues/1\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-065 should omit images without an accessible name")
  void shouldOmitImagesWithoutAnAccessibleName(Page page) {
    page.setContent("\n" +
        "    <img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\">\n" +
        "    <img alt=\"A cat\" src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\">\n" +
        "    <img style=\"cursor: pointer\" src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\">\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    // A nameless image that cannot be clicked carries no information and is omitted.
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - img \"A cat\" [ref=e3]\n" +
        "      - img [ref=e4] [cursor=pointer]\n" +
        "  ");
    assertFalse(snapshot.contains("[ref=e2]"));
  }

  @PlaywrightTest
  @DisplayName("AR-066 should omit a nameless image nested inside a link")
  void shouldOmitANamelessImageNestedInsideALink(Page page) {
    // The decorative image has no name, so it is dropped even though it sits inside a clickable link.
    page.setContent("\n" +
        "    <a style=\"cursor: pointer\" href=\"/issue/1\">Open issue <img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\"></a>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - link \"Open issue\" [ref=e2] [cursor=pointer]:\n" +
        "      - /url: /issue/1\n" +
        "  ");
    assertFalse(snapshot.contains("img"));
  }

  @PlaywrightTest
  @DisplayName("AR-067 should keep icon-only clickable elements")
  void shouldKeepIcononlyClickableElements(Page page) {
    String icon = "<svg viewBox=\"0 0 1024 1024\"><use xlink:href=\"#icon\"></use></svg>";
    page.setContent("\n" +
        "    <div style=\"cursor: pointer\" aria-haspopup=\"true\"><i>" + icon + "</i></div>\n" +
        "    <img style=\"cursor: pointer\" src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\">\n" +
        "    <a style=\"cursor: pointer\" href=\"/target\">" + icon + "</a>\n" +
        "    <div onclick=\"void 0\">" + icon + "</div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - generic [ref=e2] [cursor=pointer]\n" +
        "      - img [ref=e5] [cursor=pointer]\n" +
        "      - link [ref=e6] [cursor=pointer]:\n" +
        "        - /url: /target\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-068 should omit leaf generic whose text is already in an ancestor name")
  void shouldOmitLeafGenericWhoseTextIsAlreadyInAnAncestorName(Page page) {
    // The inner element is block so it survives as its own generic node (an inline single-text span
    // would be collapsed into the link instead). It inherits the link's pointer cursor.
    page.setContent("\n" +
        "    <a style=\"cursor: pointer\" href=\"/issues/15860\"><div>[Feature] a dedicated clipboard API</div></a>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    // The link keeps its name, and the inner leaf generic that the name was computed from is dropped
    // because its text is already shown by the name.
    expectToContainYaml(snapshot, "\n" +
        "    - link \"[Feature] a dedicated clipboard API\" [ref=e2] [cursor=pointer]:\n" +
        "      - /url: /issues/15860\n" +
        "  ");
    assertFalse(snapshot.contains("[ref=e3]"));
  }

  @PlaywrightTest
  @DisplayName("AR-069 should omit name-repeating generic behind a wrapper")
  void shouldOmitNamerepeatingGenericBehindAWrapper(Page page) {
    // The leaf generic that repeats the link's name sits inside a nameless wrapper. Its text is
    // first inlined into the wrapper, which then faces the link and removes itself.
    page.setContent("\n" +
        "    <a style=\"cursor: pointer\" href=\"/labels\"><span style=\"display: inline-block\"><span style=\"display: inline-block\"><span>P3-collecting-feedback</span></span></span></a>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - link \"P3-collecting-feedback\" [ref=e2] [cursor=pointer]:\n" +
        "      - /url: /labels\n" +
        "  ");
    assertEquals(2, snapshot.split("P3-collecting-feedback", -1).length);
  }

  @PlaywrightTest
  @DisplayName("AR-070 should resolve refs of distilled-away nodes")
  void shouldResolveRefsOfDistilledawayNodes(Page page) {
    page.setContent("\n" +
        "    <a style=\"cursor: pointer\" href=\"/issues/15860\"><div>[Feature] a dedicated clipboard API</div></a>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    // The inner leaf generic is distilled away, but its ref still resolves to the element.
    assertFalse(snapshot.contains("[ref=e3]"));
    assertThat(page.getByRef("e3")).hasText("[Feature] a dedicated clipboard API");
  }

  @PlaywrightTest
  @DisplayName("AR-071 should not distill snapshots outside of ai mode")
  void shouldNotDistillSnapshotsOutsideOfAiMode(Page page) {
    page.setContent("\n" +
        "    <h3><a href=\"/issues/1\">Clipboard API</a></h3>\n" +
        "  ");

    // The heading name would be dropped as redundant in ai mode; matching mode keeps it.
    assertThat(page.locator("body")).matchesAriaSnapshot("\n" +
        "    - heading \"Clipboard API\" [level=3]:\n" +
        "      - link \"Clipboard API\":\n" +
        "        - /url: /issues/1\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-072 should truncate data url in link")
  void shouldTruncateDataUrlInLink(Page page) {
    String base64 = Base64.getEncoder().encodeToString("<p>hello</p>".getBytes(StandardCharsets.UTF_8));
    page.setContent("<a href=\"data:text/html;base64," + base64 + "\">a link</a>");
    String snapshot = snapshotForAI(page);
    assertTrue(snapshot.contains("/url: data:text/html;base64,…"));
    assertFalse(snapshot.contains(base64));
  }

  @PlaywrightTest
  @DisplayName("AR-073 should gracefully fallback when child frame cant be captured")
  void shouldGracefullyFallbackWhenChildFrameCantBeCaptured(Page page, TestServer server) {
    page.setContent("\n" +
        "    <p>Test</p>\n" +
        "    <iframe src=\"" + server.PREFIX + "/redirectloop1.html#depth=100000\"></iframe>\n" +
        "  ", new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - paragraph [ref=e2]: Test\n" +
        "      - iframe [ref=e3]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-074 should auto-wait for navigation")
  void shouldAutowaitForNavigation(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/frames/frame.html");
    CompletableFuture<Object> reload = async(() -> page.evaluate("() => window.location.reload()"));
    String snapshot = snapshotForAI(page);
    reload.get();
    // The snapshot races the reload, which may re-number the main frame, so accept any ref.
    assertTrue(Pattern.compile("- generic \\[active\\] \\[ref=(?:f\\d+)?e\\d+\\]: Hi, I'm frame").matcher(snapshot).find());
  }

  @PlaywrightTest
  @DisplayName("AR-075 should auto-wait for blocking CSS")
  void shouldAutowaitForBlockingCSS(Page page, TestServer server) {
    server.setRoute("/css", (req, res) -> {
      res.setHeader("Content-Type", "text/css");
      // Java: the handler runs on a thread of the server, so setTimeout is a sleep before the end.
      Thread.sleep(1000);
      res.end("body { monospace }");
    });
    page.setContent("\n" +
        "    <script src=\"" + server.PREFIX + "/css\"></script>\n" +
        "    <p>Hello World</p>\n" +
        "  ", new Page.SetContentOptions().setWaitUntil(WaitUntilState.COMMIT));
    expectToContainYaml(snapshotForAI(page), "Hello World");
  }

  @PlaywrightTest
  @DisplayName("AR-076 should show visible children of hidden elements")
  void shouldShowVisibleChildrenOfHiddenElements(Page page) {
    page.setContent("\n" +
        "    <div style=\"visibility: hidden\">\n" +
        "      <div style=\"visibility: visible\">\n" +
        "        <button>Visible</button>\n" +
        "      </div>\n" +
        "      <div style=\"visibility: hidden\">\n" +
        "        <button style=\"visibility: visible\">Visible</button>\n" +
        "      </div>\n" +
        "      <div>\n" +
        "        <div style=\"visibility: visible\">\n" +
        "          <button style=\"visibility: hidden\">Hidden</button>\n" +
        "        </div>\n" +
        "        <button>Hidden</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  ");

    assertEquals(unshift("\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - button \"Visible\" [ref=e3]\n" +
        "      - button \"Visible\" [ref=e4]\n" +
        "  "), snapshotForAI(page));
  }

  @PlaywrightTest
  @DisplayName("AR-077 should include active element information")
  void shouldIncludeActiveElementInformation(Page page) {
    page.setContent("\n" +
        "    <button id=\"btn1\">Button 1</button>\n" +
        "    <button id=\"btn2\" autofocus>Button 2</button>\n" +
        "    <div>Not focusable</div>\n" +
        "  ");

    // Wait for autofocus to take effect
    page.waitForFunction("() => document.activeElement?.id === 'btn2'");

    String snapshot = snapshotForAI(page);

    expectToContainYaml(snapshot, "\n" +
        "    - generic [ref=e1]:\n" +
        "      - button \"Button 1\" [ref=e2]\n" +
        "      - button \"Button 2\" [active] [ref=e3]\n" +
        "      - generic [ref=e4]: Not focusable\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-078 should update active element on focus")
  void shouldUpdateActiveElementOnFocus(Page page) {
    page.setContent("\n" +
        "    <input id=\"input1\" placeholder=\"First input\">\n" +
        "    <input id=\"input2\" placeholder=\"Second input\">\n" +
        "  ");

    // Initially there shouldn't be an active element on the inputs
    String initialSnapshot = snapshotForAI(page);
    expectToContainYaml(initialSnapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - textbox \"First input\" [ref=e2]\n" +
        "      - textbox \"Second input\" [ref=e3]\n" +
        "  ");

    // Focus the second input
    page.locator("#input2").focus();

    // After focus, the second input should be active
    String afterFocusSnapshot = snapshotForAI(page);

    expectToContainYaml(afterFocusSnapshot, "\n" +
        "    - generic [ref=e1]:\n" +
        "      - textbox \"First input\" [ref=e2]\n" +
        "      - textbox \"Second input\" [active] [ref=e3]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-079 should mark iframe as active when it contains focused element")
  void shouldMarkIframeAsActiveWhenItContainsFocusedElement(Page page) {
    // Create a simple HTML file for the iframe
    page.setContent("\n" +
        "    <input id=\"regular-input\" placeholder=\"Regular input\">\n" +
        "    <iframe src=\"data:text/html,<input id='iframe-input' placeholder='Input in iframe'>\" tabindex=\"0\"></iframe>\n" +
        "  ");

    // Test 1: Focus the input inside the iframe
    page.frameLocator("iframe").locator("#iframe-input").focus();
    String inputInIframeFocusedSnapshot = snapshotForAI(page);

    // The iframe should be marked as active when it contains a focused element
    expectToContainYaml(inputInIframeFocusedSnapshot, "\n" +
        "    - generic [ref=e1]:\n" +
        "      - textbox \"Regular input\" [ref=e2]\n" +
        "      - iframe [active] [ref=e3]:\n" +
        "        - textbox \"Input in iframe\" [active] [ref=f1e2]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-080 return empty snapshot when iframe is not loaded")
  void returnEmptySnapshotWhenIframeIsNotLoaded(Page page, TestServer server) {
    page.setContent("\n" +
        "    <div style=\"height: 5000px;\">Test</div>\n" +
        "    <iframe loading=\"lazy\" src=\"" + server.PREFIX + "/frame.html\"></iframe>\n" +
        "  ");

    // Wait for the iframe to load
    page.waitForSelector("iframe");

    // Get the snapshot of the page
    String snapshot = snapshotForAI(page, new Page.AriaSnapshotOptions().setTimeout(3000));

    // The iframe should be present but empty
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - generic [ref=e2]: Test\n" +
        "      - iframe [ref=e3]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-081 should support many properties on iframes")
  void shouldSupportManyPropertiesOnIframes(Page page) {
    page.setContent("\n" +
        "    <input id=\"regular-input\" placeholder=\"Regular input\">\n" +
        "    <iframe style='cursor: pointer' src=\"data:text/html,<input id='iframe-input' placeholder='Input in iframe'/>\" tabindex=\"0\"></iframe>\n" +
        "  ");

    // Test 1: Focus the input inside the iframe
    page.frameLocator("iframe").locator("#iframe-input").focus();
    String inputInIframeFocusedSnapshot = snapshotForAI(page);

    expectToContainYaml(inputInIframeFocusedSnapshot, "\n" +
        "    - generic [ref=e1]:\n" +
        "      - textbox \"Regular input\" [ref=e2]\n" +
        "      - iframe [active] [ref=e3] [cursor=pointer]:\n" +
        "        - textbox \"Input in iframe\" [active] [ref=f1e2]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-082 should snapshot frameset pages")
  void shouldSnapshotFramesetPages(Page page, TestServer server) {
    server.setRoute("/frameset.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<frameset rows=\"50%,50%\"><frameset cols=\"50%,50%\"><frame src=\"/frame-one.html\"><frame src=\"/frame-two.html\"></frameset><frame src=\"/frame-three.html\"></frameset>");
    });
    for (String name : new String[]{"one", "two", "three"}) {
      server.setRoute("/frame-" + name + ".html", (req, res) -> {
        res.setHeader("Content-Type", "text/html");
        res.end("<button>Button " + name + "</button>");
      });
    }
    page.navigate(server.PREFIX + "/frameset.html");

    String snapshot = snapshotForAI(page, new Page.AriaSnapshotOptions().setTimeout(3000));
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - generic [ref=e2]:\n" +
        "        - iframe [ref=e3]:\n" +
        "          - button \"Button one\" [ref=f1e2]\n" +
        "        - iframe [ref=e4]:\n" +
        "          - button \"Button two\" [ref=f2e2]\n" +
        "      - iframe [ref=e5]:\n" +
        "        - button \"Button three\" [ref=f3e2]\n" +
        "  ");
    assertThat(page.getByRef("f2e2")).hasText("Button two");
  }

  @PlaywrightTest
  @DisplayName("AR-083 should snapshot a locator inside a frameset frame")
  @Fixme("java: Locator.toString() prints Java code, locator(\"frame\").first().contentFrame().locator(\"body\"), not the JavaScript form")
  void shouldSnapshotALocatorInsideAFramesetFrame(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/frameset.html");

    String snapshot = page.frames().get(1).locator("body").ariaSnapshot(new Locator.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI));
    expectToContainYaml(snapshot, "\n" +
        "    - generic [ref=f1e1]: Hi, I'm frame\n" +
        "  ");

    assertThat(page.getByRef("f1e1")).hasText("Hi, I'm frame");
    Locator resolved = page.getByRef("f1e1").normalize();
    assertEquals("locator('frame').first().contentFrame().locator('body')", resolved.toString());
  }

  @PlaywrightTest
  @DisplayName("AR-084 should stitch iframes inside a frameset frame")
  @Fixme("java: Locator.toString() prints Java code, getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName(\"In iframe\")), not the JavaScript form")
  void shouldStitchIframesInsideAFramesetFrame(Page page, TestServer server) {
    server.setRoute("/frameset-with-iframe.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<frameset><frame src=\"/frame-with-iframe.html\"></frameset>");
    });
    server.setRoute("/frame-with-iframe.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<button>In frame</button><iframe srcdoc=\"<button>In iframe</button>\"></iframe>");
    });
    page.navigate(server.PREFIX + "/frameset-with-iframe.html");

    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - iframe [ref=e2]:\n" +
        "      - generic [ref=f1e1]:\n" +
        "        - button \"In frame\" [ref=f1e2]\n" +
        "        - iframe [ref=f1e3]:\n" +
        "          - button \"In iframe\" [ref=f2e2]\n" +
        "  ");
    assertThat(page.getByRef("f1e2")).hasText("In frame");
    assertThat(page.getByRef("f2e2")).hasText("In iframe");

    Locator resolved = page.getByRef("f2e2").normalize();
    assertEquals("locator('frame').contentFrame().locator('iframe').contentFrame().getByRole('button', { name: 'In iframe' })", resolved.toString());
  }

  @PlaywrightTest
  @DisplayName("AR-085 should stitch nested frameset documents")
  void shouldStitchNestedFramesetDocuments(Page page, TestServer server) {
    server.setRoute("/outer-frameset.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<frameset><frame src=\"/inner-frameset.html\"></frameset>");
    });
    server.setRoute("/inner-frameset.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<frameset><frameset><frame src=\"/leaf.html\"></frameset></frameset>");
    });
    server.setRoute("/leaf.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<button>Leaf button</button>");
    });
    page.navigate(server.PREFIX + "/outer-frameset.html");

    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - iframe [ref=e2]:\n" +
        "      - iframe [ref=f1e3]:\n" +
        "        - button \"Leaf button\" [ref=f2e2]\n" +
        "  ");
    assertThat(page.getByRef("f2e2")).hasText("Leaf button");
  }

  @PlaywrightTest
  @DisplayName("AR-086 should collapse inline generic nodes")
  void shouldCollapseInlineGenericNodes(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li><b>3</b> <abbr>bds</abbr></li>\n" +
        "      <li><b>2</b> <abbr>ba</abbr></li>\n" +
        "      <li><b>1,200</b> <abbr>sqft</abbr></li>\n" +
        "    </ul>\n" +
        "    <ul>\n" +
        "      <li><div>3</div></li>\n" +
        "      <li><div>2</div></li>\n" +
        "      <li><div>1,200</div></li>\n" +
        "    </ul>");

    String snapshot1 = snapshotForAI(page);
    expectToContainYaml(snapshot1, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - list [ref=e2]:\n" +
        "        - listitem [ref=e3]: 3 bds\n" +
        "        - listitem [ref=e4]: 2 ba\n" +
        "        - listitem [ref=e5]: 1,200 sqft\n" +
        "      - list [ref=e6]:\n" +
        "        - listitem [ref=e7]:\n" +
        "          - generic [ref=e8]: \"3\"\n" +
        "        - listitem [ref=e9]:\n" +
        "          - generic [ref=e10]: \"2\"\n" +
        "        - listitem [ref=e11]:\n" +
        "          - generic [ref=e12]: 1,200\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-087 should inline single leaf generic child into parent generic")
  void shouldInlineSingleLeafGenericChildIntoParentGeneric(Page page) {
    // The nameless images are distilled away, so each wrapper is left with a single leaf generic
    // child, whose text is inlined into the wrapper - recursively for the second one.
    page.setContent("\n" +
        "    <div><img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\"><div>Status: Open.</div></div>\n" +
        "    <div><img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\"><div><img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\"><div>Nested twice.</div></div></div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - generic [ref=e2]: \"Status: Open.\"\n" +
        "      - generic [ref=e5]: Nested twice.\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-088 should inline a deeply nested generic")
  void shouldInlineADeeplyNestedGeneric(Page page) {
    // Every wrapper contains a nameless image (distilled away) plus a single generic child, so the
    // text bubbles up the whole chain - all the way into the body.
    String img = "<img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACwAAAAAAQABAAA=\">";
    page.setContent("\n" +
        "    <div>" + img + "<div>" + img + "<div>" + img + "<div>" + img + "<div>Deeply nested.</div></div></div></div></div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]: Deeply nested.\n" +
        "  ");
    assertFalse(snapshot.contains("img"));
  }

  @PlaywrightTest
  @DisplayName("AR-089 should not remove generic nodes with title")
  void shouldNotRemoveGenericNodesWithTitle(Page page) {
    page.setContent("<div title=\"Element title\">Element content</div>");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic \"Element title\" [ref=e2]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-090 should limit depth")
  void shouldLimitDepth(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>item1</li>\n" +
        "      <a href=\"about:blank\" style=\"cursor:pointer\">link</a>\n" +
        "      <li>\n" +
        "        <ul id=target>\n" +
        "          <li>item2</li>\n" +
        "          <li>\n" +
        "            <ul>\n" +
        "              <li>item3</li>\n" +
        "            </ul>\n" +
        "          </li>\n" +
        "        </ul>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");

    String snapshot1 = snapshotForAI(page, new Page.AriaSnapshotOptions().setDepth(1));
    expectToContainYaml(snapshot1, "\n" +
        "    - list [ref=e2]:\n" +
        "      - listitem [ref=e3]: item1\n" +
        "      - link \"link\" [ref=e4] [cursor=pointer]:\n" +
        "        - /url: about:blank\n" +
        "      - listitem [ref=e5]\n" +
        "  ");

    String snapshot2 = snapshotForAI(page, new Page.AriaSnapshotOptions().setDepth(3));
    expectToContainYaml(snapshot2, "\n" +
        "    - list [ref=e2]:\n" +
        "      - listitem [ref=e3]: item1\n" +
        "      - link \"link\" [ref=e4] [cursor=pointer]:\n" +
        "        - /url: about:blank\n" +
        "      - listitem [ref=e5]:\n" +
        "        - list [ref=e6]:\n" +
        "          - listitem [ref=e7]: item2\n" +
        "          - listitem [ref=e8]\n" +
        "  ");

    String snapshot3 = snapshotForAI(page, new Page.AriaSnapshotOptions().setDepth(100));
    expectToContainYaml(snapshot3, "\n" +
        "    - list [ref=e2]:\n" +
        "      - listitem [ref=e3]: item1\n" +
        "      - link \"link\" [ref=e4] [cursor=pointer]:\n" +
        "        - /url: about:blank\n" +
        "      - listitem [ref=e5]:\n" +
        "        - list [ref=e6]:\n" +
        "          - listitem [ref=e7]: item2\n" +
        "          - listitem [ref=e8]:\n" +
        "            - list [ref=e9]:\n" +
        "              - listitem [ref=e10]: item3\n" +
        "  ");

    String snapshot4 = page.locator("#target").ariaSnapshot(new Locator.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI).setDepth(1));
    expectToContainYaml(snapshot4, "\n" +
        "    - list [ref=e6]:\n" +
        "      - listitem [ref=e7]: item2\n" +
        "      - listitem [ref=e8]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-091 should annotate aria-hidden elements")
  void shouldAnnotateAriahiddenElements(Page page) {
    page.setContent("\n" +
        "    <h2>Visible heading</h2>\n" +
        "    <div aria-hidden=\"true\">\n" +
        "      <h1>Hidden heading</h1>\n" +
        "      <p>Hidden content</p>\n" +
        "    </div>\n" +
        "    <h2>After hidden</h2>\n" +
        "  ");

    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - heading \"Visible heading\" [level=2] [ref=e2]\n" +
        "      - generic [aria-hidden] [ref=e3]:\n" +
        "        - heading [level=1] [ref=e4]: Hidden heading\n" +
        "        - paragraph [ref=e5]: Hidden content\n" +
        "      - heading \"After hidden\" [level=2] [ref=e6]\n" +
        "  ");

    // Default snapshot excludes aria-hidden elements entirely.
    String defaultSnapshot = page.locator("body").ariaSnapshot();
    assertFalse(defaultSnapshot.contains("Hidden content"));
  }

  @PlaywrightTest
  @DisplayName("AR-092 should only annotate the top element in a hidden subtree")
  void shouldOnlyAnnotateTheTopElementInAHiddenSubtree(Page page) {
    page.setContent("\n" +
        "    <div aria-hidden=\"true\">\n" +
        "      <h1>Heading</h1>\n" +
        "      <p>Paragraph</p>\n" +
        "    </div>\n" +
        "  ");

    // Only the element with aria-hidden="true" gets the annotation, not its descendants.
    expectToContainYaml(snapshotForAI(page), "\n" +
        "    - generic [aria-hidden] [ref=e2]:\n" +
        "      - heading [level=1] [ref=e3]: Heading\n" +
        "      - paragraph [ref=e4]: Paragraph\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-093 should not include hidden iframes")
  void shouldNotIncludeHiddenIframes(Page page) {
    page.setContent("\n" +
        "    <iframe name=\"visible\" srcdoc=\"<button>Visible</button>\"></iframe>\n" +
        "    <iframe name=\"hidden\" style=\"visibility: hidden\" srcdoc=\"<button>Hidden</button>\"></iframe>\n" +
        "    <iframe name=\"none\" style=\"display: none\" srcdoc=\"<button>None</button>\"></iframe>\n" +
        "  ");
    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - iframe [ref=e2]:\n" +
        "      - button \"Visible\" [ref=f1e2]\n" +
        "  ");
    assertFalse(snapshot.contains("Hidden"));
    assertFalse(snapshot.contains("None"));
  }

  @PlaywrightTest
  @DisplayName("AR-094 should not generate refs for inert elements")
  @Fixme("driver: the bundled driver generates refs for inert elements, snapshot has - button \"no-ref\" [ref=e3] and - button \"no-ref\" [ref=e5]")
  void shouldNotGenerateRefsForInertElements(Page page) {
    page.setContent("\n" +
        "    <button>with-ref</button>\n" +
        "    <button inert>no-ref</button>\n" +
        "    <div inert>\n" +
        "      <button>no-ref</button>\n" +
        "    </div>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic [active] [ref=e1]:\n" +
        "      - button \"with-ref\" [ref=e2]\n" +
        "      - button: no-ref\n" +
        "      - generic:\n" +
        "        - button: no-ref\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-095 should not generate refs for elements outside of a modal dialog")
  @Fixme("driver: the bundled driver generates refs for elements outside of the modal dialog, snapshot has - generic [ref=e1], - button \"Outside\" [ref=e2], - dialog [ref=e3]")
  void shouldNotGenerateRefsForElementsOutsideOfAModalDialog(Page page) {
    page.setContent("\n" +
        "    <button>Outside</button>\n" +
        "    <dialog id=dialog>\n" +
        "      <button>Inside</button>\n" +
        "    </dialog>\n" +
        "    <script>\n" +
        "      document.getElementById('dialog').showModal();\n" +
        "    </script>\n" +
        "  ");

    String snapshot = snapshotForAI(page);
    expectToContainYaml(snapshot, "\n" +
        "    - generic:\n" +
        "      - button: Outside\n" +
        "      - dialog [ref=e1]:\n" +
        "        - button \"Inside\" [active] [ref=e2]\n" +
        "  ");
  }
}
