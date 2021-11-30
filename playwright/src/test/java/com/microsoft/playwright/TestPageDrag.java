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

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPageDrag extends TestBase {
  @Test
  void shouldWorkIfTheDragIsCanceled() {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    page.evaluate("() => {\n" +
      "      document.body.addEventListener('dragstart', event => {\n" +
      "        event.preventDefault();\n" +
      "      }, false);\n" +
      "    }");
    page.hover("#source");
    page.mouse().down();
    page.hover("#target");
    page.mouse().up();
    assertEquals(false, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))"));
  }

  @Test
  void shouldWorkIfTheDragEventIsCapturedButNotCanceled() {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    page.evaluate("() => {\n" +
      "      document.body.addEventListener('dragstart', event => {\n" +
      "        event.stopImmediatePropagation();\n" +
      "      }, false);\n" +
      "    }");
    page.hover("#source");
    page.mouse().down();
    page.hover("#target");
    page.mouse().up();
    assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))"));
  }

  @Test
  void shouldBeAbleToDragTheMouseInAFrame() {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    JSHandle eventsHandle = trackEvents(page.frames().get(1).querySelector("html"));
    page.mouse().move(30, 30);
    page.mouse().down();
    page.mouse().move(60, 60);
    page.mouse().up();
    assertEquals(asList("mousemove", "mousedown", "mousemove", "mouseup"), eventsHandle.jsonValue());
  }

  private static JSHandle trackEvents(ElementHandle target) {
    return target.evaluateHandle("target => {\n" +
      "  const events = [];\n" +
      "  for (const event of [\n" +
      "    'mousedown', 'mousemove', 'mouseup',\n" +
      "    'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'dragexit',\n" +
      "    'drop'\n" +
      "  ])\n" +
      "  target.addEventListener(event, () => events.push(event), false);\n" +
      "  return events;\n" +
      "}");
  }

  @Test
  void shouldWorkWithTheHelperMethod() {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    page.dragAndDrop("#source", "#target");
    assertEquals(true, page.evalOnSelector("#target",
      "target => target.contains(document.querySelector('#source'))")); // could not find source in target
  }

  @Test
  void shouldAllowSpecifyingThePosition() {
    page.setContent("<div style='width:100px;height:100px;background:red;' id='red'>\n" +
      "</div>\n" +
      "<div style='width:100px;height:100px;background:blue;' id='blue'>\n" +
      "</div>");
    JSHandle eventsHandle = page.evaluateHandle("() => {\n" +
      "      const events = [];\n" +
      "      document.getElementById('red').addEventListener('mousedown', event => {\n" +
      "        events.push({\n" +
      "          type: 'mousedown',\n" +
      "          x: event.offsetX,\n" +
      "          y: event.offsetY,\n" +
      "        });\n" +
      "      });\n" +
      "      document.getElementById('blue').addEventListener('mouseup', event => {\n" +
      "        events.push({\n" +
      "          type: 'mouseup',\n" +
      "          x: event.offsetX,\n" +
      "          y: event.offsetY,\n" +
      "        });\n" +
      "      });\n" +
      "      return events;\n" +
      "    }");
    page.dragAndDrop("#red", "#blue", new Page.DragAndDropOptions()
        .setSourcePosition(34, 7).setTargetPosition(10, 20));
    Object json = eventsHandle.jsonValue();
    assertJsonEquals("[{type: \"mousedown\", x: 34, y: 7},{type: \"mouseup\", x: 10, y: 20}]", json);
  }

  @Test
  void shouldWorkWithLocators() {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    page.locator("#source").dragTo(page.locator("#target"));
    assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))"));
  }
}
