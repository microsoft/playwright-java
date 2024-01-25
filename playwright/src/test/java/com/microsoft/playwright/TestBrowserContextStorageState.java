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
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBrowserContextStorageState extends TestBase {

  @Test
  void shouldCaptureLocalStorage() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page.navigate("https://www.example.com");
    page.evaluate("localStorage['name1'] = 'value1';");
    page.navigate("https://www.domain.com");
    page.evaluate("localStorage['name2'] = 'value2';");
    String storageState = context.storageState();
    assertJsonEquals("{" +
      "cookies:[]," +
      "origins:[{\n" +
      "  origin: 'https://www.example.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name1',\n" +
      "    value: 'value1'\n" +
      "  }]\n" +
      "}, {\n" +
      "  origin: 'https://www.domain.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name2',\n" +
      "    value: 'value2'\n" +
      "  }]\n" +
      "}]}", new Gson().fromJson(storageState, JsonObject.class));
  }

  @Test
  void shouldSetLocalStorage() {
    String storageState = "{\n" +
      "  origins: [\n" +
      "    {\n" +
      "      origin: 'https://www.example.com',\n" +
      "      localStorage: [{\n" +
      "        name: 'name1',\n" +
      "        value: 'value1'\n" +
      "      }]\n" +
      "    }\n" +
      "  ]\n" +
      "}";
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    Page page = context.newPage();
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page.navigate("https://www.example.com");
    Object localStorage = page.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    context.close();
  }

  @Test
  void shouldRoundTripThroughTheFile(@TempDir Path tempDir) throws IOException {
    Page page1 = context.newPage();
    page1.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page1.navigate("https://www.example.com");
    page1.evaluate("() => {\n" +
      "  localStorage['name1'] = 'value1';\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    Path path = tempDir.resolve("storage-state.json");
    context.storageState(new BrowserContext.StorageStateOptions().setPath(path));
    JsonObject expected = new Gson().fromJson(
      "{\n" +
      "  'cookies':[\n" +
      "    { \n" +
      "      'name':'username',\n" +
      "      'value':'John Doe',\n" +
      "      'domain':'www.example.com',\n" +
      "      'path':'/',\n" +
      "      'expires':-1,\n" +
      "      'httpOnly':false,\n" +
      "      'secure':false,\n" +
      "      'sameSite':'" + (isChromium() ? "Lax" : "None") + "'\n" +
      "    }],\n" +
      "  'origins':[\n" +
      "    {\n" +
      "      'origin':'https://www.example.com',\n" +
      "      'localStorage':[\n" +
      "        {\n" +
      "          'name':'name1',\n" +
      "          'value':'value1'\n" +
      "        }]\n" +
      "    }]\n" +
      "}\n", JsonObject.class);
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
      assertEquals(expected, new Gson().fromJson(reader, JsonObject.class));
    }
    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(path));
    Page page2 = context2.newPage();
    page2.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page2.navigate("https://www.example.com");
    Object localStorage = page2.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    if (!isFirefox()) {
      // TODO: fails on bots with expected: <username=John Doe> but was: <>
      Object cookie = page2.evaluate("document.cookie");
      assertEquals("username=John Doe", cookie);
    }
    context2.close();
  }

  @Test
  void shouldSerialiseStorageStateWithLoneSurrogates() {
    int[] chars = {
      14210,8342,610,1472,19632,13824,29376,52231,24579,88,36890,4099,29440,26416,368,7872,9985,62632,6848,21248,
      60513,2332,816,5504,9068,280,720,8260,54576,60417,14515,3472,4292,21022,23588,62856,15618,54344,16400,224,
      1729,31022,13314,55489,24597,51409,33318,22595,704,14765,778,56631,24578,56476,32964,39424,7828,8221,51744,
      3712,6344,53892,35214,12930,54335,17412,38458,35221,38530,12828,36826,52929,54075,14117,38543,51596,3520,
      9406,49282,46281,33302,38109,38419,5659,6227,1101,5,20566,6667,23670,6695,35098,16395,17190,49346,5565,
      46010,1051,47039,45173,1132,25204,31265,6934,352,33321,36748,40073,38546,1552,21249,6751,1046,12933,40065,
      22076,40682,6667,25192,32952,2312,49105,42577,9084,31760,49257,16515,37715,20904,2595,11524,35137,45905,
      25278,30832,13765,50053,714,1574,13587,5456,31714,51728,27160,204,18500,32854,57112,10241,11029,12673,
      16108,36873,40065,16816,16625,15436,13392,19254,37433,15982,8520,45550,11584,40368,52490,19,56704,1622,
      63553,51238,27755,34758,50245,12517,40704,7298,33479,35072,132,5252,1341,8513,37323,39640,6971,16403,17185,
      61873,32168,39565,32796,23697,24656,45365,52524,24701,20486,5280,10806,17,40,34384,21352,378,32109,27116,
      25868,39443,46994,36014,3254,24990,50578,57588,95,17205,2238,19477,12360,31960,34491,23471,54313,3566,
      22047,46654,16911,45251,54280,54371,11533,27568,7502,38757,24987,16635,9792,46500,864,35905,47223,41120,
      12047,40824,8224,1154,8560,37954,10000,18724,21097,18305,2338,17186,61967,8227,64361,63895,28094,22567,
      45901,35044,24343,17361,62467,12428,12940,58130,1794,2257,13824,33696,59144,3707,1121,9283,5060,35122,16882,
      16099,15720,55934,52917,44987,68,16649,720,31773,19171,36912,15372,33184,22574,64,142,13843,1477,44223,3872,
      1602,27321,3096,32826,33415,43034,62624,57963,48163,39146,7046,37300,27027,31927,15592,60218,24619,41025,
      22156,39659,27246,31265,36426,21236,15014,19376,26,43265,16592,6402,18144,63725,1389,368,26770,18656,10448,
      44291,37489,60845,49161,26831,198,32780,18498,2535,31051,11046,53820,22530,534,41057,29215,22784,0,
    };
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("chars => window.localStorage.setItem('foo', chars.map(c => String.fromCharCode(c)).join(''))", chars);
    String storageState = context.storageState();
  }
}
