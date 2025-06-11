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

import java.util.List;
import java.util.Map;

/**
 * Playwright has <strong>experimental</strong> support for Android automation. This includes
 * Chrome for Android and Android
 * WebView.
 *
 * <p>
 * <strong>Requirements</strong>
 * <ul>
 * <li>Android device or AVD Emulator.</li>
 * <li><a href="https://developer.android.com/studio/command-line/adb">ADB
 * daemon</a> running and authenticated with your device.
 * Typically running {@code adb devices} is all you need to do.</li>
 * <li><a href=
 * "https://play.google.com/store/apps/details?id=com.android.chrome">Chrome
 * 87</a> or newer installed on the
 * device</li>
 * <li>"Enable command line on non-rooted devices" enabled in
 * {@code chrome://flags}.</li>
 * </ul>
 *
 * <p>
 * <strong>Known limitations</strong>
 * <ul>
 * <li>Raw USB operation is not yet supported, so you need ADB.</li>
 * <li>Device needs to be awake to produce screenshots. Enabling "Stay awake"
 * developer mode will help.</li>
 * <li>We didn't run all the tests against the device, so not everything
 * works.</li>
 * </ul>
 * <p><strong>How to run</strong>
 *
 * <p>An example of the Android automation script would be:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 * import java.util.regex.Pattern;
 * import java.util.List;
 * 
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       Android android = playwright.android();
 *       List<AndroidDevice> devices = android.devices();
 *       AndroidDevice device = devices.get(0);
 *       System.out.println("Model: " + device.model());
 *       System.out.println("Serial: " + device.serial());
 *       // Take screenshot of the whole device.
 *       ScreenshotOptions screenshotOptions = new ScreenshotOptions();
 *       screenshotOptions.setPath("device.png");
 *       device.screenshot(screenshotOptions);
 * 
 *       // --------------------- WebView -----------------------
 *
 *       // Launch an application with WebView.
 *       device.shell("am force-stop org.chromium.webview_shell");
 *       device.shell("am start -n org.chromium.webview_shell/.WebViewBrowserActivity");
 *       // Get the WebView.
 *       WebViewSelector selector = new WebViewSelector();
 *       selector.setPkg("org.chromium.webview_shell");
 *       AndroidWebView webview = device.webView(selector);
 *       // Fill the input box.
 *       AndroidSelector inputSelector = new AndroidSelector();
 *       inputSelector.setRes("org.chromium.webview_shell:id/url_field");
 *       device.fill(inputSelector, 'github.com/microsoft/playwright');
 *       device.press(inputSelector, AndroidKey.ENTER);
 *
 *       // Work with WebView's page as usual.
 *       Page page = webview.page();
 *       WaitForNavigationOptions waitForNavigationOptions = new WaitForNavigationOptions();
 *       waitForNavigationOptions.setUrl(Pattern.compile(".*github.com/microsoft/playwright.*"));
 *       page.waitForNavigation(waitForNavigationOptions);
 *       System.out.println(page.title());
 *
 *       // --------------------- Browser -----------------------
 *
 *       // Launch Chrome browser.
 *       device.shell("am force-stop com.android.chrome");
 *       BrowserContext context = device.launchBrowser();
 *
 *       // Use BrowserContext as usual.
 *       Page page = context.newPage();
 *       page.navigate("https://webkit.org/");
 *       System.out.println(page.evaluate("() => window.location.href)");
 *
 *       context.close();
 *   
 *       // Close the device.
 *       device.close();
 *     }
 *   }
 * }
 * }</pre>
 */
public interface Android {
  /**
  * Options for {@link Android#connect(String, ConnectOptions)}.
  */
  class ConnectOptions {
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to `30000` (30 seconds). Pass
     * `0` to disable timeout.
     */
    public Double timeout;
    /**
     * Additional HTTP headers to send with the WebSocket connection.
     */
    public Map<String, String> headers;
    /**
     * Slows down operations by the specified amount of milliseconds.
     */
    public Double slowMo;

     /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link com.microsoft.playwright.Android#setDefaultTimeout
     * BrowserContext.setDefaultTimeout()}.
     */
    public ConnectOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Additional HTTP headers to be sent with web socket connect request. Optional.
     */
    public ConnectOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going
     * on. Defaults to `0`.
     */
    public ConnectOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
  }
  /**
   * Options for {@link Android#devices(DevicesOptions)}.
   */
  class DevicesOptions {
    /**
     * Optional port to establish ADB server connection. Default to `5037`.
     */
    public Integer port;
    /**
     * Optional host to establish ADB server connection. Default to `127.0.0.1`.
     */
    public String host;
    /**
     * Prevents automatic playwright driver installation on attach. Assumes that the drivers have been installed already.
     */
    public Boolean omitDriverInstall;

    /**
     * Optional port to connect to ADB.
     */
    public DevicesOptions setPort(int port) {
      this.port = port;
      return this;
    }
    /**
     * t
     * Optional host to connect to ADB.
     */
    public DevicesOptions setHost(String host) {
      this.host = host;
      return this;
    }
    /**
     * Prevents automatic playwright driver installation on attach. Assumes that the
     * drivers have been installed already.
     */
    public DevicesOptions setOmitDriverInstall(boolean omitDriverInstall) {
      this.omitDriverInstall = omitDriverInstall;
      return this;
    }
  }

  /**
  * This methods attaches Playwright to an existing Android device.
  *
  * @param wsEndpoint A browser websocket endpoint to connect to.
  */
  default AndroidDevice connect(String wsEndpoint) {
    return connect(wsEndpoint, null);
  }
  /**
  * Connects to an Android device via WebSocket endpoint with custom options.
  *
  * @param wsEndpoint A browser websocket endpoint to connect to.
  * @param options Connection options
  */
  AndroidDevice connect(String wsEndpoint, ConnectOptions options);
  /**
   * Returns a list of detected Android devices.
   */
  default List<AndroidDevice> devices() {
    return devices(null);
  }
  /**
   * Returns a list of detected Android devices.
   * 
   * @param options Connection options with optional port
   */
  List<AndroidDevice> devices(DevicesOptions options);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   *
   * @param timeout Maximum time in milliseconds. Pass {@code 0} to disable timeout.
  */
  void setDefaultTimeout(double timeout);

}
