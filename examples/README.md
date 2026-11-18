This directory contains sample [`pom.xml`](./pom.xml) and source code for the Playwright examples.

You can run them in terminal like this:

```sh
mvn compile exec:java -Dexec.mainClass=org.example.PageScreenshot
```

## Fake microphone input

[`FakeMicrophoneInput`](./src/main/java/org/example/FakeMicrophoneInput.java) feeds a WAV file to the page as microphone input, so a page that records audio can be tested without a microphone. Without an argument it generates a tone; pass a WAV file, such as a recorded spoken phrase, to use that instead:

```sh
mvn compile exec:java -Dexec.mainClass=org.example.FakeMicrophoneInput
mvn compile exec:java -Dexec.mainClass=org.example.FakeMicrophoneInput -Dexec.args=/path/to/phrase.wav
```

- It works in Chromium only, with the `--use-fake-ui-for-media-stream`, `--use-fake-device-for-media-stream` and `--use-file-for-fake-audio-capture` switches.
- Use a 16-bit PCM WAV file. Chromium reads it at launch and plays it in a loop, so a different file needs a new browser.
- The page must be a secure context, such as `https://` or `http://localhost`, for `getUserMedia` to be available. The example serves its page at `http://localhost/` with `page.route`.
- It feeds pages that capture audio with `getUserMedia` (MediaRecorder, WebRTC, Web Audio). It does not feed `webkitSpeechRecognition`, which listens to the system microphone.
- Playwright mutes Chromium by default. To hear the input while debugging, launch with `setIgnoreDefaultArgs(asList("--mute-audio"))` and connect the audio to `AudioContext.destination` in the page.
