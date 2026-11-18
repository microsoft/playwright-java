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

package org.example;

import com.microsoft.playwright.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

// Feeds a WAV file to the page as microphone input: the file given as the first
// argument, or a generated tone. Chromium only: the file is
// read at launch and loops, so a different clip needs a new browser. Works for
// pages that capture with getUserMedia (MediaRecorder, WebRTC, Web Audio), not
// for webkitSpeechRecognition, which listens to the real microphone.
public class FakeMicrophoneInput {
  private static final int SAMPLE_RATE = 16000;

  public static void main(String[] args) throws IOException {
    boolean generated = args.length == 0;
    Path wav = generated ? Files.createTempFile("fake-mic", ".wav") : Paths.get(args[0]);
    try {
      if (generated) {
        writeTone(wav, 440, 1000, 2000, 1000);
      }
      try (Playwright playwright = Playwright.create()) {
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setArgs(asList(
          "--use-fake-ui-for-media-stream",
          "--use-fake-device-for-media-stream",
          "--use-file-for-fake-audio-capture=" + wav.toAbsolutePath())));
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
          .setPermissions(asList("microphone")));
        Page page = context.newPage();
        page.route("http://localhost/", route -> route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(
          "<button onclick='listen()'>Listen</button>\n" +
          "<meter id='level' max='1' style='width: 300px'></meter>\n" +
          "<script>\n" +
          "  async function listen() {\n" +
          "    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });\n" +
          "    const audio = new AudioContext();\n" +
          "    const analyser = audio.createAnalyser();\n" +
          "    audio.createMediaStreamSource(stream).connect(analyser);\n" +
          "    const samples = new Float32Array(analyser.fftSize);\n" +
          "    window.peak = 0;\n" +
          "    setInterval(() => {\n" +
          "      analyser.getFloatTimeDomainData(samples);\n" +
          "      const level = Math.max(...samples.map(Math.abs));\n" +
          "      document.getElementById('level').value = level;\n" +
          "      window.peak = Math.max(window.peak, level);\n" +
          "    }, 50);\n" +
          "  }\n" +
          "</script>")));
        page.navigate("http://localhost/");
        page.getByText("Listen").click();
        page.waitForFunction("() => window.peak > 0.1");
        System.out.println("Microphone input received, peak level: " + page.evaluate("window.peak"));
        browser.close();
      }
    } finally {
      if (generated) {
        Files.deleteIfExists(wav);
      }
    }
  }

  private static void writeTone(Path path, int frequency, int leadMs, int toneMs, int trailMs) throws IOException {
    int lead = SAMPLE_RATE * leadMs / 1000;
    int tone = SAMPLE_RATE * toneMs / 1000;
    int total = lead + tone + SAMPLE_RATE * trailMs / 1000;
    ByteBuffer buffer = ByteBuffer.allocate(44 + total * 2).order(ByteOrder.LITTLE_ENDIAN);
    buffer.put("RIFF".getBytes()).putInt(36 + total * 2).put("WAVE".getBytes());
    buffer.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1)
      .putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2).putShort((short) 2).putShort((short) 16);
    buffer.put("data".getBytes()).putInt(total * 2);
    for (int i = 0; i < total; i++) {
      boolean inTone = i >= lead && i < lead + tone;
      double sample = inTone ? 0.5 * Math.sin(2 * Math.PI * frequency * i / SAMPLE_RATE) : 0;
      buffer.putShort((short) (sample * Short.MAX_VALUE));
    }
    Files.write(path, buffer.array());
  }
}
